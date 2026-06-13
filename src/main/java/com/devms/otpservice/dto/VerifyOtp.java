package com.devms.otpservice.dto;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class VerifyOtp
{
    String requestId;
    List<String> otps;
}
