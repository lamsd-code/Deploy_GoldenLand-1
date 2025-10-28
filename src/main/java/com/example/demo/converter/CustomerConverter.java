package com.example.demo.converter;


import com.example.demo.entity.Customer;
import com.example.demo.model.dto.CustomerDTO;
import com.example.demo.model.request.CustomerCreateRequest;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CustomerConverter {
    @Autowired
    private ModelMapper modelMapper;
    public CustomerDTO toCustomerDTO(Customer customerEntity) {
        CustomerDTO customerDTO = modelMapper.map(customerEntity, CustomerDTO.class);
        // ✅ Đảm bảo luôn có ID (nếu entity có Base class)
        if (customerEntity.getId() != null) {
            customerDTO.setId(customerEntity.getId());
        }
        return customerDTO;
    }
    public Customer toCustomerEntity(CustomerCreateRequest customerCreateRequest){
        Customer customerEntity = modelMapper.map(customerCreateRequest, Customer.class);
        
        customerEntity.setFullname(customerCreateRequest.getFullname());
        customerEntity.setPhone(customerCreateRequest.getPhone());
        customerEntity.setEmail(customerCreateRequest.getEmail());
        //customerEntity.setCompanyname(customerCreateRequest.getCompanyname());
        customerEntity.setDemand(customerCreateRequest.getDemand());
        // ⚙️ Nếu khách không nhập companyname hoặc status → gán mặc định
        if (customerCreateRequest.getCompanyname() == null || customerCreateRequest.getCompanyname().trim().isEmpty()) {
        	customerEntity.setCompanyname("Chưa cập nhật");
        } else {
        	customerEntity.setCompanyname(customerCreateRequest.getCompanyname());
        }

        if (customerCreateRequest.getStatus() == null || customerCreateRequest.getStatus().trim().isEmpty()) {
        	customerEntity.setStatus("Chưa xử lý");
        } else {
        	customerEntity.setStatus(customerCreateRequest.getStatus());
        }

        // ⚙️ Mặc định là khách hàng hoạt động
        customerEntity.setIsActive(1);
        return customerEntity;
    }
    public CustomerCreateRequest toCustomerCreateRequest(Customer customerEntity){
        CustomerCreateRequest customerCreateRequest = modelMapper.map(customerEntity, CustomerCreateRequest.class);
        return customerCreateRequest;
    }
    
 // === [ADDED from File 2] — Overload: tạo Entity có bao gồm username/password (nếu request có) ===
    /**
     * Tạo Customer entity từ request nhưng có xử lý thêm field đăng nhập (username/password) nếu tồn tại trong request.
     * LƯU Ý: password cần được encode ở Service trước khi lưu DB.
     */
    public Customer toCustomerEntityIncludingCredentials(CustomerCreateRequest req) {
        Customer entity = modelMapper.map(req, Customer.class);

        // Giữ logic tường minh tương tự file 2
        entity.setFullname(req.getFullname());
        entity.setPhone(req.getPhone());
        entity.setEmail(req.getEmail());
        entity.setDemand(req.getDemand());

        // Thử lấy username/password bằng reflection (đề phòng request chưa định nghĩa các getter này)
        try {
            Object u = req.getClass().getMethod("getUsername").invoke(req);
            if (u instanceof String && !((String) u).trim().isEmpty()) {
                entity.setUsername(((String) u).trim());
            }
        } catch (Exception ignored) {}

        try {
            Object p = req.getClass().getMethod("getPassword").invoke(req);
            if (p instanceof String && !((String) p).trim().isEmpty()) {
                entity.setPassword(((String) p).trim()); // ⚠️ nhớ encode ở Service
            }
        } catch (Exception ignored) {}

        if (req.getCompanyname() == null || req.getCompanyname().trim().isEmpty()) {
            entity.setCompanyname("Chưa cập nhật");
        } else {
            entity.setCompanyname(req.getCompanyname().trim());
        }

        if (req.getStatus() == null || req.getStatus().trim().isEmpty()) {
            entity.setStatus("Chưa xử lý");
        } else {
            entity.setStatus(req.getStatus().trim());
        }

        entity.setIsActive(1);
        return entity;
    }
 // === [END ADDED]

    // === [ADDED from File 2] — Cập nhật hồ sơ an toàn (không đụng tới các trường nhạy cảm trừ khi cho phép) ===
    /**
     * Áp dụng cập nhật hồ sơ từ CustomerDTO vào Entity một cách an toàn.
     * Mặc định KHÔNG thay đổi username/password/email trừ khi bạn chủ động mở cho phép.
     */
    public void applyProfileUpdate(Customer entity, CustomerDTO dto) {
        if (dto == null || entity == null) return;

        if (dto.getFullname() != null) entity.setFullname(dto.getFullname().trim());
        if (dto.getPhone() != null) entity.setPhone(dto.getPhone().trim());
        // Cho phép đổi email? mở comment nếu cần:
        // if (dto.getEmail() != null) entity.setEmail(dto.getEmail().trim());

        if (dto.getCompanyname() != null) {
            String val = dto.getCompanyname().trim();
            entity.setCompanyname(val.isEmpty() ? "Chưa cập nhật" : val);
        }
        if (dto.getDemand() != null) entity.setDemand(dto.getDemand().trim());
        if (dto.getStatus() != null) entity.setStatus(dto.getStatus().trim());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());
    }
 
}

