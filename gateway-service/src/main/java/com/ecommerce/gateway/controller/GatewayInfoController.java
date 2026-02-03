package com.ecommerce.gateway.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Gateway Health and Info Controller
 */
@RestController
@RequestMapping("/actuator")
@Slf4j
public class GatewayInfoController {

    @GetMapping("/info")
    public Mono<ResponseEntity<Map<String, Object>>> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("service", "API Gateway");
        info.put("version", "1.0.0");
        info.put("timestamp", Instant.now().toString());
        info.put("status", "UP");

        return Mono.just(ResponseEntity.ok(info));
    }
}
