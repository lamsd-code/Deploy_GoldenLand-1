package com.example.demo.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerDTO extends AbstractDTO{
    private String fullname;
    private String phone;
    private String email;
    private String demand;
    private String status;
 // phục vụ hiển thị/định danh profile
    private String username;

    // hiển thị thêm
    private String companyname;

    private Integer isActive;
}
