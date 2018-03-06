package photoshareserver;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

public class ServerLogic {

	private String passwordsPath;
	
	
	public ServerLogic(String passwordsPath) {
		
		this.passwordsPath = passwordsPath;
		
	}
	
	public boolean getAuthenticated(String user, String password) {
		return false;
	}
	
	private void registerUser(String user, String password) {
		
	}
	
	private HashMap<String, String> loadPasswords(String path) throws IOException {
		
		BufferedReader filereader = new BufferedReader(new FileReader(path));

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

        System.out.println("Import complete.");
        
        filereader.close();

        return userpwd;
	}
	
}
