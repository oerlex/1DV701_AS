package Response;

import Response.Response;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Ludvig on 2/23/2016.
 */
public class Created201Response extends Response {

    @Override
    public String sendResponse(DataOutputStream dataOutputStream, String contentType) {
        response += "\"HTTP/1.1 201 Created \r\n\"";
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
