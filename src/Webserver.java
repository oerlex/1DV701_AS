import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by oerlex on 2016-02-15.
 */
public class Webserver {

    private int port;
    private enum Method{GET,POST; }

    private final String HTMLContent = "Content-Type: text/html"+ "\r\n";
    private final String IMGContent = "Content-Type: image/png"+ "\r\n";
    private final String folder = "src/sharedFolder/";

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
        Socket clientSocket;
        String command;
        DataOutputStream dataOutputStream;
        BufferedReader bufferedReader;
        public Connection(Socket socket){
            clientSocket = socket;
            this.start();
        }

        public void run(){
            // set up the read and write end of the communication socket
            try {
                bufferedReader = new BufferedReader (new InputStreamReader(clientSocket.getInputStream()));
                dataOutputStream = new DataOutputStream (clientSocket.getOutputStream());

                String requestedPath = parseRequest();

                String contentType = "";
                File file = new File("");

                if(command != null) {

                    //If the requested file or directory doesn't exists we send a 404Html page back
                    if(!pathExists(folder + requestedPath)) {
                      throw404();

                        //If the requested file is existent but in a nonpublic folder we send back a 403 HTML page
                    } else if(pathIsSecret(requestedPath)) {               //Secret
                       throw403();

                        //If it's existend and not restricted we check if a directory or a file has been requested.
                    } else {
                        //If its and directory we check for the corresponding index.html file and add it to the requested path
                        if(isDirectoryAndHasIndex(folder + requestedPath)) {
                            requestedPath += "/index.html";
                        }

                        //If its a file we just leave the requested path like it is and determine the content type
                        file = new File(folder+ requestedPath);
                        contentType = setContentType(file,requestedPath);
                        //If the content type is filled with either png or html/htm we send the requested file back otherwise we send a 404 page
                        if(contentType != ""){
                            send200(file,contentType);
                        } else {
                           throw404();
                        }
                    }
                }

                clientSocket.close();
                Thread.currentThread().interrupt();
                return;

            } catch (Exception e) {
                e.printStackTrace();
                InternalServerError500Response internalServerError500Response = new InternalServerError500Response();
                internalServerError500Response.sendResponse(dataOutputStream,HTMLContent);
                File file = new File("src/responsecodes/InternalServerError500.html");
                internalServerError500Response.sendFile(file,dataOutputStream);
            }
        }
        public String parseRequest() throws IOException{
            String message = bufferedReader.readLine();
            System.out.println("Reading...");

            String[] messageArray = message.split("\\s");

            StringBuilder sb = new StringBuilder();

            while(bufferedReader.ready()){
                sb.append(message);
                System.out.println(message);
                message = bufferedReader.readLine();
            }

            for(String s : messageArray){
                if(s.equals("GET")){command = s;}
                System.out.println(s);
            }

            String requestedPath = messageArray[1];
            requestedPath = requestedPath.substring(1);
            return requestedPath;
        }

        private String setContentType(File file, String requestedPath){
            String contentType = "";

            if(file.isFile()) {
                if(getPrefix(requestedPath).equals("png")) {
                    contentType = IMGContent;
                } else {
                    contentType = HTMLContent;
                }
            }
            return contentType;
        }

        private void send200(File file , String contentType){
            OK200Response ok200Response = new OK200Response();
            ok200Response.sendResponse(dataOutputStream,contentType);
            ok200Response.sendFile(file, dataOutputStream);
        }

        private void throw403(){
            Forbidden403Response forbidden403Response = new Forbidden403Response();
            forbidden403Response.sendResponse(dataOutputStream,HTMLContent);
            File file = new File("src/responsecodes/Forbidden403.html");
            forbidden403Response.sendFile(file,dataOutputStream);
        }

        private void throw404() {
            File file = new File("src/responsecodes/FileNotFound404.html");
            FileNotFound404Response fileNotFound = new FileNotFound404Response();
            fileNotFound.sendResponse(dataOutputStream, HTMLContent);
            try {
                fileNotFound.sendFile(file, dataOutputStream);
                clientSocket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        public String getPrefix(String requestedPath) {
            String[] split = requestedPath.split("\\.");
            String prefix = split[1];
            return prefix;
        }

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

        public boolean pathIsSecret(String requestedPath) {
            if(requestedPath.contains("secret"))
                return true;
            return false;
        }



    }
}
