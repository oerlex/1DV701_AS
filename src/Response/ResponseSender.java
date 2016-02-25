package Response;

import Response.*;

import java.io.DataOutputStream;
import java.io.File;

/**
 * Created by Ludde on 2016-02-18.
 */
public class ResponseSender {

    private DataOutputStream dataOutputStream;
    //Content types we can send back
    private final String HTMLContent = "Content-Type: text/html";
    private final String PNGContent = "Content-Type: image/png";
    private final String JPGContent = "Content-Type: image/jpg";


    public ResponseSender(DataOutputStream dataOutputStream) {
        this.dataOutputStream = dataOutputStream;
    }

    private String setContentType(File file){
        String contentType = "";
        if(file.isFile()) {
            if(getPrefix(file.getPath()).equals("png")) {
                contentType = PNGContent;
            }else if(getPrefix(file.getPath()).equals("jpg")){
                contentType = JPGContent;
            }
            else {
                contentType = HTMLContent;
            }
        }
        return contentType;
    }


    private String getPrefix(String requestedPath) {
        String[] split = requestedPath.split("\\.");
        String prefix = split[split.length-1];
        return prefix;
    }

    //Methods for sending back the different responses

    public void send200(File file){
        //This will send the file requested if it is of a contentype that we support. New contenttypes for example ".jpeg" can easily be added to this class.
        String contentType = setContentType(file);
        if(!contentType.equals("")) {
            OK200Response ok200Response = new OK200Response();
            ok200Response.sendResponse(dataOutputStream, contentType);
            ok200Response.sendFile(file, dataOutputStream);
        } else {
            send404();
        }
    }

    public void send201() {
        Created201Response created201 = new Created201Response();
        created201.sendResponse(dataOutputStream, HTMLContent);
        File file = new File("src/responsecodes/Created201.html");
        created201.sendFile(file,dataOutputStream);
    }

    public void send400() {
        File file = new File("src/responsecodes/BadRequest400.html");
        PaymentRequired402 paymentRequired402 = new PaymentRequired402();
        paymentRequired402.sendResponse(dataOutputStream,HTMLContent);
        paymentRequired402.sendFile(file,dataOutputStream);
    }

    public void send402() {
        File file = new File("src/responsecodes/PaymentRequired402.html");
        PaymentRequired402 paymentRequired402 = new PaymentRequired402();
        paymentRequired402.sendResponse(dataOutputStream,HTMLContent);
        paymentRequired402.sendFile(file,dataOutputStream);
    }

    public void send403(){
        Forbidden403Response forbidden403Response = new Forbidden403Response();
        forbidden403Response.sendResponse(dataOutputStream,HTMLContent);
        File file = new File("src/responsecodes/Forbidden403.html");
        forbidden403Response.sendFile(file,dataOutputStream);
    }

    public void send404() {
        File file = new File("src/responsecodes/FileNotFound404.html");
        FileNotFound404Response fileNotFound = new FileNotFound404Response();
        fileNotFound.sendResponse(dataOutputStream, HTMLContent);
        fileNotFound.sendFile(file, dataOutputStream);
    }

    public void send415() {
        File file = new File("src/responsecodes/UnsupportedMediaType415.html");
        UnsupportedMediaType415Response unsupportedMediaType415Response = new UnsupportedMediaType415Response();
        unsupportedMediaType415Response.sendResponse(dataOutputStream,HTMLContent);
        unsupportedMediaType415Response.sendFile(file,dataOutputStream);
    }

    public void send500() {
        InternalServerError500Response internalServerError500Response = new InternalServerError500Response();
        internalServerError500Response.sendResponse(dataOutputStream,HTMLContent);
        File file = new File("src/responsecodes/InternalServerError500.html");
        internalServerError500Response.sendFile(file,dataOutputStream);
    }

}
