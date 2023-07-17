package sk.stuba.fei.uim.vsa.pr2.web;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import sk.stuba.fei.uim.vsa.pr2.User.User;
import sk.stuba.fei.uim.vsa.pr2.auth.Role;
import sk.stuba.fei.uim.vsa.pr2.auth.Secured;
import sk.stuba.fei.uim.vsa.pr2.domain.Educator;
import sk.stuba.fei.uim.vsa.pr2.domain.FinalThesis;
import sk.stuba.fei.uim.vsa.pr2.domain.Student;
import sk.stuba.fei.uim.vsa.pr2.service.StudentService;
import sk.stuba.fei.uim.vsa.pr2.service.ThesesService;
import sk.stuba.fei.uim.vsa.pr2.web.response.MessageDto;
import sk.stuba.fei.uim.vsa.pr2.web.response.TeacherResponse;
import sk.stuba.fei.uim.vsa.pr2.web.response.ThesisResponse;
import sk.stuba.fei.uim.vsa.pr2.web.response.factory.StudentConverter;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;

@Path("theses")
public class ThesesResource {

    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(TeacherResponse.class);
    private final ThesesService thesesService = new ThesesService();
    private final ObjectMapper json = new ObjectMapper();
    private final StudentService studentService = new StudentService();

    @Context
    SecurityContext securityContext;

    @GET
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllTheses() {
        List<FinalThesis> theses = thesesService.getTheses();

        List<ThesisResponse> thesesResponses = new ArrayList<>();

        for (FinalThesis thes : theses) {
            ThesisResponse thesesResponse = StudentConverter.convertToThesisResponse(thes);
            thesesResponses.add(thesesResponse);
        }
        try {
            String thesesString = json.writeValueAsString(thesesResponses);
            return Response.ok(thesesString).build();
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
    @Secured({Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response createThesis(String body) {
        try {
            FinalThesis t = json.readValue(body, FinalThesis.class);
            String userEmail = ((User) securityContext.getUserPrincipal()).getName();
            log.info("Registration number" + t.getRegistrationNumber());

            FinalThesis theses = thesesService.makeThesisAssignment(userEmail,t.getTitle(),t.getType().toString(),t.getDescription(), t.getRegistrationNumber());

            ThesisResponse thesesResponse = StudentConverter.convertToThesisResponse(theses);

            String thesesString = json.writeValueAsString(thesesResponse);

            return Response.status(Response.Status.CREATED).entity(thesesString).build();
        } catch (IllegalArgumentException e) {

            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(MessageDto.buildError(400,e.getMessage(),"type","trace"))
                    .build();
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,e.getMessage(),"type","trace"))
                    .build();
        }
    }

    @GET
    @Path("/{id}")
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response getThesis(@PathParam("id") long id) {
        try {
            FinalThesis thesis = thesesService.getThesis(id);
            if (thesis != null) {
                ThesisResponse thesesResponse = StudentConverter.convertToThesisResponse(thesis);

                String thesesString = json.writeValueAsString(thesesResponse);
                return Response.ok(thesesString).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while retrieving the student","type","trace"))
                    .build();
        }
    }

    @DELETE
    @Path("/{id}")
    @Secured({Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteThesis(@PathParam("id") long id) {
        try {
            FinalThesis t = thesesService.getThesis(id);
            if (t == null){
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(MessageDto.buildError(404,"Thesis not found","type","trace"))
                        .build();
            }
            String userEmail = ((User) securityContext.getUserPrincipal()).getName();
            Educator teacher = t.getEducator();

            if(!teacher.getEmail().equals(userEmail)){
                return Response
                        .status(Response.Status.FORBIDDEN)
                        .build();
            }

            FinalThesis thesis = thesesService.deleteThesis(id);
            if (thesis != null) {
                ThesisResponse thesesResponse = StudentConverter.convertToThesisResponse(thesis);

                String thesesString = json.writeValueAsString(thesesResponse);
                return Response.ok(thesesString).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND)
                        .entity(MessageDto.buildError(404,"Thesis not found","type","trace"))
                        .build();
            }
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while deleting the student","type","trace"))
                    .build();
        }
    }

    @POST
    @Path("/{id}/assign")
    @Secured({Role.STUDENT,Role.TEACHER})
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response assignThesis(@PathParam("id") long id, String body) throws JsonProcessingException {
        ThesisAssignRequest request = json.readValue(body, ThesisAssignRequest.class);
        try {
            String userEmail = ((User) securityContext.getUserPrincipal()).getName();
            Role role = ((User) securityContext.getUserPrincipal()).getRole();
            Long studentId = request.getStudentId();

            FinalThesis thesis;
            ThesisResponse thesesResponse;
                if (role.equals(Role.TEACHER)) {
                    if (studentId == null) {
                        return Response.status(Response.Status.BAD_REQUEST)
                                .entity(MessageDto.buildError(400, "Missing studentId in the request body", "type", "trace"))
                                .build();
                    }
                    thesis = thesesService.assignThesis(id, studentId);
                    thesesResponse = StudentConverter.convertToThesisResponse(thesis);
                } else {
                    Student student = studentService.getStudentByEmail(userEmail);
                    thesis = thesesService.assignThesis(id, student.getId());
                    thesesResponse = StudentConverter.convertToThesisResponse(thesis);

                }

                String thesesString = json.writeValueAsString(thesesResponse);
                return Response.ok(thesesString).build();


        }
        catch(Exception e){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                        .entity(MessageDto.buildError(500, "An error occurred while assigning the thesis", "type", "trace"))
                        .build();
            }
        }


    @POST
    @Path("/{id}/submit")
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response submitThesis(@PathParam("id") long id, SubmitThesisRequest request) {
        try {
            FinalThesis thesis = thesesService.submitThesis(id);
            if (thesis != null) {
                ThesisResponse thesesResponse = StudentConverter.convertToThesisResponse(thesis);

                String thesesString = json.writeValueAsString(thesesResponse);
                return Response.ok(thesesString).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while submitting the thesis","type","trace"))
                    .build();
        }
    }

    @POST
    @Secured({Role.STUDENT,Role.TEACHER})
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchTheses(SearchThesesRequest request) {
        try {
            FinalThesis studentThesis = null;
            List<FinalThesis> teacherThesis = null;

            if (request.getStudentId() != 0) {
                studentThesis = thesesService.getThesisByStudent(request.getStudentId());
                if (studentThesis != null) {
                    ThesisResponse studentThesesResponse = StudentConverter.convertToThesisResponse(studentThesis);

                    String studentThesesString = json.writeValueAsString(studentThesesResponse);
                    return Response.ok(studentThesesString).build();
                } else {
                    return Response.ok().entity(emptyList()).build();
                }
            } else if (request.getTeacherId() != 0) {
                teacherThesis = thesesService.getThesesByTeacher(request.getTeacherId());
                if (teacherThesis != null) {
                    List<ThesisResponse> thesesList = new ArrayList<>();

                    for (FinalThesis thesis : teacherThesis){
                        ThesisResponse teacherThesesResponse = StudentConverter.convertToThesisResponse(thesis);
                        thesesList.add(teacherThesesResponse);
                    }

                    String teacherThesesString = json.writeValueAsString(thesesList);
                    return Response.ok(teacherThesesString).build();
                } else {
                    return Response.ok().entity(emptyList()).build();
                }
            }
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(MessageDto.buildError(404,"An error occurred while submitting the thesis","type","trace"))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(MessageDto.buildError(500,"An error occurred while submitting the thesis","type","trace"))
                    .build();
        }
    }
}

class SearchThesesRequest {
    private long studentId;
    private long teacherId;

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    public long getTeacherId() {
        return teacherId;
    }

    public void setTeacherId(long teacherId) {
        this.teacherId = teacherId;
    }
}

class SubmitThesisRequest {
    private long studentId;

    public long getStudentId() {
        return studentId;
    }

    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }


}

