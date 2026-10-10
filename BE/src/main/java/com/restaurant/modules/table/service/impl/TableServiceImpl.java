package com.restaurant.modules.table.service.impl;

import com.restaurant.common.exception.BusinessException;
import com.restaurant.common.exception.ErrorCode;
import com.restaurant.common.response.PageRequestParams;
import com.restaurant.common.util.SpecificationUtils;
import com.restaurant.modules.table.dto.request.TableCreateRequest;
import com.restaurant.modules.table.dto.request.TableSearchRequest;
import com.restaurant.modules.table.dto.request.TableUpdateRequest;
import com.restaurant.modules.table.dto.response.TableBriefResponse;
import com.restaurant.modules.table.dto.response.TableResponse;
import com.restaurant.modules.table.entity.DiningTable;
import com.restaurant.modules.table.enums.TableStatus;
import com.restaurant.modules.table.mapper.TableMapper;
import com.restaurant.modules.table.repository.TableRepository;
import com.restaurant.modules.table.service.TableService;
import com.restaurant.modules.table.validator.TableValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TableServiceImpl implements TableService {

    // Tên field client được phép sắp xếp (snake_case) -> thuộc tính entity
    private static final Map<String, String> SORTABLE_FIELDS =
            Map.of("name", "name", "capacity", "capacity", "zone", "zone", "status", "status");

    private final TableRepository tableRepository;
    private final TableValidator tableValidator;
    private final TableMapper tableMapper;

    @Override
    @Transactional(readOnly = true)
    public Page<TableResponse> searchTables(TableSearchRequest request, PageRequestParams params) {
        Specification<DiningTable> spec = SpecificationUtils.containsIgnoreCase("name", request.name());
        if (request.zone() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("zone"), request.zone()));
        }
        if (request.minCapacity() != null) {
            spec = spec.and((root, query, cb) -> cb.greaterThanOrEqualTo(root.get("capacity"), request.minCapacity()));
        }
        if (request.status() != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("status"), request.status()));
        }
        return tableRepository.findAll(spec, params.toPageable(SORTABLE_FIELDS, "name")).map(tableMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TableResponse getTable(UUID id) {
        return tableMapper.toResponse(loadTable(id));
    }

    @Override
    @Transactional
    public TableResponse createTable(TableCreateRequest request) {
        tableValidator.validateNameUnique(request.name(), null);
        DiningTable table = tableMapper.fromCreateRequest(request);
        table.setId(UUID.randomUUID());
        table.setStatus(TableStatus.AVAILABLE);
        return tableMapper.toResponse(saveAndFlushUnique(table));
    }

    @Override
    @Transactional
    public TableResponse updateTable(UUID id, TableUpdateRequest request) {
        DiningTable table = loadTable(id);
        tableValidator.validateNameUnique(request.name(), id);
        tableValidator.validateStatusChange(table, request.status());
        tableValidator.validateCapacity(id, request.capacity());

        tableMapper.updateFromRequest(request, table);
        if (request.status() != null) {
            table.setStatus(request.status());
        }
        return tableMapper.toResponse(saveAndFlushUnique(table));
    }

    @Override
    @Transactional
    public void deleteTable(UUID id) {
        DiningTable table = loadTable(id);
        tableValidator.validateDeletable(table);
        tableRepository.delete(table);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableBriefResponse> listBookableTables(int minCapacity) {
        return tableRepository.findByStatusNotAndCapacityGreaterThanEqualOrderByCapacityAscNameAsc(TableStatus.OUT_OF_SERVICE, minCapacity)
                .stream().map(tableMapper::toBriefResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableBriefResponse> getTableBriefs(Collection<UUID> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return tableRepository.findAllById(ids).stream().map(tableMapper::toBriefResponse).toList();
    }

    @Override
    @Transactional
    public boolean transitionStatus(UUID id, Collection<TableStatus> from, TableStatus to) {
        return tableRepository.transition(id, from, to) == 1;
    }

    private DiningTable loadTable(UUID id) {
        return tableRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Không tìm thấy bàn."));
    }

    // Unique index uq_dining_tables_name bắt trường hợp hai request cùng tên chạy song song
    private DiningTable saveAndFlushUnique(DiningTable table) {
        try {
            return tableRepository.saveAndFlush(table);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.TABLE_NAME_EXISTS);
        }
    }
}
