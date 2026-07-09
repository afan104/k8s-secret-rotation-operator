package com.afan104.statusapp;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import java.util.Map;
import java.util.HashMap;

@RestController
public class StatusController {
    @GetMapping("/status")
    public Map<String, Object> status() {    
        String configValue = System.getenv("CONFIG_VALUE");
        String secretValue = System.getenv("SECRET_VALUE");

        Map<String, Object> response = new HashMap<>();
        response.put("config_value", configValue);
        response.put("secret_loaded", secretValue != null);
        response.put("secret_length", secretValue != null ? secretValue.length() : 0);

        return response;
    }
}