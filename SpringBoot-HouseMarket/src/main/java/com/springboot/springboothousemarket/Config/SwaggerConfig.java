package com.springboot.springboothousemarket.Config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("房源市场-房屋租售在线平台")   // 标题
                        .description("进行房屋租售的在线平台") // 描述
                        .version("1.0")                  // 版本
                        .contact(new Contact()
                                .name("成都工业职业技术学院EnderManTeam")
                                .url("http://localhost:8082/")
                                .email("absolutezero.cold200@simplelogin.com")));
    }
}
