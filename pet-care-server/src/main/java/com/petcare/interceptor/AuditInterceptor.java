package com.petcare.interceptor;

import com.petcare.entity.AuditLog;
import com.petcare.mapper.AuditLogMapper;
import com.petcare.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class AuditInterceptor implements HandlerInterceptor {
    @Autowired private AuditLogMapper auditLogMapper;
    @Autowired private JwtUtils jwtUtils;

    private static final ThreadLocal<AuditLog> currentLog = new ThreadLocal<>();

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            if (jwtUtils.validateToken(token)) {
                AuditLog log = new AuditLog();
                log.setUserId(jwtUtils.getUserId(token));
                log.setAction(getActionFromMethod(request.getMethod()));
                log.setIp(request.getRemoteAddr());
                currentLog.set(log);
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                           Object handler, ModelAndView modelAndView) {
        AuditLog log = currentLog.get();
        if (log != null && response.getStatus() >= 200 && response.getStatus() < 300) {
            log.setTarget(extractTarget(request.getRequestURI()));
            log.setTargetId(extractTargetId(request.getRequestURI()));
            log.setDetail(request.getMethod() + " " + request.getRequestURI());
            try { auditLogMapper.insert(log); } catch (Exception e) {}
        }
        currentLog.remove();
    }

    private String getActionFromMethod(String method) {
        switch (method) {
            case "POST": return "CREATE";
            case "PUT": case "PATCH": return "UPDATE";
            case "DELETE": return "DELETE";
            default: return "READ";
        }
    }

    private String extractTarget(String uri) {
        String[] parts = uri.split("/");
        if (parts.length >= 3) return parts[2]; // /api/pet -> pet, /api/order -> order
        return "unknown";
    }

    private Long extractTargetId(String uri) {
        String[] parts = uri.split("/");
        try {
            for (int i = parts.length - 1; i >= 0; i--) {
                Long id = Long.parseLong(parts[i]);
                return id;
            }
        } catch (NumberFormatException e) {}
        return null;
    }
}
