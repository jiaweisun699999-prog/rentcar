package com.msb.rentcarhou.config;

import com.msb.rentcarhou.common.interceptor.JwtInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册 JWT 拦截器
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**") // 拦截所有 /api 开头的请求
                .excludePathPatterns(
                        "/api/user/login",     // 放行登录
                        "/api/user/register",  // 放行注册
                        "/api/car/recommend",  // 放行首页推荐查询
                        "/api/car/model/list",
                        "/api/store/list",     // 放行门店列表查询
                        "/api/upload",         // 放行文件上传(如果需要外部可访问)
                        "/error"               // 放行框架默认错误路径
                );
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 全局跨域配置
        registry.addMapping("/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将本地 uploads 目录映射为 /uploads/**，让上传后的文件可以通过 URL 直接访问
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + System.getProperty("user.dir") + "/uploads/");
        // 兼容项目内置静态车辆图片，和用户运行时上传的 uploads 目录分开管理
        registry.addResourceHandler("/car-images/**")
                .addResourceLocations("classpath:/images/");
    }
}
