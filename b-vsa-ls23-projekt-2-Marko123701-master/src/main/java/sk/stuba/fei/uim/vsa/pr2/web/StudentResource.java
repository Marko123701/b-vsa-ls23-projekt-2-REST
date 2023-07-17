package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.stuba.fei.uim.vsa.pr2.BCryptService;
import sk.stuba.fei.uim.vsa.pr2.User.User;
import sk.stuba.fei.uim.vsa.pr2.User.UserSevice;
import sk.stuba.fei.uim.vsa.pr2.auth.Role;
import sk.stuba.fei.uim.vsa.pr2.auth.Secured;
import sk.stuba.fei.uim.vsa.pr2.domain.Student;
import sk.stuba.fei.uim.vsa.pr2.service.StudentService;
import sk.stuba.fei.uim.vsa.pr2.web.response.MessageDto;
import sk.stuba.fei.uim.vsa.pr2.web.response.StudentResponse;
import sk.stuba.fei.uim.vsa.pr2.web.response.factory.StudentConverter;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Path("/students")
public class StudentResource {

    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(StudentResource.class);
    private final StudentService studentService = new StudentService();
    private final UserSevice userService = new UserSevice();
    private final ObjectMapper json = new ObjectMapper();

    @Context
    SecurityContext securityContext;

    @GET
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllStudents() throws JsonProcessingException {
        log.info(securityContext.getUserPrincipal().toString());
        log.info("email: " + ((User) securityContext.getUserPrincipal()).getName());

        List<Student> students = studentService.getStudents();

        List<StudentResponse> studentResponses = new ArrayList<>();

        for (Student student : students) {
            StudentResponse studentResponse = StudentConverter.convertToStudentResponse(student);
            studentResponses.add(studentResponse);
        }

        try {
            String studentString = json.writeValueAsString(studentResponses);
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
    public Response createStudent(String body) {
        try {
            Student s = json.readValue(body, Student.class);
            String codePassword = s.getPassword();
            byte[] decodedBytes = Base64.getDecoder().decode(codePassword);
            String password = new String(decodedBytes);
            log.info("password: " + password);

            Student student = studentService.createStudent(s.getAisId(),s.getName(),s.getEmail(), BCryptService.hash(password), s.getYear(), s.getTerm(), s.getProgramme());

            StudentResponse studentResponse = StudentConverter.convertToStudentResponse(student);

            String studentString = json.writeValueAsString(studentResponse);

            userService.createUser(s.getEmail(),BCryptService.hash(password), Role.STUDENT);

            return Response.status(Response.Status.CREATED).entity(studentString).build();
        } catch (IllegalArgumentException e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(MessageDto.buildError(400,e.getMessage(),"type","trace"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while deleting the student","type","trace"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStudent(@PathParam("id") long id) {
        try {
            Student student = studentService.getStudent(id);
            if (student != null) {
                StudentResponse studentResponse = StudentConverter.convertToStudentResponse(student);

                String studentString = json.writeValueAsString(studentResponse);
                return Response.ok(studentString).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(MessageDto.buildError(404,"Student with given ID not found","type","trace"))
                        .build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while deleting the student","type","trace"))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteStudent(@PathParam("id") long id) {
        try {
                Student s = studentService.getStudent(id);
                String userEmail = ((User) securityContext.getUserPrincipal()).getName();
                Role role = ((User) securityContext.getUserPrincipal()).getRole();

                if(!s.getEmail().equals(userEmail) && role.equals(Role.STUDENT)){
                    return Response
                            .status(Response.Status.FORBIDDEN)
                            .build();
                }

                Student student = studentService.deleteStudent(id);
                if (student != null) {
                    StudentResponse studentResponse = StudentConverter.convertToStudentResponse(student);
                    userService.deleteUserByEmail(student.getEmail());

                    String studentString = json.writeValueAsString(studentResponse);
                    return Response.ok(studentString).build();
                } else {
                    Response.status(Response.Status.NOT_FOUND)
                            .entity(MessageDto.buildError(404,"An error occurred while deleting the student","type","trace"))
                            .build();
                }
            }
         catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while deleting the student","type","trace"))
                    .build();
        }
        return Response.status(Response.Status.BAD_REQUEST)
                .entity(MessageDto.buildError(400,"An error occurred while deleting the student","type","trace"))
                .build();
    }
}
