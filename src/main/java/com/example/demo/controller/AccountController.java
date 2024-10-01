package com.example.demo.controller;

import com.example.demo.model.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AccountController {
    @GetMapping("/myAccount")
    public String getAccountDetails()
    {
        System.out.println("getAccountDetails  Calleddddddddddddddddddddddddddddddddddddddd.........");
        return "Here is your account details..";
    }

    @GetMapping("/user")
    public User getUserDetails(){
        User user = new User("Appu","Appu@234","Admin");
        return user;
    }
}
