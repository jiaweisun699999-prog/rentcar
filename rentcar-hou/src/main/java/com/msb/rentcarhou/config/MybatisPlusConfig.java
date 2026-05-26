package com.msb.rentcarhou.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 框架核心配置类
 * 作用：在这个类中向 Spring 容器注册各种需要用到的数据库增强插件
 */
@Configuration // 标识这是一个配置类，Spring 启动时会解析并加载里面的内容
public class MybatisPlusConfig {

    /**
     * 注册分页插件拦截器
     * 原理：如果不配这个，MyBatis-Plus 的 page() 方法只会把所有数据查出来，不会真正在 SQL 层面加 LIMIT。
     * 加了这个插件后，它会自动拦截我们要执行的 SQL，并根据当前用的数据库（这里指定了 MYSQL），
     * 帮我们自动在 SQL 末尾拼上 "LIMIT offset, pageSize"，实现真正的物理分页。
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        // 添加针对 MySQL 数据库的分页支持组件。
        // 注意事项: 如果项目中还需要配置防全表更新插件等，切记要把这个分页插件放在最后面添加。
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
