package sk.stuba.fei.uim.vsa.pr2.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyError {
    private String type;
    private String trace;
}
