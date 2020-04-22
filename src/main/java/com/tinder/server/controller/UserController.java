package com.tinder.server.controller;

import com.tinder.server.model.Dislike;
import com.tinder.server.model.Like;
import com.tinder.server.model.ServerResponse;
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

    private final Set<String> loggedUsers = new HashSet<>();
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
    public ResponseEntity<Object> addUser(@RequestBody User newUser) {
        log.debug("Регистрация нового пользователя...");
        ServerResponse serverResponse;
        if (userService.usernameIsExists(newUser)) {
            log.debug("Имя пользователя занято");
            serverResponse = new ServerResponse(false, "Имя пользователя занято");
        } else if (!isValidUserInfo(newUser)) {
            log.debug("Введены некорректные данные");
            serverResponse = new ServerResponse(false, "Введены некорректные данные");
        } else {
            try {
                userService.put_one(newUser);
                log.debug("Регистрация прошла успешно");
                serverResponse = new ServerResponse(true, newUser.getId());
            } catch (DataIntegrityViolationException e) {
                serverResponse = new ServerResponse(false, "Ошибка сохранения учетной записи в БД");
            }
        }
        log.debug("Ответ на запрос о регистрации: {}: {}", serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.CREATED) :
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.BAD_REQUEST);

    }

    @PostMapping("/login")
    public ResponseEntity<Object> logInUser(@RequestBody User logInUser) {
        ServerResponse serverResponse;
        log.debug("Логин пользователя");
        User existingUser = usersRepo.findByUsername(logInUser.getUsername()).orElse(null);
        if (existingUser == null) {
            serverResponse = new ServerResponse(false, "Пользователя с именем " +
                    logInUser.getUsername() + " не существует");
            log.debug("Пользователя не существует");
        } else {
            log.debug("Пользователь {} существует: ", logInUser.getUsername());
            if (existingUser.getPassword().equals(logInUser.getPassword())) {
                loggedUsers.add(existingUser.getUsername());
                serverResponse = new ServerResponse(true, "Авторизация прошла успешно");
                log.debug("Пользователь {} авторизован: ", logInUser.getUsername());
            } else {
                serverResponse = new ServerResponse(false, "Введенный пароль неверный");
                log.debug("Неверный пароль");
            }
        }
        log.debug("Ответ на запрос об авторизации: {}: {}", serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.UNAUTHORIZED);
    }

    //newUser -- источник данных get
    //user -- пункт назначения set
    // user.set(newUser.get())

    @PostMapping("login/edit")
    public ResponseEntity<Object> changeDescription(@RequestBody User logInUser) {
        ServerResponse serverResponse;
        log.debug("Редактирование информации о себе...");
        if (loggedUsers.contains(logInUser.getUsername())) {
            User existingUser = userService.getUserByName(logInUser.getUsername());
            if (existingUser != null && isValidDescription(logInUser)) {
                try {
                    usersRepo.editingDescription(logInUser.getId(), logInUser.getDescription());
                    serverResponse = new ServerResponse(true, "информация о себе успешно обновлена");
                    log.debug("Описание изменено");
                } catch (DataIntegrityViolationException e) {
                    serverResponse = new ServerResponse(false, "Ошибка сохранения информации о себе в БД");
                    log.debug("Не удалось изменить информацию о себе");
                }
            } else {
                serverResponse = new ServerResponse(false, "Введены некорректные данные");
                log.debug("Введены некорректные данные");
            }
        } else {
            serverResponse = new ServerResponse(false, "Пользователь не авторизован");
            log.debug("Пользователь не авторизован");
        }
        log.debug("Ответ на запрос об обновлении информации о себе: {}: {}", serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.BAD_REQUEST);
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
    public ResponseEntity<Object> deleteUser(@RequestBody User logInUser) {
        ServerResponse serverResponse;
        if (loggedUsers.contains(logInUser.getUsername())) {
            log.debug("Удаление учетной записи пользователя...");
            loggedUsers.remove(logInUser.getUsername());
            usersRepo.deleteUser(logInUser.getUsername());
            serverResponse = new ServerResponse(true, "Учетная запись успешно удалена");
            log.debug("Удаление успешно");
        } else {
            serverResponse = new ServerResponse(false, "Ошибка удаления. Данный пользователь не авторизован");
            log.debug("Данный пользователь не авторизован");
        }
        log.debug("Ответ на запрос об удалении учетной записи пользователя: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(serverResponse.getAddition().toString(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("users/like/matching")
    public ResponseEntity<Iterable<User>> matchedUsersByLikes() {
        log.debug("Получение пользователей ответивших взаимной симпатией...");
        ServerResponse serverResponse;
        Iterable<User> likedUsers;
        User currentUser = userService.getUserByName(loggedUsers.iterator().next());
        if (currentUser != null) {
            likedUsers = usersRepo.getLikeMatchedUsers(currentUser.getId());
            serverResponse = new ServerResponse(true, likedUsers);
            log.debug("Список понравившихся пользователей получен");
        } else {
            serverResponse = new ServerResponse(false, "Вы не авторизованы");
            log.debug("Вы не авторизованы");
        }
        log.debug("Ответ на запрос о получении списка понравившихся юзерков: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>((Iterable<User>) serverResponse.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("users/dislike/matching")
    public ResponseEntity<Iterable<User>> matchedUsersByDislikes() {
        log.debug("Получение пользователей ответивших взаимной антипатией...");
        ServerResponse serverResponse;
        User currentUser = userService.getUserByName(loggedUsers.iterator().next());
        if (currentUser != null) {
            Iterable<User> dislikedUsers = usersRepo.getDislikeMatchedUsers(currentUser.getId());
            serverResponse = new ServerResponse(true, dislikedUsers);
            log.debug("Список НЕпонравившихся пользователей получен");
        } else {
            serverResponse = new ServerResponse(false, "Вы не авторизованы");
            log.debug("Вы не авторизованы");
        }
        log.debug("Ответ на запрос о получении списка НЕпонравившихся юзерков: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>((Iterable<User>) serverResponse.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("users/like/matching/{id}")
    public ResponseEntity<Object> getLikedUser(@PathVariable Long id) {
        log.debug("Выбор пользователя из списка понравившихся...");
        ServerResponse serverResponse;
        User matchedUser = userService.getUserById(id);
        if (matchedUser != null) {
            serverResponse = new ServerResponse(true, new User(matchedUser.getUsername(), matchedUser.getDescription()));
            log.debug("Получен понравившийся юзер");
        } else {
            serverResponse = new ServerResponse(false, "Не удалось получить информацию о понравившемся юзере");
            log.debug("Не удалось получить информацию о понравившемся юзере");
        }
        log.debug("Ответ на запрос о получении понравившегося юзера: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @GetMapping("users/dislike/matching/{id}")
    public ResponseEntity<Object> getDislikedUser(@PathVariable Long id) {
        log.debug("Выбор пользователя из списка НЕпонравившихся...");
        ServerResponse serverResponse;
        User matchedUser = userService.getUserById(id);
        if (matchedUser != null) {
            serverResponse = new ServerResponse(true, new User(matchedUser.getUsername(), matchedUser.getDescription()));
            log.debug("Получен НЕпонравившийся юзер");
        } else {
            serverResponse = new ServerResponse(false, "Не удалось получить информацию о НЕпонравившемся юзере");
            log.debug("Не удалось получить информацию о НЕпонравившемся юзере");
        }
        log.debug("Ответ на запрос о получении НЕпонравившегося юзера: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
    }

    @PostMapping("login/like")//вправо
    public ResponseEntity<Object> putLike(@RequestBody User likedUser) {
        log.debug("Проявление симпатии...");
        ServerResponse serverResponse;
        String loggedUsername = loggedUsers.iterator().next();
        User loggedUser = usersRepo.findByUsername(loggedUsername).orElse(null);

        if (loggedUser != null) {
            Like like = new Like(loggedUser.getId(), likedUser.getId());
            likeService.put_one(like);
            log.debug("добавили в БД лайк");
            //юзеры, с которыми взаимно
            Iterable<User> matchingUsers = usersRepo.getLikeMatchedUsers(loggedUser.getId());
            //из них выбираем того, кого сейчас лайкнули
            User matchUser = userService.getUsersAsList(matchingUsers).stream()
                    .filter(user -> likedUser.getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
            if (matchUser != null) {
                log.debug("юзер ответил взаимностью");
                serverResponse = new ServerResponse(true, matchUser.getUsername() + " ответил(а) мне взаимностью!");
            } else {
                log.debug("юзер не ответил взаимностью");
                serverResponse = new ServerResponse(false, "юзер НЕ ответил(а) мне взаимностью :'( ");
            }
        } else {
            log.debug("юзер не авторизован");
            serverResponse = new ServerResponse(false, "Неавторизованные юзеры не могут лайкать");
        }
        log.debug("Ответ на запрос о постановке лайка юзеру: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping("login/dislike")//влево
    public ResponseEntity<Object> putDislike(@RequestBody User dislikedUser) {
        log.debug("Проявление антипатии...");
        ServerResponse serverResponse;
        String loggedUsername = loggedUsers.iterator().next();
        User loggedUser = usersRepo.findByUsername(loggedUsername).orElse(null);

        if (loggedUser != null) {
            Dislike dislike = new Dislike(loggedUser.getId(), dislikedUser.getId());
            dislikeService.put_one(dislike);
            log.debug("добавили в БД дизлайк");
            //юзеры, с которыми взаимно
            Iterable<User> matchingUsers = usersRepo.getDislikeMatchedUsers(loggedUser.getId());
            //из них выбираем того, кого сейчас лайкнули
            User matchUser = userService.getUsersAsList(matchingUsers).stream()
                    .filter(user -> dislikedUser.getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
            if (matchUser != null) {
                log.debug("юзер ответил взаимностью");
                serverResponse = new ServerResponse(true, matchUser.getUsername() + " ответил(а) мне взаимностью!");
            } else {
                log.debug("юзер не ответил взаимностью");
                serverResponse = new ServerResponse(false, "юзер НЕ ответил(а) мне взаимностью :'( ");
            }
        } else {
            log.debug("юзер не авторизован");
            serverResponse = new ServerResponse(false, "Неавторизованные юзеры не могут дизлайкать");
        }
        log.debug("Ответ на запрос о постановке дизлайка юзеру: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.BAD_REQUEST);
    }

    @GetMapping("users/next")
    public ResponseEntity<Object> nextProfile(@RequestParam(value = "id") Long id) {
        log.debug("Получение следующего профиля...");
        ServerResponse serverResponse;
        User profileToDisplay;
        String loggedUsername = loggedUsers.iterator().next();
        User loggedUser = usersRepo.findByUsername(loggedUsername).orElse(null);
        if (loggedUser == null) {
            log.debug("Получение следующего профиля с НЕавторизованного юзера...");
            profileToDisplay = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(id), null);
            profilesForUnauthorizedUser.add(profileToDisplay);
            if (profilesForUnauthorizedUser.size() == usersRepo.getNumberOfUsersAnyGender(id)) {
                log.debug("Просмотрены все профили");
                profilesForUnauthorizedUser.clear();
            }
            log.debug("Показан следующий случайный профиль. Вы не авторизованы");
            serverResponse = new ServerResponse(false, "Показан следующий случайный профиль. Вы не авторизованы");
        } else {
            log.debug("Получение следующего профиля с авторизованного юзера...");
            //исключить юзеров, удовлетворяющих кретериям, но которых уже показывали
            profileToDisplay = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(id), loggedUser);
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
            log.debug("Показан следующий профиль противоположного пола");
            serverResponse = new ServerResponse(true, "Показан следующий профиль противоположного пола");
        }
        log.debug("Ответ на запрос для просмотра следующего профиля: {}: {}",
                serverResponse.isStatus(), serverResponse.getAddition());
        return serverResponse.isStatus() ?
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(serverResponse.getAddition(), HttpStatus.UNAUTHORIZED);
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
