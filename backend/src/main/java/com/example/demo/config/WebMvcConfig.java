package com.example.demo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.method.HandlerTypePredicate;
import org.springframework.web.servlet.config.annotation.ApiVersionConfigurer;
import org.springframework.web.servlet.config.annotation.PathMatchConfigurer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/** Web MVC 設定。API パスプレフィックスとバージョニングを構成する。 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  @Override
  public void configurePathMatch(final PathMatchConfigurer configurer) {
    configurer.addPathPrefix(
        "/api/{version}", HandlerTypePredicate.forAnnotation(RestController.class));
  }

  @Override
  public void configureApiVersioning(final ApiVersionConfigurer configurer) {
    configurer.usePathSegment(1);
  }
}
