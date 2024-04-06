package no.ntnu.SMPBachelor.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final UserSessionInterceptor userSessionInterceptor;

    public WebMvcConfig( UserSessionInterceptor userSessionInterceptor) {
        this.userSessionInterceptor = userSessionInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(userSessionInterceptor);
    }
}

