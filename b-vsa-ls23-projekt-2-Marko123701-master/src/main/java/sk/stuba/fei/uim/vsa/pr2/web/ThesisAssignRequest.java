package sk.stuba.fei.uim.vsa.pr2.web;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ThesisAssignRequest {
    private Long studentId;

    public Long getStudentId() {
        return studentId;
    }
}
