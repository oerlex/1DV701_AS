package Response;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by oerlex on 2016-02-17.
 * This abstract class is the parent class of all responses
 * All subclasses need the following methods and use them.
 */
public abstract class Response {

    protected String response = "";

    public Response(){}

    //This abstract method has to be specialiced for each case. The subversions are pretty similar but they have different response codes
    public abstract String sendResponse(DataOutputStream dataOutputStream, String contentType);


    //This method actually sends the requested file back
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
