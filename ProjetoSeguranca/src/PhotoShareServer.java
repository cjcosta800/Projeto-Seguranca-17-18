import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.net.ServerSocket;
import java.net.Socket;

public class PhotoShareServer {

    public static void main(String[] args) throws IOException {

        /* Check number of args. Must be 1 */
        if (args.length != 1) {
            System.err.println("Server must be run with the following command: 'PhotoShareServer <port>'");
            System.err.println("For example: 'PhotoShareServer 23456'");
            System.exit(0);
        }
        
        int socket = Integer.parseInt(args[0]);

        /* Load User-Password file */
        System.out.println("Importing users and passwords...");
        HashMap<String, String> userpwd = loadPasswords();
        
        PhotoShareServer photoShareServer = new PhotoShareServer();
        photoShareServer.startServer(socket);

    }
    
    public void startServer(int socket) {
    	
    	ServerSocket sSoc = null;
    	
    	try {
			sSoc = new ServerSocket(socket);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
    	
    	while(true) {
    		Socket inSoc = null;
			try {
				inSoc = sSoc.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
	    		newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
    		
    	}
    	
    }
    
    class ServerThread extends Thread {
    	
    	private Socket socket = null;
    	
    	ServerThread(Socket inSoc) {
    		socket = inSoc;
    	}
    	
    	public void run() {
    		// TODO
    	}
    	
    }

    /**
     * Imports users and passwords from a file
     * @return HashMap with user - password
     * @throws IOException
     */
    public static HashMap<String, String> loadPasswords() throws IOException {
        BufferedReader filereader = new BufferedReader(new FileReader("password.txt"));

        String line = filereader.readLine();

        // HashMap <User, Password>
        HashMap<String, String> userpwd = new HashMap<>();
        String tokenised[] = null;
        // user;password
        while (line != null) {

            tokenised = line.split(":");

            userpwd.put(tokenised[0], tokenised[1]);

            line = filereader.readLine();

        }

        filereader.close();

        return userpwd;
    }

}
