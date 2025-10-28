package com.example.demo.model.request;

import lombok.Data;

@Data
public class ChatMessageRequest {
    private Long senderId;      // ID của người gửi
    private Long recipientId;   // Nếu null -> hệ thống tự phân staff
    private String content;
    private String roomId;      // optional; nếu null -> build từ staff/customer
}
