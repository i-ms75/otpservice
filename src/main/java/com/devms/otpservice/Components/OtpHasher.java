package com.devms.otpservice.Components;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Component
public class OtpHasher
{
    private final SecretKeySpec key;
    private static final String algorithm="HmacSHA256";
    public OtpHasher(@Value("${pepper}") String pepper)
    {
        key=new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8),algorithm);
    }
    ;
//    SecretKeySpec key=new SecretKeySpec(pepper.getBytes(StandardCharsets.UTF_8),"HmacSHA256");

    public String hashOtp(String email, String code) throws NoSuchAlgorithmException
    {
        try
        {
            Mac mac=Mac.getInstance(algorithm);
            mac.init(key);
            byte[] digest= mac.doFinal((email+"|"+code).getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(digest);
        }
        catch (GeneralSecurityException e)
        {
            throw new IllegalStateException("Couldn't compute Hmac: ",e);
        }

    }
}
