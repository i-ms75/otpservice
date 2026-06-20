package com.devms.otpservice.controller;
import com.devms.otpservice.dto.EmailObject;
import com.devms.otpservice.dto.VerifyOtp;
import com.devms.otpservice.service.EmailService;
import com.devms.otpservice.service.OtpSerVice;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OtpController {

    @Autowired
    OtpSerVice otpService;

    @Autowired
    EmailService emailService;


    @PostMapping("api/generate")
    public ResponseEntity<String> generateOtp(@RequestBody String uuid)
    {
        return  otpService.sendOtp(uuid);
    }

    @PostMapping("api/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtp verifyOtp)
    {
        if (verifyOtp.getOtps().size() != 3)
        {
            return new ResponseEntity<>("Invalid size of otps, please send only the otps from the actual approvers", HttpStatus.BAD_REQUEST);
        }
        return otpService.verifyOtp(verifyOtp.getRequestId(),verifyOtp.getOtps());
    }

    @PostMapping("/api/sendEmail")
    public String sendEmail(@RequestBody EmailObject emailObject)
    {
        emailService.sendEmail(emailObject.getTo(),emailObject.getSubject(),emailObject.getText());
        log.info("Email sent by {}: ",emailObject.getSender());
        return "email sent";
    }
}
