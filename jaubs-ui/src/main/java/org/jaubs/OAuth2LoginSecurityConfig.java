package org.jaubs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.util.AntPathMatcher;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Configuration
@EnableWebSecurity
public class OAuth2LoginSecurityConfig {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(authorize ->
                        authorize
                                .requestMatchers("/login").permitAll()
                                .requestMatchers("/main.html").permitAll()
                                .requestMatchers("/js/**", "/css/**", "/images/**", "/webjars/**").permitAll()
                                .requestMatchers("/jaubs/ui").authenticated()
                                .requestMatchers("/jaubs/ui/admin/**").hasAnyAuthority("ROLE_jaubs-admin")
                                .requestMatchers("/jaubs/ui/**").hasAnyAuthority("ROLE_jaubs-user", "ROLE_jaubs-admin")
                                .anyRequest().permitAll()
                )

                .oauth2Login(oauth2 ->
                        oauth2
                                .loginPage("/login")
                                .userInfoEndpoint(epConfig->epConfig.oidcUserService(this.userService()))
                                .authorizationEndpoint(
                                cfg -> cfg.authorizationRequestResolver(
                                        pkceResolver(clientRegistrationRepository))))
                .oauth2Client(Customizer.withDefaults())
                .logout(logout -> logout
                                .logoutRequestMatcher(new AntPathRequestMatcher("/logout"))
                                .logoutSuccessHandler(oidcLogoutSuccessHandler())
                                                         );

            return http.build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> userService() {
        final OidcUserService delegate = new OidcUserService();
        return (userRequest)->{
            OidcUser oidcUser = delegate.loadUser(userRequest);

            List<String> roles = oidcUser.getIdToken().getClaim("roles");
            if(roles==null){
                roles = List.of();
            }
            List<SimpleGrantedAuthority> listAuthorities
                    = roles.stream().map(SimpleGrantedAuthority::new).toList();
            Set<GrantedAuthority> mappedAuthorities = new HashSet<>(listAuthorities);
            return new DefaultOidcUser(mappedAuthorities,oidcUser.getIdToken(),oidcUser.getUserInfo());
        };
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // Sets the location that the End-User's User Agent will be redirected to
        // after the logout has been performed at the Provider
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/main.html");
        return oidcLogoutSuccessHandler;
    }

    public OAuth2AuthorizationRequestResolver pkceResolver(ClientRegistrationRepository repo) {
        var resolver = new DefaultOAuth2AuthorizationRequestResolver(repo, "/oauth2/authorization");
        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());
        return resolver;
    }

}
