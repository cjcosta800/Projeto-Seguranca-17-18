import java.io.*;
import java.security.InvalidKeyException;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Scanner;
import java.util.Random;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.SecretKeyFactory;



public class manUsers {

	private static boolean createUser(String user, String password) throws IOException, NoSuchAlgorithmException{

		String userPath = ServerPaths.SERVER_PATH + "users" + ServerPaths.FILE_SEPARATOR + user;
		File file = new File(userPath + "/followers.txt");
		BufferedReader buf = new BufferedReader(new FileReader(ServerPaths.PASSWORD_FILE));
		String linha = buf.readLine();
		String [] tokens;

		while(linha != null){
			tokens = linha.split(":");
			if(tokens[0].equals(user)){
				System.out.println("User already exists");
				return false;
			}
			linha = buf.readLine();
		}
		byte[] salt = ServerSecurity.getSalt();
		byte[] pass = password.getBytes();
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(salt);

		file.getParentFile().mkdirs();
		file.createNewFile();
		Encoder encode = Base64.getEncoder();

		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(ServerPaths.PASSWORD_FILE,true));
		fileWriter.write(user + ":" + encode.encodeToString(salt) + ":"
				+ encode.encodeToString(md.digest(pass))+ "\n");
		fileWriter.close();

		System.out.println("New user " + user + " created.");

		return true;
	}

	private static void removeUser(String user) throws IOException {

		File file = new File(ServerPaths.PASSWORD_FILE);
		File newFile = new File(ServerPaths.TEMP_PASSWORD_FILE);

		BufferedReader fileReader = new BufferedReader(new FileReader(file));
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(newFile));

		String linha = fileReader.readLine();
		String[] userDetails;

		if (linha == null) {
			System.out.println("There are no users registered");
			newFile.delete();
		} else {
			while (linha != null) {

				userDetails = linha.split(":");

				if (!user.equals(userDetails[0]))
					fileWriter.write(linha +"\n");

				linha = fileReader.readLine();
			}
			fileReader.close();
			fileWriter.close();
			newFile.renameTo(file);
		}

		System.out.println("Removed " + user);
	}

	private static boolean changePassword(String user, String password) throws IOException {

		boolean userExists = false;

		File file = new File(ServerPaths.PASSWORD_FILE);
		File newFile = new File(ServerPaths.TEMP_PASSWORD_FILE);
		Encoder encode = Base64.getEncoder();

		BufferedReader fileReader = new BufferedReader(new FileReader(file));
		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(newFile, true));

		String linha = fileReader.readLine();
		String[] userDetails;

		if (linha == null) {
			System.out.println("You can't change the password because the user still does not exist");
			newFile.delete();
			return false;
		} else {
			while (linha != null) {

				userDetails = linha.split(":");

				if (user.equals(userDetails[0])) {
					byte[] salt = ServerSecurity.getSalt();
					byte[] pass = password.getBytes();
					try {
						MessageDigest md = MessageDigest.getInstance("SHA-256");
						md.update(salt);
						fileWriter.write(user + ":" + encode.encodeToString(salt) + ":"
								+ encode.encodeToString(md.digest(pass)) + "\n");
					}
					catch(NoSuchAlgorithmException e){
						System.err.println("Could not create MessageDigest");
					}

					userExists = true;
				} else
					fileWriter.write(linha);

				linha = fileReader.readLine();
			}
			fileReader.close();
			fileWriter.close();
			newFile.renameTo(file);


			if (!userExists) {
				System.out.println("The user " + user + " is not yet registered");
				return false;
			}
		}
		return true;

	}

	public static void main(String[] args) {

		try {
			Scanner scan = new Scanner(System.in);
			System.out.print("Insert the password: ");
			String password = scan.next();

			byte[] salt = {(byte) 0xc9, (byte) 0x36, (byte) 0x78, (byte) 0x99, (byte) 0x52, (byte) 0x3e, (byte) 0xea, (byte) 0xf2};
			byte[] pass = password.getBytes();
			SecretKey key = ServerSecurity.secretKeyGenerator(password, salt);
			byte[] mac = ServerSecurity.getMac(key, pass);
			byte[] otherMac;

			File adminFile = new File(ServerPaths.ADMIN_FILE);
			File passwords = new File(ServerPaths.PASSWORD_FILE);

			ObjectInputStream ois;
			ObjectOutputStream oos;
			FileInputStream fis;
			FileOutputStream fos;

			String user, userPass;
			int count = 0;

			//verifica se o ficheiro passwords existe, se nao existir cria-o
			if (!passwords.exists()) {
				try {
					passwords.createNewFile();
				} catch (IOException e) {
					System.err.println("Failed to create passwords file");
				}
			}

			//verifica se o ficheiro passwords existe, se nao existir cria-o
			if (!adminFile.exists()) {
				try {
					adminFile.createNewFile();
					fos = new FileOutputStream(ServerPaths.ADMIN_FILE);
					oos = new ObjectOutputStream(fos);
					oos.writeObject(mac);
					oos.close();
				} catch (IOException e) {
					System.err.println("Failed to create Admin password file");
				}
			}
			else {
				//pegar nos bytes do ficheiro password e na pass do admin
				fis = new FileInputStream(ServerPaths.ADMIN_FILE);
				ois = new ObjectInputStream(fis);
				otherMac = (byte[]) ois.readObject();
				ois.close();

				if (!ServerSecurity.compareMac(mac, otherMac)) {
					System.out.println("The given password is wrong. Terminating the program...");
					return;
				}
			}
			System.out.println("Authentication successful");
			while (count == 0) {
				System.out.print("Awaiting your operation: ");
				String op = scan.next();
				switch (op) {
					case "create":
						System.out.print("Insert the username of the desired client: ");
						user = scan.next();
						System.out.println();
						System.out.print("Insert the password for the client: ");
						userPass = scan.next();
						System.out.println();
						createUser(user, userPass);
						break;
					case "remove":
						System.out.print("Insert the username of the client you want to remove: ");
						user = scan.next();
						System.out.println();
						removeUser(user);
						break;
					case "change":
						System.out.print("Insert the username of the client to change the password : ");
						user = scan.next();
						System.out.println();
						System.out.print("Insert the new password for the desired client: ");
						userPass = scan.next();
						System.out.println();
						changePassword(user, userPass);
						break;
					case "exit":
						System.out.println("Ending the process. Closing the system...");
						count--;
						break;
					default:
						System.out.println("Type of inexistent operation.");
						System.out.println("Please choose one of the 4 valid operations : create, remove, change or exit");
						break;
				}
			}
		}catch(IOException e) {
			e.printStackTrace();
		}catch(ClassNotFoundException e){
			System.err.println("Could not read the bytes of the given file");
		}catch(NoSuchAlgorithmException e){
			System.err.println("Could not create user");
		}
	}
}
