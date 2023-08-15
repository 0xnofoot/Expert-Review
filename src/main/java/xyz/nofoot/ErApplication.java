package xyz.nofoot;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@EnableAspectJAutoProxy(exposeProxy = true)
@MapperScan("xyz.nofoot.mapper")
@SpringBootApplication
public class ErApplication {

    public static void main(String[] args) {
        SpringApplication.run(ErApplication.class, args);
    }

}
