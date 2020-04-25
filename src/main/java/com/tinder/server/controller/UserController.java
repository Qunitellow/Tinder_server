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

    private final Set<String> loggedUsersname = new HashSet<>();
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
                loggedUsersname.add(existingUser.getUsername());
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
        String currentUserName = loggedUsersname.iterator().next();
        if (!loggedUsersname.isEmpty()) {
            User currentUser = userService.getUserByName(currentUserName);
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

    //Пример контроллера:
    //
    //@Controller
    //class FooController {
    //       @RequestMapping("...")
    //       void bar(@RequestBody String body, @RequestParam("baz") baz) {
    //           //method body
    //       }
    //}

    //@RequestBody : переменная body будет содержать тело HTTP-запроса
    //@RequestParam : переменная baz будет содержать значение параметра запроса baz

    @PostMapping("login/edit/delete")
    public ResponseEntity<String> deleteUser(@RequestBody Long id) {
        Response response;
        User currentUser = userService.getUserById(id);
        if (currentUser != null) {
            log.debug("Удаление учетной записи пользователя...");
            loggedUsersname.remove(currentUser.getUsername());
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

    @PostMapping("users/like/matching")
    public ResponseEntity<Map<Integer, String>> matchedUsersByLikes(@RequestBody Map<String, String> likedUsersMap) {
        log.debug("Получение списка пользователей ответивших взаимной симпатией...");
        Response response;
        Iterable<User> likeMatchUsers;
        User currentUser = userService.getUserByName(loggedUsersname.iterator().next());
        if (currentUser != null) {
            likeMatchUsers = usersRepo.getAllLikeMatchedUsers(Long.parseLong(likedUsersMap.get("id")));
            response = new Response(true, likeMatchUsersAsMap(likeMatchUsers));
            log.debug("Список лайк-матчей получен");
        } else {
            response = new Response(false, "Вы не авторизованы");
            log.debug("Вы не авторизованы");
        }
        log.debug("Ответ на запрос о получении списка лайк-матчей: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>((Map<Integer, String>) response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
    }

    public Map<Integer, String> likeMatchUsersAsMap(Iterable<User> likeMatchUsers) {
        int i = 0;
        Map<Integer, String> likedUsersMap = new HashMap<>();
        likeMatchUsers.forEach(user -> likedUsersMap.put(i + 1, user.getUsername()));
        return likedUsersMap;
    }


    @GetMapping("users/like/matching/{id}")
    public ResponseEntity<Map<String, String>> getLikedUser(@RequestParam Long id) {
//    public ResponseEntity<Object> getLikedUser(@PathVariable Long id) {
        log.debug("Выбор пользователя из списка понравившихся...");
        Response response;
        User matchedUser = userService.getUserById(id);
        if (matchedUser != null) {
            Map<String, String> selectedProfile = new HashMap<>();
            selectedProfile.put("username", matchedUser.getUsername());
            selectedProfile.put("profileMessage", matchedUser.getDescription());
            response = new Response(true, selectedProfile);
            log.debug("Получен понравившийся юзер");
        } else {
            response = new Response(false, "Такого юзера нет");
            log.debug("Такого юзера нет");
        }
        log.debug("Ответ на запрос о получении понравившегося юзера: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("login/dislike")//влево
    public ResponseEntity<Object> putDislike(@RequestBody String s) {
//    public ResponseEntity<Object> putDislike(@RequestBody Long id) {
        log.debug("Проявление антипатии...");
        String loggedUsername = loggedUsersname.iterator().next();
        Response response;
//        User loggedUser = userService.getUserById(id);
        if (loggedUsername != null) {
//        if (loggedUser != null) {
            User loggedUser = userService.getUserByName(loggedUsername);
            User userForDislike = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(loggedUser.getId()), loggedUser);
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
        String loggedUsername = loggedUsersname.iterator().next();
//        User loggedUser = userService.getUserById(id);

        if (loggedUsername != null) {
            User loggedUser = userService.getUserByName(loggedUsername);
            User userForLike = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(loggedUser.getId()), loggedUser);
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
    public ResponseEntity<Object> nextProfile(@RequestBody String s) {
        log.debug("Получение следующего профиля...");
        Response response;
        User profileToDisplay;
        String loggedUsername = loggedUsersname.iterator().next();
        User loggedUser = usersRepo.findByUsername(loggedUsername).orElse(null);

        if (loggedUser == null) {
            log.debug("Получение для показа следующего профиля с НЕавторизованного юзера...");
            profileToDisplay = getUnviewedProfile(usersRepo.findAll(), null);
            profilesForUnauthorizedUser.add(profileToDisplay);
            if (profilesForUnauthorizedUser.size() == usersRepo.getNumberOfAllUsers()) {
                log.debug("Просмотрены все профили");
                profilesForUnauthorizedUser.clear();
            }
            log.debug("Показан следующий случайный профиль. Вы не авторизованы" + s);
            response = new Response(false, profileToDisplay.getUsername() + " " + profileToDisplay.getDescription());
        } else {
            log.debug("Получение для показа следующего профиля с авторизованного юзера...");
            //исключить юзеров, удовлетворяющих кретериям, но которых уже показывали
            profileToDisplay = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(loggedUser.getId()), loggedUser);
            if ((profileToDisplay.getGender()).equals("сударыня")) {
                viewedFemaleProfiles.add(profileToDisplay);
                if (viewedFemaleProfiles.size() == usersRepo.getNumberOfUsers(loggedUser.getId(), "сударыня")) {
                    log.debug("Просмотрены все женские профили");
                    viewedFemaleProfiles.clear();
                }
            } else if ((profileToDisplay.getGender()).equals("сударь")) {
                viewedMaleProfiles.add(profileToDisplay);
                if (viewedMaleProfiles.size() == usersRepo.getNumberOfUsers(loggedUser.getId(), "сударь")) {
                    log.debug("Просмотрены все мужские профили");
                    viewedMaleProfiles.clear();
                }
            }
            log.debug("Показан следующий профиль противоположного пола");
            response = new Response(true, profileToDisplay.getUsername() + " " + profileToDisplay.getDescription());
        }
        log.debug("Ответ на запрос для просмотра следующего профиля: {}: {}",
                response.isStatus(), response.getAddition());
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.UNAUTHORIZED);
    }


    public User getUnviewedProfile(Iterable<User> profilesToDisplay, User loggedUser) {
        List<User> profilesToDisplayAsList = userService.getUsersAsList(profilesToDisplay);
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
}
