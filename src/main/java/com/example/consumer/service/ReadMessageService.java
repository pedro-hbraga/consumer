package com.example.consumer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReadMessageService {

    public void readMessage(String message){
        System.out.println(message);
    }

}
