package com.example.demo.service;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;

import java.util.Map;
import java.util.Optional;

public interface ChatService {

    /** Upsert phòng cho customer: PK room.id = customerId */
    ChatRoom createOrGetWaitingRoom(Long customerId);

    Optional<ChatRoom> getActiveRoomForStaff(Long staffId);

    Optional<ChatRoom> assignNextWaitingRoomToStaff(Long staffId);

    void closeRoom(Long roomId);

    ChatMessage saveMessage(ChatMessage message, ChatRoom room);

    Map<String, Object> activeAssignmentStats();

    boolean isStaff(Long userId);

    int resetStaffSession(Long staffId);
    ChatRoom findById(Long roomId);

    /** Staff bấm Thoát: đóng phòng hiện tại (hoặc roomId) và claim phòng chờ tiếp theo (nếu có). */
    Map<String, Object> closeCurrentAndAssignNext(Long staffId, Long roomId);

    default ChatRoom getOrCreateRoomForCustomer(Long customerId) {
        return createOrGetWaitingRoom(customerId);
    }
}
