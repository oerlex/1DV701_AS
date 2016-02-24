package Response;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Ludvig on 2/24/2016.
 */
public class UnsupportedMediaType415Response extends Response {

    @Override
    public String sendResponse(DataOutputStream dataOutputStream, String contentType) {
        response += "\"HTTP/1.1 415 Unsupported Media Type \r\n\"";
        response += contentType+"\r\n";
        response += "\r\n";
        try {
            dataOutputStream.writeBytes(response);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return  response;
    }

}
