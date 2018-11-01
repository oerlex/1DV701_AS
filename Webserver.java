import Response.ResponseSender;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by oerlex and ludvig on 2016-02-15.
 */
public class Webserver {

    //Add/Remove what filetypes our POST upload file supports here.
    private final ArrayList<String> supportedFiletypes = new ArrayList<>(
            Arrays.asList("png","jpeg","jpg","gif")
    );

    private int port;

    public Webserver(int port){
        this.port = port;
        try {
            start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() throws IOException {
        ServerSocket listenSocket = new ServerSocket(port);
        System.out.println("Server started...");
        while(true) {
            //When a new connection gets in create a new connection object
            Socket clientSocket = listenSocket.accept();
            Connection c = new Connection(clientSocket);
        }
    }

    //The Connection class extending thread is echoing back every request
    class Connection extends Thread {
        private ResponseSender responseSender;
        private Socket clientSocket;
        private String command;
        private DataOutputStream dataOutputStream;
        private String requestedPath;

        private BufferedReader bufferedReader;
        private String postData="";

        public Connection(Socket socket){
            clientSocket = socket;
            this.start();
        }

        public void run(){
            // set up the read and write end of the communication socket
            try {
                bufferedReader = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
                dataOutputStream = new DataOutputStream (clientSocket.getOutputStream());
                responseSender = new ResponseSender(dataOutputStream);
                requestedPath = parseRequest();


                if(command.equals("GET")) {
                    getRequestHandling();
                } else if(command.equals("POST")) {
                    postRequestHandling();
                }
                else if(command.equals("PUT")){
                    putRequestHandling();
                }

                clientSocket.close();
                Thread.currentThread().interrupt();

            } catch (NullPointerException e) {
                System.out.println("nullpointer...");
            }
            catch (Exception e) {
                e.printStackTrace();
                responseSender.send500();
            }
        }

        //If the command was set to "PUT" in the parseRequest method this method will take care that the PUT-Request is handled properly
        private void putRequestHandling(){
            String[] splitter;
            String fileName="";
            String content="";

            try {
                postData = postData.substring(13);

                splitter = postData.split("base64=");
                fileName = splitter[0].substring(4, splitter[0].length() - 2);
                content = splitter[1].split(",")[1];
            } catch(ArrayIndexOutOfBoundsException e) {
                responseSender.send400();
            }


            //If the type of file is supported
            if(supportedFiletypes.contains(getPrefix(fileName))) {
                String path;

                requestedPath = requestedPath.substring(1);
                path = requestedPath + fileName;

                //Get the byte64 as bytes
                byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(content);
                //Write them to an image.
                try {
                    Files.write(Paths.get(path), imageBytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                responseSender.send201PUT();

            } else {
                //Else we throw MediaType not supported.
                responseSender.send415();
            }

        }

        //If the command was set to "POST" in the parseRequest method this method will take care that the POST-Request is handled properly
        private void postRequestHandling(){
           /*
            Split the BODY of the post request, into a filename and a Byte64 String which is the picture data.
             */
            String[] splitter;
            String fileName="";
            String content="";

            try {
                splitter = postData.split("base64=");
                fileName = splitter[0].substring(4, splitter[0].length() - 2);
                content = splitter[1].split(",")[1];
            } catch(ArrayIndexOutOfBoundsException e) {
                responseSender.send400();
            }

            //If the type of file is supported
            if(supportedFiletypes.contains(getPrefix(fileName))) {
                String path  = "src/sharedFolder/images/" + fileName;

                if(pathExists(path)) {
                    //Send some HTL page saying file exists here?
                    responseSender.send409();
                    System.out.println("File" + fileName + " already exists in that directory.");
                } else {
                        //Get the byte64 as bytes
                        byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(content);
                        //Write them to an image.
                        try {
                            Files.write(Paths.get(path), imageBytes);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        responseSender.send201();
                    }
                } else {
                //Else we throw MediaType not supported.
                responseSender.send415();
            }
        }

        //If the command was set to "GET" in the parseRequest method this method will take care that the GET-Request is handled properly
        private void getRequestHandling() throws IOException {
            File file = new File("");
            if(requestedPath.endsWith("/")) {                                           //Discount ending slash "/"
                requestedPath = requestedPath.substring(0,requestedPath.length()-1);
            }
                                                                                        //If the requested file or directory doesn't exists we send a 404Html page back
            String folder = "src/sharedFolder";
            if(!pathExists(folder + requestedPath)) {
                responseSender.send404();
                clientSocket.close();
                                                                                        //If the requested file is existent but in a nonpublic folder we send back a 403 HTML page
            } else if(pathIsSecret(requestedPath)) {
                responseSender.send403();
                clientSocket.close();
                                                                                        //If it's existent and not restricted we check if a directory or a file has been requested.
            } else if(pathRequiresPayment(requestedPath)) {
                responseSender.send402();
                clientSocket.close();
            } else {
                                                                                        //If its and directory we check for the corresponding index.html file and add it to the requested path
                if(isDirectoryAndHasIndex(folder + requestedPath)) {
                    requestedPath += "/index.html";
                }
                                                                                        //If its a file we just leave the requested path like it is and determine the content type
                file = new File(folder + requestedPath);                                 //If the content type is filled with either png or html/htm we send the requested file back otherwise we send a 404 page
                responseSender.send200(file);
            }
        }


        //this method derives information from the request so that we can handle it properly.
        private String parseRequest() throws IOException {

            StringBuilder response = new StringBuilder();
            char[] charBuffer = new char[8192];                                         //Works as a buffer.
            int n;
            do{
                n = bufferedReader.read(charBuffer, 0,charBuffer.length);               //Load as many chars as the buffer can hold and append it to a string.
                response.append(charBuffer);

            } while(bufferedReader.ready());                                            //For as long as we receive data.

                                                                                        //Divide at spaces to get headers separately

            String[] divideMessages = response.toString().split("\\s+");
            String requestedPath = "";
            try {
                command = divideMessages[0];                                            //Request type will be the first header, eg "GET" or "POST"
                requestedPath = divideMessages[1];                                      //Path is second header.

            } catch(ArrayIndexOutOfBoundsException e) {
                responseSender.send500();                                                // /Chrome sends some sort of keepalive call periodicly which causes ugly ArrayIndexOutOfBounds in console.
            }

            if(command.equals("POST")) {
                String[] bodySeparation = response.toString().split("\r\n\r\n");        //Retrieve Body of POST by splitting at double carriage return and newline

                postData = bodySeparation[1];
                if(postData.contains("_method=put")) {                                  //The data will be checked if the post request had a wrapped put request
                    command = "PUT";                                                    //If so the command is set to PUT
                }
            }
            System.out.printf("Received a %s request\n", command);
            return requestedPath;
        }

        //If path requested is a directory, look for index.html inside it and serve that if it exists.
        private boolean isDirectoryAndHasIndex(String path) {
            File tryFile = new File(path);
                if(tryFile.isDirectory()) {
                    tryFile = new File(path + "/index.html");
                    if(tryFile.exists()) {
                        return true;
                    }
                }
            return false;
        }

        //Check if path exists.
        private boolean pathExists(String path) {
            File tryFile = new File(path);
            return tryFile.exists();
        }

        //Get ending prefix of a file like .png or .html
        private String getPrefix(String fileName) {
            String[] split = fileName.split("\\.");
            return split[split.length-1];
        }
        //Methods to check if secret etc.. to make use of additional HTTP responses
        private boolean pathIsSecret(String requestedPath) {
            return requestedPath.contains("secret");
        }
        private boolean pathRequiresPayment(String requestedPath) {
            return requestedPath.contains("premium");
        }
    }
}
