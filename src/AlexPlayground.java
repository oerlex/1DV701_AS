import java.io.File;

/**
 * Created by oerlex on 2016-02-16.
 */
public class AlexPlayground {

    public static void main(String args[]){





    }

    public static boolean checkFile(String url){

        //   localhost:8888/websites ->   requestedPath = websites


        File f = new File(url        );
        if(f.exists() && !f.isDirectory()) {

            return true;
        }
        return false;
    }
}


