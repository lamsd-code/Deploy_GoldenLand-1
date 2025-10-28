package com.example.demo.security;

import com.example.demo.entity.Customer;
import com.example.demo.entity.User;
import com.example.demo.model.dto.MyUserDetail;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.UserRepository;
import jakarta.transaction.Transactional;
import com.example.demo.constant.SystemConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * ✅ Hàm loadUserByUsername — được Spring Security gọi khi user đăng nhập.
     * Dùng @Transactional để tránh lỗi LazyInitialization khi load roles.
     */
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        // 🟩 1️⃣ Ưu tiên tìm trong bảng User (Admin/Staff)
        User userEntity = userRepository.findOneByUserName(username);
        if (userEntity != null) {
            userEntity.getRoles().size(); // ép Hibernate load roles
            
         // Chuyển roles → GrantedAuthority
            List<GrantedAuthority> authorities = new ArrayList<>();
            
            if (userEntity.getRoles() == null || userEntity.getRoles().isEmpty()) {
                // ✅ Nếu là user nhưng chưa có vai trò → gán mặc định CUSTOMER
                authorities.add(new SimpleGrantedAuthority(SystemConstant.CUSTOMER_ROLE));
            } else {
                // ✅ Nếu có vai trò → load đúng quyền trong DB
                userEntity.getRoles().forEach(role -> 
                    authorities.add(new SimpleGrantedAuthority(role.getCode()))
                );
            }
            
            MyUserDetail myUserDetail = new MyUserDetail(
                    userEntity.getUserName(),
                    userEntity.getPassword(),
                    true, true, true, true,
                    authorities
            );
            myUserDetail.setId(userEntity.getId());
            myUserDetail.setFullName(userEntity.getFullName());
            return myUserDetail;
        }

        // 🟦 2️⃣ Nếu không có trong bảng User → kiểm tra Customer
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        if (customer != null) {
            List<GrantedAuthority> authorities = List.of(
                    new SimpleGrantedAuthority(SystemConstant.CUSTOMER_ROLE)
            );

            MyUserDetail myUserDetail = new MyUserDetail(
                    customer.getUsername(),
                    customer.getPassword(),
                    true, true, true, true,
                    authorities
            );
            myUserDetail.setFullName(customer.getFullname());
            myUserDetail.setId(customer.getId());
            return myUserDetail;
        }

        // 🟥 3️⃣ Nếu cả 2 bảng đều không có → báo lỗi
        throw new UsernameNotFoundException("Không tìm thấy tài khoản: " + username);
    }
}
