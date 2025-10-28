package com.example.demo.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Getter
@Setter
@Entity
@Table(name = "review")
public class Review extends Base{
    
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "rating")
    private Integer rating;
    
    @ManyToOne(fetch = FetchType.EAGER) // ⚙️ EAGER để luôn load Customer khi lấy Review
    @JoinColumn(name = "buildingid")
    @JsonIgnoreProperties({"reviews"}) // tránh vòng lặp khi serialize Building
    private Building building;

    @ManyToOne(fetch = FetchType.EAGER) // ⚙️ EAGER load Customer luôn
    @JoinColumn(name = "customerid")
    @JsonIgnoreProperties({"reviews", "password", "email", "isActive"}) // loại bỏ field không cần trả
    private Customer customer;

//    @ManyToOne
//    @JoinColumn(name = "buildingid")
//    private Building building;
//
//    @ManyToOne
//    @JoinColumn(name = "customerid")
//    private Customer customer;
}
