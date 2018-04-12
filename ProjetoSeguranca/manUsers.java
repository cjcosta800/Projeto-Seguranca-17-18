package PhotoShareServer.src;

import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;

public class manUsers {

	private HashMap<String, String> userPwd;
	private String user;
	private String userPath;
	public static final Random RANDOM = new SecureRandom();
	private final String SERVER_PATH = "../PhotoShareServer/PhotoShare/";

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
	private HashMap<String, String> loadPasswords() throws IOException {

		BufferedReader filereader = new BufferedReader(new FileReader(ServerPaths.PASSWORD_FILE));

		String line = filereader.readLine();

		// HashMap <User, Password>
		HashMap<String, String> userpwd = new HashMap<>();
		String tokenised[];
		// user;password
		while (line != null) {

			tokenised = line.split(":");

			userpwd.put(tokenised[0], tokenised[1]);

			line = filereader.readLine();

		}

		filereader.close();

		return userpwd;
	}
	  //necessario tratar de possiveis erros a fazer o salted hash?
	  public static byte[] getSaltedHashedPassword(String password) {

		  byte[] salt = new byte[32];
		  RANDOM.nextBytes(salt);
		  try {
			  PBEKeySpec passSpec = new PBEKeySpec(password.toCharArray(), salt, 20);
			  SecretKeyFactory secPass = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
			  return secPass.generateSecret(passSpec).getEncoded();
		  } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			  throw new AssertionError("Erro ao fazer hash a uma password:" + e.getMessage(), e);
		  }
	  }
}
