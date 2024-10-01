package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController {
    @PostMapping("/contact")
    public String saveContactenquiryDetails() {
        return "Enquiry details saved to DB";
    }
}