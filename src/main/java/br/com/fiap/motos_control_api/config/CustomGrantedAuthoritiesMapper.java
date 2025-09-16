package br.com.fiap.motos_control_api.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.OidcUserAuthority;
import org.springframework.security.oauth2.core.user.OAuth2UserAuthority;
import org.springframework.stereotype.Component;

@Component
public class CustomGrantedAuthoritiesMapper {

    @Value("${app.admin.emails}")
    private List<String> adminEmails;

    public Collection<? extends GrantedAuthority> mapAuthorities(Collection<? extends GrantedAuthority> authorities) {
        Set<GrantedAuthority> mappedAuthorities = new HashSet<>();

        authorities.forEach(authority -> {
            if (authority instanceof OidcUserAuthority oidcUserAuthority) {
                String email = oidcUserAuthority.getAttributes().get("email").toString();
                if (adminEmails.contains(email)) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
            } else if (authority instanceof OAuth2UserAuthority oauth2UserAuthority) {
                String email = oauth2UserAuthority.getAttributes().get("email").toString();
                if (adminEmails.contains(email)) {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                } else {
                    mappedAuthorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                }
            }
        });

        return mappedAuthorities.isEmpty() ? Collections.singleton(new SimpleGrantedAuthority("ROLE_USER"))
                : mappedAuthorities;
    }
}
