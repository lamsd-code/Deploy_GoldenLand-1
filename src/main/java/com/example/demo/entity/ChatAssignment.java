package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "chat_assignment",
        indexes = {
                @Index(name = "idx_assignment_staff_active", columnList = "staffId,active"),
                @Index(name = "idx_assignment_customer_active", columnList = "customerId,active")
        })
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ChatAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long staffId;
    private Long customerId;

    @Column(nullable = false)
    private boolean active;  // true khi phiên còn hiệu lực

    @Column(nullable = false)
    private Instant startedAt;
    private Instant endedAt;
}
