package com.tinder.server.controller;

import com.tinder.server.model.Dislike;
import com.tinder.server.model.Like;
import com.tinder.server.external.Response;
import com.tinder.server.model.User;
import com.tinder.server.repository.DislikeRepository;
import com.tinder.server.repository.LikeRepository;
import com.tinder.server.repository.UsersRepository;
import com.tinder.server.service.DislikeService;
import com.tinder.server.service.LikeService;
import com.tinder.server.service.UserService;
import lombok.EqualsAndHashCode;
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
@EqualsAndHashCode
public class UserController {

    private String nameLoggedUser = null;
    private final Set<User> viewedFemaleProfiles = new HashSet<>();
    private final Set<User> viewedMaleProfiles = new HashSet<>();
    private final Set<User> profilesForUnauthorizedUser = new HashSet<>();
    private final Set<User> alreadyLikeMatchedMaleProfiles = new HashSet<>();
    private final Set<User> alreadyLikeMatchedFemaleProfiles = new HashSet<>();
    private final Set<User> alreadyDislikeMatchedMaleProfiles = new HashSet<>();
    private final Set<User> alreadyDislikeMatchedFemaleProfiles = new HashSet<>();
    boolean forIncludeMatchUsers = false;

    private static final Logger log = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;
    private final LikeService likeService;
    private final DislikeService dislikeService;
    private final UsersRepository usersRepo;
    private final LikeRepository likeRepo;
    private final DislikeRepository dislikeRepo;

    @Autowired
    public UserController(UserService userService, LikeService likeService,
                          DislikeService dislikeService, UsersRepository usersRepo, LikeRepository likeRepo, DislikeRepository dislikeRepo) {
        this.userService = userService;
        this.likeService = likeService;
        this.dislikeService = dislikeService;
        this.usersRepo = usersRepo;
        this.likeRepo = likeRepo;
        this.dislikeRepo = dislikeRepo;
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
                        params.get("password"), params.get("description")));
                log.debug("Регистрация прошла успешно");
                response = new Response(true,
                        userService.getUserByName(params.get("username")));
//                        userService.getUserByName(params.get("username")).getId());
            } catch (DataIntegrityViolationException e) {
                response = new Response(false, "Ошибка сохранения учетной записи в БД");
            }
        }
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK);
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
                response = new Response(true, existingUser.getId());
                log.debug("Пользователь {} авторизован: ", params.get("username"));
                clearLists();
            } else {
                response = new Response(false, "Введенный пароль неверный");
                log.debug("Неверный пароль");
            }
        }
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.CREATED);
    }

    private void clearLists() {
        viewedFemaleProfiles.clear();
        viewedMaleProfiles.clear();
        profilesForUnauthorizedUser.clear();
        alreadyLikeMatchedMaleProfiles.clear();
        alreadyLikeMatchedFemaleProfiles.clear();
        alreadyDislikeMatchedMaleProfiles.clear();
        alreadyDislikeMatchedFemaleProfiles.clear();
    }


    @PutMapping("login/edit")
    public ResponseEntity<Object> changeDescription(@RequestBody String newDesc) {
        log.debug("Редактирование информации о себе...");
        Response response;
        if (nameLoggedUser != null) {
            User currentUser = userService.getUserByName(nameLoggedUser);

            if (isValidDescription(newDesc) && currentUser != null) {
                try {
                    currentUser.setDescription(newDesc);
                    userService.put_one(currentUser);
//                    usersRepo.editDescription(currentUser.getId(), newDesc);
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
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.CREATED);
    }


    @PostMapping("login/edit/delete")
    public ResponseEntity<String> deleteUser(@RequestBody Long id) {
        Response response;
        User currentUser = userService.getUserById(id);
        if (currentUser != null) {
            log.debug("Удаление учетной записи пользователя...");
            nameLoggedUser = null;
            userService.delete_one(id);
            response = new Response(true, "|| Ваша анкета удалена безвозвратно ||");
            log.debug("Удаление успешно");
        } else {
            response = new Response(false, "Ошибка удаления. Данный пользователь не авторизован");
            log.debug("Данный пользователь не авторизован");
        }
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.OK);
    }


    @GetMapping("me/like/matching/{id}")
//    @GetMapping("me/dislike/matching/{id}")
    public ResponseEntity<Iterable<User>> matchedUsersByLikes(@PathVariable Long id) {
        log.debug("Получение списка пользователей ответивших взаимной симпатией...");
        Response response;
        Iterable<User> likeMatchUsers;
        likeMatchUsers = usersRepo.getAllLikeMatchedUsers(id);
//        likeMatchUsers = usersRepo.getAllLikeMatchedUsers(id);
//        likeMatchUsers.forEach(System.out::println);//
        if (likeMatchUsers == null) {
            response = new Response(false, "матчей нет ((");
        } else {
            log.debug("Список лайк-матчей получен");
            response = new Response(true, likeMatchUsers);
//            response = new Response(true, likeMatchUsersAsMap(likeMatchUsers));
            likeMatchUsers.forEach(System.out::println);
        }
        return response.isStatus() ?
                new ResponseEntity<>((Iterable<User>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.OK);
    }


    @PostMapping("login/dislike")//влево
    public ResponseEntity<Object> putDislike(@RequestBody Long showId) {
        log.debug("Проявление антипатии...");
        Response response;
        if (nameLoggedUser != null) {
            User loggedUser = userService.getUserByName(nameLoggedUser);
            User userForDislike = userService.getUserById(showId);

            //удаление лайка перед тем как поставить дизлайк
            Set<Like> likeStatus = likeRepo.findAllByByIdAndToId(loggedUser.getId(), userForDislike.getId());
            if (!likeStatus.isEmpty()) {
                likeService.delete_one(likeStatus.iterator().next().getLikeId());
            }

            //////////
            System.out.println("\nдизлайк от " + loggedUser.getId() + " для " + userForDislike.getId() + "\n");
            if (!dislikeRepo.existsDislikeByByIdAndToId(loggedUser.getId(), userForDislike.getId())) {
                Dislike dislike = new Dislike(loggedUser.getId(), userForDislike.getId());
                dislikeService.put_one(dislike);
            }
            log.debug("добавили в БД дизлайк");

            //юзеры, с которыми взаимно
            Iterable<User> matchingUsers = usersRepo.getAllDislikeMatchedUsers(loggedUser.getId());
            /////////
            System.out.println("юзеры, с которыми взаимно ");
            matchingUsers.forEach(user -> System.out.println(user.getUsername()));
            //из них выбираем того, кого сейчас дизлайкнули
            User matchUser = userService.getUsersAsList(matchingUsers).stream()
                    .filter(user -> userForDislike.getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
            ////////////
            System.out.println("ищем того кого дизлайкнули в матчах");
            boolean match = matchUser != null;
            System.out.println("матч: " + match);
            if (matchUser != null) {
                addDislikeMatch(loggedUser, matchUser);
                response = new Response(true, "|| Вы НЕлюбимы! ||");
            } else {
                log.debug("юзер не ответил взаимностью");
                response = new Response(true, "юзер НЕ ответил(а) взаимностью :'( ");
            }
        } else {
            log.debug("юзер не авторизован");
            response = new Response(false, "Пожалуйста авторизуйтесь чтобы другие люди видели ваши дизлайки");
        }
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK);
    }


    @PostMapping("login/like")//вправо
    public ResponseEntity<Object> putLike(@RequestBody Long showId) {
        log.debug("Проявление симпатии...");
        Response response;
        if (nameLoggedUser != null) {
            User loggedUser = userService.getUserByName(nameLoggedUser);
            User userForLike = userService.getUserById(showId);

            //удаление дизлайка перед тем как поставить лайк
            Set<Dislike> dislikeStatus = dislikeRepo.findAllByByIdAndToId(loggedUser.getId(), userForLike.getId());
            if (!dislikeStatus.isEmpty()) {
                dislikeService.delete_one(dislikeStatus.iterator().next().getDislikeId());
            }
            //проверка существования лайка
            if (!likeRepo.existsLikeByByIdAndToId(loggedUser.getId(), userForLike.getId())) {
                Like like = new Like(loggedUser.getId(), userForLike.getId());
                likeService.put_one(like);
                log.debug("добавили в БД лайк");
            }

            //////////
            System.out.println("\nлайк от " + loggedUser.getId() + " для " + userForLike.getId() + "\n");

            //юзеры, с которыми взаимно
            Iterable<User> matchingUsers = usersRepo.getAllLikeMatchedUsers(loggedUser.getId());
            /////////
            System.out.println("юзеры, с которыми взаимно \n");
            matchingUsers.forEach(user -> System.out.println(user.getUsername()));
            //из них выбираем того, кого сейчас лайкнули
            User matchUser = userService.getUsersAsList(matchingUsers).stream()
                    .filter(user -> userForLike.getId().equals(user.getId()))
                    .findFirst()
                    .orElse(null);
            ////
            System.out.println("\nищем того кого лайкнули в матчах\n");
            boolean match = matchUser != null;
            System.out.println("матч: " + match + "\n");

            if (matchUser != null) {
                log.debug("юзер ответил взаимностью");
////
                addLikeMatch(loggedUser, matchUser);

                response = new Response(true, "|| Вы любимы ||");
            } else {
                log.debug("юзер не ответил взаимностью");
                response = new Response(true, "юзер НЕ ответил(а) взаимностью :'( ");
            }
        } else {
            log.debug("юзер не авторизован");
            response = new Response(false, "Неавторизованные юзеры не могут лайкать");
        }
        return response.isStatus() ?
                new ResponseEntity<>(response.getAddition(), HttpStatus.CREATED) :
                new ResponseEntity<>(response.getAddition(), HttpStatus.OK);
    }

    public void addLikeMatch(User loggedUser, User matchUser) {
        if (matchUser.getGender().equals("сударь")) {
            alreadyLikeMatchedMaleProfiles.add(matchUser);
            if (alreadyLikeMatchedMaleProfiles.size() == usersRepo.getNumberOfUsersByGender(loggedUser.getId(), "сударь")) {
                alreadyLikeMatchedMaleProfiles.clear();
            }
        } else {
            alreadyLikeMatchedFemaleProfiles.add(matchUser);
            if (alreadyLikeMatchedFemaleProfiles.size() == usersRepo.getNumberOfUsersByGender(loggedUser.getId(), "сударыня")) {
                alreadyLikeMatchedFemaleProfiles.clear();
            }
        }
    }

    public void addDislikeMatch(User loggedUser, User matchUser) {
        if (matchUser.getGender().equals("сударь")) {
            alreadyDislikeMatchedMaleProfiles.add(matchUser);
            if (alreadyDislikeMatchedMaleProfiles.size() == usersRepo.getNumberOfUsersByGender(loggedUser.getId(), "сударь")) {
                alreadyDislikeMatchedMaleProfiles.clear();
            }
        } else {
            alreadyDislikeMatchedFemaleProfiles.add(matchUser);
            if (alreadyDislikeMatchedFemaleProfiles.size() == usersRepo.getNumberOfUsersByGender(loggedUser.getId(), "сударыня")) {
                alreadyDislikeMatchedFemaleProfiles.clear();
            }
        }
    }


    public User getUnviewedProfile(Iterable<User> profilesToDisplay) {
        List<User> profilesToDisplayAsList = userService.getUsersAsList(profilesToDisplay);
        profilesToDisplayAsList.forEach(System.out::println);
        User loggedUser = userService.getUserByName(nameLoggedUser);
        System.out.println("loggedUser: " + loggedUser);
        if (loggedUser != null) {
            if (loggedUser.getGender().equals("сударь")) {
                return profilesToDisplayAsList.stream()
                        .filter(user -> isNotViewed(user, viewedFemaleProfiles))
                        .filter(user -> isNotViewed(user, alreadyLikeMatchedFemaleProfiles))
                        .filter(user -> user.getGender().equals("сударыня"))
                        .findFirst()
                        .orElse(null);
            } else if (loggedUser.getGender().equals("сударыня")) {
                return profilesToDisplayAsList.stream()
                        .filter(user -> isNotViewed(user, viewedMaleProfiles))
                        .filter(user -> isNotViewed(user, alreadyLikeMatchedMaleProfiles))
                        .filter(user -> user.getGender().equals("сударь"))
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

    public boolean isNotViewed(User user, Set<User> viewedProfiles) { //(юзер из общей кучи, просмотренные юзеры)

        for (User viewedProfile : viewedProfiles) {
            if (user.equals(viewedProfile)) {
                return false;
            }
        }
        return true;
    }


    @PostMapping("login/users/next")
    public ResponseEntity<Map<String, String>> nextProfileAuth(@RequestBody Long id) {
        log.debug("Получение следующего профиля...");
        Response response;
        User profileToDisplay;

        log.debug("Получение для показа следующего профиля с авторизованного юзера...");
        //исключить юзеров, удовлетворяющих кретериям, но которых уже показывали

//      userForLike = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(loggedUser.getId()));
        System.out.println(id);
        profileToDisplay = getUnviewedProfile(usersRepo.getAllUsersWithoutMe(id));
        System.out.println("////////////////////////////////////////");
        System.out.println("******* Профиль для показа: " + profileToDisplay + " ********");
        System.out.println("////////////////////////////////////////");

        System.out.println("_______________________________");
        viewedFemaleProfiles.forEach(user -> System.out.println("viewedFemaleProfiles: " + user.getUsername()));
        viewedMaleProfiles.forEach(user -> System.out.println("viewedMaleProfiles: " + user.getUsername()));
        profilesForUnauthorizedUser.forEach(user -> System.out.println("profilesForUnauthorizedUser: " + user.getUsername()));
        alreadyLikeMatchedMaleProfiles.forEach(user -> System.out.println("alreadyMatchedMaleProfiles: " + user.getUsername()));
        alreadyLikeMatchedFemaleProfiles.forEach(user -> System.out.println("alreadyMatchedFemaleProfiles: " + user.getUsername()));
        System.out.println("_______________________________");

        Map<String, String> showUser = new HashMap<>();

        if (profileToDisplay == null) {
            showUser.put("description", "Нет профилей для просмотра");
            response = new Response(false, showUser);
//            response = new Response(false, "Нет профилей для просмотра");
        } else {
            showUser.put("id", profileToDisplay.getId() + "");
            showUser.put("description", profileToDisplay.getUsername() + " " + profileToDisplay.getDescription());
            response = new Response(true, showUser);
            if ((profileToDisplay.getGender()).equals("сударыня")) {
                viewedFemaleProfiles.add(profileToDisplay);
                System.out.println("viewedFemaleProfiles(показанные юзеры) = " + viewedMaleProfiles.size());
                System.out.println("все юзеры женского пола = " + usersRepo.getNumberOfUsersByGender(id, "сударыня"));
                System.out.println("flag: " + forIncludeMatchUsers);
                int numberUsersToView = usersRepo.getNumberOfUsersByGender(id, "сударыня");
                if (forIncludeMatchUsers) {
                    numberUsersToView -= alreadyLikeMatchedFemaleProfiles.size();
                }
                if (viewedFemaleProfiles.size() == numberUsersToView) {
                    log.debug("Просмотрены все женские профили");
                    viewedFemaleProfiles.clear();
                    forIncludeMatchUsers = true;
                }
            } else if ((profileToDisplay.getGender()).equals("сударь")) {
                viewedMaleProfiles.add(profileToDisplay);
                System.out.println("viewedMaleProfiles(показанные юзеры) = " + viewedMaleProfiles.size());
                System.out.println("все юзеры мужского пола = " + usersRepo.getNumberOfUsersByGender(id, "сударь"));
                System.out.println("flag: " + forIncludeMatchUsers);
                int numberUsersToView = usersRepo.getNumberOfUsersByGender(id, "сударь");
                if (forIncludeMatchUsers) {
                    numberUsersToView -= alreadyLikeMatchedMaleProfiles.size();
                }
                if (viewedMaleProfiles.size() == numberUsersToView) {
                    log.debug("Просмотрены все мужские профили");
                    viewedMaleProfiles.clear();
                    forIncludeMatchUsers = true;
                }
            }
        }
        return response.isStatus() ?
                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.CREATED);
    }


    @GetMapping("login/users/next")
    public ResponseEntity<Map<String, String>> nextProfileNoAuth(@RequestParam Long num) {
        log.debug("Получение для показа следующего профиля " + num + " с НЕавторизованного юзера...");
        Response response;
        User profileToDisplay = getUnviewedProfile(usersRepo.findAll());

        Map<String, String> showUser = new HashMap<>();

        if (profileToDisplay == null) {
            showUser.put("description", "Нет профилей для просмотра");
            response = new Response(false, showUser);
        } else {
            showUser.put("id", profileToDisplay.getId() + "");
            showUser.put("description", profileToDisplay.getUsername() + " " + profileToDisplay.getDescription());
            response = new Response(true, showUser);
            log.debug("Показан следующий случайный профиль. Вы не авторизованы");
            profilesForUnauthorizedUser.add(profileToDisplay);
            if (profilesForUnauthorizedUser.size() == usersRepo.getNumberOfAllUsers()) {
                log.debug("Просмотрены все профили");
                profilesForUnauthorizedUser.clear();
            }
        }
        return new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK);
    }


    @GetMapping("login/currentuser")
    public ResponseEntity<Map<String, String>> getCurrentUser(@RequestParam Integer num) {
        Response response;
        log.debug(num + "");
        User currentUser = userService.getUserByName(nameLoggedUser);
        if (currentUser == null) {
            response = new Response(false);
        } else {
            Map<String, String> currentUserAsMap = new HashMap<>();
            currentUserAsMap.put("id", currentUser.getId() + "");
            currentUserAsMap.put("gender", currentUser.getGender());
            currentUserAsMap.put("username", currentUser.getUsername());
            currentUserAsMap.put("password", currentUser.getPassword());
            currentUserAsMap.put("description", currentUser.getDescription());

            response = new Response(true, currentUserAsMap);
        }
        return response.isStatus() ?
                new ResponseEntity<>((Map<String, String>) response.getAddition(), HttpStatus.OK) :
                new ResponseEntity<>(null, HttpStatus.OK);
    }


    @PostMapping("/breakuser")
    public ResponseEntity<String> breakUser(@RequestBody String s) {
        nameLoggedUser = null;
        return new ResponseEntity<>(s, HttpStatus.OK);
    }


    @PostMapping("/logout")
    public ResponseEntity<String> logOut(@RequestBody String s) {
        nameLoggedUser = null;
        Response response = new Response(true, "|| Вы вышли из учетной записи ||"+s);
        return new ResponseEntity<>(response.getAddition().toString(), HttpStatus.OK);
    }
}

//Пример:
//       @RequestMapping("...")
//       void bar(@RequestBody String body, @RequestParam("baz") baz) {
//           //method body
//@RequestBody : переменная body будет содержать тело HTTP-запроса
//@RequestParam : переменная baz будет содержать значение параметра запроса baz