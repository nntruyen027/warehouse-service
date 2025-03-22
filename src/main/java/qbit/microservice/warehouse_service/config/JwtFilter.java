package qbit.microservice.warehouse_service.config;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import qbit.microservice.warehouse_service.client.AuthServiceClient;
import qbit.microservice.warehouse_service.dto.AccountDto;
import qbit.microservice.warehouse_service.util.JwtUtil;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;
    @Autowired
    private AuthServiceClient authServiceClient;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException, java.io.IOException {

        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(jwt);
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            ResponseEntity<AccountDto> responseEntity = authServiceClient.getUserByJwt("Bearer " + jwt);
            AccountDto accountDto = responseEntity.getBody();

            assert accountDto != null;
            if (jwtUtil.validateToken(jwt, accountDto.getUsername())) {
                JwtAuthentication authenticationToken =
                        new JwtAuthentication(accountDto, null,
                                accountDto.getRoles().stream().map((role) -> new SimpleGrantedAuthority("ROLE_" + role.getRoleName())).toList(),
                                jwt);
                authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authenticationToken);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Sử dụng mã trạng thái hợp lệ
            }
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // Sử dụng mã trạng thái hợp lệ
        }


        chain.doFilter(request, response);
    }

}