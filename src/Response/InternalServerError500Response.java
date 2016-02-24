package Response;

import Response.Response;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Ludde on 2016-02-18.
 */
public class InternalServerError500Response extends Response {


    @Override
    public String sendResponse(DataOutputStream dataOutputStream, String contentType) {
        response += "\"HTTP/1.1 500 Internal Error \r\n\"";
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
