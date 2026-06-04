package com.swapcampus.chat.service.impl;

import com.swapcampus.chat.service.ChatService;
import org.springframework.stereotype.Service;

@Service
public class ChatServiceImpl implements ChatService {

    @Override
    public String moduleName() {
        return "chat";
    }
}
