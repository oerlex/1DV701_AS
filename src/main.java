import java.io.IOException;

/**
 * Created by oerlex on 2016-02-15.
 */
public class main {

    public static void main(String args[]){
        Webserver webserver = new Webserver(8888);
        try {
            webserver.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
