package com.devms.otpservice.service;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
//@NoArgsConstructor
public class OtpSerVice {
    EmailService emailService;
    @Value("${approvers}")
    List<String> adminEmail;

    public String generateOtp() {
        for(String email: adminEmail)
        {
            emailService.sendEmail(email,"From Spring boot application", "Test text");
        }
        return "otp sent successfully";
    }
}
