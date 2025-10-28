package com.example.demo.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Dành cho StompJS v7 (native WebSocket)
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // không withSockJS()
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // ✅ bật simple broker cho /topic
        registry.enableSimpleBroker("/topic");

        // ✅ prefix cho message client gửi lên server
        registry.setApplicationDestinationPrefixes("/app");
    }
//    @Bean
//    public SimpMessagingTemplate simpMessagingTemplate(
//            @Qualifier("brokerChannel") MessageChannel brokerChannel) {
//        return new SimpMessagingTemplate(brokerChannel);
//    }
}
