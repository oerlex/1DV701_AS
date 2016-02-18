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
                    String folder = "src/sharedFolder/";
                    System.out.println("\nWe have a get request !");
                    if(!pathExists(folder + requestedPath)) {
                        //shitty path
                        file = new File("src/responsecodes/FileNotFound404.html");
                        FileNotFound404Response fileNotFound = new FileNotFound404Response();
                        fileNotFound.sendResponse(dataOutputStream, HTMLContent);
                        try {
                            fileNotFound.sendFile(file, dataOutputStream);
                            clientSocket.close();
                        } catch (IOException e) {
                        }

                    } else if(pathIsSecret(requestedPath)) {               //Secret
                        Forbidden403Response forbidden403Response = new Forbidden403Response();
                        forbidden403Response.sendResponse(dataOutputStream,HTMLContent);
                        file = new File("src/responsecodes/Forbidden403.html");
                        forbidden403Response.sendFile(file,dataOutputStream);
                    } else {
                        if(containsIndex(folder + requestedPath)) {
                            requestedPath += "/index.html";
                        }
                        file = new File(folder+ requestedPath);
                        if(file.isFile()) {
                            if(getPrefix(requestedPath).equals("png")) {
                                contentType = IMGContent;
                            } else {
                                contentType = HTMLContent;
                            }
                        OK200Response ok200Response = new OK200Response();
                        ok200Response.sendResponse(dataOutputStream,contentType);
                        ok200Response.sendFile(file, dataOutputStream);
                        } else {
                            //404
                            file = new File("src/responsecodes/FileNotFound404.html");
                            FileNotFound404Response fileNotFound404Response = new FileNotFound404Response();
                            fileNotFound404Response.sendResponse(dataOutputStream,HTMLContent);
                            fileNotFound404Response.sendFile(file, dataOutputStream);
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



        public String getPrefix(String requestedPath) {
            String[] split = requestedPath.split("\\.");
            String prefix = split[1];
            return prefix;
        }

        public boolean containsIndex(String path) {
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
