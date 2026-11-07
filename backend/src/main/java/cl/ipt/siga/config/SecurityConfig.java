package cl.ipt.siga.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Protects the REST API with the JWT access tokens issued by Keycloak.
 * - No token or invalid token  -> 401 Unauthorized
 * - Valid token, wrong role     -> 403 Forbidden
 * Rules follow section 4.3 of the assignment (ADMIN, TEACHER, STUDENT).
 * Ownership checks (a student only sees their own data) live in CurrentUser.
 */
@Configuration
public class SecurityConfig {

    private static final String ADMIN = "ADMIN";
    private static final String TEACHER = "TEACHER";
    private static final String STUDENT = "STUDENT";

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Who am I (any logged-in user)
                        .requestMatchers("/api/me").authenticated()

                        // E6: enrollment (students) and E7: grades (teachers)
                        .requestMatchers(HttpMethod.POST, "/api/enrollments").hasRole(STUDENT)
                        .requestMatchers(HttpMethod.DELETE, "/api/enrollments/*").hasAnyRole(STUDENT, ADMIN)
                        .requestMatchers(HttpMethod.PUT, "/api/enrollments/*/grade").hasRole(TEACHER)

                        // Student's own data (ownership checked in the controllers)
                        .requestMatchers(HttpMethod.GET, "/api/students/*", "/api/students/*/enrollments",
                                "/api/students/*/available-sections", "/api/students/*/academic-record")
                        .hasAnyRole(ADMIN, STUDENT)

                        // Teacher's own sections and their students
                        .requestMatchers(HttpMethod.GET, "/api/teachers/*", "/api/teachers/*/sections",
                                "/api/sections/*/enrollments")
                        .hasAnyRole(ADMIN, TEACHER)

                        // Catalog and academic offer: readable by every logged-in user
                        .requestMatchers(HttpMethod.GET, "/api/careers", "/api/careers/*", "/api/careers/*/study-plans",
                                "/api/study-plans/*", "/api/study-plans/*/subjects", "/api/subjects/*",
                                "/api/periods", "/api/periods/*", "/api/periods/*/sections", "/api/sections/*")
                        .authenticated()

                        // Everything else (management, matriculations, closing, lists) is for the administrator
                        .requestMatchers("/api/**").hasRole(ADMIN)
                        .anyRequest().permitAll())
                .oauth2ResourceServer(oauth -> oauth.jwt(jwt -> jwt.jwtAuthenticationConverter(keycloakRolesConverter())));
        return http.build();
    }

    /**
     * Keycloak puts realm roles in the "realm_access.roles" claim, which Spring does not
     * read by default. This maps e.g. "ADMIN" to the authority "ROLE_ADMIN".
     */
    @Bean
    public JwtAuthenticationConverter keycloakRolesConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
            Object roles = realmAccess == null ? null : realmAccess.get("roles");
            if (!(roles instanceof Collection<?> roleList)) {
                return List.of();
            }
            return roleList.stream()
                    .map(role -> (GrantedAuthority) new SimpleGrantedAuthority("ROLE_" + role))
                    .toList();
        });
        return converter;
    }
}
