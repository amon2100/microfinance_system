package com.sacco.app;

import com.sacco.model.User;

public final class SessionContext {
    private static User currentUser;

    private SessionContext() {
    }

    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public static User getCurrentUser() {
        return currentUser;
    }

    public static Long getCurrentUserId() {
        return currentUser != null ? currentUser.getId() : null;
    }
}
