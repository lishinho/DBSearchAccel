package io.github.dbsearchaccel.admin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * DSA运维控制台启动类
 * 提供场景管理、同步管理、降级管理、一致性校验、监控大盘等功能
 *
 * @author DBSearchAccel Team
 */
@SpringBootApplication
public class DsaAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(DsaAdminApplication.class, args);
    }
}
