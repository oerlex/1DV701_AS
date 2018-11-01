package Response;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by Ludde on 2016-02-25.
 */
public class PaymentRequired402 extends Response {
    @Override
    public String sendResponse(DataOutputStream dataOutputStream, String contentType) {
        response += "HTTP/1.1 402 Payment Required \r\n";
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
