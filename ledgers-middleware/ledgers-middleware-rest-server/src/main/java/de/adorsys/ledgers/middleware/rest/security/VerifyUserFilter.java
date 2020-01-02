package de.adorsys.ledgers.middleware.rest.security;

import de.adorsys.ledgers.um.api.domain.UserTypeBO;
import de.adorsys.ledgers.um.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static de.adorsys.ledgers.um.api.domain.UserTypeBO.FAKE;
import static org.springframework.http.HttpStatus.FORBIDDEN;
import static org.springframework.http.MediaType.APPLICATION_JSON;

@RequiredArgsConstructor
public class VerifyUserFilter extends OncePerRequestFilter {
    private static final List<String> EXCLUDE_URL = Collections.singletonList("/emails/email-verification");
    private final UserService userService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        MiddlewareAuthentication authentication = (MiddlewareAuthentication) SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserTypeBO userType =  userService.findById(authentication.getName()).getUserType();
            if (userType == FAKE) {
                response.setContentType(APPLICATION_JSON.toString());
                response.setStatus(FORBIDDEN.value());
                response.getOutputStream().println("{\"message\": \"Please verify your account!\"}");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String servletPath = request.getServletPath();
        boolean b = EXCLUDE_URL.stream()
                            .anyMatch(servletPath::startsWith);
        return b;
    }
}