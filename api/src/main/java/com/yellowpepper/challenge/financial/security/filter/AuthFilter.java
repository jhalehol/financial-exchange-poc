package com.yellowpepper.challenge.financial.security.filter;

import com.yellowpepper.challenge.financial.exception.UnauthorizedException;
import com.yellowpepper.challenge.financial.model.User;
import com.yellowpepper.challenge.financial.security.UserDetailsImpl;
import com.yellowpepper.challenge.financial.service.auth.JwtService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class AuthFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String AUTHORIZATION_HEADER_PREFIX = "Bearer";
    private static final String EXCLUDE_URL = "oauth2/";
    private static final Pattern BEARER_PATTERN = Pattern.compile(String.format("^%s *([^ ]+) *$",
            AUTHORIZATION_HEADER_PREFIX), Pattern.CASE_INSENSITIVE);
    private static final int TOKEN_INDEX = 1;

    private final JwtService jwtService;

    public AuthFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        try {
            String path = request.getRequestURI();
            if (path.contains(EXCLUDE_URL)) {
                filterChain.doFilter(request, response);
                return;
            }

            authenticateWithJwt(request);
            filterChain.doFilter(request, response);
        } catch (UnauthorizedException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED,
                    String.format("Something failed in the authentication: %s", e.getMessage()));
        }
    }

    private void authenticateWithJwt(final HttpServletRequest request) {
        final String authorization = request.getHeader(AUTHORIZATION_HEADER);
        if (StringUtils.isEmpty(authorization)) {
            return;
        }

        final String jwt = getJwtTokenFromHeader(authorization);
        final User user = jwtService.validateJwtToken(jwt);
        final UserDetails userDetails = new UserDetailsImpl(user);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private static String getJwtTokenFromHeader(final String headerValue) {
        Matcher matcher = BEARER_PATTERN.matcher(headerValue);
        if (matcher.matches()) {
            return matcher.group(TOKEN_INDEX);
        }

        return "";
    }
}
