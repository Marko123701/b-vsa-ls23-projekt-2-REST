package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.stuba.fei.uim.vsa.pr2.BCryptService;
import sk.stuba.fei.uim.vsa.pr2.User.User;
import sk.stuba.fei.uim.vsa.pr2.User.UserSevice;
import sk.stuba.fei.uim.vsa.pr2.auth.Role;
import sk.stuba.fei.uim.vsa.pr2.auth.Secured;
import sk.stuba.fei.uim.vsa.pr2.domain.Educator;
import sk.stuba.fei.uim.vsa.pr2.service.TeacherService;
import sk.stuba.fei.uim.vsa.pr2.web.response.MessageDto;
import sk.stuba.fei.uim.vsa.pr2.web.response.TeacherResponse;
import sk.stuba.fei.uim.vsa.pr2.web.response.factory.StudentConverter;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/teachers")
public class TeacherResource {

    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(StudentResource.class);
    private final TeacherService teacherService = new TeacherService();
    private final UserSevice userService = new UserSevice();
    private final ObjectMapper json = new ObjectMapper();

    @Context
    SecurityContext securityContext;

    @GET
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTeachers() {
        List<Educator> teachers = teacherService.getTeachers();

        List<TeacherResponse> teacherResponses = new ArrayList<>();

        for (Educator teacher : teachers) {
            TeacherResponse teacherResponse = StudentConverter.convertToTeacherResponse(teacher);
            teacherResponses.add(teacherResponse);
        }
        try {
            String studentString = json.writeValueAsString(teacherResponses);
            return Response.ok(studentString).build();
        } catch (JsonProcessingException e) {
            try {
                String errorMessage = json.writeValueAsString(MessageDto.buildError(400,e.getMessage(),"type","trace"));
                return Response.ok(errorMessage).build();
            } catch (JsonProcessingException ex) {
                return Response.serverError().build();
            }
        }
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeacher(String body) {
        try {
            Educator e = json.readValue(body, Educator.class);

            String codePassword = e.getPassword();
            byte[] decodedBytes = Base64.getDecoder().decode(codePassword);
            String password = new String(decodedBytes);
            log.info("password: " + password);
            // create the teacher
            Educator educator = teacherService.createTeacher(e.getAisId(),e.getName(),e.getEmail(), BCryptService.hash(password), e.getInstitute(), e.getDepartment());

            TeacherResponse teacherResponse = StudentConverter.convertToTeacherResponse(educator);

            String teacherString = json.writeValueAsString(teacherResponse);

            userService.createUser(e.getEmail(),BCryptService.hash(password), Role.TEACHER);
            // return the created student
            return Response.status(Response.Status.CREATED).entity(teacherString).build();
        } catch (IllegalArgumentException e) {
            // return a 400 Bad Request response if the request is invalid
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(MessageDto.buildError(400,e.getMessage(),"type","trace"))
                    .build();
        } catch (Exception e) {
            // return a 500 Internal Server Error response for any other errors
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"Error ocurred","type","trace"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeacher(@PathParam("id") long id) {
        try {
            Educator teacher = teacherService.getTeacher(id);
            if (teacher != null) {
                TeacherResponse teacherResponse = StudentConverter.convertToTeacherResponse(teacher);

                String teacherString = json.writeValueAsString(teacherResponse);
                return Response.ok(teacherString).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(MessageDto.buildError(404,"Teacher with given ID not found","type","trace"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while retrieving the student","type", "trace"))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured({Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTeacher(@PathParam("id") long id) {
        try {
            Educator t = teacherService.getTeacher(id);
            String userEmail = ((User) securityContext.getUserPrincipal()).getName();

            if(!userEmail.equals(t.getEmail())){
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            Educator teacher = teacherService.deleteTeacher(id);
            if (teacher != null) {
                TeacherResponse teacherResponse = StudentConverter.convertToTeacherResponse(teacher);
                userService.deleteUserByEmail(userEmail);
                String teacherString = json.writeValueAsString(teacherResponse);
                return Response.ok(teacherString).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(MessageDto.buildError(404,"Teacher with given ID not found","type","trace"))
                        .build();
            }
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while deleting the student","type","trace"))
                    .build();
        }
    }
}
