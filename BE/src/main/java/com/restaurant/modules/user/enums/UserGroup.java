package com.restaurant.modules.user.enums;

import java.util.Set;

/** Hai nhóm tài khoản Quản lý quản trị riêng: nhân viên (UC008) và khách hàng (UC009). */
public enum UserGroup {
    STAFF(Role.STAFF_ROLES),
    CUSTOMER(Set.of(Role.CUSTOMER));

    private final Set<Role> roles;

    UserGroup(Set<Role> roles) {
        this.roles = roles;
    }

    public Set<Role> roles() {
        return roles;
    }
}
