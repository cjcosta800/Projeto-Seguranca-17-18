package photoshareclient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

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

        outStream.close();
        inStream.close();

    }

    public void listPhotos (String user) {


	}

	public void likeDislikeCounter (String userid, String photoName) {

	}

	public void backupAllPhotos(String userid) {

	}

	public void commentPhoto(String comment,String userid,String photoName) {

	}

	public void likePhoto(String userid, String photoName) {

	}

	public void dislikePhoto(String userid, String photoName) {

	}

	public void followLocalUser(String users) {

		String[] usersList = users.split(",");
		String followersPath = "./src/photoshareserver/Photos/" + currUser + "/followers.txt";

		try {
			File followers = new File(followersPath);
			if (!followers.isFile()) {
				System.out.println("Ficheiro não existe!");
				return;
			}

			BufferedReader buffReader = new BufferedReader(new FileReader(followers));
			PrintWriter pw = new PrintWriter(new FileWriter(followers));
			String user;

			while((user = buffReader.readLine()) != null) {
				for (int i = 0; i < usersList.length; i++) {
					if (!user.trim().equals(usersList[i])) {
						pw.println(user);
						pw.flush();
					}
				}
			}
			pw.close();
			buffReader.close();

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}

	public void unfollowLocalUser(String users) { //FALTAM OS ERROS
		String[] usersList = users.split(",");
		String followersPath = "./src/photoshareserver/Photos/" + currUser + "/followers.txt";

		try {
			File followers = new File(followersPath);
			if (!followers.isFile()) {
				System.out.println("Ficheiro não existe!");
				return;
			}

			File aux = new File (followersPath + ".tmp");
			BufferedReader buffReader = new BufferedReader(new FileReader(followers));
			PrintWriter pw = new PrintWriter(new FileWriter(aux));
			String user;

			while((user = buffReader.readLine()) != null) {
				for (int i = 0; i < usersList.length; i++) {
					if (!user.trim().equals(usersList[i])) {
						pw.println(user);
						pw.flush();
					}
				}
			}
			pw.close();
			buffReader.close();

			if (!followers.delete()) {
				System.out.println("Ficheiro não foi removido!");
				return;
			}

			if (!aux.renameTo(followers)) {
				System.out.println("Nome do ficheiro não foi alterado!");
			}

		} catch (FileNotFoundException ex) {
			ex.printStackTrace();
		} catch (IOException ex) {
			ex.printStackTrace();
		}
	}
}

//LER FICHEIRO TODO, PROCURAR USERS A REMOVER, NAO ABRIR COM PARAMETRO A TRUE, ESCREVER DE NOVO O FICHEIRO SEM INCLUIR O USER QUE ACABAMOS DE REMOVER.
//REESCREVER O FICHEIRO SEM O USER QUE SE VAI EXCLUIR.

