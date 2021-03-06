package Response;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by oerlex on 2016-02-26.
 */
public class Conflict409Response extends Response {
    @Override
    public String sendResponse(DataOutputStream dataOutputStream, String contentType) {
        response += "\"HTTP/1.1 409 Conflict \r\n\"";
        response += contentType+"\r\n";
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
