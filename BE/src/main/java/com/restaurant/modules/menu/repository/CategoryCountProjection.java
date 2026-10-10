package com.restaurant.modules.menu.repository;

import com.restaurant.modules.menu.enums.DishCategory;

/** Số món của một danh mục. */
public interface CategoryCountProjection {

    DishCategory getCategory();

    long getCount();
}
