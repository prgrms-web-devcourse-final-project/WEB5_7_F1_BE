package io.f1.backend.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 클라이언트가 /image/thumbnail/~~ 로 요청
        // 실제 서버 경로 images/thumbnail/ 에서 리소스 찾아서 응답
        registry.addResourceHandler("/images/thumbnail/**")
                .addResourceLocations("file:images/thumbnail/");
    }
}
