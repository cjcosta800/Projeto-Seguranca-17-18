 package photoshareserver;

import java.io.*;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ServerLogic {

	private String passwordsPath;
	private HashMap<String, String> userPwd;
	private String user;
	private String userPath;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public ServerLogic(String passwordsPath, ObjectOutputStream outputStream, ObjectInputStream inputStream) {

		this.passwordsPath = passwordsPath;
		this.outputStream = outputStream;
		this.inputStream = inputStream;

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

			if(userPwd.get(user).equals(password)) {
				this.user = user;
				this.userPath = "./src/photoshareserver/Photos/" + user;
				return true;
			}
			else
				return false;
		}
		else {
			boolean registered = registerUser(user, password);

			this.user = user;

			return registered;
		}

	}

	/**
	 * Adds a new user to the passwords file
	 * @param user
	 * @param password
	 * @throws IOException
	 */
	private boolean registerUser(String user, String password) throws IOException {
		
		File file = new File("./src/photoshareserver/Photos/" + user + "/followers.txt");
		this.userPath = "./src/photoshareserver/Photos/" + user;

		file.getParentFile().mkdirs();
		file.createNewFile();

		BufferedWriter fileWriter = new BufferedWriter(new FileWriter(this.passwordsPath, true));

		fileWriter.write(user + ":" + password + "\n");

		fileWriter.close();

		System.out.println("New user " + user + " created.");
		
		return true;

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

		filereader.close();

		return userpwd;
	}

	/**
	 * Receives one file from the client
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void receivePhoto() throws IOException, ClassNotFoundException {

		// recebe "pergunta" se o cliente pode comecar a enviar. Particularmente importante para o caso de varias fotos
		String photoName = (String) inputStream.readObject();

		// caso o client indique que tem de fazer skip a esta foto (foto nao existe no client)
		if (photoName.equals("skip")) {
			return;
		}

		File newPhoto = new File(userPath + "/" + photoName);

		if(!newPhoto.exists()) {

			outputStream.writeObject(new Boolean(false));
			// recebe tamanho da foto
			int photoSize = inputStream.readInt();

			newPhoto.createNewFile();
			byte[] buffer = new byte[photoSize];

			FileOutputStream fos = new FileOutputStream(newPhoto);
			BufferedOutputStream writefile = new BufferedOutputStream(fos);
			int byteread = 0;

			while ((byteread = inputStream.read(buffer, 0, buffer.length)) != -1) {
				writefile.write(buffer, 0, byteread);
			}
			// writes new meta file
			createPhotoMetaFile(photoName);

			writefile.flush();
			writefile.close();
			fos.close();

		} else {
			// caso a foto ja esteja presente no servidor... envia-se uma mensagem de erro, neste caso bool false
			outputStream.writeObject(new Boolean (true));
		}

	}

	/**
	 * Receives multiple photos from the client
	 * @param numPhotos
	 */
	public void receivePhotos(int numPhotos) throws IOException, ClassNotFoundException {

		for (int i = 0; i < numPhotos; i++) {

			receivePhoto();

		}

	}

	/**
	 * Creates "metafile" for the photo
	 * @param photoName
	 * @throws IOException
	 */
	private void createPhotoMetaFile(String photoName) throws IOException {

		/* Line 1: Current date
		 * Line 2: Likes:Dislikes
		 * Line 3: Comment
		 * Line 4: Comment ...
		 */

        String photoNameSplit[] = photoName.split("\\.");

		String photometapath = userPath + "/" + photoNameSplit[0] + ".txt";

		File photometa = new File(photometapath);
		photometa.createNewFile();

		BufferedWriter fwriter = new BufferedWriter(new FileWriter(photometapath));

		// writes date as: 04 July 2001 12:08:56
		SimpleDateFormat sdfDate = new SimpleDateFormat("dd MM yy, HH:mm:ss");
		Date now = new Date();
		String date = sdfDate.format(now);

		// write current date
		fwriter.write(now + "\n");
		// write likes and dislikes (both starting at 0)
		fwriter.write("0:0\n");

		fwriter.flush();

		fwriter.close();

	}
}
