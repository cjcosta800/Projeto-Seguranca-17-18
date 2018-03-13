import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
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

			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());
			ClientLogic clientLogic = new ClientLogic(currUser,socket, outStream, inStream);

			/* Envia o login ao servidor */

			outStream.writeObject(new String(currUser));
			outStream.writeObject(new String(password));
			Boolean answer = (Boolean)inStream.readObject();


			if (answer) {

				if (args.length > 3) {

					String command = args[3];
					outStream.writeObject(new String(args[3]));

					if(command.equals("-a")) {
						clientLogic.addPhotos(args);
					}

					else if(command.equals("-l")) {
						String user = args[4];
						clientLogic.listPhotos(user);
					}

					else if(command.equals("-i")) {
						String userId = args[4];
						String photoName = args[5];
						clientLogic.fetchComments(userId, photoName);
					}
					else if(command.equals("-g")) {
						String userId = args[4];
						clientLogic.getPhotos(userId);
					}

					else if(command.equals("-c")) {
						clientLogic.commentPhoto(args);
					}

					else if(command.equals("-L")) {
						String userid = args[4];
						String photoName = args[5];
						clientLogic.likeDislike(userid, photoName,0);
					}

					else if(command.equals("-D")) {
						String userid = args[4];
						String photoName = args[5];
						clientLogic.likeDislike(userid, photoName,1);
					}

					else if(command.equals("-f")) {
						clientLogic.followUnfollowUsers(args, 0);
					}

					else if(command.equals("-r")) {
						clientLogic.followUnfollowUsers(args, 1);
					}

					else
						System.err.println("Command Unknown");
				}

				outStream.writeObject(new String("finished"));

			}

			else {
				outStream.writeObject(new String("finished"));

				System.err.println("Login not successful, wrong password!");
			}

			outStream.close();
			inStream.close();
			socket.close();
		} catch (ConnectException e) {
		    System.err.println("Couldn't connect with server (Connection refused). Maybe it is offline?");
		    System.exit(0);
        } catch (UnknownHostException e) {
            e.printStackTrace();
		} catch (FileNotFoundException e) {
			System.err.println("File not found!");
			System.exit(0);
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}


	public static Socket startClient(String ip, int port) throws IOException {

		Socket socket = null;

		socket = new Socket(ip, port);

		if(socket.isConnected())
		    System.out.println("Connected.");

		return socket;
	}


}
