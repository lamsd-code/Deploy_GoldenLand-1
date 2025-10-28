package com.example.demo.repository;

import com.example.demo.entity.ChatAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatAssignmentRepository extends JpaRepository<ChatAssignment, Long> {

    List<ChatAssignment> findByCustomerIdAndActiveTrue(Long customerId);

    List<ChatAssignment> findByStaffIdAndActiveTrue(Long staffId);

    @Query("select count(a) from ChatAssignment a where a.active = true")
    long countAllActive();
}
