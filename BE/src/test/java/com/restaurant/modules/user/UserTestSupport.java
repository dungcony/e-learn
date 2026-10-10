package com.restaurant.modules.user;

import com.restaurant.modules.user.service.UserDeletionGuard;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.support.StaticListableBeanFactory;

/** Dựng {@link ObjectProvider} thật cho test, không cần Spring context. */
public final class UserTestSupport {

    private UserTestSupport() {
    }

    public static ObjectProvider<UserDeletionGuard> guards(UserDeletionGuard... guards) {
        StaticListableBeanFactory factory = new StaticListableBeanFactory();
        for (int i = 0; i < guards.length; i++) {
            factory.addBean("guard" + i, guards[i]);
        }
        return factory.getBeanProvider(UserDeletionGuard.class);
    }
}
