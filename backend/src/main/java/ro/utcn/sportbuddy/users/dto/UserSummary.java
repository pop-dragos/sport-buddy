package ro.utcn.sportbuddy.users.dto;

import ro.utcn.sportbuddy.users.model.User;

public record UserSummary(Long id, String username, String fullName, String email) {
    public static UserSummary from(User u) {
        return new UserSummary(u.getId(), u.getUsername(), u.getFullName(), u.getEmail());
    }
}