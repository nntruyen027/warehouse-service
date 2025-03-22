package qbit.microservice.warehouse_service.config;

import lombok.Getter;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class JwtAuthentication extends UsernamePasswordAuthenticationToken {
    private final String jwt;

    public JwtAuthentication(Object principal, Object credentials, Collection<?> authorities, String jwt) {
        super(principal, credentials, (Collection<? extends GrantedAuthority>) authorities);
        this.jwt = jwt;
    }

}