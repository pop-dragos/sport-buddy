package ro.utcn.sportbuddy.users.service;

// Implementată de modulul friends. Modulul users o folosește pentru setarea profileVisibility = FRIENDS
// fără să depindă direct de friends (care, la rândul lui, depinde de users): dependența e inversată.
public interface FriendshipChecker {
    boolean areFriends(Long userId, Long otherUserId);
}
