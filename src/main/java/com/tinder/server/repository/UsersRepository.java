package com.tinder.server.repository;

import com.tinder.server.model.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UsersRepository extends CrudRepository<User, Long> {

    //исключены юзеры которым я поставил лайк или дизлайк, и я сам
    @Query(value = "select * from users where id not in(select like_to from likes  where like_by != ?1 )" +
            "and id  not in(select dislike_to from dislikes  where dislike_by != ?1) and id!=?1", nativeQuery = true)
    Iterable<User> getUsersToDisplay(Long id);

    @Query(nativeQuery = true, value = "select * from users where id in(select like_to  from likes where like_by=?1)")
    Iterable<User> getAllLikeMatchedUsers(Long id);

    @Query(nativeQuery = true, value = "select * from users where id in(select dislike_to  from DISLIKES where dislike_by=?1 )")
    Iterable<User> getAllDislikeMatchedUsers(Long id);

    @Query(nativeQuery = true, value = "select * from users where id != ?1")
    Iterable<User> getAllUsersWithoutMe(Long id);

    @Query(nativeQuery = true, value = "select count(*) from users")
    int getNumberOfAllUsers();

    @Query(nativeQuery = true, value = "delete from users where id = ?1")
    void deleteUserById(Long id);

    @Query(nativeQuery = true, value = "select count(*) from users where id != ?1 and gender = ?2")
    int getNumberOfUsers(Long id, String gender);

    Optional<User> findByUsername(String username);

    @Query(nativeQuery = true, value = "update users set description = ?2 where id = ?1")
    void editDescription(Long id, String Desc);


}