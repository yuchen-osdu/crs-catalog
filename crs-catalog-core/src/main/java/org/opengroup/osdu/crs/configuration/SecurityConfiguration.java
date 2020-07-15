package org.opengroup.osdu.crs.configuration;

import org.opengroup.osdu.crs.middleware.AuthenticationRequestFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

import static org.springframework.security.config.http.SessionCreationPolicy.STATELESS;

@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    private static final String[] ALLOWED_URLS = {
            "/",
            "/v2/api-docs",
            "/swagger-resources/**",
            "/swagger-ui.html",
            "/webjars/**",
            "/_ah/**",
            "/error",
            "/favicon.ico",
            "/csrf" // Required to prevent errors in logs while Swagger is trying to discover a valid csrf token. Should be deleted after the issue on the Swagger's side https://github.com/springfox/springfox/issues/2578 is resolved
    };

    private final HandlerExceptionResolver handlerExceptionResolver;
    private final String entUrl;

    public SecurityConfiguration(HandlerExceptionResolver handlerExceptionResolver,
                                 @Value("${osdu.entitlement.url}") String entUrl) {
        this.handlerExceptionResolver = handlerExceptionResolver;
        this.entUrl = entUrl;
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        AuthenticationRequestFilter authhenticationFilter = new AuthenticationRequestFilter(entUrl, handlerExceptionResolver);
        http.csrf().disable()
                .sessionManagement().sessionCreationPolicy(STATELESS)
                .and()
                .authorizeRequests()
                .anyRequest().authenticated()
                .and()
                .addFilterAfter(authhenticationFilter, UsernamePasswordAuthenticationFilter.class);
    }

    @Override
    public void init(WebSecurity web) throws Exception {
        web.ignoring().mvcMatchers(ALLOWED_URLS);
        super.init(web);
    }
}
