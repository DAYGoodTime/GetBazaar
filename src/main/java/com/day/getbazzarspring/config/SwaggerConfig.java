package com.day.getbazzarspring.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.oas.annotations.EnableOpenApi;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

@EnableOpenApi //在这里开启Swagger
//@EnableSwagger2 swagger2 旧版本的启动方式
@EnableKnife4j
@Configuration
public class SwaggerConfig {

    public static final String SwaggerTitle = "Getbazzar";
    public static final String SwaggerDescription = "Hpyixel-skyblock-bazzar数据接口";
    public static final String Version = "0.1";
//    public static final String SwaggerTitle = "";

    @Bean
    public Docket Docker2() {
        return new Docket(DocumentationType.OAS_30)
                .groupName("otherGroup");
    }

    @Bean
    public Docket createRestApi(Environment environment) {
        //获取配置文件是否处于所指定的环境(支持多个文件)
        boolean dev = environment.acceptsProfiles(Profiles.of("dev"));
        //指定使用Swagger2规范
        return new Docket(DocumentationType.OAS_30)
                .apiInfo(apiInfo())
                //分组名称
                .groupName("bazzar")
                .select()
                //这里指定Controller扫描包路径
                .apis(RequestHandlerSelectors.basePackage("com.day.getbazzarspring.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     * 添加摘要信息
     *
     * @return 返回ApiInfo对象
     */
    private ApiInfo apiInfo() {
        // 用ApiInfoBuilder进行定制
        return new ApiInfoBuilder()
                // 设置标题
                .title(SwaggerTitle)
                // 服务条款
                .termsOfServiceUrl("一个一个一个条款")
                // 描述
                .description(SwaggerDescription)
                // 作者信息
                .contact(new Contact("DAYGood_Time", "https://github.com/DAYGoodTime", "osutimemail@gmail.com"))
                // 版本
                .version(Version)
                //协议
                .license("The Apache License")
                // 协议url
                .licenseUrl("http://www.apache.org/licenses/LICENSE-2.0.html")
                .build();
    }

}