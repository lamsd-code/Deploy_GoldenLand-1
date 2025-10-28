package com.example.demo.api;

import com.example.demo.entity.ChatRoom;
import com.example.demo.model.dto.ChatNotification;
import com.example.demo.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class ChatRestAPI {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatRestAPI(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/room/wait")
    public ResponseEntity<?> waitRoom(@RequestParam("customerId") Long customerId) {
        ChatRoom room = chatService.createOrGetWaitingRoom(customerId);

        if (room.getStaffId() != null) {
            new Thread(() -> {
                try { Thread.sleep(300); } catch (InterruptedException ignored) {}
                ChatNotification notif = new ChatNotification(
                        null,
                        "👋 Nhân viên #" + room.getStaffId() + " đang hỗ trợ bạn.",
                        room.getStaffId(),
                        room.getCustomerId(),
                        room.getId()
                );
                messagingTemplate.convertAndSend("/topic/room." + room.getId(), notif);
                System.out.println("📡 Gửi lại notify cho /topic/room." + room.getId());
            }).start();
        }

        return ResponseEntity.ok(Map.of(
                "roomId", room.getId(),
                "staffId", room.getStaffId(),
                "customerId", room.getCustomerId(),
                "active", room.getActive(),
                "createdAt", room.getCreatedAt()
        ));
    }


    @GetMapping("/room/active-by-staff")
    public ResponseEntity<?> activeByStaff(@RequestParam("staffId") Long staffId) {
        return chatService.getActiveRoomForStaff(staffId)
                .<ResponseEntity<?>>map(r -> ResponseEntity.ok(Map.of(
                        "roomId", r.getId(),
                        "customerId", r.getCustomerId(),
                        "roomCode", r.getRoomCode()
                )))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/staff/next")
    public ResponseEntity<?> claimNext(@RequestParam("staffId") Long staffId) {
        return chatService.assignNextWaitingRoomToStaff(staffId)
                .<ResponseEntity<?>>map(r -> {
                    // Khi staff nhận phòng, thông báo tới khách hàng
                    try { Thread.sleep(500); } catch (InterruptedException ignored) {}
//                    ChatNotification notif = new ChatNotification(
//                            null,
//                            "👋 Nhân viên #" + staffId + " đã tham gia hỗ trợ bạn.",
//                            staffId,
//                            r.getCustomerId(),
//                            r.getId()
//                    );
//                    messagingTemplate.convertAndSend("/topic/room." + r.getId(), notif); // 🔔 gửi tới room
                    System.out.println("📡 Notify -> /topic/room." + r.getId() + " (staff=" + staffId + ")");

                    return ResponseEntity.ok(Map.of(
                            "roomId", r.getId(),
                            "customerId", r.getCustomerId(),
                            "roomCode", r.getRoomCode()
                    ));
                })
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/room/close")
    public ResponseEntity<?> closeRoom(@RequestParam("roomId") Long roomId) {
        chatService.closeRoom(roomId);
        return ResponseEntity.ok(Map.of("closed", true));
    }

    /** Staff bấm Thoát: đóng + claim tiếp theo (nếu có) */
    @PostMapping("/staff/close-and-next")
    public ResponseEntity<?> closeAndNext(@RequestParam Long staffId,
                                          @RequestParam(required = false) Long roomId) {
        return ResponseEntity.ok(chatService.closeCurrentAndAssignNext(staffId, roomId));
    }

    @PostMapping("/staff/reset")
    public ResponseEntity<?> resetStaff(@RequestParam Long staffId) {
        int closed = chatService.resetStaffSession(staffId);
        return ResponseEntity.ok(Map.of("closedRooms", closed));
    }
    @PostMapping("/room/replay")
    public ResponseEntity<?> replayNotify(@RequestParam("roomId") Long roomId) {
        ChatRoom room = chatService.findById(roomId);
        if (room == null) return ResponseEntity.notFound().build();

        ChatNotification notif = new ChatNotification(
                null,
                "👋 Nhân viên #" + room.getStaffId() + " đang hỗ trợ bạn.",
                room.getStaffId(),
                room.getCustomerId(),
                room.getId()
        );
        messagingTemplate.convertAndSend("/topic/room." + room.getId(), notif);
        System.out.println("📡 Replay notify -> /topic/room." + room.getId());
        return ResponseEntity.ok(Map.of("ok", true));
    }
}
