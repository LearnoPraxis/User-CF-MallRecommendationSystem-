package com.mall;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 商城推荐系统启动类
 */
@SpringBootApplication
@MapperScan("com.mall.mapper")
public class MallRecommendationApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallRecommendationApplication.class, args);
    }
}
