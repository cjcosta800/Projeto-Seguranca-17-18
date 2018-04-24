import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class PhotoShareServer {


	final static String password = "grupo026";

	public static void main(String[] args) throws IOException {
		
		System.setProperty("javax.net.ssl.keyStore",ServerPaths.SSLKEYSTORE_FILE);
		System.setProperty("javax.net.ssl.keyStorePassword", password);

		/* Check number of args. Must be 1 */
		if (args.length != 1) {
			System.err.println("Server must be run with the following command: 'PhotoShareServer <port>'");
			System.err.println("For example: 'PhotoShareServer 23232'");
			System.exit(0);
		}

		int socket = Integer.parseInt(args[0]);

		if (socket != 23232) {
			System.err.println("PhotoShareServer listens at socket 23232");
			System.exit(0);
		}


		System.out.println("Listening for new connections at " + args[0] + "...");

		

		PhotoShareServer photoShareServer = new PhotoShareServer();
		photoShareServer.startServer(socket);


	}

	public void startServer(int socket) {

		SSLServerSocketFactory sslfact = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
		SSLServerSocket ssl = null;

		try {
			ssl = (SSLServerSocket) sslfact.createServerSocket(socket);
		} catch (IOException e) {

			e.printStackTrace();
			System.exit(-1);
		}

		while(true) {
			Socket inSoc = null;
			try {
				inSoc = ssl.accept();
				ServerThread newServerThread = new ServerThread(inSoc);
				newServerThread.start();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

	class ServerThread extends Thread {

		private Socket socket = null;

		ServerThread(Socket inSoc) {
			socket = inSoc;
			System.out.println("New connection with client");
		}

		/**
		 * 
		 */
		public void run() {
			try {
				ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
				ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

				ServerLogic serverLogic = new ServerLogic(outStream, inStream);

				String user = null;
				String password = null;

				user = (String) inStream.readObject();
				password = (String) inStream.readObject();

				if(!serverLogic.getAuthenticated(user, password)) {
					// informs client that login failed
					outStream.writeObject(new Boolean(false));

					System.out.println("Connection with client refused, wrong password");

					outStream.close();
					inStream.close();
					socket.close();
					return;
				}

				// informs client that login was successful!
				outStream.writeObject(new Boolean(true));
				System.out.println("User " + user + " is now logged in.");

				String command = (String) inStream.readObject();
				// adiciona/copia fotos para o servidor
				if(command.equals("-a")) {
					int numphotos = inStream.readInt();

					System.out.println("Receiving " + numphotos + " photos from user " + user);
					if(numphotos == 1)
						serverLogic.receivePhoto();
					else
						serverLogic.receivePhotos(numphotos);

					System.out.println("All photos added.");

					// lista as fotografias do do userid	
				} else if (command.equals("-l")) {
                    String userId = (String) inStream.readObject();

                    System.out.println(user + " is asking for " + userId + "'s photo list.");

                    serverLogic.listPhotos(userId);

					// devolve comentarios e o numero de likes da foto	
				} else if (command.equals("-i")) {
					String userId = (String) inStream.readObject();
					String photoName = (String) inStream.readObject();

					serverLogic.fetchComments(userId, photoName);

					// copia do servidor para o cliente fotos de userid
				} else if (command.equals("-g")) {
					String userId = (String) inStream.readObject();

					System.out.println(user + " is asking for " + userId + "photos.");

					serverLogic.downloadPhotos(userId);

					// adiciona um comentario a fotografia
				} else if (command.equals("-c")) {
					String comment = (String) inStream.readObject();
					String userId = (String) inStream.readObject();
					String photo = (String) inStream.readObject();

					System.out.println(user + " is commenting on photo " + photo + " of user " + userId + ".");

					serverLogic.commentPhoto(comment, userId, photo);

					// adiciona um Like à fotografia
				} else if (command.equals("-L")) {
					String userId = (String) inStream.readObject();
					String photo = (String) inStream.readObject();
					serverLogic.incrementLikeDislike(userId,photo,0);

					// adiciona um Dislike à fotografia
				} else if (command.equals("-D")) {
					String userId = (String) inStream.readObject();
					String photo = (String) inStream.readObject();
                    serverLogic.incrementLikeDislike(userId,photo,1);


                    // adiciona utilizadores como seguidores do user
				} else if (command.equals("-f")) {

					int numUsers = (Integer) inStream.readObject();

					System.out.println("User is trying to follow " + numUsers + " users.");

					serverLogic.followUnfollowUsers(numUsers, 0);

					// remove utilizadores de seguidores do user
				} else if (command.equals("-r")) {

                    int numUsers = (Integer) inStream.readObject();

                    System.out.println("User is trying to follow " + numUsers + " users.");

                    serverLogic.followUnfollowUsers(numUsers, 1);

				}

				outStream.close();
				inStream.close();
				socket.close();

				System.out.println("Connection closed with user " + user + ".");

			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NoSuchPaddingException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (InvalidKeyException e) {
				e.printStackTrace();
			}


		}

	}



}
