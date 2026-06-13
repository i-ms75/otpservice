package com.devms.otpservice.service;

import com.devms.otpservice.Components.OtpHasher;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.stereotype.Service;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@AllArgsConstructor
public class OtpSerVice
{
    private final StringRedisTemplate redisTemplate;
    private final SecureRandom secureRandom=new SecureRandom();
    private final OtpHasher otpHasher;
    EmailService emailService;
    @Value("${approvers}")
    List<String> adminEmail;

    Map<String, String> hashedOtps=new HashMap<>();

    public String sendOtp(String requestId) {
        String key="OTP:"+requestId;
        for(String email: adminEmail)
        {
            String sentOtp=generateOtp();
            hashedOtps.put(email,otpHasher.hashOtp(email,sentOtp));
            System.out.println("key: "+key+" email: "+email+" OTP: "+sentOtp);

            emailService.sendEmail(email,"Otp service", "Your otp to approve vault data update is: "+sentOtp);
            redisTemplate.opsForHash().putAll(key,hashedOtps);
            redisTemplate.expire(key, Expiration.milliseconds(300000));
        }
        hashedOtps.forEach(
                (key1,value) ->
                {
                    System.out.println("Key: "+key1+" Value: "+value);
                }
        );
        return "otp sent successfully";
    }

    public String generateOtp() {
        return String.format("%06d",secureRandom.nextInt(1_000_000));
    }

    public String verifyOtp(String requestId, List<String> receivedOtps)
    {
        String key="OTP:"+requestId;
        HashOperations<String,String,String> ops=redisTemplate.opsForHash();

        for(String email: adminEmail)
        {
            String storedHash=ops.get(key,email);
            String match=receivedOtps.stream()
                    .filter(code -> otpHasher.matches(email,code,storedHash))
                    .findFirst()
                    .orElse(null);
            if (match==null)
            {
                return "OTP verification failed";
            }

        }


        return "OTP verification successful";
    }
}
