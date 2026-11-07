package model;

public record User(
        int id,
        String username,
        String password,
        UserRoleStatus.Role role,
        UserRoleStatus.Status status
) {
}