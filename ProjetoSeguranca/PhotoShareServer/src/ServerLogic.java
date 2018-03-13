import java.io.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class ServerLogic {
	private final String serverPath = "./PhotoShareServer/PhotoShare/";

	private String passwordsPath;
	private HashMap<String, String> userPwd;
	private String user;
	private String userPath;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public ServerLogic(String passwordsPath, ObjectOutputStream outputStream, ObjectInputStream inputStream) {

		this.passwordsPath = passwordsPath;
		verifyPasswordsFile();
		this.outputStream = outputStream;
		this.inputStream = inputStream;

	}

	/**
	 * Checks if passwordsPath points to an existing file. If it doesn't, creates a new passwords file
	 * @param passwordsPath
	 */
	private void verifyPasswordsFile() {
		File passwords = new File(passwordsPath);
		if(!passwords.isFile()) {
			try {
				passwords.createNewFile();
			} catch (IOException e) {
				System.err.println("Failed to create passwords file");
			}
		}
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

		if (userPwd.containsKey(user)) {

			if (userPwd.get(user).equals(password)) {
				this.user = user;
				this.userPath = serverPath + user;
				return true;
			} else
				return false;
		} else {
			boolean registered = registerUser(user, password);

			this.user = user;

			return registered;
		}

	}

	/**
	 * Checks if a user exists
	 * @param userToFollow
	 * @return true if user exists
	 */
	private boolean isUser(String userToFollow) {

		return userPwd.containsKey(userToFollow);

	}

	/**
	 * Loads users and passwords from the passwords file (provided by passwordsPath)
	 * @return HashMap<User, Password> containing all users and corresponding password
	 *
	 @throws IOException
	 */
	private HashMap<String, String> loadPasswords() throws IOException {

		BufferedReader filereader = new BufferedReader(new FileReader(this.passwordsPath));

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

	/**
	 * Receives one file from the client
	 *
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

		if (!newPhoto.exists()) {

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
			outputStream.writeObject(new Boolean(true));
		}

	}

	/**
	 * Receives multiple photos from the client
	 *
	 * @param numPhotos
	 */
	public void receivePhotos(int numPhotos) throws IOException, ClassNotFoundException {

		for (int i = 0; i < numPhotos; i++) {

			receivePhoto();

		}

	}

	/**
	 * Follows or Unfollows numUsers users
	 * @param numUsers
	 * @param option 0 if follow new users 1 if unfollow users
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void followUnfollowUsers(int numUsers, int option) throws IOException, ClassNotFoundException {

		int counter = 0;

		while( counter < numUsers ) {
			String userToFollowUnfollow = (String) inputStream.readObject();

			if (option == 0)
				followUser(userToFollowUnfollow);
			if (option == 1)
				unfollowUser(userToFollowUnfollow);

			counter++;
		}

	}

	/**
	 * Creates "metafile" for the photo
	 *
	 * @param photoName
	 * @throws IOException
	 */
	private void createPhotoMetaFile(String photoName) throws IOException {

		/* Line 1: Current date
		 * Line 2: Likes:Dislikes
		 * Line 3: Comment
		 * Line 4: Comment ...
		 */

		String photometapath = userPath + "/" + photoName + ".txt";

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

	/**
	 * Sends a list of photo and creation date to user
	 * @param userId
	 */
	public void listPhotos(String userId) {

		try {
			int isFollower = isFollowerOf(userId);

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
	 * Adds a new comment to photoName if current user is a follower of userId
	 * @param comment
	 * @param userId
	 * @param photoName
	 */
	public void commentPhoto(String comment, String userId, String photoName) {

		int isFollower = isFollowerOf(userId);

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
	 * Sends userId photos to user if user is an userId follower
	 * @param userId
	 */
	public void downloadPhotos(String userId) {

		try {
			int isFollower = isFollowerOf(userId);
			outputStream.writeObject(new Integer(isFollower));

			if (isFollower == 0) {

				String userIdPath = serverPath + userId;
				ArrayList<String> photoNames = getPhotosList(userIdPath);

				outputStream.writeObject(new Integer(photoNames.size()));
				if(photoNames.size() != 0) {
					for (String photo : photoNames) {
						System.out.println("Sending photo " + photo + " and comments...");

						sendPhoto(photo, userIdPath);
						sendComments(photo, userIdPath);
					}
				}

				System.out.println("Success, all photos sent.");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Fetches photo comments, likes and dislikes and sends to server
	 * @param userId
	 * @param photoName
	 */
	public void fetchComments(String userId, String photoName) {

		try {
			int isFollower = isFollowerOf(userId);

			outputStream.writeObject(new Integer(isFollower));
			if (isFollower == 0) {
				String photoMetaPath = getPhotoMetaPath(userId, photoName);
				if(photoMetaPath != null) {
					String comments = getComments(photoMetaPath);
					outputStream.writeObject(new Integer(0));

					outputStream.writeObject(new String(comments));
				} else {
					// error 3 - photo doesn't exist on server
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
	 * Increments like or dislike (depending on posToChange)
	 * @param userId
	 * @param photoName
	 * @param posToChange if 0: increments like, if 1 increments dislike
	 *
	 * @requires posToChange 0 or 1
	 */
	public void incrementLikeDislike(String userId, String photoName, int posToChange) {

        String photometapath = getPhotoMetaPath(userId, photoName);
        int isFollower = isFollowerOf(userId);

        try {

            if (isFollower == 0) {

                if (photometapath != null) {

                    File metaData = new File(photometapath);
                    File aux = new File(userPath + ".tmp");
                    BufferedReader buffReader = new BufferedReader(new FileReader(metaData));
                    BufferedWriter fwriter = new BufferedWriter(new FileWriter(aux));

                    String photoDetails = buffReader.readLine();
                    fwriter.write(photoDetails + "\n");

                    String likesDislikes = buffReader.readLine();
                    String[] counters = likesDislikes.split(":");
                    int data = Integer.parseInt(counters[posToChange]) + 1;
                    counters[posToChange] = Integer.toString(data);
                    fwriter.write(counters[0] + ":" + counters[1] + "\n");

                    String line = buffReader.readLine();
                    while (line  != null) {
                        fwriter.write(line + "\n");
                        line = buffReader.readLine();
                    }

                    fwriter.close();
                    buffReader.close();
                    metaData.delete();
                    aux.renameTo(metaData);

                    outputStream.writeObject(new Integer(0)); //SUCESSO
                } else
                    outputStream.writeObject(new Integer(3)); //FICHEIRO NÃO EXISTE
            } else
                outputStream.writeObject(new Integer(isFollower));

        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

	/**
	 * Follows a user
	 * @param userToFollow
	 */
	private void followUser(String userToFollow) {

		String followersPath = getFollowersPath(user);

		try {

			if (userToFollow.equals(user)) {
				outputStream.writeObject(3); // o user nao se pode seguir
				return;
			}

			File followers = new File(followersPath);
			BufferedWriter fwriter = new BufferedWriter(new FileWriter(followers,true));
			String line;

			if (isUser(userToFollow)) {
				if(!userIsFollowedBy(userToFollow, followers)) {
					fwriter.write(userToFollow + "\n");
					outputStream.writeObject(new Integer(0));
				} else {
					outputStream.writeObject(new Integer(2)); // user ja e seguido
				}
			} else {
				outputStream.writeObject(new Integer(1)); // user nao existe
			}

			fwriter.flush();
			fwriter.close();

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Check if local user follows otherUser
	 * @param otherUser
	 * @param followers
	 * @return true if otherUser is already followed
	 * @throws IOException
	 */
	private boolean userIsFollowedBy(String otherUser, File followers) throws IOException {

		BufferedReader freader = new BufferedReader(new FileReader(followers));
		String line = freader.readLine();

		boolean userFound = false;

		while (line != null && !userFound) {
			if (otherUser.equals(line)) {
				userFound = true;
			}

			line = freader.readLine();
		}

		return userFound;

	}

	/**
	 * Removes a user from follower (if he was a follower)
	 * @param userToUnfollow
	 */
	private void unfollowUser (String userToUnfollow) {

		String followersPath = getFollowersPath(this.user);

		try {
			File followers = new File(followersPath);

			if (userToUnfollow.equals(this.user)) {
				outputStream.writeObject(3); // nao se pode fazer unfollow a si mesmo
			}

			if(userIsFollowedBy(userToUnfollow, followers)) {
				File tmp = new File(followersPath + ".tmp");
				BufferedReader readFollowers = new BufferedReader(new FileReader(followers));
				BufferedWriter writeTmp = new BufferedWriter(new FileWriter(tmp));

				String line = readFollowers.readLine();

				while (line != null) {

					System.out.println(line);

					if(!line.equals(userToUnfollow))
						writeTmp.write(line + "\n");

					line = readFollowers.readLine();
				}

				readFollowers.close();
				writeTmp.close();
				followers.delete();
				tmp.renameTo(followers);

				outputStream.writeObject(new Integer(0)); // correu tudo bem

			} else {
				outputStream.writeObject(new Integer(1)); // user nao era follower
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	/**
	 * Adds a new user to the passwords file
	 *
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
	 * Each photo has a "meta file" that contains the date it was uploaded, likes, dislikes and comments (if it has any)
	 * @param userid
	 * @param photoName
	 * @return photo meta file path if photo exists, null if it doesn't
	 */
	private String getPhotoMetaPath(String userid, String photoName) {

		String photoMetaPath = serverPath + userid + "/" + photoName + ".txt";

		File photo = new File(photoMetaPath);

		return photo.isFile() ? photoMetaPath : null;
	}

	/**
	 * Each user has a followers file, that contains all its followers (if it has any)
	 * @param userId
	 * @return followers path if it exists, null if it doesn't
	 */
	private String getFollowersPath(String userId) {

		String followersPath = serverPath + userId + "/" + "followers.txt";

		File followers = new File(followersPath);

		return followers.isFile() ? followersPath : null;
	}


	/**
	 * Each user has a folder with his photos. This method returns all photos on a string with the upload date
	 * @param userIdPath
	 * @return photo names
	 */
	private String getPhotoList(String userIdPath) {

		StringBuilder sb = new StringBuilder();

		ArrayList<String> photoNames = getPhotosList(userIdPath);

		for (String photo : photoNames) {
			sb.append(photo + " was uploaded at " + uploadDate(photo + ".txt", userIdPath) +"\n");

		}

		return sb.toString();
	}

	/**
	 * Returns the upload date of a photo
	 * @param photometafile
	 * @param userIdPath
	 * @return upload date of a photo
	 */
	private String uploadDate(String photometafile, String userIdPath) {
		try {
			BufferedReader freader = new BufferedReader(new FileReader(userIdPath + "/" + photometafile));

			String result = freader.readLine();
			freader.close();

			return result;

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return null;
	}

	/**
	 * Checks if user is a follower of userId. Being a follower means local user can see, comment, like and dislike userId's
	 * photos
	 * @param userId
	 * @return 0 if user is a follower, 1 if user is not a follower, 2 if userId doesn't exist, 3 if IO error
	 */
	private int isFollowerOf(String userId) {

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

	/**
	 * Gets userId photos listed on an ArrayList
	 * @param userIdPath path of the userId photos
	 * @return all photo names (with extension) on an ArrayList
	 */
	private ArrayList<String> getPhotosList(String userIdPath) {

		ArrayList<String> result = new ArrayList<>();

		File folder = new File(userIdPath);
		File[] listOfFiles = folder.listFiles();

		for (File file: listOfFiles) {

			if (file.isFile()) {

				String photoName = file.getName();
				String[] split = photoName.split("\\.");

				if (!split[1].equals("txt")) {
					if(split.length == 2)
						result.add(photoName);
				}
			}
		}

		return result;
	}

	/**
	 * Sends a file to client
	 * @param file file to be sent to client
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	private void sendFile(File file) throws IOException, ClassNotFoundException {
		FileInputStream fileOut = new FileInputStream(file);
		BufferedInputStream readFileBytes = new BufferedInputStream(fileOut);

		outputStream.writeObject(new String(file.getName()));

		Boolean canProceed = (Boolean) inputStream.readObject();

		if(canProceed) {
			outputStream.writeObject(new Integer((int)file.length()));

			byte buffer[] = new byte[(int) file.length()];

			readFileBytes.read(buffer, 0, buffer.length);

			outputStream.write(buffer,0, buffer.length);
			outputStream.flush();
		}

		readFileBytes.close();
		fileOut.close();
	}

	/**
	 * Sends a photo to client
	 * @param photoName
	 * @param userIdPath
	 */
	private void sendPhoto(String photoName, String userIdPath) {
		try {
			File photo = new File(userIdPath + "/" + photoName);
			sendFile(photo);

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Sends photo comments (and likes/dislikes) to client
	 * @param photo
	 * @param userIdPath
	 */
	private void sendComments(String photo, String userIdPath) {

		String photoMetaFile = userIdPath + "/" + photo + ".txt";
		//photo-comments.txt
		String photoCommentsTmp = userIdPath + "/" + photo + "-comments.txt";

		try {
			BufferedWriter fwriter = new BufferedWriter(new FileWriter(photoCommentsTmp));

			String comments = getComments(photoMetaFile);

			fwriter.write(comments);

			fwriter.flush();
			fwriter.close();

			// sends photo comments and then deletes it
			File photoComments = new File(photoCommentsTmp);
			sendFile(photoComments);
			photoComments.delete();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Gets photo comments, likes and dislikes from photo "meta file" and puts them on a String
	 * @param photoMetaFilePath
	 * @return string containing comments, likes and dislikes
	 * @throws IOException
	 */
	private String getComments(String photoMetaFilePath) throws IOException {

		StringBuilder sb = new StringBuilder();
		BufferedReader freader = new BufferedReader(new FileReader(photoMetaFilePath));

		// first line is "trash"
		freader.readLine();
		String line = freader.readLine();

		int likes = Integer.parseInt(line.split(":")[0]);
		int dislikes = Integer.parseInt(line.split(":")[1]);

		sb.append("Likes: " + likes + "\nDislikes: " + dislikes + "\n");
		sb.append("Comments:\n");
		// read comments
		line = freader.readLine();
		while(line != null) {
			sb.append(line + "\n");
			line = freader.readLine();
		}

		freader.close();


		return sb.toString();
	}
}
