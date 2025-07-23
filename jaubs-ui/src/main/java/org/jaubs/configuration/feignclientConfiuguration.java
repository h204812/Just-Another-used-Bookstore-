package org.jaubs.configuration;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

@Configuration
public class feignclientConfiuguration {
    @Autowired
    private OAuth2AuthorizedClientService azdCliService;

    @Bean
    public RequestInterceptor oauth2FeignRequestInterceptor(){
        return requestTemplate -> {
            Authentication token = SecurityContextHolder.getContext().getAuthentication();

            System.out.println("Auth token class: " + (token != null ? token.getClass().getName() : "null"));
            System.out.println("Is OAuth2AuthenticationToken: " + (token instanceof OAuth2AuthenticationToken));

            if(token instanceof OAuth2AuthenticationToken tkn){
                String clieRegId = tkn.getAuthorizedClientRegistrationId();
                String principalName = tkn.getName();

                OAuth2AuthorizedClient authorizedClient =
                        azdCliService.loadAuthorizedClient(clieRegId,principalName);
                if(authorizedClient!=null && authorizedClient.getAccessToken()!=null){
                    String acessToken = authorizedClient.getAccessToken().getTokenValue();
                    requestTemplate.header("Authorization","Bearer "+acessToken);
                }
            }
        };
    }
}
