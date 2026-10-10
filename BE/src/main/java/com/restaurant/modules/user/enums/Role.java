package com.restaurant.modules.user.enums;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * Vai trò của tài khoản; khách hàng tự đăng ký, bốn vai trò còn lại do Quản lý
 * cấp.
 */
public enum Role {
    CUSTOMER,
    WAITER,
    CASHIER,
    CHEF,
    MANAGER;

    /** Bốn vai trò do Quản lý cấp, phân biệt với khách hàng tự đăng ký. */
    public static final Set<Role> STAFF_ROLES = Collections.unmodifiableSet(EnumSet.of(WAITER, CASHIER, CHEF, MANAGER));
}
