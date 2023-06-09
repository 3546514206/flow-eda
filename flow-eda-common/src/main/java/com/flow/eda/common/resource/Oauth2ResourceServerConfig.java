package com.flow.eda.common.resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configurers.ResourceServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.security.oauth2.provider.token.ResourceServerTokenServices;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/** 声明资源服务器，通过Oauth2鉴权认证 */
@Configuration
@EnableResourceServer
public class Oauth2ResourceServerConfig extends ResourceServerConfigurerAdapter {

    @Value("${security.oauth2.resource-id}")
    private String resourceId;

    @Value("${security.oauth2.client-id}")
    private String clientId;

    @Value("${security.oauth2.client-secret}")
    private String clientSecret;

    @Value("${security.oauth2.check-token-url}")
    private String checkTokenUrl;

    @Override
    public void configure(ResourceServerSecurityConfigurer resources) {
        resources.resourceId(resourceId);
        resources.tokenServices(tokenServices());
    }

    @Bean
    public ResourceServerTokenServices tokenServices() {
        RemoteTokenServices tokenServices = new RemoteTokenServices();
        tokenServices.setRestTemplate(getRestTemplate());
        tokenServices.setCheckTokenEndpointUrl(checkTokenUrl);
        tokenServices.setClientId(clientId);
        tokenServices.setClientSecret(clientSecret);
        return tokenServices;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 指定的请求需要鉴权，其余放行
        http.cors()
                .and()
                .csrf()
                .disable()
                .authorizeRequests()
                .antMatchers("/api/http/**", "/w/**")
                .permitAll()
                .anyRequest()
                .authenticated();
    }

    /** 自定义restTemplate异常处理 */
    private RestTemplate getRestTemplate() {
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(
                new DefaultResponseErrorHandler() {
                    @Override
                    public void handleError(ClientHttpResponse response) throws IOException {
                        if (response.getRawStatusCode() == HttpServletResponse.SC_FORBIDDEN) {
                            // 认证服务器返回403，抛出限流异常
                            throw new RateLimitExceededException("Rate limit exceeded");
                        }
                        if (response.getRawStatusCode() != HttpServletResponse.SC_BAD_REQUEST) {
                            super.handleError(response);
                        }
                    }
                });
        return restTemplate;
    }
}
