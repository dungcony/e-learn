package com.elearning.user.repository;

import com.elearning.common.util.LikeUtils;
import com.elearning.user.dto.request.UserSearchRequest;
import com.elearning.user.entity.User;
import com.elearning.user.enums.Role;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Expression;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Dựng điều kiện tìm kiếm giảng viên, học viên (UC006): các tiêu chí đã nhập được AND với nhau.
 */
public final class UserSpecification {

    private UserSpecification() {
    }

    public static Specification<User> search(Role role, UserSearchRequest request) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("role"), role));
            addContains(predicates, cb, root.get("fullName"), request.name());
            addContains(predicates, cb, root.get("email"), request.email());
            addContains(predicates, cb, root.get("phone"), request.phone());
            if (request.gender() != null) {
                predicates.add(cb.equal(root.get("gender"), request.gender()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private static void addContains(List<Predicate> predicates, CriteriaBuilder cb,
                                    Expression<String> field, String value) {
        if (StringUtils.hasText(value)) {
            predicates.add(cb.like(cb.lower(field), LikeUtils.contains(value), LikeUtils.ESCAPE));
        }
    }
}
