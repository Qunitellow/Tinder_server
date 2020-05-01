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

    //        выбрать все матчи
    @Query(nativeQuery = true, value = "select * from users where id in(select like_to  from likes where like_by=?1)" +
            "and id in(select like_by  from likes where like_to=?1)")
    Iterable<User> getAllLikeMatchedUsers(Long id);


    //    @Query(nativeQuery = true, value = "select * from users where id in(select dislike_to  from DISLIKES where dislike_by=?1 )")
    @Query(nativeQuery = true, value = "select * from users where id in(select dislike_to  from DISLIKES where dislike_by=?1)" +
            "and id in(select dislike_by  from DISLIKES where dislike_to=?1)")
    Iterable<User> getAllDislikeMatchedUsers(Long id);

    @Query(nativeQuery = true, value = "select * from users where id != ?1")
    Iterable<User> getAllUsersWithoutMe(Long id);

    @Query(nativeQuery = true, value = "select count(*) from users")
    int getNumberOfAllUsers();

    @Query(nativeQuery = true, value = "select count(*) from users where id != ?1 and gender = ?2")
    int getNumberOfUsersByGender(Long id, String gender);

    @Query(nativeQuery = true, value = "select * from users where username = ?1")
    Optional<User> findByUsername(String username);

    @Query(nativeQuery = true, value = "select count(*) from users where id != ?1 and gender = ?2 and id in(select like_to  from likes where like_by=?1) and id in(select like_by  from likes where like_to=?1)")
    int getNumberOfUsersByLikeMatch(Long id, String gender);

    @Query(nativeQuery = true, value = "select count(*) from users where id != ?1 and gender = ?2 and id in(select dislike_to  from DISLIKES where dislike_by=?1) and id in(select dislike_by  from DISLIKES where dislike_to=?1)")
    int getNumberOfUsersByDislikeMatch(Long id, String gender);

//    @Query(nativeQuery = true, value = "select * from users where id in (select like_to from likes where ) ")
//    Iterable<User> itsLikedUser(Long loggedUser_id, Long dislikedUser_id);

//    @Query(nativeQuery = true, value = "select count(*) from users where id != ?1 and gender = ?2 and (id in(select dislike_to  from likes where dislike_by=?1) and id in(select dislike_by  from likes where dislike_to=?1)) or (id in(select like_to  from likes where like_by=?1) and id in(select like_by  from likes where like_to=?1))")
//    int getNumberOfAllMatchUsers(Long id, String gender);


}