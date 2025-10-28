package com.example.demo.repository.custom.impl;

import com.example.demo.entity.Customer;
import com.example.demo.repository.custom.CustomerRepositoryCustom;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@Repository
@Primary
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {
    @PersistenceContext
    private EntityManager entityManager;
    @Override
    public List<Customer> findAll(Map<String, Object> conditions) {
        StringBuilder sql = new StringBuilder("SELECT c.* FROM customer c ");
        joinTable(conditions, sql);
        sql.append(" WHERE 1 = 1 AND c.is_active = 1 ");
        appendConditions(conditions, sql);
        sql.append(" GROUP BY c.id ");
        Query query = entityManager.createNativeQuery(sql.toString(), Customer.class);
        return query.getResultList();
    }

    @Override
    public Page<Customer> findAll(Map<String, Object> conditions, Pageable pageable) {
        StringBuilder sql = new StringBuilder("SELECT c.* FROM customer c ");
        joinTable(conditions, sql);
        sql.append(" WHERE 1 = 1 AND c.is_active = 1 ");
        appendConditions(conditions, sql);
        sql.append(" GROUP BY c.id ");
        // Sắp xếp ổn định theo ngày tạo (nếu có), rồi theo id
        sql.append(" ORDER BY c.createddate DESC, c.id DESC ");

        Query dataQuery = entityManager.createNativeQuery(sql.toString(), Customer.class);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());

        @SuppressWarnings("unchecked")
        List<Customer> content = dataQuery.getResultList();
        long total = countAll(conditions);

        return new PageImpl<>(content, pageable, total);
    }

    @Override
    public long countAll(Map<String, Object> conditions) {
        StringBuilder countSql = new StringBuilder("SELECT COUNT(DISTINCT c.id) FROM customer c ");
        joinTable(conditions, countSql);
        countSql.append(" WHERE 1 = 1 AND c.is_active = 1 ");
        appendConditions(conditions, countSql);

        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        Object single = countQuery.getSingleResult();
        if (single instanceof Number) return ((Number) single).longValue();
        return Long.parseLong(single.toString());
    }

    private void joinTable(Map<String, Object> conditions, StringBuilder sql){
        if (conditions.get("staffId") != null && conditions.get("staffId").toString().trim() != ""){
            sql.append(" JOIN assignmentcustomer ass ON c.id = ass.customerid ");
        }
    }

    private void appendConditions(Map<String, Object> conditions, StringBuilder sql){
        for(Map.Entry<String, Object> entry : conditions.entrySet()){
            String key = entry.getKey();
            Object value = entry.getValue();
            if(key.equals("staffId") && value != null && value.toString().trim() != ""){
                sql.append(" AND ass.staffid = " + value);
            }
            else{
                if (!key.equals("d-3677046-p") && value != null && value.toString().trim() != ""){
                    sql.append(" AND c." + key + " LIKE '%" + value + "%' ");
                }
            }
        }
    }
}
