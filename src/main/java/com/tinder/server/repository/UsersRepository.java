package com.tinder.server.repository;

import com.tinder.server.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {

    //выбрать все матчи
    @Query(nativeQuery = true, value = "SELECT * FROM USERS WHERE id IN(SELECT like_to FROM likes WHERE like_by=?1)" +
            "AND id IN(SELECT like_by FROM likes WHERE like_to=?1)")
    Iterable<User> getAllLikeMatchedUsers(Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM USERS WHERE id IN(SELECT dislike_to FROM DISLIKES WHERE dislike_by=?1)" +
            "AND id IN(SELECT dislike_by FROM DISLIKES WHERE dislike_to=?1)")
    Iterable<User> getAllDislikeMatchedUsers(Long id);

    @Query(nativeQuery = true, value = "SELECT * FROM USERS WHERE id != ?1")
    Iterable<User> getAllUsersWithoutMe(Long id);

    @Query(nativeQuery = true, value = "SELECT count(*) FROM USERS")
    int getNumberOfAllUsers();

    @Query(nativeQuery = true, value = "SELECT count(*) FROM USERS WHERE id != ?1 AND gender = ?2")
    int getNumberOfUsersByGender(Long id, String gender);

    @Query(nativeQuery = true, value = "SELECT * FROM USERS WHERE username = ?1")
    Optional<User> findByUsername(String username);
}
