import java.io.*;
import java.util.HashMap;
import java.util.Random;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;

public class manUsers {

	private static final int NUMBER_OF_ITERATIONS = 20;

	private HashMap<String, String> userPwd;
	private String user;
	private String userPath;
	public static final Random RANDOM = new SecureRandom();
	private final String SERVER_PATH = "../PhotoShareServer/PhotoShare/";

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
	  public static byte[] getSaltedHashedPassword(String password) {

		  byte[] salt = new byte[32];
		  RANDOM.nextBytes(salt);
		  try {
			  PBEKeySpec passSpec = new PBEKeySpec(password.toCharArray(), salt, NUMBER_OF_ITERATIONS);
			  SecretKeyFactory secPass = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
			  return secPass.generateSecret(passSpec).getEncoded();
		  } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			  throw new AssertionError("Erro ao fazer hash a uma password:" + e.getMessage(), e);
		  }
	  }
}
