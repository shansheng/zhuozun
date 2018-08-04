package com.qs.game;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * Created by zun.wei on 2018/7/30.
 * To change this template use File|Default Setting
 * |Editor|File and Code Templates|Includes|File Header
 */
@EnableSwagger2
@EnableFeignClients
@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient //暴露自己给eureka ,给其他服务调用
public class ProductProviderApp {

    public static void main(String[] args) {
        SpringApplication.run(ProductProviderApp.class, args);
    }
}
