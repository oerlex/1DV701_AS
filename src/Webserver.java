import org.apache.commons.codec.binary.Base64;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.StringTokenizer;

/**
 * Created by oerlex on 2016-02-15.
 */
public class Webserver {

    private int port;
    private enum Method{GET,POST; }

    private final String folder = "src/sharedFolder";

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
        ResponseSender responseSender;
        Socket clientSocket;
        String command;
        String body;
        byte[] buffer;
        DataOutputStream dataOutputStream;
        DataInputStream dataInputStream;
        BufferedReader bufferedReader;
        String postData="";
        public Connection(Socket socket){
            clientSocket = socket;
            this.start();
        }

        public void run(){
            // set up the read and write end of the communication socket
            try {
                bufferedReader = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
                dataInputStream = new DataInputStream(clientSocket.getInputStream());
                dataOutputStream = new DataOutputStream (clientSocket.getOutputStream());
                responseSender = new ResponseSender(dataOutputStream);
                buffer = new byte[1024];
                String requestedPath = parseRequest();

                String contentType = "";
                File file = new File("");

                if(command.equals("GET")) {
                    //If the requested file or directory doesn't exists we send a 404Html page back
                    if(!pathExists(folder + requestedPath)) {
                      responseSender.send404();
                        clientSocket.close();
                        //If the requested file is existent but in a nonpublic folder we send back a 403 HTML page
                    } else if(pathIsSecret(requestedPath)) {               //Secret
                       responseSender.send403();
                        clientSocket.close();
                        //If it's existend and not restricted we check if a directory or a file has been requested.
                    } else {
                        //If its and directory we check for the corresponding index.html file and add it to the requested path
                        if(isDirectoryAndHasIndex(folder + requestedPath)) {
                            requestedPath += "/index.html";
                        }
                        //If its a file we just leave the requested path like it is and determine the content type
                        file = new File(folder+ requestedPath);
                        //If the content type is filled with either png or html/htm we send the requested file back otherwise we send a 404 page
                        responseSender.send200(file);
                    }
                } else if(command.equals("POST")) {

                    String[] splitter = postData.split("base64=");
                    String fileName = splitter[0].substring(4,splitter[0].length()-2);

                    String content = splitter[1].split(",")[1];

                            System.out.println("FILENAME: "+fileName);
                    System.out.println("CONTENT: "+content);

                   // byte[] data = Base64.decodeBase64(content);


                    String path = "src/sharedFolder/images/"+fileName;
                    File outputfile = new File(path);
                    byte[] imageBytes = javax.xml.bind.DatatypeConverter.parseBase64Binary(content);

                    Files.write(Paths.get(path),imageBytes);

                    responseSender.send201();
                    //System.out.println(body);


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

        public String parseRequest() throws IOException {

            StringBuffer response = new StringBuffer();

            char[] charBuffer = new char[65500];
            int n = 0;
            do{
                n = bufferedReader.read(charBuffer, n,charBuffer.length);
            } while(bufferedReader.ready());

            response.append(charBuffer);

            String[] divideMessages = response.toString().split("\\s+");
            command = divideMessages[0];
            String requestedPath = divideMessages[1];
            System.out.printf("Received a %s request\n", command);
            if(command.equals("POST")) {
                String[] bodySeparation = response.toString().split("\r\n\r\n");
                postData = bodySeparation[1];
                System.out.println("BODY: " + postData);
            }


            return requestedPath;
        }

        public String parseRequest2() throws IOException {
            int contentLength = 0;

            String ding = "";
            StringBuffer response = new StringBuffer();
            String message = "";
            int n = 0;

            while(bufferedReader.ready()){
                message = bufferedReader.readLine();
                response.append(message+ "\r\n");
                System.out.println(message);

                if(message.contains("Content-Length")) {
                    ding = message.split(" ")[1];
                    contentLength = Integer.parseInt(ding);
                }

                if(message.equals(""))
                    break;
            }
            StringTokenizer st = new StringTokenizer(response.toString());
            command = st.nextToken();

            String requestedPath = st.nextToken();

            response = new StringBuffer();

            int read ;

            if(contentLength > 0) {
                while ((read = bufferedReader.read()) != -1) {
                    char c = (char)read;
                    System.out.print(c);
                    response.append(c);
                    if (response.length() == contentLength)
                        break;
                }
            }

            postData = response.toString();

            System.out.println("PATH: "+ requestedPath);
            System.out.println("COMMAND: "+ command);
            return requestedPath;
        }

       /* public String parseRequest() throws IOException{
            String message = bufferedReader.readLine();
            System.out.println("Reading...");

            String[] messageArray = message.split("\\s");

            StringBuilder sb = new StringBuilder();

            while(bufferedReader.ready()){
                sb.append(message);
                message = bufferedReader.readLine();
                if(message.contains("firstname")) {
                    getBody(message);
                }
                System.out.println(message);
            }

            command = messageArray[0];

            String requestedPath = messageArray[1];
            requestedPath = requestedPath.substring(1);
            return requestedPath;
        }
*/
        public boolean isDirectoryAndHasIndex(String path) {
            File tryFile = new File(path);
                if(tryFile.isDirectory()) {
                    tryFile = new File(path + "/index.html");
                    if(tryFile.exists()) {
                        return true;
                    }
                }
            return false;
        }

        public boolean pathExists(String path) {
            File tryFile = new File(path);
            if(tryFile.exists()) {
                return true;
            }
            return false;
        }
        private void getBody(String body) {
            this.body = body;
            //To be further implemented.
        }

        public boolean pathIsSecret(String requestedPath) {
            if(requestedPath.contains("secret"))
                return true;
            return false;
        }
    }
}
