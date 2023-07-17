package sk.stuba.fei.uim.vsa.pr2.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class TeacherResponse {
    private Long id;
    private Long aisId;
    private String name;
    private String email;
    private String institute;
    private String department;
    private List<ThesisResponse> theses;

}
