package com.restaurant.modules.table.validator;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.modules.table.entity.DiningTable;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.repository.TableRepository;
import com.restaurant.modules.table.service.TableDeletionGuard;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableValidatorTest {

    @Mock
    private TableRepository tableRepository;

    /** Guard giả: {@code blocking} là kết quả hasBlockingData, {@code maxGuests} là số khách lớn nhất đã gán. */
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

    private TableValidator validator(TableDeletionGuard... guards) {
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        for (int i = 0; i < guards.length; i++) {
            factory.addBean("guard" + i, guards[i]);
        }
        return new TableValidator(tableRepository, factory.getBeanProvider(TableDeletionGuard.class));
    }

    private DiningTable table(TableStatus status) {
        DiningTable t = new DiningTable();
        t.setId(UUID.randomUUID());
        t.setName("B05");
        t.setCapacity(6);
        t.setStatus(status);
        return t;
    }

    private void assertCode(Runnable action, String code) {
        assertThatThrownBy(action::run).isInstanceOf(BusinessException.class).extracting("code").isEqualTo(code);
    }

    @Test
    void validateNameUnique_rejects_duplicate_for_new_and_for_other_table() {
        UUID self = UUID.randomUUID();
        when(tableRepository.existsByNameIgnoreCase("B05")).thenReturn(true);
        when(tableRepository.existsByNameIgnoreCaseAndIdNot("B06", self)).thenReturn(true);

        assertCode(() -> validator().validateNameUnique("B05", null), "TABLE_NAME_EXISTS");
        assertCode(() -> validator().validateNameUnique("B06", self), "TABLE_NAME_EXISTS");
    }

    @Test
    void validateNameUnique_passes_when_free() {
        assertThatCode(() -> validator().validateNameUnique("B99", null)).doesNotThrowAnyException();
    }

    @Test
    void validateStatusChange_allows_manual_available_and_out_of_service_on_a_free_table() {
        assertThatCode(() -> validator(guard(false, 0)).validateStatusChange(table(TableStatus.AVAILABLE), TableStatus.OUT_OF_SERVICE))
                .doesNotThrowAnyException();
        assertThatCode(() -> validator().validateStatusChange(table(TableStatus.OUT_OF_SERVICE), TableStatus.AVAILABLE))
                .doesNotThrowAnyException();
    }

    @Test
    void validateStatusChange_without_requested_status_changes_nothing() {
        assertThatCode(() -> validator().validateStatusChange(table(TableStatus.OCCUPIED), null)).doesNotThrowAnyException();
    }

    @Test
    void validateStatusChange_rejects_system_managed_statuses() {
        assertCode(() -> validator().validateStatusChange(table(TableStatus.AVAILABLE), TableStatus.OCCUPIED), "VALIDATION_ERROR");
        assertCode(() -> validator().validateStatusChange(table(TableStatus.AVAILABLE), TableStatus.RESERVED), "VALIDATION_ERROR");
    }

    @Test
    void validateStatusChange_rejects_changing_a_reserved_or_occupied_table() {
        assertCode(() -> validator().validateStatusChange(table(TableStatus.OCCUPIED), TableStatus.OUT_OF_SERVICE),
                "TABLE_STATUS_NOT_EDITABLE");
        assertCode(() -> validator().validateStatusChange(table(TableStatus.RESERVED), TableStatus.AVAILABLE),
                "TABLE_STATUS_NOT_EDITABLE");
    }

    @Test
    void validateStatusChange_keeping_the_same_status_is_always_fine() {
        assertThatCode(() -> validator().validateStatusChange(table(TableStatus.OUT_OF_SERVICE), TableStatus.OUT_OF_SERVICE))
                .doesNotThrowAnyException();
    }

    @Test
    void validateStatusChange_rejects_locking_a_table_with_unfinished_reservation_or_open_order() {
        assertCode(() -> validator(guard(true, 4)).validateStatusChange(table(TableStatus.AVAILABLE), TableStatus.OUT_OF_SERVICE),
                "TABLE_IN_USE");
    }

    @Test
    void validateCapacity_rejects_capacity_below_assigned_guest_count() {
        UUID id = UUID.randomUUID();

        assertCode(() -> validator(guard(false, 5)).validateCapacity(id, 4), "TABLE_CAPACITY_BELOW_RESERVATION");
        assertThatCode(() -> validator(guard(false, 5)).validateCapacity(id, 5)).doesNotThrowAnyException();
        assertThatCode(() -> validator(guard(false, 0), guard(false, 3)).validateCapacity(id, 3)).doesNotThrowAnyException();
    }

    @Test
    void validateCapacity_uses_the_largest_count_among_all_guards() {
        assertCode(() -> validator(guard(false, 2), guard(false, 8)).validateCapacity(UUID.randomUUID(), 6),
                "TABLE_CAPACITY_BELOW_RESERVATION");
    }

    @Test
    void validateDeletable_rejects_an_occupied_table_and_a_guarded_table() {
        assertCode(() -> validator().validateDeletable(table(TableStatus.OCCUPIED)), "TABLE_IN_USE");
        assertCode(() -> validator(guard(true, 0)).validateDeletable(table(TableStatus.AVAILABLE)), "TABLE_IN_USE");
    }

    @Test
    void validateDeletable_passes_for_a_free_table_without_blocking_data() {
        assertThatCode(() -> validator(guard(false, 0)).validateDeletable(table(TableStatus.AVAILABLE))).doesNotThrowAnyException();
        assertThatCode(() -> validator().validateDeletable(table(TableStatus.OUT_OF_SERVICE))).doesNotThrowAnyException();
    }
}
