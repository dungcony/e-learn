package com.restaurant.modules.table.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.modules.table.dto.request.TableCreateRequest;
import com.restaurant.modules.table.dto.request.TableSearchRequest;
import com.restaurant.modules.table.dto.request.TableUpdateRequest;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.dto.response.TableResponse;
import com.restaurant.modules.table.entity.DiningTable;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.enums.TableZone;
import com.restaurant.modules.table.mapper.TableMapperImpl;
import com.restaurant.modules.table.repository.TableRepository;
import com.restaurant.modules.table.service.TableDeletionGuard;
import com.restaurant.modules.table.validator.TableValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableServiceImplTest {

    @Mock
    private TableRepository tableRepository;

    private DiningTable table;

    @BeforeEach
    void setUp() {
        table = new DiningTable();
        table.setId(UUID.randomUUID());
        table.setName("B05");
        table.setZone(TableZone.INDOOR);
        table.setCapacity(6);
        table.setStatus(TableStatus.AVAILABLE);
    }

    private TableServiceImpl service(TableDeletionGuard... guards) {
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        for (int i = 0; i < guards.length; i++) {
            factory.addBean("guard" + i, guards[i]);
        }
        return new TableServiceImpl(tableRepository,
                new TableValidator(tableRepository, factory.getBeanProvider(TableDeletionGuard.class)), new TableMapperImpl());
    }

    private static TableDeletionGuard guard(boolean blocking, int maxGuests) {
        return new TableDeletionGuard() {
            @Override
            public boolean hasBlockingData(UUID tableId) {
                return blocking;
            }

            @Override
            public int maxGuestCountAssigned(UUID tableId) {
                return maxGuests;
            }
        };
    }

    @SuppressWarnings("unchecked")
    @Test
    void searchTables_returns_tables_sorted_by_name_by_default() {
        when(tableRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(new PageImpl<>(List.of(table)));

        var page = service().searchTables(new TableSearchRequest("b0", TableZone.INDOOR, 4, TableStatus.AVAILABLE),
                PageRequestParams.of(null, null, null, "asc"));

        assertThat(page.getContent()).singleElement().extracting(TableResponse::name).isEqualTo("B05");
        ArgumentCaptor<Pageable> pageable = ArgumentCaptor.forClass(Pageable.class);
        verify(tableRepository).findAll(any(Specification.class), pageable.capture());
        assertThat(pageable.getValue().getSort().getOrderFor("name")).isNotNull();
    }

    @Test
    void getTable_unknown_id_is_not_found() {
        UUID id = UUID.randomUUID();
        when(tableRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service().getTable(id))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("NOT_FOUND");
    }

    @Test
    void createTable_always_starts_available() {
        when(tableRepository.saveAndFlush(any(DiningTable.class))).thenAnswer(inv -> inv.getArgument(0));

        TableResponse response = service().createTable(new TableCreateRequest("B06", TableZone.VIP_ROOM, 8, "Có máy chiếu"));

        ArgumentCaptor<DiningTable> captor = ArgumentCaptor.forClass(DiningTable.class);
        verify(tableRepository).saveAndFlush(captor.capture());
        assertThat(captor.getValue().getId()).isNotNull();
        assertThat(captor.getValue().getStatus()).isEqualTo(TableStatus.AVAILABLE);
        assertThat(response.zone()).isEqualTo(TableZone.VIP_ROOM);
        assertThat(response.capacity()).isEqualTo(8);
    }

    @Test
    void createTable_rejects_duplicate_name() {
        when(tableRepository.existsByNameIgnoreCase("B05")).thenReturn(true);

        assertThatThrownBy(() -> service().createTable(new TableCreateRequest("B05", TableZone.INDOOR, 4, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TABLE_NAME_EXISTS");
        verify(tableRepository, never()).saveAndFlush(any());
    }

    @Test
    void createTable_maps_unique_index_violation_to_name_exists() {
        when(tableRepository.saveAndFlush(any(DiningTable.class))).thenThrow(new DataIntegrityViolationException("uq_dining_tables_name"));

        assertThatThrownBy(() -> service().createTable(new TableCreateRequest("B09", TableZone.INDOOR, 4, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TABLE_NAME_EXISTS");
    }

    @Test
    void updateTable_changes_fields_and_manual_status() {
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
        when(tableRepository.saveAndFlush(table)).thenReturn(table);

        TableResponse response = service(guard(false, 0)).updateTable(table.getId(),
                new TableUpdateRequest("B05", TableZone.OUTDOOR, 8, "Gần cửa sổ", TableStatus.OUT_OF_SERVICE));

        assertThat(table.getZone()).isEqualTo(TableZone.OUTDOOR);
        assertThat(table.getCapacity()).isEqualTo(8);
        assertThat(table.getStatus()).isEqualTo(TableStatus.OUT_OF_SERVICE);
        assertThat(response.note()).isEqualTo("Gần cửa sổ");
    }

    @Test
    void updateTable_without_status_keeps_the_current_status() {
        table.setStatus(TableStatus.OCCUPIED);
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
        when(tableRepository.saveAndFlush(table)).thenReturn(table);

        service().updateTable(table.getId(), new TableUpdateRequest("B05", TableZone.INDOOR, 6, null, null));

        assertThat(table.getStatus()).isEqualTo(TableStatus.OCCUPIED);
    }

    @Test
    void updateTable_rejects_capacity_below_assigned_reservation() {
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));

        assertThatThrownBy(() -> service(guard(false, 5)).updateTable(table.getId(),
                new TableUpdateRequest("B05", TableZone.INDOOR, 4, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TABLE_CAPACITY_BELOW_RESERVATION");
        verify(tableRepository, never()).saveAndFlush(any());
    }

    @Test
    void updateTable_rejects_duplicate_name_of_another_table() {
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));
        when(tableRepository.existsByNameIgnoreCaseAndIdNot("B06", table.getId())).thenReturn(true);

        assertThatThrownBy(() -> service().updateTable(table.getId(),
                new TableUpdateRequest("B06", TableZone.INDOOR, 6, null, null)))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TABLE_NAME_EXISTS");
    }

    @Test
    void deleteTable_removes_a_free_table() {
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));

        service(guard(false, 0)).deleteTable(table.getId());

        verify(tableRepository).delete(table);
    }

    @Test
    void deleteTable_rejects_a_table_with_blocking_data() {
        when(tableRepository.findById(table.getId())).thenReturn(Optional.of(table));

        assertThatThrownBy(() -> service(guard(true, 0)).deleteTable(table.getId()))
                .isInstanceOf(BusinessException.class)
                .extracting("code").isEqualTo("TABLE_IN_USE");
        verify(tableRepository, never()).delete(any(DiningTable.class));
    }

    @Test
    void listBookableTables_excludes_out_of_service_and_orders_by_capacity() {
        when(tableRepository.findByStatusNotAndCapacityGreaterThanEqualOrderByCapacityAscNameAsc(TableStatus.OUT_OF_SERVICE, 4))
                .thenReturn(List.of(table));

        List<TableBriefResponse> tables = service().listBookableTables(4);

        assertThat(tables).singleElement().extracting(TableBriefResponse::name).isEqualTo("B05");
    }

    @Test
    void getTableBriefs_with_no_ids_does_not_query() {
        assertThat(service().getTableBriefs(List.of())).isEmpty();
        verify(tableRepository, never()).findAllById(any());
    }

    @Test
    void getTableBriefs_maps_found_tables() {
        when(tableRepository.findAllById(Set.of(table.getId()))).thenReturn(List.of(table));

        assertThat(service().getTableBriefs(Set.of(table.getId()))).singleElement()
                .extracting(TableBriefResponse::id).isEqualTo(table.getId());
    }

    @Test
    void transitionStatus_is_true_only_when_exactly_one_row_changed() {
        UUID id = UUID.randomUUID();
        when(tableRepository.transition(id, Set.of(TableStatus.AVAILABLE), TableStatus.OCCUPIED)).thenReturn(1);
        when(tableRepository.transition(id, Set.of(TableStatus.OCCUPIED), TableStatus.AVAILABLE)).thenReturn(0);

        assertThat(service().transitionStatus(id, Set.of(TableStatus.AVAILABLE), TableStatus.OCCUPIED)).isTrue();
        assertThat(service().transitionStatus(id, Set.of(TableStatus.OCCUPIED), TableStatus.AVAILABLE)).isFalse();
    }
}
