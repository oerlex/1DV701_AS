import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Ludde on 2016-02-18.
 */
public class Forbidden403Response extends Response {


    @Override
    public String sendResponse(DataOutputStream dataOutputStream, String contentType) {
        response += "\"HTTP/1.1 403 Forbidden \r\n\"";
        response += contentType+"/r/n";
        response += "Connection: close\r\n";
        response += "\r\n";

        try {
            dataOutputStream.writeBytes(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  response;
    }
}
