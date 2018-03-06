package photoshareserver;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class ServerLogic {

	private String passwordsPath;
	private HashMap<String, String> userPwd;
	
	
	public ServerLogic(String passwordsPath) {
		
		this.passwordsPath = passwordsPath;
		
	}
	
	/**
	 * Authenticates user. If user doesn't exist, creates a new one with 
	 * the given user and password.
	 * 
	 * @param user
	 * @param password
	 * @return true if authentications successful or registred with success or
	 * false if incorrect password
	 * @throws IOException
	 */
	public boolean getAuthenticated(String user, String password) throws IOException {
		
		this.userPwd = loadPasswords();
		
		if(userPwd.containsKey(user)) {
			return userPwd.get(user).equals(password);
		}
		else {
			registerUser(user, password);
			
			return true;
		}
		
	}
	
	/**
	 * Adds a new user to the passwords file
	 * @param user
	 * @param password
	 * @throws IOException
	 */
	private void registerUser(String user, String password) throws IOException {
		
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(this.passwordsPath, true));
		
		fileWriter.write(user + ":" + password);
		
		fileWriter.close();
		
	}
	
	/**
	 * Loads users and passwords from the passwords file (provided by passwordsPath)
	 * @return HashMap<User, Password> containing all users and corresponding password
	 * @throws IOException
	 */
	private HashMap<String, String> loadPasswords() throws IOException {
		
		BufferedReader filereader = new BufferedReader(new FileReader(this.passwordsPath));

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
