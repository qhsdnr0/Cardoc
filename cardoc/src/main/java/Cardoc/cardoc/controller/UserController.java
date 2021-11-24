package Cardoc.cardoc.controller;

import Cardoc.cardoc.models.Trim;
import Cardoc.cardoc.models.User;
import Cardoc.cardoc.models.UserTrim;
import Cardoc.cardoc.repository.TrimRepository;
import Cardoc.cardoc.repository.UserRepository;
import Cardoc.cardoc.service.TrimService;
import Cardoc.cardoc.service.UserService;
import lombok.RequiredArgsConstructor;
import Cardoc.cardoc.token.Token;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("users")
@Transactional
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TrimRepository trimRepository;
    private final TrimService trimService;

    @PostMapping("/signup")
    public HashMap<String, String> newUser(@RequestBody UserForm userForm) {
        User user = new User();
        HashMap<String, String> result = new HashMap<>();
        userService.validateUserPassword(userForm.getPassword());
        String hashedPassword = BCrypt.hashpw(userForm.getPassword(), BCrypt.gensalt());

        user.setAccount(userForm.getAccount());
        user.setPassword(hashedPassword);
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        userService.createUser(user);
        result.put("message", "CREATED");
        return result;
    }

    @PostMapping("/signin")
    public HashMap<String, String> logIn(@RequestBody UserForm userForm) {
        HashMap<String, String> result = new HashMap<>();
        List<User> findUser = userRepository.findByAccount(userForm.getAccount());

        if (!findUser.isEmpty() && BCrypt.checkpw(userForm.getPassword(), findUser.get(0).getPassword())) {
            result.put("message", "SUCCESS");
            result.put("accessToken", Token.makeJwtToken(findUser.get(0)));
        }

        return result;
    }

    @PostMapping("/trims")
    public void saveTrim(@RequestHeader("Authorization") String token, @RequestBody UserForm userForm) {
        User user = userRepository.findOne(Token.decodeJwtToken(token));
        System.out.println("-------------------------------" +user.getAccount());
        Trim trim =  trimService.findTrim(userForm.getTrimId());
        if (userForm.getAccount().equals(user.getAccount())) {
            UserTrim userTrim = new UserTrim();
            userTrim.setTrim(trim);
            userTrim.setUser(user);
            userService.createUserTrim(userTrim);
        }
    }
}