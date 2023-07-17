package sk.stuba.fei.uim.vsa.pr2.auth;

import sk.stuba.fei.uim.vsa.pr2.User.User;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Provider
@Secured
@Priority(Priorities.AUTHORIZATION)
public class AuthorizationFilter implements ContainerRequestFilter {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(AuthorizationFilter.class);

    @Context
    private ResourceInfo resourceInfo;

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        User user = (User) containerRequestContext.getSecurityContext().getUserPrincipal();

        Method resourceMethod = resourceInfo.getResourceMethod();
        Set<Role> roles = extractRolesFromMethod(resourceMethod);
        log.info("Resource class" + resourceInfo.getResourceClass());
        log.info("Resource method" + resourceInfo.getResourceMethod());



        if (!roles.contains(user.getRole()) ){
            containerRequestContext.abortWith(Response
                    .status(Response.Status.FORBIDDEN)
                    .build());
            return;
        }

    }

    private Set<Role> extractRolesFromMethod(Method method){
        if (method == null){
            return new HashSet<>();
        }
        Secured secured =method.getAnnotation(Secured.class);
        if (secured == null){
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(secured.value()));
    }
}
