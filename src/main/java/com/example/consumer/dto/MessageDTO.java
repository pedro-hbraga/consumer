package com.example.consumer.dto;

import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class MessageDTO implements Serializable {
    private static final long serialVersionUID = -7712096970682639524L;
    
    private String message;
}
