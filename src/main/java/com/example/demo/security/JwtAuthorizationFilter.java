package com.example.demo.security;

import io.jsonwebtoken.*;

import io.jsonwebtoken.security.SignatureException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

// import static org.hibernate.annotations.common.util.StringHelper.isEmpty;
// import static org.hibernate.annotations.common.util.StringHelper.isNotEmpty;


public class JwtAuthorizationFilter extends BasicAuthenticationFilter {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthorizationFilter.class);

    public JwtAuthorizationFilter(AuthenticationManager authenticationManager) {
        super(authenticationManager);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain) throws IOException, ServletException {
        Authentication authentication = getAthentication(req);
        String header = req.getHeader(SecurityConstants.TOKEN_HEADER);

        if(isEmpty(header) || !header.startsWith(SecurityConstants.TOKEN_PREFIX)) {
            chain.doFilter(req, res);

            return;
        }

        SecurityContextHolder.getContext().setAuthentication(authentication);
        chain.doFilter(req, res);
    }

    private boolean isEmpty(final CharSequence cs) {
        return cs == null || cs.length() == 0;
    }

    private boolean isNotEmpty(final CharSequence cs) {
        return !isEmpty(cs);
    }

    private UsernamePasswordAuthenticationToken getAthentication(HttpServletRequest req) {
        String token = req.getHeader(SecurityConstants.TOKEN_HEADER);

        // 여기서 token은 문자열을 의미. 문자열이 Null인지 아닌지를 판단
        if (isEmpty(token)) {
            try {
                byte[] signingKey = SecurityConstants.JWT_SECRET.getBytes();

                Jws<Claims> parsedToken = Jwts.parser()
                        .setSigningKey(signingKey)
                        .parseClaimsJws(token.replace("JwTtEsT", ""));

                String username = parsedToken
                        .getBody()
                        .getSubject();

                // ((List<?>)의 ?는 와일드카드라고 함: 데이터를 가리지 않고 다 받겠다.
                List<SimpleGrantedAuthority> authorities = ((List<?>) parsedToken.getBody()
                        .get("rol")).stream()
                        .map(auth -> new SimpleGrantedAuthority((String) auth))
                        .collect(Collectors.toList());

                if(isNotEmpty(username)) {
                    return new UsernamePasswordAuthenticationToken(
                            username, null, authorities
                    );
                }
            } catch (ExpiredJwtException e) {
                log.warn("Request: parse expired JWT - {} failed: {}",
                        token, e.getMessage());
            } catch (UnsupportedJwtException e) {
                log.warn("Request: parse unsupported JWT - {} failed: {}",
                        token, e.getMessage());
            } catch (MalformedJwtException e) {
                log.warn("Request: parse invalid JWT - {} failed: {}",
                        token, e.getMessage());
            } catch (SignatureException e) {
                log.warn("Request: parse JWT with invalid signature - {} failed: {}",
                        token, e.getMessage());
            } catch (IllegalArgumentException e) {
                log.warn("Request: parse empty or NULL - {} failed: {}",
                        token, e.getMessage());
            }
        }

        return null;
    }
}
