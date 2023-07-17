package sk.stuba.fei.uim.vsa.pr2.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageDto {

    private int code;
    private String message;
    private MyError error;

    public static MessageDto buildError(int code,String message, String type, String trace){

        return new MessageDto(code,message, new MyError(type,trace));
    }
}
