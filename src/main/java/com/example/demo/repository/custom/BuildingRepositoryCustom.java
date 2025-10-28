package com.example.demo.repository.custom;


import com.example.demo.builder.BuildingSearchBuilder;
import com.example.demo.entity.Building;
import com.example.demo.model.dto.BuildingDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BuildingRepositoryCustom {
    List<Building> findAll(BuildingSearchBuilder buildingSearchBuilder);
    void increaseViewCount(Long buildingId);

    List<Building> findTop4RelatedByDistrict(Long excludeId, String district);
//    List<BuildingEntity> getAllBuildings(Pageable pageable);
//
//    int countTotalItem();
    
 // Lấy tất cả bài đăng theo customer, có thể dùng builder
    List<Building> findAllByCustomerId(Long customerId);
    List<Building> findAll(BuildingSearchBuilder buildingSearchBuilder, Pageable pageable);
    long countAll(BuildingSearchBuilder buildingSearchBuilder);

}

