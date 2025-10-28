package com.example.demo.service.impl;

import com.example.demo.entity.ChatMessage;
import com.example.demo.entity.ChatRoom;
import com.example.demo.enums.ChatRoomStatus;
import com.example.demo.model.dto.ChatNotification;
import com.example.demo.repository.ChatMessageRepository;
import com.example.demo.repository.ChatRoomRepository;
import com.example.demo.service.ChatService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatServiceImpl implements ChatService {

    @Value("${chat.staff.ids}")
    private String staffIdsConfig;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatServiceImpl(ChatRoomRepository chatRoomRepository,
                           ChatMessageRepository chatMessageRepository,
                           @Qualifier("brokerMessagingTemplate")
                           SimpMessagingTemplate messagingTemplate) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatMessageRepository = chatMessageRepository;
        this.messagingTemplate = messagingTemplate;
    }

    private Set<Long> staffIds() {
        if (staffIdsConfig == null || staffIdsConfig.isBlank()) return Set.of();
        return Arrays.stream(staffIdsConfig.split(","))
                .map(String::trim).filter(s -> !s.isEmpty())
                .map(Long::parseLong)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    @Override public boolean isStaff(Long userId) { return userId != null && staffIds().contains(userId); }

    /** Upsert theo yêu cầu: room.id = customerId */
    @Override
    public ChatRoom createOrGetWaitingRoom(Long customerId) {
        if (customerId == null) throw new IllegalArgumentException("customerId không được null");

        return chatRoomRepository.findById(customerId)
                .map(r -> {
                    if (Boolean.TRUE.equals(r.getActive())) return r;    // đã active
                    // kích hoạt lại
                    r.setActive(true);
                    r.setStaffId(null);
                    r.setAssignedAt(null);
                    r.setClosedAt(null);
                    r.setStatus(ChatRoomStatus.WAITING);
                    r.setCreatedAt(LocalDateTime.now());
                    return chatRoomRepository.save(r);
                })
                .orElseGet(() -> {
                    ChatRoom room = ChatRoom.builder()
                            .id(customerId)
                            .customerId(customerId)
                            .staffId(null)
                            .active(true)
                            .createdAt(LocalDateTime.now())
                            .status(ChatRoomStatus.WAITING)
                            .roomCode("ROOM-C-" + customerId + "-" + System.currentTimeMillis())
                            .build();
                    ChatRoom saved = chatRoomRepository.save(room);
                    System.out.printf("🟢 Customer %d tạo phòng id=%d (WAITING)%n", customerId, saved.getId());
                    return saved;
                });
    }

    @Override
    public Optional<ChatRoom> getActiveRoomForStaff(Long staffId) {
        if (staffId == null) return Optional.empty();
        return chatRoomRepository.findFirstByStaffIdAndActiveTrueOrderByCreatedAtDesc(staffId);
    }
    @Override
    public ChatRoom findById(Long roomId) {
        return chatRoomRepository.findById(roomId).orElse(null);
    }
    @Override
    @Transactional
    public Optional<ChatRoom> assignNextWaitingRoomToStaff(Long staffId) {
        if (staffId == null) return Optional.empty();

        // Nếu đang có phòng active → trả lại ngay
        Optional<ChatRoom> current = getActiveRoomForStaff(staffId);
        if (current.isPresent()) return current;

        var locked = chatRoomRepository.findOldestWaitingRoomsForUpdate(PageRequest.of(0, 1));
        if (locked.isEmpty()) return Optional.empty();

        ChatRoom r = locked.get(0);
        r.setStaffId(staffId);
        r.setAssignedAt(LocalDateTime.now());
        r.setStatus(ChatRoomStatus.IN_PROGRESS);
        ChatRoom saved = chatRoomRepository.saveAndFlush(r);

        //start
        ChatNotification notif = new ChatNotification(
                null,
                "👋 Nhân viên #" + staffId + " đã tham gia hỗ trợ bạn.",
                staffId,
                saved.getCustomerId(),
                saved.getId()
        );
        System.out.println("📡 Gửi notify tới /topic/room." + saved.getId() +
                " cho customer " + saved.getCustomerId() +
                " staff " + staffId);
        messagingTemplate.convertAndSend("/topic/room." + saved.getId(), notif);
        //end


        System.out.printf("✅ Staff %d nhận phòng id=%d (customer %d)%n",
                staffId, saved.getId(), saved.getCustomerId());
        return Optional.of(saved);
    }

    @Override
    public void closeRoom(Long roomId) {
        chatRoomRepository.findById(roomId).ifPresent(r -> {
            if (Boolean.TRUE.equals(r.getActive())) {
                r.setActive(false);
                r.setClosedAt(LocalDateTime.now());
                r.setStatus(ChatRoomStatus.CLOSED);
                chatRoomRepository.save(r);
                System.out.printf("🟥 Đóng phòng id=%d%n", roomId);
            }
        });
    }

    @Override
    @Transactional
    public Map<String, Object> closeCurrentAndAssignNext(Long staffId, Long roomId) {
        Long closedId = null;

        ChatRoom toClose = null;
        if (roomId != null) {
            toClose = chatRoomRepository.findById(roomId)
                    .filter(r -> Boolean.TRUE.equals(r.getActive()))
                    .filter(r -> Objects.equals(r.getStaffId(), staffId))
                    .orElse(null);
        } else {
            toClose = chatRoomRepository.findFirstByStaffIdAndActiveTrueOrderByCreatedAtDesc(staffId).orElse(null);
        }
        if (toClose != null) {
            toClose.setActive(false);
            toClose.setClosedAt(LocalDateTime.now());
            toClose.setStatus(ChatRoomStatus.CLOSED);
            chatRoomRepository.save(toClose);
            closedId = toClose.getId();
        }

        Optional<ChatRoom> next = assignNextWaitingRoomToStaff(staffId);

        Map<String, Object> res = new LinkedHashMap<>();
        res.put("closedRoomId", closedId);
        res.put("nextRoomId", next.map(ChatRoom::getId).orElse(null));
        res.put("nextCustomerId", next.map(ChatRoom::getCustomerId).orElse(null));
        return res;
    }

    @Override
    @Transactional
    public ChatMessage saveMessage(ChatMessage message, ChatRoom room) {
        message.setRoom(room);
        message.setCreatedAt(Instant.now());

        Long sender = message.getSenderId();
        if (sender != null) {
            if (Objects.equals(sender, room.getStaffId())) message.setSenderRole("STAFF");
            else if (Objects.equals(sender, room.getCustomerId())) message.setSenderRole("CUSTOMER");
            else message.setSenderRole("UNKNOWN");
        } else message.setSenderRole("UNKNOWN");

        if (message.getRecipientId() == null) {
            Long recip = (sender != null ? sender : 0L);
            message.setRecipientId(recip);
        }
        return chatMessageRepository.save(message);
    }

    @Override
    public Map<String, Object> activeAssignmentStats() {
        Map<String, Object> out = new LinkedHashMap<>();
        long totalRooms = chatRoomRepository.count();
        long totalMessages = chatMessageRepository.count();
        Map<Long, Long> perStaff = new LinkedHashMap<>();
        for (Long sid : staffIds()) {
            perStaff.put(sid, chatRoomRepository.countByStaffIdAndActiveTrue(sid));
        }
        out.put("perStaffActive", perStaff);
        out.put("totalRooms", totalRooms);
        out.put("totalMessages", totalMessages);
        out.put("note", "PK room = customerId; claim dùng khoá PESSIMISTIC_WRITE.");
        return out;
    }

    @Override
    @Transactional
    public int resetStaffSession(Long staffId) {
        var rooms = chatRoomRepository.findByStaffIdAndActiveTrue(staffId);
        rooms.forEach(r -> {
            r.setActive(false);
            r.setClosedAt(LocalDateTime.now());
            r.setStatus(ChatRoomStatus.CLOSED);
        });
        chatRoomRepository.saveAll(rooms);
        System.out.printf("🧹 Reset staff %d: đóng %d phòng%n", staffId, rooms.size());
        return rooms.size();
    }
}
