import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import javax.crypto.SecretKey;

public class manUsers {

	public final Random RANDOM_ = new SecureRandom();
	
	public boolean getAuthenticated(String user, String password) throws IOException {

		this.userPwd = loadPasswords();

		if (userPwd.containsKey(user)) {

			if (userPwd.get(user).equals(password)) {
				this.user = user;
				this.userPath = ServerPaths.SERVER_PATH + user;
				return true;
			} else
				return false;
		} else {
			boolean registered = registerUser(user, password);

			this.user = user;

			return registered;
		}

	}
	//alterar para usar o salted Hash no gravar ficheiro
	private boolean registerUser(String user, String password) throws IOException {

		this.userPath = ServerPaths.SERVER_PATH + user;
		File file = new File(userPath + "/followers.txt");

		file.getParentFile().mkdirs();
		file.createNewFile();

		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(ServerPaths.PASSWORD_FILE, true));

		fileWriter.write(user + ":" + password + "\n");

		fileWriter.close();

		System.out.println("New user " + user + " created.");

		return true;

	}
	  //necessario tratar de possiveis erros a fazer o salted hash?
	  public static byte[] getSaltedHashedPassword(String password){
		  
		    byte[] salt = new byte[32];
		    RANDOM.nextBytes(salt);
		    
		    PBEKeySpec passSpec = new PBEKeySpec(password.toCharArray(),salt,20);
		    SecretKeyFactory secPass = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
		    
		    return pass.generateSecret(passSpec).getEncoded();
		  }
}