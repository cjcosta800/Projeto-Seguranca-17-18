package photoshareclient;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientLogic {

	private Socket socket;
	private String currUser;

	public ClientLogic(String currUser, Socket socket) {
		this.currUser = currUser;
		this.socket = socket;
	}

	public void sendPhotos(String photos) {

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

