package com.restaurant.modules.menu.repository;

import com.restaurant.modules.menu.entity.Dish;
import com.restaurant.modules.menu.enums.DishCategory;
import com.restaurant.modules.menu.enums.DishStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface DishRepository extends JpaRepository<Dish, UUID>, JpaSpecificationExecutor<Dish> {

    boolean existsByCategoryAndNameIgnoreCase(DishCategory category, String name);

    boolean existsByCategoryAndNameIgnoreCaseAndIdNot(DishCategory category, String name, UUID id);

    /** Số món theo danh mục, chỉ tính các món có trạng thái trong {@code statuses}; danh mục không có món không xuất hiện. */
    @Query("select d.category as category, count(d) as count from Dish d where d.status in :statuses group by d.category")
    List<CategoryCountProjection> countVisibleByCategory(@Param("statuses") Collection<DishStatus> statuses);
}
