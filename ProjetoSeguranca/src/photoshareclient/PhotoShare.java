package photoshareclient;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class PhotoShare {

	public static void main(String[] args) {


		if(args.length < 3) {
			System.err.print("Client must be run with the following command: 'PhotoShare <localUserId> <password>" +
					" <serverAddress>'. Can also have the following options:\n");
			System.err.println("[ -a <photos> | -l <userId>  | -i <userId> <photo> | -g <userId> | \n" +
					"-c <comment> <userId> <photo> | -L <userId> <photo> |\n" +
					"-D <userId> <photo> | -f <followUserIds>  | -r <followUserIds> ] ");
			System.err.println("For example: 'PhotoShare <localUserId> <password> <serverAddress> -a <photos>'");
			System.exit(0);
		}


		String currUser = args[0];
		String password = args[1];

		String[] serverAddress = args[2].split(":");
		String ip = serverAddress[0];
		int port = Integer.parseInt(serverAddress[1]);

		try {

			Socket socket = startClient(ip,port);

			ClientLogic clientLogic = new ClientLogic(socket);

			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

			/* Envia o login ao servidor */

			outStream.writeObject(new String(currUser));
			outStream.writeObject(new String(password));
			Boolean answer = (Boolean)inStream.readObject();


			if (answer) {

				if (args.length > 3) {

					String command = args[3];
					outStream.writeObject(new String(args[3]));

					if(command.equals("-a")) {
					    String photos = args[4];
						clientLogic.sendPhotos(photos);
					}

					else if(command.equals("-l")) {
						String user = args[4];
						clientLogic.listPhotos(user);
					}

					else if(command.equals("-i")) {
					    String userid = args[4];
					    String photoName = args[5];
						clientLogic.likeDislikeCounter (userid, photoName);
					}
					else if(command.equals("-g")) {
					    String userid = args[4];
						clientLogic.backupAllPhotos(userid);
					}

					else if(command.equals("-c")) {
					    String comment = args[4];
					    String userid = args[5];
					    String photoName = args[6];
						clientLogic.commentPhoto(comment,userid,photoName);
					}

					else if(command.equals("-L")) {
					    String userid = args[4];
					    String photoName = args[5];
						clientLogic.likePhoto(userid, photoName);
					}

					else if(command.equals("-D")) {
                        String userid = args[4];
                        String photoName = args[5];
                        clientLogic.dislikePhoto(userid, photoName);
					}

					else if(command.equals("-f")) {
					    String users = args[4];
					    clientLogic.followLocalUser(users); //Utilizadores s�o separados por virgulas.
                    }

                    else if(command.equals("-f")) {
                        String users = args[4];
                        clientLogic.unfollowLocalUser(users); //Utilizadores s�o separados por virgulas.
                    }

					else
						System.err.println("Command Unknown");
				}
			}

			else {
				outStream.writeObject(new String("finished"));
			}

			outStream.close();
			inStream.close();
			socket.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("File not found!");
			System.exit(1);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}


	public static Socket startClient(String ip, int port) {

		Socket socket = null;
		try {
			socket = new Socket(ip, port);

			if(socket.isConnected())
				System.out.println("Connected.");

		} catch (IOException e) {
			e.printStackTrace();
		}
		return socket;
	}

}
