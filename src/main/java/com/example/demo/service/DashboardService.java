package com.example.demo.service;

import com.example.demo.repository.DashboardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class DashboardService {

    @Autowired
    private DashboardRepository dashboardRepository;

    public Map<String, Object> getDashboardData() {
        Map<String, Object> data = new HashMap<>();

        // --- Doanh thu ---
        List<Object[]> revenueData = dashboardRepository.sumRevenueByMonth();
        Map<Integer, Double> revenueMap = new LinkedHashMap<>();
        for (Object[] row : revenueData) {
            revenueMap.put(((Number) row[0]).intValue(), ((Number) row[1]).doubleValue());
        }

        // --- Khách hàng mới ---
        List<Object[]> customerData = dashboardRepository.countCustomersByMonth();
        Map<Integer, Long> customerMap = new LinkedHashMap<>();
        for (Object[] row : customerData) {
            customerMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        // --- Tòa nhà mới ---
        List<Object[]> buildingData = dashboardRepository.countBuildingsByMonth();
        Map<Integer, Long> buildingMap = new LinkedHashMap<>();
        for (Object[] row : buildingData) {
            buildingMap.put(((Number) row[0]).intValue(), ((Number) row[1]).longValue());
        }

        // --- Tạo list dữ liệu cho 4 tháng gần nhất ---
        Calendar cal = Calendar.getInstance();
        int currentMonth = cal.get(Calendar.MONTH) + 1; // 1–12
        List<String> labels = new ArrayList<>();
        List<Double> revenues = new ArrayList<>();
        List<Long> customers = new ArrayList<>();
        List<Long> buildings = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            labels.add("Tháng " + month);
            revenues.add(revenueMap.getOrDefault(month, 0.0));
            customers.add(customerMap.getOrDefault(month, 0L));
            buildings.add(buildingMap.getOrDefault(month, 0L));
        }


        data.put("labels", labels);
        data.put("revenues", revenues);
        data.put("customers", customers);
        data.put("buildings", buildings);

        return data;
    }
}
