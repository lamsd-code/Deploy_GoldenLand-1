// src/main/java/com/example/demo/model/request/RegisterRequest.java
package com.example.demo.model.request;

import lombok.Getter; import lombok.Setter;

@Getter @Setter
public class RegisterRequest {
    private String fullName;
    private String phone;
    private String email;
    private String username;
    private String password;
    private String confirmPassword;
}
