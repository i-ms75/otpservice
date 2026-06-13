package com.devms.otpservice.controller;

import com.devms.otpservice.service.OtpSerVice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OtpController {

    @Autowired
    OtpSerVice otpService;

    @PostMapping("api/generate")
    public String generateOtp()
    {

        return  otpService.sendOtp();
//        return "ok";
    }

    @PostMapping("api/verify")
    public String verifyOtp()
    {
        return "otp verified";
    }
}
