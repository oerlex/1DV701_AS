package Response;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by oerlex on 2016-02-17.
 */
public abstract class Response {

    protected String response = "";

    public Response(){}

    public abstract String sendResponse(DataOutputStream dataOutputStream, String contentType);

    // Abstract class for all responses.

    protected void sendFile (File file, DataOutputStream out) {
        try {
            FileInputStream fileIN = new FileInputStream(file);
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = fileIN.read(buffer)) != -1) {
                out.write(buffer, 0, bytesRead);
            }
            fileIN.close();
        } catch(IOException e) {
            System.out.println(e.getMessage());
        }
    }

}
