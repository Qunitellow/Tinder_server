package com.tinder.server.service;

import com.tinder.server.external.Response;
import com.tinder.server.external.Util;
import com.tinder.server.model.Dislike;
import com.tinder.server.model.Like;
import com.tinder.server.model.User;
import com.tinder.server.repository.DislikeRepository;
import com.tinder.server.repository.LikeRepository;
import com.tinder.server.repository.UsersRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RelationService {

    protected final UserLists userLists;
    protected final UsersRepository usersRepo;
    protected final DislikeRepository dislikeRepo;
    protected final LikeRepository likeRepo;
    protected final Util util;

    private static final Logger log = LoggerFactory.getLogger(RelationService.class);

    @Autowired
    public RelationService(UserLists userLists, UsersRepository usersRepo, DislikeRepository dislikeRepo,
                           LikeRepository likeRepo, Util util) {
        this.userLists = userLists;
        this.usersRepo = usersRepo;
        this.dislikeRepo = dislikeRepo;
        this.likeRepo = likeRepo;
        this.util = util;
    }


    /**
     * Request processing
     */
    public Response responseLike(Long showId) {
        if (userLists.nameLoggedUser == null) {
            return new Response(false, "Пожалуйста авторизуйтесь чтобы другие люди видели ваши дизлайки");
        }
        User loggedUser = usersRepo.findByUsername(userLists.nameLoggedUser).orElse(null);
        User userForLike = usersRepo.findById(showId).orElse(null);

        if (loggedUser == null || userForLike == null) {
            return new Response(false, "Запрашиваемые данные отсутствуют");
        }
        pushLike(loggedUser.getId(), userForLike.getId());

        //матч орнул
        Iterable<User> matchingUsers = usersRepo.getAllLikeMatchedUsers(loggedUser.getId());
        User matchUser = util.getUsersAsList(matchingUsers).stream()
                .filter(user -> userForLike.getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
        if (matchUser == null) {
            return new Response(true, "юзер НЕ ответил(а) взаимностью :'( ");
        }

        addToListOfViewedProfiles(loggedUser.getId(), matchUser, matchUser.getGender().equals("сударь") ?
                userLists.alreadyLikeMatchedMaleProfiles : userLists.alreadyLikeMatchedFemaleProfiles);

        return new Response(true, "|| Вы Любимы! ||");
    }

    public Response responseDislike(Long showId) {
        if (userLists.nameLoggedUser == null) {
            return new Response(false, "Пожалуйста авторизуйтесь чтобы другие люди видели ваши дизлайки");
        }
        User loggedUser = usersRepo.findByUsername(userLists.nameLoggedUser).orElse(null);
        User userForDislike = usersRepo.findById(showId).orElse(null);

        if (loggedUser == null || userForDislike == null) {
            return new Response(false, "Запрашиваемые данные отсутствуют");
        }
        pushDislike(loggedUser.getId(), userForDislike.getId());

        //матч орнул
        Iterable<User> matchingUsers = usersRepo.getAllDislikeMatchedUsers(loggedUser.getId());
        User matchUser = util.getUsersAsList(matchingUsers).stream()
                .filter(user -> userForDislike.getId().equals(user.getId()))
                .findFirst()
                .orElse(null);
        if (matchUser == null) {
            return new Response(true, "юзер НЕ ответил(а) взаимностью :'( ");
        }
        addToListOfViewedProfiles(loggedUser.getId(), matchUser, matchUser.getGender().equals("сударь") ?
                userLists.alreadyDislikeMatchedMaleProfiles : userLists.alreadyDislikeMatchedFemaleProfiles);
        return new Response(true, "|| Вы НЕлюбимы! ||");
    }

    public Response responseAuthNext(Long id) {
        User loggedUser = usersRepo.findByUsername(userLists.nameLoggedUser).orElse(null);

        List<User> profilesToDisplayAsList = util.getUsersAsList(usersRepo.getAllUsersWithoutMe(id));

        User profileToDisplay = getUnviewedProfile(profilesToDisplayAsList, loggedUser,
                loggedUser.getGender().equals("сударь") ? userLists.viewedFemaleProfiles : userLists.viewedMaleProfiles,
                loggedUser.getGender().equals("сударь") ? userLists.alreadyLikeMatchedFemaleProfiles : userLists.alreadyLikeMatchedMaleProfiles);

        Map<String, String> showUser = new HashMap<>();
        if (profileToDisplay == null) {
            showUser.put("description", "Нет профилей для просмотра. Все пользователи - ваши любимцы!" +
                    "ознакомьтесь со списком в меню любимцев.");
            return new Response(false, showUser);
        }

        listOfViewedProfiles(profileToDisplay, id,
                profileToDisplay.getGender().equals("сударыня") ? userLists.viewedFemaleProfiles : userLists.viewedMaleProfiles,
                profileToDisplay.getGender().equals("сударыня") ? userLists.alreadyLikeMatchedFemaleProfiles : userLists.alreadyLikeMatchedMaleProfiles);

        showUser.put("id", profileToDisplay.getId() + "");
        showUser.put("description", profileToDisplay.getUsername() + " | " + profileToDisplay.getDescription());
        return new Response(true, showUser);
    }

    public Response responseNoAuthNext() {

        List<User> profilesToDisplayAsList = util.getUsersAsList(usersRepo.findAll());
        User profileToDisplay = profilesToDisplayAsList.stream()
                .filter(user -> isNotViewed(user, userLists.profilesForUnauthorizedUser))
                .findFirst()
                .orElse(null);

        Map<String, String> showUser = new HashMap<>();
        if (profileToDisplay == null) {
            showUser.put("description", "Нет профилей для просмотра");
            return new Response(false, showUser);
        }

        userLists.profilesForUnauthorizedUser.add(profileToDisplay);
        if (userLists.profilesForUnauthorizedUser.size() == usersRepo.getNumberOfAllUsers()) {
            log.debug("Просмотрены все профили");
            userLists.profilesForUnauthorizedUser.clear();
        }

        showUser.put("id", profileToDisplay.getId() + "");
        showUser.put("description", profileToDisplay.getUsername() + " | " + profileToDisplay.getDescription());
        return new Response(true, showUser);
    }

    public Response responseMatch(Long id) {
        Iterable<User> likeMatchUsers = usersRepo.getAllLikeMatchedUsers(id);
        if (likeMatchUsers == null) {
            return new Response(false, "матчей нет ((");
        }
        return new Response(true, likeMatchUsers);
    }


    /**
     * Support
     */
    private void listOfViewedProfiles(User profileToDisplay, Long id, Set<User> viewedUser, Set<User> matchUser) {
        viewedUser.add(profileToDisplay);
        if (viewedUser.size() == usersRepo.getNumberOfUsersByGender(id, profileToDisplay.getGender()) - matchUser.size()) {
            log.debug("Просмотрены все профили");
            viewedUser.clear();
        }
    }

    public User getUnviewedProfile(List<User> profilesToDisplay, User loggedUser, Set<User> viewed, Set<User> matched) {
        String oppositeGender = loggedUser.getGender().equals("сударь") ? "сударыня" : "сударь";
        return profilesToDisplay.stream()
                .filter(user -> isNotViewed(user, viewed))
                .filter(user -> isNotViewed(user, matched))
                .filter(user -> user.getGender().equals(oppositeGender))
                .findFirst()
                .orElse(null);
    }

    private void pushDislike(Long loggedId, Long forDisId) {
        Like likeStatus = likeRepo.findLikeByByIdAndToId(loggedId, forDisId);
        if (likeStatus != null) {
            likeRepo.deleteById(likeStatus.getLikeId());
        }
        if (!dislikeRepo.existsDislikeByByIdAndToId(loggedId, forDisId)) {
            Dislike dislike = new Dislike(loggedId, forDisId);
            dislikeRepo.save(dislike);
        }
    }

    private void pushLike(Long loggedId, Long forLikeId) {
        Dislike dislikeStatus = dislikeRepo.findDislikeByByIdAndToId(loggedId, forLikeId);
        if (dislikeStatus != null) {
            dislikeRepo.deleteById(dislikeStatus.getDislikeId());
        }
        if (!likeRepo.existsLikeByByIdAndToId(loggedId, forLikeId)) {
            Like like = new Like(loggedId, forLikeId);
            likeRepo.save(like);
        }
    }

    public void addToListOfViewedProfiles(Long id, User matchUser, Set<User> allMatchProfiles) {
        allMatchProfiles.add(matchUser);
        if (allMatchProfiles.size() == usersRepo.getNumberOfUsersByGender(id, matchUser.getGender())) {
            allMatchProfiles.clear();
        }
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
