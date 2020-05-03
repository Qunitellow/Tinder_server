package com.tinder.server.service;

import com.tinder.server.external.Response;
import com.tinder.server.external.Util;
import com.tinder.server.model.User;
import com.tinder.server.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Set;

import static com.tinder.server.external.Util.isValidDescription;
import static com.tinder.server.external.Util.isValidUserInfo;

@Service
public class ProfileService {

    private final Util util;
    private final UsersRepository usersRepo;
    private final UserLists userLists;

    @Autowired
    public ProfileService(Util util, UsersRepository usersRepo, UserLists userLists) {
        this.util = util;
        this.usersRepo = usersRepo;
        this.userLists = userLists;
    }

    /**
     * Request processing
     */

    public Response responseReg(Map<String, String> params) {
        Response response;
        if (usernameIsExists(params.get("username"))) {
            response = new Response(false, "Имя пользователя занято");
        } else if (!isValidUserInfo(params)) {
            response = new Response(false, "Введены некорректные данные");
        } else {
            try {
                usersRepo.save(new User(params.get("gender"), params.get("username"),
                        params.get("password"), params.get("description")));
                response = new Response(true,
                        usersRepo.findByUsername(params.get("username")).orElse(null));
            } catch (DataIntegrityViolationException e) {
                response = new Response(false, "Ошибка сохранения учетной записи в БД");
            }
        }
        return response;
    }

    public Response responseLogIn(Map<String, String> params) {
        User existingUser = usersRepo.findByUsername(params.get("username")).orElse(null);
        if (existingUser == null) {
            return new Response(false, "Пользователя с именем "
                    + params.get("username") + " не существует");
        }
        if (!existingUser.getPassword().equals(params.get("password"))) {
            return new Response(false, "Введенный пароль неверный");
        }
        userLists.nameLoggedUser = existingUser.getUsername();
        clearViewedUserList();
        readingTheRelation(existingUser, existingUser.getGender().equals("сударь")
                ? userLists.alreadyLikeMatchedFemaleProfiles
                : userLists.alreadyLikeMatchedMaleProfiles);
        return new Response(true, existingUser.getId());
    }


    public Response responseDel(Long id) {
        Response response;
        User currentUser = usersRepo.findById(id).orElse(null);
        if (currentUser != null) {
            userLists.nameLoggedUser = null;
            usersRepo.deleteById(id);
            response = new Response(true, "|| Ваша анкета удалена безвозвратно ||");
        } else {
            response = new Response(false, "Ошибка удаления. Данный пользователь не авторизован");
        }
        return response;
    }

    public Response responseEdit(String newDesc) {
        if (userLists.nameLoggedUser == null) {
            return new Response(false, "Пользователь не авторизован");
        }
        User currentUser = usersRepo.findByUsername(userLists.nameLoggedUser).orElse(null);
        if (!isValidDescription(newDesc) || currentUser == null) {
            return new Response(false, "Введены некорректные данные");
        }
        try {
            currentUser.setDescription(newDesc);
            usersRepo.save(currentUser);
            return new Response(true, currentUser.getUsername() + " " + currentUser.getDescription());
        } catch (DataIntegrityViolationException e) {
            return new Response(false, "Ошибка сохранения информации о себе в БД");
        }
    }

    /**
     * Support
     */

    private void readingTheRelation(User existingUser, Set<User> userList) {
        Iterable<User> likeMatchUsers = usersRepo.getAllLikeMatchedUsers(existingUser.getId());
        util.getUsersAsList(likeMatchUsers).stream()
                .filter(user -> !user.getGender().equals(existingUser.getGender()))
                .forEach(userList::add);
    }

    private void clearViewedUserList() {
        userLists.viewedFemaleProfiles.clear();
        userLists.viewedMaleProfiles.clear();
        userLists.profilesForUnauthorizedUser.clear();
        userLists.alreadyLikeMatchedMaleProfiles.clear();
        userLists.alreadyLikeMatchedFemaleProfiles.clear();
        userLists.alreadyDislikeMatchedMaleProfiles.clear();
        userLists.alreadyDislikeMatchedFemaleProfiles.clear();
    }

    public boolean usernameIsExists(String username) {
        for (User user : usersRepo.findAll()) {
            if (user.getUsername().equals(username)) {
                return true;
            }
        }
        return false;
    }
}
