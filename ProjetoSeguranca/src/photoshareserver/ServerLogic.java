 package photoshareserver;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ServerLogic {

	private final String serverPath = "./src/photoshareserver/Photos/";
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
				this.userPath = serverPath + user;
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

		this.userPath = serverPath + user;
		File file = new File(userPath + "/followers.txt");

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
		SimpleDateFormat sdfDate = new SimpleDateFormat("EEEE, dd 'de' MMMMM 'de' yyyy 'as' HH:mm:ss");
		Date now = new Date();
		String date = sdfDate.format(now);

		// write current date
		fwriter.write(date + "\n");
		// write likes and dislikes (both starting at 0)
		fwriter.write("0:0\n");

		fwriter.flush();

		fwriter.close();

	}


	public void listPhotos(String userId) {

		try {
		int isFollower = isFollower(userId);

		if (isFollower == 0) {
			outputStream.writeObject(new Integer(isFollower));

			String photoList = getPhotoList(serverPath + userId);

			outputStream.writeObject(photoList);
		} else {

			outputStream.writeObject(new Integer(isFollower));

		}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 *
	 * @param userIdPath
	 * @return
	 */
	private String getPhotoList(String userIdPath) {

		StringBuilder sb = new StringBuilder();

		File folder = new File(userIdPath);
		File[] listOfFiles = folder.listFiles();

		int counter = 0;

		for (File file: listOfFiles) {

			if (file.isFile()) {

				String photoName = file.getName();

				if (!photoName.split("\\.")[1].equals("txt")) {

					if (counter % 4 == 0) {
						sb.append(photoName + "\n");
					} else {
						sb.append(photoName + " ");
					}

				}
			}
		}

		return sb.toString();
	}

	/**
	 *
	 * @param comment
	 * @param userId
	 * @param photoName
	 */
    public void commentPhoto(String comment, String userId, String photoName) {

        int isFollower = isFollower(userId);

        try {
			if (isFollower == 0) {

				String photoMetaPath = getPhotoMetaPath(userId, photoName);

				if (photoMetaPath != null) {

					BufferedWriter fwriter = new BufferedWriter(new FileWriter(photoMetaPath, true));

					// writes date as: 04/07/2001
					SimpleDateFormat sdfDate = new SimpleDateFormat("dd/MM/yy");
					Date now = new Date();
					String date = sdfDate.format(now);

					fwriter.write("[" + date + "] " + this.user + ": " + comment + "\n");
					fwriter.flush();
					fwriter.close();

					outputStream.writeObject(new Integer(isFollower));

				} else {
					// Error code 3: photoName isn't a photo
					outputStream.writeObject(new Integer(3));
				}

			} else {
				outputStream.writeObject(new Integer(isFollower));
			}

		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 *
	 * @param userid
	 * @param photoName
	 * @return
	 */
	private String getPhotoMetaPath(String userid, String photoName) {

		String photoNameNoExtension = photoName.split("\\.")[0];
		String photoMetaPath = serverPath + userid + "/" + photoNameNoExtension + ".txt";

		File photo = new File(photoMetaPath);

		return photo.isFile() ? photoMetaPath : null;
	}

	/**
	 *
	 * @param userId
	 * @return
	 */
	private int isFollower(String userId) {

	    // if userId is the localuser, he got permissions
	    if (userId.equals(this.user)) {
	        return 0;
        }

        FileReader userIdFollowers = null;

        try {

            userIdFollowers = new FileReader(serverPath + userId + "/followers.txt");

            BufferedReader filereader = new BufferedReader(userIdFollowers);

            String line = filereader.readLine();
            boolean found = false;

            while (line != null && !found) {

                if (user.equals(line)) {
                    found = true;
                }

                line = filereader.readLine();
            }

            return found ? 0 : 1;


        } catch (FileNotFoundException e) {
            // user doesn't exist
            return 2;
        } catch (IOException e) {
            System.err.println("IO Error occurred while reading followers file.");
            return 3;
        }

	}
}
