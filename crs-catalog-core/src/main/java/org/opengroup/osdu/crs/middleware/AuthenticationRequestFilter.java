package org.opengroup.osdu.crs.middleware;

import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class AuthenticationRequestFilter extends OncePerRequestFilter {

    private final AuthenticationService authenticationService;

    public AuthenticationRequestFilter(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;

    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest httpServletRequest,
                                    @NonNull HttpServletResponse httpServletResponse,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
       if (authenticationService.isAuthorized(httpServletRequest, httpServletResponse)) {
            filterChain.doFilter(httpServletRequest, httpServletResponse);
       }
    }
}
