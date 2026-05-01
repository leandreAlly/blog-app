package com.ally.blogapp.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.security.authentication.event.AbstractAuthenticationFailureEvent;
import org.springframework.security.authentication.event.AuthenticationSuccessEvent;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

@Component
public class SecurityEventListener {

    private static final Logger log = LoggerFactory.getLogger(SecurityEventListener.class);

    @EventListener
    public void onSuccess(AuthenticationSuccessEvent event) {
        String username = event.getAuthentication().getName();
        String ip = extractIp(event);
        log.info("[AUTH] SUCCESS | user={} | ip={}", username, ip);
    }

    @EventListener
    public void onFailure(AbstractAuthenticationFailureEvent event) {
        String username = event.getAuthentication().getName();
        String reason = event.getException().getMessage();
        String ip = extractIp(event);
        log.warn("[AUTH] FAILURE | user={} | ip={} | reason={}", username, ip, reason);
    }

    private String extractIp(org.springframework.context.ApplicationEvent event) {
        if (event instanceof AuthenticationSuccessEvent e
                && e.getAuthentication().getDetails() instanceof WebAuthenticationDetails d) {
            return d.getRemoteAddress();
        }
        if (event instanceof AbstractAuthenticationFailureEvent e
                && e.getAuthentication().getDetails() instanceof WebAuthenticationDetails d) {
            return d.getRemoteAddress();
        }
        return "unknown";
    }
}
