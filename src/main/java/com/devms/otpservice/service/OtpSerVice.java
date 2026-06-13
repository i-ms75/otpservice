package com.devms.otpservice.service;

import com.devms.otpservice.Components.OtpHasher;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OtpSerVice
{
    private final SecureRandom secureRandom=new SecureRandom();
    private final OtpHasher otpHasher;
    EmailService emailService;
    @Value("${approvers}")
    List<String> adminEmail;

    Map<String, String> hasedOtps=new HashMap<>();

    public String sendOtp()
    {
        for(String email: adminEmail)
        {
            String otp=generateOtp();
            try
            {
                hasedOtps.put(email,otpHasher.hashOtp(email,otp));
            }
            catch (GeneralSecurityException e)
            {
                throw new IllegalStateException("Couldn't perform otp hashing: ",e);
            }

//            otps.put(email,Integer.valueOf(otp));
            emailService.sendEmail(email,"Otp service", "Your otp to approve vault data update is: "+otp);
        }
        hasedOtps.forEach(
                (key,value) ->
                {
                    System.out.println("Key: "+key+" Value: "+value);
                }
        );
        return "otp sent successfully";
    }

    public String generateOtp()
    {
        return String.format("%06d",secureRandom.nextInt(1_000_000));
    }
}
