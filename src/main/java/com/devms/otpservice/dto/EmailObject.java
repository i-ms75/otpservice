package com.devms.otpservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailObject
{
    String to;
    String subject;
    String text;
    String sender;
}
