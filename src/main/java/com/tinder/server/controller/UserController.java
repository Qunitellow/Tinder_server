package com.tinder.server.controller;

import com.tinder.server.model.Dislike;
import com.tinder.server.model.Like;
import com.tinder.server.external.Response;
import com.tinder.server.model.User;
import com.tinder.server.repository.UsersRepository;
import com.tinder.server.service.DislikeService;
import com.tinder.server.service.LikeService;
import com.tinder.server.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import static com.tinder.server.external.Support.*;

@RestController
public class UserController {

    private String nameLoggedUser = null;
    private final Set<User> viewedFemaleProfiles = new HashSet<>();
    private final Set<User> viewedMaleProfiles = new HashSet<>();
    private final Set<User> profilesForUnauthorizedUser = new HashSet<>();

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final LikeService likeService;
    private final DislikeService dislikeService;
    private final UsersRepository usersRepo;

    @Autowired
    public UserController(UserService userService, LikeService likeService,
                          DislikeService dislikeService, UsersRepository usersRepo) {
        this.userService = userService;
        this.likeService = likeService;
        this.dislikeService = dislikeService;
        this.usersRepo = usersRepo;
    }


    @PostMapping("/register")
    public ResponseEntity<Object> addUser(@RequestBody Map<String, String> params) {
        log.debug("Регистрация нового пользователя...");
        Response response;
        if (userService.usernameIsExists(params.get("username"))) {
            log.debug("Имя пользователя занято");
            response = new Response(false, "Имя пользователя занято");
        } else if (!isValidUserInfo(params)) {
            log.debug("Введены некорректные данные");
            response = new Response(false, "Введены некорректные данные");
        } else {
            try {
                userService.put_one(new User(params.get("gender"), params.get("username"),
                        params.get("password"), params.get("profileMessage")));
                log.debug("Регистрация прошла успешно");
                response = new Response(true,
                        userService.getUserByName(params.get("username")).getId());
            } catch (DataIntegrityViolationException e) {
                response = new Response(false, "Ошибка сохранения учетной записи в БД");
            }
        }
        log.debug("Ответ на запрос о регистрации: {}: {}", response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.BAD_REQUEST);
    }


    @PostMapping("/login")
    public ResponseEntity<Object> logInUser(@RequestBody Map<String, String> params) {
        Response response;
        log.debug("Логин пользователя");
        User existingUser = usersRepo.findByUsername(params.get("username")).orElse(null);
        if (existingUser == null) {
            response = new Response(false, "Пользователя с именем " +
                    params.get("username") + " не существует");
            log.debug("Пользователя не существует");
        } else {
            log.debug("Пользователь {} существует: ", params.get("username"));
            if (existingUser.getPassword().equals(params.get("password"))) {
                nameLoggedUser = existingUser.getUsername();
//                nameLoggedUser.add(existingUser.getUsername());
                response = new Response(true, existingUser.getId());
                log.debug("Пользователь {} авторизован: ", params.get("username"));
            } else {
                response = new Response(false, "Введенный пароль неверный");
                log.debug("Неверный пароль");
            }
        }
        log.debug("Ответ на запрос об авторизации: {}: {}", response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.UNAUTHORIZED);
    }


    @PutMapping("login/edit")
    public ResponseEntity<Object> changeDescription(@RequestBody String message) {
        log.debug("Редактирование информации о себе...");
        Response response;
        if (nameLoggedUser != null) {
            User currentUser = userService.getUserByName(nameLoggedUser);
            if (currentUser != null && isValidDescription(message)) {
                try {
                    usersRepo.editDescription(currentUser.getId(), message);
                    response = new Response(true, currentUser.getUsername() + " " + currentUser.getDescription());
                    log.debug("Описание изменено");
                } catch (DataIntegrityViolationException e) {
                    response = new Response(false, "Ошибка сохранения информации о себе в БД");
                    log.debug("Не удалось изменить информацию о себе");
                }
            } else {
                response = new Response(false, "Введены некорректные данные");
                log.debug("Введены некорректные данные");
            }
        } else {
            response = new Response(false, "Пользователь не авторизован");
            log.debug("Пользователь не авторизован");
        }
        log.debug("Ответ на запрос об обновлении информации о себе: {}: {}", response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.BAD_REQUEST);
    }


    @PostMapping("login/edit/delete")
    public ResponseEntity<String> deleteUser(@RequestBody Long id) {
        Response response;
        User currentUser = userService.getUserById(id);
        if (currentUser != null) {
            log.debug("Удаление учетной записи пользователя...");
            nameLoggedUser = null;
            usersRepo.deleteUserById(id);
            response = new Response(true, "|| Ваша анкета удалена безвозвратно ||");
            log.debug("Удаление успешно");
        } else {
            response = new Response(false, "Ошибка удаления. Данный пользователь не авторизован");
            log.debug("Данный пользователь не авторизован");
        }
        log.debug("Ответ на запрос об удалении учетной записи пользователя: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.BAD_REQUEST);
    }


    @GetMapping("me/like/matching/{id}")
    public ResponseEntity<Map<Integer, User>> matchedUsersByLikes(@PathVariable Long id) {
        log.debug("Получение списка пользователей ответивших взаимной симпатией...");
        Response response;
        Iterable<User> likeMatchUsers;
        likeMatchUsers = usersRepo.getAllLikeMatchedUsers(id);

        if (likeMatchUsers == null) {
            response = new Response(false, "матчей нет ((");
        } else {
            log.debug("Список лайк-матчей получен");
            response = new Response(true, likeMatchUsersAsMap(likeMatchUsers));
        }
        log.debug("Ответ на запрос о получении списка лайк-матчей: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>((Map<Integer, User>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    public Map<Integer, User> likeMatchUsersAsMap(Iterable<User> likeMatchUsers) {
        int i = 0;
        Map<Integer, User> likedUsersMap = new HashMap<>();
        likeMatchUsers.forEach(user -> likedUsersMap.put(i + 1, user));
        return likedUsersMap;
    }


    @PostMapping("login/dislike")//влево
    public ResponseEntity<Object> putDislike(@RequestBody String s) {
//    public ResponseEntity<Object> putDislike(@RequestBody Long id) {
        log.debug("Проявление антипатии...");
//        String loggedUsername = nameLoggedUser.iterator().next();
        Response response;
//        User loggedUser = userService.getUserById(id);
        if (nameLoggedUser != null) {
            User loggedUser = userService.getUserByName(nameLoggedUser);
            User userForDislike = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(loggedUser.getId()));
            Dislike dislike = new Dislike(loggedUser.getId(), userForDislike.getId());
            dislikeService.put_one(dislike);
            log.debug("добавили в БД дизлайк");

            //юзеры, с которыми взаимно
            Iterable<User> matchingUsers = usersRepo.getAllDislikeMatchedUsers(loggedUser.getId());
            //из них выбираем того, кого сейчас дизлайкнули
            User matchUser = userService.getUsersAsList(matchingUsers).stream()
                    .filter(user -> userForDislike.getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);

            if (matchUser != null) {
                log.debug("юзер ответил взаимностью");
                response = new Response(true, "|| Вы НЕлюбимы! ||");
            } else {
                log.debug("юзер не ответил взаимностью");
                response = new Response(true, "юзер НЕ ответил(а) взаимностью :'( ");
            }
        } else {
            log.debug("юзер не авторизован");
            response = new Response(false, "Пожалуйста авторизуйтесь чтобы другие люди видели ваши дизлайки" + s);
        }
        log.debug("Ответ на запрос о постановке лайка юзеру: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.UNAUTHORIZED);
    }


    @PostMapping("login/like")//вправо
    public ResponseEntity<Object> putLike(@RequestBody String s) {
        log.debug("Проявление симпатии...");
        Response response;
//        String loggedUsername = nameLoggedUser.iterator().next();
        if (nameLoggedUser != null) {
            User loggedUser = userService.getUserByName(nameLoggedUser);
            User userForLike = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(loggedUser.getId()));
            Like like = new Like(loggedUser.getId(), userForLike.getId());
            likeService.put_one(like);
            log.debug("добавили в БД лайк");

            //юзеры, с которыми взаимно
            Iterable<User> matchingUsers = usersRepo.getAllLikeMatchedUsers(loggedUser.getId());
            //из них выбираем того, кого сейчас лайкнули
            User matchUser = userService.getUsersAsList(matchingUsers).stream()
                    .filter(user -> userForLike.getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);

            if (matchUser != null) {
                log.debug("юзер ответил взаимностью");
                response = new Response(true, "|| Вы любимы ||");
            } else {
                log.debug("юзер не ответил взаимностью");
                response = new Response(true, "юзер НЕ ответил(а) взаимностью :'( ");
            }
        } else {
            log.debug("юзер не авторизован");
            response = new Response(false, "Неавторизованные юзеры не могут лайкать" + s);
        }
        log.debug("Ответ на запрос о постановке лайка юзеру: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.UNAUTHORIZED);
    }


    @PostMapping("login/users/next")
    public ResponseEntity<Object> nextProfileAuth(@RequestBody Long id) {
        log.debug("Получение следующего профиля...");
        Response response;
        User profileToDisplay;

            log.debug("Получение для показа следующего профиля с авторизованного юзера...");
            //исключить юзеров, удовлетворяющих кретериям, но которых уже показывали
            profileToDisplay = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(id));
        if (profileToDisplay == null) {
            response = new Response(false, "Нет профилей для просмотра");
        } else {
            log.debug("Показан следующий профиль");
            response = new Response(true, profileToDisplay.getUsername() + " " + profileToDisplay.getDescription());
            if ((profileToDisplay.getGender()).equals("сударыня")) {
                viewedFemaleProfiles.add(profileToDisplay);
                if (viewedFemaleProfiles.size() == usersRepo.getNumberOfUsers(id, "сударыня")) {
                    log.debug("Просмотрены все женские профили");
                    viewedFemaleProfiles.clear();
                }
            } else if ((profileToDisplay.getGender()).equals("сударь")) {
                viewedMaleProfiles.add(profileToDisplay);
                if (viewedMaleProfiles.size() == usersRepo.getNumberOfUsers(id, "сударь")) {
                    log.debug("Просмотрены все мужские профили");
                    viewedMaleProfiles.clear();
                }
            }
        }
        log.debug("Ответ на запрос для просмотра следующего профиля: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.UNAUTHORIZED);
    }


    @GetMapping("login/users/next")
    public ResponseEntity<Object> nextProfileNoAuth(@RequestParam Long num) {
        log.debug("Получение для показа следующего профиля " + num + " с НЕавторизованного юзера...");
        Response response;
        User profileToDisplay = getUnviewedProfile(usersRepo.findAll());
        if (profileToDisplay == null) {
            response = new Response(false, "Нет профилей для просмотра");
        } else {
            response = new Response(true, profileToDisplay.getUsername() + " " + profileToDisplay.getDescription());
            log.debug("Показан следующий случайный профиль. Вы не авторизованы");
            profilesForUnauthorizedUser.add(profileToDisplay);
            if (profilesForUnauthorizedUser.size() == usersRepo.getNumberOfAllUsers()) {
                log.debug("Просмотрены все профили");
                profilesForUnauthorizedUser.clear();
            }
        }
        log.debug("Ответ на запрос для просмотра следующего профиля: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.UNAUTHORIZED);
    }






    public User getUnviewedProfile(Iterable<User> profilesToDisplay) {
        List<User> profilesToDisplayAsList = userService.getUsersAsList(profilesToDisplay);
//        String loggedUsername = nameLoggedUser.iterator().next();
        User loggedUser = userService.getUserByName(nameLoggedUser);
        if (loggedUser != null) {
            if (loggedUser.getGender().equals("сударь")) {
                return profilesToDisplayAsList.stream()
                        .filter(user -> user.getGender().equals("сударыня"))
                        .filter(user -> isNotViewed(user, viewedFemaleProfiles))
                        .findFirst()
                        .orElse(null);
            } else if (loggedUser.getGender().equals("сударыня")) {
                return profilesToDisplayAsList.stream()
                        .filter(user -> user.getGender().equals("сударь"))
                        .filter(user -> isNotViewed(user, viewedMaleProfiles))
                        .findFirst()
                        .orElse(null);
            }
        } else {
            return profilesToDisplayAsList.stream()
                    .filter(user -> isNotViewed(user, profilesForUnauthorizedUser))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public boolean isNotViewed(User user, Set<User> viewedProfiles) {

        for (User viewedProfile : viewedProfiles) {
            if (user.equals(viewedProfile)) {
                return false;
            }
        }
        return true;
    }


    //для получения в клиенте авторизованного юзера
    @PostMapping("login/currentuser")
    public ResponseEntity<User> getCurrentUser(@RequestBody Integer num) {
        Response response;
        log.info(num+"");
        User currentUser = userService.getUserByName(nameLoggedUser);
        if (currentUser == null) {
            response = new Response(false, null);
        } else {
            response = new Response(true, currentUser);
        }
        return response.isStatus() ?
                new ResponseEntity<>((User) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>((User) response.getAddition(), HttpStatus.BAD_REQUEST);
    }
}





















//    @GetMapping("login/currentuser")
//    public ResponseEntity<Map<String, String>> getCurrentUser(@RequestParam int num) {
//        Response response;
//        log.info(num+"");
//        User currentUser = userService.getUserByName(nameLoggedUser);
//        if (currentUser == null) {
//            response = new Response(false);
//        } else {
//            Map<String, String> currentUserAsMap = new HashMap<>();
//            currentUserAsMap.put("id", currentUser.getId()+"");
//            currentUserAsMap.put("gender", currentUser.getGender());
//            currentUserAsMap.put("username", currentUser.getUsername());
//            currentUserAsMap.put("password", currentUser.getPassword());
//            currentUserAsMap.put("description", currentUser.getDescription());
//
//            response = new Response(true, currentUserAsMap);
//        }
//        return response.isStatus() ?
//                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK) :
//                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
//    }
//
//


//Пример:
//       @RequestMapping("...")
//       void bar(@RequestBody String body, @RequestParam("baz") baz) {
//           //method body
//@RequestBody : переменная body будет содержать тело HTTP-запроса
//@RequestParam : переменная baz будет содержать значение параметра запроса baz