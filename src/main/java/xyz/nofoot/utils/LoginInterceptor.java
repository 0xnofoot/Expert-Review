package xyz.nofoot.utils;

import org.springframework.web.servlet.HandlerInterceptor;
import xyz.nofoot.dto.UserDTO;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        UserDTO user = UserHolder.getUser();

        if (user == null) {
            response.setStatus(401);
            return false;
        }

        return true;
    }

}
