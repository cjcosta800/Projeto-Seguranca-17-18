package photoshareclient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientLogic {

	private String currUser;
	private ObjectOutputStream outputStream;
	private ObjectInputStream inputStream;

	public ClientLogic(String currUser, Socket socket, ObjectOutputStream outStream, ObjectInputStream inStream) {
		this.currUser = currUser;
		this.outputStream = outStream;
		this.inputStream = inStream;
	}

    /**
     * Handler for command addPhotos (-a)
     * @param args args coming from main
     * @throws IOException
     * @throws ClassNotFoundException
     */
	public void addPhotos(String args[]) throws IOException, ClassNotFoundException {

        ArrayList<String> photonames = getArgs(args);
		System.out.println("Sending " + photonames.size() + " photos.");
        sendPhotos(photonames);
        System.out.println("Task completed.");

	}

    /**
     * Sends 1 or more photos to client
     * @param photoNames list containing all photo names
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void sendPhotos(ArrayList<String> photoNames) throws IOException, ClassNotFoundException {

        FileInputStream fileOut;
        BufferedInputStream readPhotoBytes;

        File photo;
        String photoName;
        Boolean photoExists;
        byte buffer[];


	    outputStream.writeInt(photoNames.size());

	    // iterates through all photo names and sends each one
        // TODO: replace with foreach
        for (int i = 0; i < photoNames.size(); i++) {

            photoName = photoNames.get(i);

            photo = new File(photoName);

			// Photo exists locally
			if(photo.exists()) {

				// Send Photo name
				outputStream.writeObject(new String(photoName));

                photoExists = (boolean) inputStream.readObject();
				// Photo doesn't exist on server
                if (!photoExists) {

                    outputStream.writeInt((int) photo.length());

                    fileOut = new FileInputStream(photo);
                    readPhotoBytes = new BufferedInputStream(fileOut);
                    // byte buffer to send the photo
                    buffer = new byte[(int) photo.length()];
                    // reads bytes from photo and puts them on the byte buffer
                    readPhotoBytes.read(buffer, 0, buffer.length);
                    // writes byte buffer to output stream (sends byte buffer to server)
                    outputStream.write(buffer, 0, buffer.length);
                    outputStream.flush();

                    System.out.println("Photo " + photoName + " was successfully sent.");

					fileOut.close();
					readPhotoBytes.close();

                } else {
                    System.err.println("Photo " + photoName + " is already present at the server.");
                }
            } else {
                // photo is not present on the client side. Maybe user misspelled photo name.
			    outputStream.writeObject(new String("skip"));

                System.err.println("Photo " + photoName + " not found. Typo?\nSkipping...");
            }

        }

    }

    public void listPhotos (String user) throws IOException, ClassNotFoundException {

        System.out.println("Getting photo list of user " + user + ":");

		outputStream.writeObject(new String(user));

		/* isFollower = 0: yes, localuser is follower of user
		 * isFollower = 1: no, localuser  is not follower of user
		 * isFollower = 2: user doesn't exist
		 */
		int isFollower = (Integer) inputStream.readObject();
		if (isFollower == 0) {
			String photoList = (String) inputStream.readObject();

			System.out.print(photoList);

		} else if(isFollower == 1){
			System.err.println("You don't have permissions to check " + user + " photos list.");
		} else if(isFollower == 2){
			System.err.println("User " + user + " doesn't exist.");
		} else {
		    System.out.println("Some error occurred on the server side.");
        }

	}

    /**
     * Fetches photoName photos and prints them
     * @param userId
     * @param photoName
     */
	public void fetchComments(String userId, String photoName) {

        System.out.println("Getting " + photoName + " comments:");

        try {
            outputStream.writeObject(new String(userId));
            outputStream.writeObject(new String(photoName));

            int isFollower = (Integer) inputStream.readObject();
            if (isFollower == 0) {
                int isSuccessful = (Integer) inputStream.readObject();
                if(isSuccessful == 0) {
                    String comments = (String) inputStream.readObject();
                    System.out.print(comments);
                } else {
                    System.err.println("Photo " + photoName + " doesn't exist.");
                }

            } else if (isFollower == 1) {
                System.err.println("You don't have permissions to check " + userId + " photos comments.");
            } else if (isFollower == 2) {
                System.err.println("User " + userId + " doesn't exist.");
            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

	public void getPhotos(String userId) throws IOException, ClassNotFoundException {

        System.out.println("Getting " + userId + " photos...");

        outputStream.writeObject(new String(userId));

        int isFollower = (Integer) inputStream.readObject();

        if (isFollower == 0) {
            String receivePath = "./" + userId + "/";
            File receiveP = new File(receivePath);

            if (!receiveP.isDirectory()) {
                receiveP.mkdirs();
            }

            int numPhotos = (Integer) inputStream.readObject();

            for (int i = 0; i < numPhotos; i++) {
                // receive photo
                receiveFile(receivePath);
                // receive comments
                receiveFile(receivePath);

            }

            if (numPhotos != 0) {
                System.out.println("Received " + numPhotos + " photos from server.\nPhotos are located at " +
                        receiveP.getAbsolutePath());
            } else {
                System.out.println("Received no photos from server.\nUser has no photos!");
            }

        } else if(isFollower == 1){
            System.err.println("You don't have permissions to get " + userId + " photos.");
        } else if(isFollower == 2){
            System.err.println("User " + userId + " doesn't exist.");
        } else {
            System.out.println("Some error occurred on the server side.");
        }

	}

    /**
     * Adds a new comment to photoName
     * @param args
     * @throws IOException
     * @throws ClassNotFoundException
     */
    public void commentPhoto(String[] args) throws IOException, ClassNotFoundException {

        int argsLength = args.length;
        String photoName = args[argsLength - 1];
        String userId = args[argsLength - 2];

        String comment = getComment(args);

        outputStream.writeObject(new String(comment));
        outputStream.writeObject(new String(userId));
        outputStream.writeObject(new String(photoName));

        int success = (Integer) inputStream.readObject();

        switch (success) {
            case 1:
                System.err.println("You don't have permissions to add comments to " + userId + " photos.");
                break;

            case 2:
                System.err.println("User " + userId + " doesn't exist.");
                break;

            case 3:
                System.err.println("Photo " + photoName + " doesn't exist.");
                break;

            default:
                System.out.println("New comment added to photo " + photoName + ".");
                break;
        }

    }

    public void likeDislike (String userId, String photoName, int decider) throws IOException, ClassNotFoundException {

        if (decider == 0)  //DAR LIKE
            System.out.println("Liking " + userId + "'s " + photoName + "...");

        else
            System.out.println("Disliking " + userId + "'s " + photoName + "...");


        outputStream.writeObject(new String(userId));
        outputStream.writeObject(new String(photoName));

        int success = (Integer) inputStream.readObject();

        switch (success) {
            case 1:
                System.err.println("You don't have permissions to like " + userId + "'s photos.");
                break;

            case 2:
                System.err.println("User " + userId + " doesn't exist.");
                break;

            case 3:
                System.err.println("Photo " + photoName + " doesn't exist.");
                break;

            default:
                if (decider == 0)
                    System.out.println("You liked " + userId + "'s " + photoName + ".");
                else
                    System.out.println("You disliked " + userId + "'s " + photoName + ".");

                break;
        }
    }

    public static String argsToString (String[] args) {
        StringBuilder sb = new StringBuilder();
        int argsLength = args.length;

        for (int i = 4; i < argsLength; i++) { // user pass server op user1 user2 user3 ....
            sb.append(args[i] + " ");
        }

        sb.deleteCharAt(sb.length()-1);

        return sb.toString();
    }


    /**
     * Converts comments from args to String
     * @param args
     * @return comment in a string
     */
    private String getComment(String[] args) {

        String[] comments = Arrays.copyOfRange(args, 4, args.length - 2);

        StringBuilder sb = new StringBuilder();

        for (String comment: comments) {
            sb.append(comment + " ");
        }

        sb.deleteCharAt(sb.length() - 1);

        return sb.toString();
    }

    /**
     *
     * @param receivePath Must end with "dir/"
     * @throws IOException
     * @throws ClassNotFoundException
     */
    private void receiveFile(String receivePath) throws IOException, ClassNotFoundException {

        String fileName = (String) inputStream.readObject();
        File newFile = new File(receivePath + fileName);

        if(newFile.exists()) {
            outputStream.writeObject(new Boolean(false));
        } else {
            outputStream.writeObject(new Boolean(true));
            // recebe tamanho da foto
            int photoSize = (Integer) inputStream.readObject();

            newFile.createNewFile();
            byte[] buffer = new byte[photoSize];

            FileOutputStream fos = new FileOutputStream(newFile);
            BufferedOutputStream writefile = new BufferedOutputStream(fos);
            int byteread = 0;

            while ((byteread = inputStream.read(buffer, 0, buffer.length)) != -1) {
                writefile.write(buffer, 0, byteread);
            }
            writefile.flush();
            writefile.close();
            fos.close();
        }

    }

    public void followUnfollowUsers(String[] args, int option) {

        ArrayList<String> users = getArgs(args);

        System.out.println("Trying to follow " + users.size() + " users...");

        followUnfollowUsers(users, option);

        System.out.println("Task completed.");

    }

    private ArrayList<String> getArgs(String[] args) {
        int counter = 4;
        ArrayList<String> result = new ArrayList<>();

        while (counter < args.length) {
            result.add(args[counter]);

            counter++;
        }

        return result;
    }

    private void followUnfollowUsers(ArrayList<String> users, int option) {

        try {
            // send number of users to follow or unfollow
            outputStream.writeObject(new Integer(users.size()));

            int counter = 0;

            while(counter < users.size()) {

                outputStream.writeObject(users.get(counter));

                int answer = (Integer) inputStream.readObject();

                switch (answer) {
                    case 1:
                        if (option == 0)
                            System.err.println("User " + users.get(counter) + " is not a valid user.");
                        else
                            System.err.println("User " + users.get(counter) + " isn't a follower"); //TODO
                        break;

                    case 2:
                        System.err.println("User " + users.get(counter) + " is already a follower.");
                        break;

                    case 3:
                        if (option == 0)
                            System.err.println("You can't follow yourself.");
                        else
                            System.err.println("You can't unfollow yourself.");
                        break;

                    default:
                        if (option == 0)
                            System.out.println("User " + users.get(counter) + " is now a follower.");
                        else
                            System.out.println("User " + users.get(counter) + " is no longer a follower.");
                        break;
                }

                counter++;

            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

}


