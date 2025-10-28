package com.example.demo.controller.web;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.model.dto.ChatNotification;
import com.example.demo.service.ChatService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWsController(ChatService chatService,
                            SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat.send")
    public void processMessage(@Payload ChatMessage incoming) {
        try {
            Long sender = incoming.getSenderId();
            if (sender == null) return;

            ChatRoom room;
            boolean senderIsStaff = chatService.isStaff(sender);

            if (senderIsStaff) {
                room = chatService.getActiveRoomForStaff(sender)
                        .orElseGet(() -> chatService.assignNextWaitingRoomToStaff(sender).orElse(null));
                if (room == null) {
                    messagingTemplate.convertAndSend("/topic/system",
                            new ChatNotification(null, "Hiện chưa có khách trong hàng chờ.", null, null, null));
                    return;
                }
                incoming.setRecipientId(room.getCustomerId());
            } else {
                // customer: luôn upsert room có id = customerId
                room = chatService.createOrGetWaitingRoom(sender);
                if (room.getStaffId() != null) incoming.setRecipientId(room.getStaffId());
            }

            ChatMessage saved = chatService.saveMessage(incoming, room);

            ChatNotification notif = new ChatNotification(
                    saved.getSenderId(), saved.getContent(),
                    room.getStaffId(), room.getCustomerId(), room.getId()
            );
            messagingTemplate.convertAndSend("/topic/room." + room.getId(), notif);

        } catch (Exception e) {
            e.printStackTrace();
            messagingTemplate.convertAndSend("/topic/system",
                    new ChatNotification(null, "❌ Lỗi: " + e.getMessage(), null, null, null));
        }
    }
}
