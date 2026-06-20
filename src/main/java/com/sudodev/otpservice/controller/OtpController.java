package com.sudodev.otpservice.controller;
import com.sudodev.otpservice.dto.EmailObject;
import com.sudodev.otpservice.dto.VerifyOtp;
import com.sudodev.otpservice.service.EmailService;
import com.sudodev.otpservice.service.OtpSerVice;
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
    public ResponseEntity<String> verifyOtp(@RequestBody VerifyOtp verifyOtp) {
        String reeuestId = null;
        try {
            if (verifyOtp == null || verifyOtp.getOtps() == null) {
                return new ResponseEntity<>("Please check your request, your otps never arrived", HttpStatus.BAD_REQUEST);
            }
            if (verifyOtp.getOtps().size() != 3) {
                return new ResponseEntity<>("Invalid size of otps, please send only the otps from the actual approvers", HttpStatus.BAD_REQUEST);
            }
            reeuestId = verifyOtp.getRequestId();
            return otpService.verifyOtp(reeuestId, verifyOtp.getOtps());
        } catch (Exception e) {
            log.warn("Error occurred: {}", reeuestId, e);
            return new ResponseEntity<>("Unexpected error occurred.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/api/sendEmail")
    public String sendEmail(@RequestBody EmailObject emailObject)
    {
        emailService.sendEmail(emailObject.getTo(),emailObject.getSubject(),emailObject.getText());
        log.info("Email sent by {}: ",emailObject.getSender());
        return "email sent";
    }
}
