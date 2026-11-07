package model;

import java.util.Locale;

public final class UserRoleStatus {

    private UserRoleStatus() {
    }

    public enum Role {
        ADMIN,
        CASHIER,
        CUSTOMER
    }

    public enum Status {
        PENDING,
        APPROVED,
        REJECTED
    }

    public static Role parseRole(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Role is required.");
        }

        try {
            return Role.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid role. Choose ADMIN, CASHIER, or CUSTOMER."
            );
        }
    }

    public static Status parseStatus(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Status is required.");
        }

        try {
            return Status.valueOf(
                    value.trim().toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(
                    "Invalid status. Choose PENDING, APPROVED, or REJECTED."
            );
        }
    }
}