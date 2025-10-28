package com.example.demo.repository;

import com.example.demo.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    @Query("SELECT r FROM Review r " +
           "JOIN FETCH r.customer c " +
           "JOIN FETCH r.building b " +
           "WHERE b.id = :buildingId " +
           "ORDER BY r.createdDate DESC")
    List<Review> findAllByBuildingIdOrderByCreatedDateDesc(@Param("buildingId") Long buildingId);
}
