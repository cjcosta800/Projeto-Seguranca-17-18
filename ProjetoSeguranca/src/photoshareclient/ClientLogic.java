package photoshareclient;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class ClientLogic {

	private Socket socket;
	private String currUser;

	public ClientLogic(String currUser, Socket socket) {
		this.currUser = currUser;
		this.socket = socket;
	}

	public void addPhotos(String args[]) throws IOException {

	    int counter = 4;
        ArrayList<String> photonames = new ArrayList<>();

        while (counter < args.length) {
            photonames.add(args[counter]);

            counter++;
        }

        sendPhotos(photonames);

        System.out.println("Success!");

	}

    private void sendPhotos(ArrayList<String> photonames) throws IOException {

        ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
        ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
        FileInputStream fileOut;
        BufferedInputStream in;

        File photo;
        String photoName;
        Boolean photoExists;
        byte buffer[];

	    outStream.writeInt(photonames.size());

        for (int i = 0; i < photonames.size(); i++) {

            photoName = photonames.get(i);

            photo = new File(photoName);

            if(photo.exists()) {

                outStream.writeObject(new String(photoName));

                photoExists = inStream.readBoolean();

                if (!photoExists) {

                    outStream.writeInt((int) photo.length());

                    fileOut = new FileInputStream(photo);
                    in = new BufferedInputStream(fileOut);

                    buffer = new byte[(int) photo.length()];

                    in.read(buffer, 0, buffer.length);

                    outStream.write(buffer, 0, buffer.length);
                    outStream.flush();

                    fileOut.close();
                    in.close();

                } else {
                    System.err.println("Photo " + photoName + " already exists.");
                }
            } else {
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

