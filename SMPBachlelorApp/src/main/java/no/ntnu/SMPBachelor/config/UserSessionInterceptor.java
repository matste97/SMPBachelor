package no.ntnu.SMPBachelor.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import no.ntnu.SMPBachelor.service.AccessUserService;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

@Component
public class UserSessionInterceptor implements HandlerInterceptor {

    private final AccessUserService userService;

    public UserSessionInterceptor(AccessUserService userService) {
        this.userService = userService;
    }

    /**
     * If  modelAndView is not null, add the session user to the model.
     * Makes session user available everywhere.
     */

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView){
        if (modelAndView != null) {
            modelAndView.addObject("user", userService.getSessionUser());
        }
    }
}
