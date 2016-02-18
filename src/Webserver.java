import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by oerlex on 2016-02-15.
 */
public class Webserver {

    private int port;
    private enum Method{GET,POST; }

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
        ResponseSender responseSender;
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
                responseSender = new ResponseSender(dataOutputStream);
                String requestedPath = parseRequest();

                String contentType = "";
                File file = new File("");

                if(command != null) {
                    //If the requested file or directory doesn't exists we send a 404Html page back
                    if(!pathExists(folder + requestedPath)) {
                      responseSender.throw404();
                        clientSocket.close();
                        //If the requested file is existent but in a nonpublic folder we send back a 403 HTML page
                    } else if(pathIsSecret(requestedPath)) {               //Secret
                       responseSender.throw403();
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
                }

                clientSocket.close();
                Thread.currentThread().interrupt();

            } catch (Exception e) {
                e.printStackTrace();
                responseSender.throw500();
            }
        }
        public String parseRequest() throws IOException{
            String message = bufferedReader.readLine();
            System.out.println("Reading...");
            System.out.println(message);
            String[] messageArray = message.split("\\s");

            StringBuilder sb = new StringBuilder();

            while(bufferedReader.ready()){
                sb.append(message);
                System.out.println(message);
                message = bufferedReader.readLine();
            }

            command = messageArray[0];

            String requestedPath = messageArray[1];
            requestedPath = requestedPath.substring(1);
            return requestedPath;
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
