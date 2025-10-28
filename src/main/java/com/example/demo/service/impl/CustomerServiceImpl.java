package com.example.demo.service.impl;

import com.example.demo.converter.CustomerConverter;
import com.example.demo.entity.Customer;
import com.example.demo.entity.User;
import com.example.demo.model.dto.AssignmentDTO;
import com.example.demo.model.dto.CustomerDTO;
import com.example.demo.model.request.CustomerCreateRequest;
import com.example.demo.model.response.ResponseDTO;
import com.example.demo.model.response.StaffResponseDTO;
import com.example.demo.repository.CustomerRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.CustomerService;
import com.example.demo.utils.CustomerUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class CustomerServiceImpl implements CustomerService {
    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private CustomerConverter customerConverter;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CustomerUtils customerUtils;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public List<CustomerDTO> findAll(Map<String, Object> conditions) {
        List<Customer> customerEntityList = customerRepository.findAll(conditions);
        List<CustomerDTO> customerDTOS = new ArrayList<>();
        for(Customer c : customerEntityList){
            customerDTOS.add(customerConverter.toCustomerDTO(c));
        }
        return customerDTOS;
    }
    @Override
    public Page<CustomerDTO> findAll(Map<String, Object> conditions, Pageable pageable) {
        Page<Customer> page = customerRepository.findAll(conditions, pageable);
        // map entity -> DTO
        List<CustomerDTO> dtos = new ArrayList<>();
        for (Customer c : page.getContent()) {
            dtos.add(customerConverter.toCustomerDTO(c));
        }
        return new org.springframework.data.domain.PageImpl<>(dtos, pageable, page.getTotalElements());
    }
//    @Override
//    public Page<CustomerDTO> findAll(Map<String, Object> conditions, int page, int size) {
//        Pageable pageable = PageRequest.of(page, size);
//        Page<Customer> customerPage = customerRepository.findAll(conditions, pageable);
//        return customerPage.map(customer -> customerConverter.toCustomerDTO(customer));
//    }

    @Override
    @Transactional
    public ResponseDTO save(CustomerCreateRequest customerCreateRequest) {
        ResponseDTO responseDTO = new ResponseDTO();
        try {
            Customer customerEntity;

            // Nếu có ID → cập nhật
            if (customerCreateRequest.getId() != null) {
                Customer existing = customerRepository.findById(customerCreateRequest.getId())
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy khách hàng"));
                customerUtils.setCustomerField(customerConverter.toCustomerEntity(customerCreateRequest), existing);
                customerEntity = existing;
            } else {
                // Nếu là khách hàng mới
                customerEntity = customerConverter.toCustomerEntity(customerCreateRequest);
                customerEntity.setCreatedBy("web-form");
            }

            System.out.println("👉 Nhận từ form: " + customerCreateRequest.getFullname() + " - " + customerCreateRequest.getEmail());
            Customer savedCustomer = customerRepository.save(customerEntity);
            System.out.println("✅ Đã lưu khách hàng mới: " + savedCustomer.getFullname());
            responseDTO.setMessage(customerCreateRequest.getId() != null ?
                    "Cập nhật khách hàng thành công" : "Thêm khách hàng thành công");

            // Trả dữ liệu để confirm
            responseDTO.setData(customerConverter.toCustomerDTO(savedCustomer));
        } catch (Exception e) {
            e.printStackTrace();
            responseDTO.setMessage("❌ Thêm khách hàng thất bại: " + e.getMessage());
        }

        return responseDTO;
    }


    @Override
    public CustomerCreateRequest findOneById(Long id) {
        Customer customerEntity = customerRepository.findById(id).orElse(null);
        if (customerEntity != null) return customerConverter.toCustomerCreateRequest(customerEntity);
        return null;
    }

    @Override
    public ResponseDTO disableActivity(List<Long> id) {
        ResponseDTO responseDTO = new ResponseDTO();
        for (Long i : id) {
            Customer customerEntity = customerRepository.findById(i).orElse(null);
            if (customerEntity != null) {
                customerEntity.setIsActive(0);
                try {
                    customerRepository.save(customerEntity);
                } catch (Exception e) {
                    responseDTO.setMessage("Xóa khách hàng " + customerEntity.getId() + " thất bại");
                    return responseDTO;
                }
            }
        }
        responseDTO.setMessage("Xóa khách hàng thành công");
        return responseDTO;
    }

    @Override
    public ResponseDTO findStaffsByCustomerId(Long id) {
        Customer customerEntity = customerRepository.findById(id).orElse(null);
        List<User> staffList = userRepository.findByStatusAndRoles_Code(1, "STAFF");
        List<User> assignedStaffList = customerEntity != null ? customerEntity.getUserEntities() : new ArrayList<>();
        List<StaffResponseDTO> staffResponseDTOS = new ArrayList<>();
        for (User u : staffList) {
            StaffResponseDTO staffResponseDTO = new StaffResponseDTO();
            staffResponseDTO.setStaffId(u.getId());
            staffResponseDTO.setFullName(u.getFullName());
            staffResponseDTO.setChecked(assignedStaffList.contains(u) ? "checked" : "");
            staffResponseDTOS.add(staffResponseDTO);
        }
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setData(staffResponseDTOS);
        responseDTO.setMessage("success");
        return responseDTO;
    }

    @Override
    public ResponseDTO updateAssignmentTable(AssignmentDTO assignmentDTO) {
        List<Long> staffIds = assignmentDTO.getStaffs();
        Customer customerEntity = customerRepository.findById(assignmentDTO.getId()).orElse(null);
        List<User> userEntities = new ArrayList<>();
        for (Long id : staffIds) {
            userEntities.add(userRepository.findById(id).orElse(null));
        }
        if (customerEntity != null) {
            customerEntity.setUserEntities(userEntities);
        }
        ResponseDTO responseDTO = new ResponseDTO();
        responseDTO.setMessage("success");
        return responseDTO;
    }
    
    @Override
    public boolean existsByUsername(String username) {
        return customerRepository.existsByUsername(username);
    }

    @Override
    public boolean existsByEmail(String email) {
        return customerRepository.existsByEmail(email);
    }

    @Override
    @Transactional
    public Customer saveDirect(Customer customer) {
        return customerRepository.save(customer);
    }

    @Override
    public CustomerDTO findOneByUsername(String username) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        return customerConverter.toCustomerDTO(customer);
    }

    @Override
    public CustomerDTO updateProfile(String username, CustomerDTO dto) {
        Customer customer = customerRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customerConverter.applyProfileUpdate(customer, dto);
        customer = customerRepository.save(customer);
        return customerConverter.toCustomerDTO(customer);
    }


    @Override
    public boolean changePassword(Long customerId, String oldPassword, String newPassword) {
        Customer customer = customerRepository.findById(customerId).orElse(null);
        if (customer == null) return false;

        // dùng instance passwordEncoder thay vì static
        if (customer.getPassword() == null || !passwordEncoder.matches(oldPassword, customer.getPassword())) {
            return false;
        }

        customer.setPassword(passwordEncoder.encode(newPassword));
        customerRepository.save(customer);
        return true;
    }
}
