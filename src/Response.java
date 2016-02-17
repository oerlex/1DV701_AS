import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by oerlex on 2016-02-17.
 */
public abstract class Response {
    public static enum ReturnOptions{FileNotFound, OK;}


    protected final String HTMLContent = "Content-Type: text/html"+ "\r\n";
    protected final String IMGContent = "Content-Type: image/png"+ "\r\n";
    protected String response = "";


    public Response(){ }

    public abstract String sendResponse(DataOutputStream dataOutputStream, String contentType);

    protected void sendFile (File file, DataOutputStream out) throws Exception {
        FileInputStream fileIN = new FileInputStream(file);
        byte[] buffer = new byte[1024] ;
        int bytesRead;

        while ((bytesRead = fileIN.read(buffer)) != -1 ) {
            out.write(buffer, 0, bytesRead);
        }
        fileIN.close();
    }

}
