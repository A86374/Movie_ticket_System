package com.mts.apps.controller;

import com.mts.apps.exception.MtsException;
import com.mts.apps.model.User;
import com.mts.apps.service.IUserService;
import com.mts.apps.util.InputUtil;

import java.util.Scanner;

public class UserController {

    private final IUserService userService;
    private final Scanner scanner;

    public UserController(IUserService userService, Scanner scanner) {
        this.userService = userService;
        this.scanner = scanner;
    }

    // US-01
    public void register() throws MtsException {
        User user = new User();
        user.setName(InputUtil.readText(scanner, "Name: "));
        user.setEmail(InputUtil.readText(scanner, "Email: "));
        user.setPhone(InputUtil.readText(scanner, "Phone (10 digits): "));
        user.setPassword(InputUtil.readText(scanner, "Password (at least 6 characters): "));

        userService.register(user);
        System.out.println("  Account created, you can log in now");
    }

    // US-02
    public User login() throws MtsException {
        String email = InputUtil.readText(scanner, "Email: ");
        String password = InputUtil.readText(scanner, "Password: ");

        User user = userService.login(email, password);
        System.out.println("  Welcome, " + user.getName());
        return user;
    }
}