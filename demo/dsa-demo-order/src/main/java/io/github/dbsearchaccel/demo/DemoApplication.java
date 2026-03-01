package io.github.dbsearchaccel.demo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DSA示例项目启动类
 * 演示电商订单查询场景
 *
 * @author DBSearchAccel Team
 */
@SpringBootApplication
@MapperScan("io.github.dbsearchaccel.demo.mapper")
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }
}
