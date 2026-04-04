package org.example.quizizz.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Controller
public class SpaForwardController {

    private static final List<String> EXCLUDED_PREFIXES = List.of(
            "/api",
            "/actuator",
            "/swagger-ui",
            "/v3/api-docs",
            "/webjars",
            "/socket.io",
            "/ws",
            "/error"
    );

    @GetMapping({"/{path:[^\\.]*}", "/**/{path:[^\\.]*}"})
    public String forwardToSpa(HttpServletRequest request) {
        String uri = request.getRequestURI();

        for (String prefix : EXCLUDED_PREFIXES) {
            if (uri.startsWith(prefix)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND);
            }
        }

        return "forward:/index.html";
    }
}
