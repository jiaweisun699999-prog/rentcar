package com.msb.rentcarhou;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.msb.rentcarhou.mapper")
public class RentcarHouApplication {

    public static void main(String[] args) {
        SpringApplication.run(RentcarHouApplication.class, args);
    }

}
