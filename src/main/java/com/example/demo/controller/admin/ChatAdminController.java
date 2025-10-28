package com.example.demo.controller.web;

import com.example.demo.service.ChatService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ChatAdminController {

    private final ChatService chatService;

    public ChatAdminController(ChatService chatService) {
        this.chatService = chatService;
    }

    // Thống kê số khách đang active trên mỗi staff
    @GetMapping("/api/chat/assignments/active")
    public Map<String, Object> activeAssignments() {
        return chatService.activeAssignmentStats();
    }
}
