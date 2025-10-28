package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;
import com.example.demo.enums.ChatRoomStatus;
import java.time.LocalDateTime;

@Entity
@Table(name = "chat_room")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatRoom {

    /** PK = customerId (KHÔNG auto-increment) */
    @Id
    @Column(name = "id", nullable = false)
    private Long id;

    /** Lưu cho dễ truy vấn, luôn = id; có thể đặt CHECK(id=customer_id) ở DB */
    @Column(name = "customer_id", nullable = false)
    private Long customerId;

    /** Có thể NULL khi WAITING */
    @Column(name = "staff_id")
    private Long staffId;

    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ChatRoomStatus status;

    @Column(name = "room_code", length = 50, unique = true, nullable = false)
    private String roomCode;

    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ChatRoomStatus.WAITING;
        if (active == null) active = true;
        if (customerId == null) customerId = id;
        if (roomCode == null) {
            String staffPart = (staffId == null ? "NA" : String.valueOf(staffId));
            roomCode = "ROOM-" + staffPart + "-" + id + "-" + System.currentTimeMillis();
        }
    }
}
