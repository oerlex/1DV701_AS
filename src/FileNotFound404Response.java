import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Created by oerlex on 2016-02-17.
 */
public class FileNotFound404Response extends Response {


    public FileNotFound404Response() {
        super();
    }

    @Override
    public String sendResponse(DataOutputStream dataOutputStream,String contentType) {
        response += "\"HTTP/1.1 404 Not Found \r\n";

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
