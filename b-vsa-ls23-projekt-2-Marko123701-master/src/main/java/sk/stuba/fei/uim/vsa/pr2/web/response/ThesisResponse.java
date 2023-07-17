package sk.stuba.fei.uim.vsa.pr2.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThesisResponse {
    private Long id;
    private String registrationNumber;
    private String title;
    private String description;
    private String institute;
    private String department;
    private TeacherResponse supervisor;
    private StudentResponse author;
    private String publishedOn;
    private String deadline;
    private String type;
    private String status;

}
