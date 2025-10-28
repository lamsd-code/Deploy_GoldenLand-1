package com.example.demo.repository;

import com.example.demo.entity.ChatRoom;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Optional<ChatRoom> findByCustomerIdAndActiveTrue(Long customerId);

    List<ChatRoom> findByStaffIdAndActiveTrue(Long staffId);

    long countByStaffIdAndActiveTrue(Long staffId);

    Optional<ChatRoom> findFirstByStaffIdAndActiveTrueOrderByCreatedAtDesc(Long staffId);

    Optional<ChatRoom> findFirstByActiveTrueAndStaffIdIsNullOrderByCreatedAtAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
           SELECT r FROM ChatRoom r
            WHERE r.active = true AND r.staffId IS NULL
            ORDER BY r.createdAt ASC
           """)
    List<ChatRoom> findOldestWaitingRoomsForUpdate(Pageable pageable);
}
