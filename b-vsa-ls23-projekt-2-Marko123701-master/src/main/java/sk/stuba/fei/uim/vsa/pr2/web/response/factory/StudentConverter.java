package sk.stuba.fei.uim.vsa.pr2.web.response.factory;

import sk.stuba.fei.uim.vsa.pr2.domain.Educator;
import sk.stuba.fei.uim.vsa.pr2.domain.FinalThesis;
import sk.stuba.fei.uim.vsa.pr2.domain.Student;
import sk.stuba.fei.uim.vsa.pr2.service.ThesesService;
import sk.stuba.fei.uim.vsa.pr2.web.response.StudentResponse;
import sk.stuba.fei.uim.vsa.pr2.web.response.TeacherResponse;
import sk.stuba.fei.uim.vsa.pr2.web.response.ThesisResponse;

import java.util.ArrayList;
import java.util.List;

public class StudentConverter {
    private static final org.slf4j.Logger log=org.slf4j.LoggerFactory.getLogger(StudentConverter.class);
    private static final ThesesService thesesService = new ThesesService();

    public static StudentResponse convertToStudentResponse(Student student) {
        ThesisResponse thesisResponse = null;
        if (student.getFinalThesis() != null) {
            thesisResponse = convertToThesisResponse(student.getFinalThesis());
        }

        return new StudentResponse(
                student.getId(),
                student.getAisId(),
                student.getName(),
                student.getEmail(),
                student.getYear(),
                student.getTerm(),
                student.getProgramme(),
                thesisResponse
        );
    }

    public static ThesisResponse convertToThesisResponse(FinalThesis finalThesis) {
        TeacherResponse supervisorResponse = convertToTeacherResponse(finalThesis.getEducator());
        StudentResponse studentResponse = null;

        if(finalThesis.getStudent() != null){
            studentResponse = convertToStudentResponse(finalThesis.getStudent());
        }


        return new ThesisResponse(
                finalThesis.getId(),
                finalThesis.getRegistrationNumber(),
                finalThesis.getTitle(),
                finalThesis.getDescription(),
                finalThesis.getInstitute(),
                finalThesis.getDepartment(),
                supervisorResponse,
                studentResponse,
                finalThesis.getPublicationDate().toString(),
                finalThesis.getSubmissionDeadline().toString(),
                finalThesis.getType().name(),
                finalThesis.getStatus().name()
        );
    }

    public static TeacherResponse convertToTeacherResponse(Educator teacher) {
        List<ThesisResponse> thesesResponses = new ArrayList<>();

        List<FinalThesis> finalTheses = thesesService.getThesesByTeacher(teacher.getId());
        TeacherResponse teacherResponse;
        StudentResponse studentResponse;
        if(finalTheses != null){
            for (FinalThesis thesis : finalTheses) {
                teacherResponse = null;
                studentResponse = null;

                if (thesesService.getThesesByTeacher(teacher.getId()) != null){
                    teacherResponse = new TeacherResponse(
                            thesis.getEducator().getId(),
                            thesis.getEducator().getAisId(),
                            thesis.getEducator().getName(),
                            thesis.getEducator().getEmail(),
                            thesis.getEducator().getInstitute(),
                            thesis.getEducator().getDepartment(),
                            null
                    );
                }

                if (thesis.getStudent() != null){
                    studentResponse = new StudentResponse(
                            thesis.getStudent().getId(),
                            thesis.getStudent().getAisId(),
                            thesis.getStudent().getName(),
                            thesis.getStudent().getEmail(),
                            thesis.getStudent().getYear(),
                            thesis.getStudent().getTerm(),
                            thesis.getStudent().getProgramme(),
                            null
                    );
                }

                ThesisResponse thesisResponse = new ThesisResponse(
                        thesis.getId(),
                        thesis.getRegistrationNumber(),
                        thesis.getTitle(),
                        thesis.getDescription(),
                        thesis.getInstitute(),
                        thesis.getDepartment(),
                        teacherResponse,
                        studentResponse,
                        thesis.getPublicationDate().toString(),
                        thesis.getSubmissionDeadline().toString(),
                        thesis.getType().name(),
                        thesis.getStatus().name()
                );
                thesesResponses.add(thesisResponse);
            }
        }

        return new TeacherResponse(
                teacher.getId(),
                teacher.getAisId(),
                teacher.getName(),
                teacher.getEmail(),
                teacher.getInstitute(),
                teacher.getDepartment(),
                thesesResponses
        );
    }
}
