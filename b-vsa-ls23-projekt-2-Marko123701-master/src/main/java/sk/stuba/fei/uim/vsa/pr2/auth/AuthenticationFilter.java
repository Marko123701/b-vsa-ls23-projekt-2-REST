package sk.stuba.fei.uim.vsa.pr2.auth;//package sk.stuba.fei.uim.vsa.pr2.auth;
//
//import javax.ws.rs.container.ContainerRequestContext;
//import javax.ws.rs.container.ContainerRequestFilter;
//import javax.ws.rs.core.HttpHeaders;
//import javax.ws.rs.ext.Provider;
//import java.io.IOException;
//import java.util.Base64;
//
//@Provider
//public class AuthenticationFilter implements ContainerRequestFilter {
//    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(AuthenticationFilter.class);
//
//    @Override
//    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
//        String authHeader = containerRequestContext.getHeaderString(HttpHeaders.AUTHORIZATION);
//        String[] credentials = extractFromHeader(authHeader);
//        log.info("Recieved credential" + credentials[0] + ", " + credentials[1]);
//    }
//
//    private String[] extractFromHeader(String authHeader){
//        return new String(Base64.getDecoder()
//                .decode(authHeader
//                        .replace("Basic", "")
//                        .trim()))
//                .split(":");
//    }
//
//
//}
import sk.stuba.fei.uim.vsa.pr2.BCryptService;
import sk.stuba.fei.uim.vsa.pr2.MockUserDb;
import sk.stuba.fei.uim.vsa.pr2.User.User;
import sk.stuba.fei.uim.vsa.pr2.User.UserSevice;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.ext.Provider;

@Provider
@Secured
@Priority(Priorities.AUTHENTICATION)
public class AuthenticationFilter implements ContainerRequestFilter {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(AuthenticationFilter.class);

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {

        String authHeader = requestContext.getHeaderString(HttpHeaders.AUTHORIZATION);

        if (authHeader != null && authHeader.startsWith("Authorization: Basic")) {
            String encodedCredentials = authHeader.substring("Authorization: Basic ".length()).trim();
            String credentials = new String(Base64.getDecoder().decode(encodedCredentials), StandardCharsets.UTF_8);

            String[] emailAndPassword = credentials.split(":", 2);
            String email = emailAndPassword[0];
            String password = emailAndPassword[1];
            log.info(email);
            log.info(password);

            UserSevice userSevice = new UserSevice();
            Optional<User> userOptional = userSevice.getUserByEmail(email);
            log.info(String.valueOf(userOptional));

            if (!userOptional.isPresent() || !BCryptService.verify(password,userOptional.get().getPassword())){
                requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED).build());
                return;
            }

            final SecurityContext securityContext = requestContext.getSecurityContext();
            MySecurityContext context = new MySecurityContext(userOptional.get());
            context.setSecure(securityContext.isSecure());
            requestContext.setSecurityContext(context);

        } else {
            requestContext.abortWith(Response.status(Response.Status.UNAUTHORIZED)
                            .header(HttpHeaders.WWW_AUTHENTICATE,"Basic realm=\"VSA\"")
                    .build());
        }
    }
}