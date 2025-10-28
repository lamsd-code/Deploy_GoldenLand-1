package com.example.demo.controller.web;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class UserRegistryController {

    private final SimpUserRegistry userRegistry;

    // Xem các session đang online qua WebSocket (debug)
    @GetMapping("/api/online-users")
    public Set<String> onlineUsers() {
        return userRegistry.getUsers().stream().map(SimpUser::getName).collect(Collectors.toSet());
    }
}
