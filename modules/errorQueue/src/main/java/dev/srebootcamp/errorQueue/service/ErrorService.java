package dev.srebootcamp.errorQueue.service;

import org.springframework.stereotype.Service;

@Service
public class ErrorService {
    public void error(String msg) {
        System.out.println("Error: " + msg);
    }
}
