package com.petcare.interceptor;

import com.petcare.common.RequireRole;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) return true;
        HandlerMethod hm = (HandlerMethod) handler;

        RequireRole methodAnno = hm.getMethodAnnotation(RequireRole.class);
        RequireRole classAnno = hm.getBeanType().getAnnotation(RequireRole.class);
        RequireRole anno = methodAnno != null ? methodAnno : classAnno;
        if (anno == null) return true; // no restriction

        Integer role = (Integer) request.getAttribute("role");
        if (role == null) {
            response.setStatus(401);
            response.getWriter().write("{\"code\":401,\"message\":\"Login required\"}");
            return false;
        }

        int[] allowed = anno.value();
        if (allowed.length == 0) return true; // any authenticated user
        for (int r : allowed) {
            if (r == role) return true;
        }

        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":403,\"message\":\"Permission denied\"}");
        return false;
    }
}
