package com.example.demo.repository;

import com.example.demo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DashboardRepository extends JpaRepository<Transaction, Long> {

    // 1️⃣ Doanh thu theo tháng (bỏ điều kiện SUCCESS nếu test dữ liệu chưa có)
    @Query("""
        SELECT MONTH(t.createdDate) AS month, COALESCE(SUM(t.amount), 0) AS total
        FROM Transaction t
        WHERE YEAR(t.createdDate) = YEAR(CURRENT_DATE)
        GROUP BY MONTH(t.createdDate)
        ORDER BY MONTH(t.createdDate)
    """)
    List<Object[]> sumRevenueByMonth();

    // 2️⃣ Khách hàng mới theo tháng
    @Query("""
        SELECT MONTH(c.createdDate) AS month, COUNT(c.id) AS total
        FROM Customer c
        WHERE YEAR(c.createdDate) = YEAR(CURRENT_DATE)
        GROUP BY MONTH(c.createdDate)
        ORDER BY MONTH(c.createdDate)
    """)
    List<Object[]> countCustomersByMonth();

    // 3️⃣ Tòa nhà mới theo tháng
    @Query("""
        SELECT MONTH(b.createdDate) AS month, COUNT(b.id) AS total
        FROM Building b
        WHERE YEAR(b.createdDate) = YEAR(CURRENT_DATE)
        GROUP BY MONTH(b.createdDate)
        ORDER BY MONTH(b.createdDate)
    """)
    List<Object[]> countBuildingsByMonth();
}
