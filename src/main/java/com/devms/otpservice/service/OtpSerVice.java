package com.devms.otpservice.service;
import com.devms.otpservice.Components.OtpHasher;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.types.Expiration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

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

    public ResponseEntity<String> sendOtp(String requestId) {
        Map<String, String> hashedOtps=new HashMap<>();
        String key="OTP:"+requestId;
        for(String email: adminEmail)
        {
            String sentOtp=generateOtp();
            hashedOtps.put(email,otpHasher.hashOtp(email,sentOtp));
            emailService.sendEmail(email,"Otp service", "Your otp to approve vault data update is: "+sentOtp);
            redisTemplate.opsForHash().putAll(key,hashedOtps);
            redisTemplate.expire(key, Expiration.milliseconds(300000));
        }
        return new ResponseEntity<>("otp sent successfully",HttpStatus.CREATED);
    }

    public String generateOtp() {
        return String.format("%06d",secureRandom.nextInt(1_000_000));
    }

    public ResponseEntity<String> verifyOtp(String requestId, List<String> receivedOtp)
    {
        String key="OTP:"+requestId;
        String attemptsKey="otpAttempt:"+requestId;
        if(!redisTemplate.hasKey(key))
        {
            return new ResponseEntity<>("Key not found or has already expired", HttpStatus.NOT_FOUND);
        }
        HashOperations<String,String,String> ops=redisTemplate.opsForHash();
        Long attempts=redisTemplate.opsForValue().increment(attemptsKey);
        if(attempts!=null && attempts==1L)
        {
            Long ttl=redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
            redisTemplate.expire(attemptsKey,Expiration.milliseconds(ttl));
        }


        Integer MAX_ATTEMPTS = 3;
        try {
            if(attempts> MAX_ATTEMPTS)
            {
                redisTemplate.delete(key);
                return new ResponseEntity<>("Maximum retries exceeded, please request for new otp",HttpStatus.TOO_MANY_REQUESTS);
            }
        }
        catch (Exception e)
        {
            return new ResponseEntity<>("Error occured! Please tr again",HttpStatus.INTERNAL_SERVER_ERROR);
        }

        for(String email: adminEmail)
        {
            String storedHash=ops.get(key,email);
            String match=receivedOtp.stream()
                    .filter(code -> otpHasher.matches(email,code,storedHash))
                    .findFirst()
                    .orElse(null);
            if (match==null)
            {
                return new ResponseEntity<>("OTP verification failed, remaining attempts is "+(MAX_ATTEMPTS-attempts),HttpStatus.NOT_ACCEPTABLE);
            }

        }
        redisTemplate.delete(key);
        return new ResponseEntity<>( "OTP verification successful",HttpStatus.OK);
    }
}
