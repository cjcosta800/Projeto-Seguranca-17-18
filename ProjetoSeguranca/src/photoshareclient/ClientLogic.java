package photoshareclient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;

public class ClientLogic {

	private String currUser;
	private ObjectOutputStream outStream;
	private ObjectInputStream inStream;

	public ClientLogic(String currUser, Socket socket, ObjectOutputStream outStream, ObjectInputStream inStream) {
		this.currUser = currUser;
		this.outStream = outStream;
		this.inStream = inStream;
	}

    /**
     * Handler for command addPhotos (-a)
     * @param args args coming from main
     * @throws IOException
     * @throws ClassNotFoundException
     */
	public void addPhotos(String args[]) throws IOException, ClassNotFoundException {

	    int counter = 4;
        ArrayList<String> photonames = new ArrayList<>();

        while (counter < args.length) {
            photonames.add(args[counter]);

            counter++;
        }

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


	    outStream.writeInt(photoNames.size());

	    // iterates through all photo names and sends each one
        // TODO: replace with foreach
        for (int i = 0; i < photoNames.size(); i++) {

            photoName = photoNames.get(i);

            photo = new File(photoName);

			// Photo exists locally
			if(photo.exists()) {

				// Send Photo name
				outStream.writeObject(new String(photoName));

                photoExists = (boolean) inStream.readObject();
				// Photo doesn't exist on server
                if (!photoExists) {

                    outStream.writeInt((int) photo.length());

                    fileOut = new FileInputStream(photo);
                    readPhotoBytes = new BufferedInputStream(fileOut);
                    // byte buffer to send the photo
                    buffer = new byte[(int) photo.length()];
                    // reads bytes from photo and puts them on the byte buffer
                    readPhotoBytes.read(buffer, 0, buffer.length);
                    // writes byte buffer to output stream (sends byte buffer to server)
                    outStream.write(buffer, 0, buffer.length);
                    outStream.flush();

                    System.out.println("Photo " + photoName + " was successfully sent.");

					fileOut.close();
					readPhotoBytes.close();

                } else {
                    System.err.println("Photo " + photoName + " is already present at the server.");
                }
            } else {
                // photo is not present on the client side. Maybe user misspelled photo name.
			    outStream.writeObject(new String("skip"));

                System.err.println("Photo " + photoName + " not found. Typo?\nSkipping...");
            }

        }

    }

    public void listPhotos (String user) throws IOException, ClassNotFoundException {

        System.out.println("Getting photo list of user " + user);

		outStream.writeObject(new String(user));

		/* isFollower = 0: yes, localuser is follower of user
		 * isFollower = 1: no, localuser  is not follower of user
		 * isFollower = 2: user doesn't exist
		 */
		int isFollower = (Integer) inStream.readObject();
		if (isFollower == 0) {
			String photoList = (String) inStream.readObject();

			System.out.println(photoList);

		} else if(isFollower == 1){
			System.err.println("You don't have permissions to check " + user + " photos list.");
		} else if(isFollower == 2){
			System.err.println("User " + user + " doesn't exist.");
		} else {
		    System.out.println("Some error occurred on the server side.");
        }

	}

	public void likeDislikeCounter (String userid, String photoName) {

	}

	public void backupAllPhotos(String userid) {

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

        outStream.writeObject(new String(comment));
        outStream.writeObject(new String(userId));
        outStream.writeObject(new String(photoName));

        int success = (Integer) inStream.readObject();

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
}


