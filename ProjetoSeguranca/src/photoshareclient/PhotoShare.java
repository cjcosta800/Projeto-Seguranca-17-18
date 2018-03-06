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


		String userId = args[0];
		String password = args[1];

		String[] serverAddress = args[2].split(":");
		String ip = serverAddress[0];
		int port = Integer.parseInt(serverAddress[1]);

		try {

			Socket socket = startClient(ip,port);
			ObjectOutputStream outStream = new ObjectOutputStream(socket.getOutputStream());
			ObjectInputStream inStream = new ObjectInputStream(socket.getInputStream());

			/* Envia o login ao servidor */

			outStream.writeObject(new String(userId));
			outStream.writeObject(new String(password));
			Boolean answer = (Boolean)inStream.readObject();

			if (answer) {

				if (args.length > 3) {

					outStream.writeObject(new String(args[3]));

					switch (args[3]) {
					case "-a" : 
						//TODO S� passa uma foto de cada vez
						break;
					case "-l" :
						outStream.writeObject(new String(args[4]));
						inStream.readObject();
						break;
					case "-i" :
						outStream.writeObject(new String(args[4]));
						outStream.writeObject(new String(args[5]));
						inStream.readObject();
						break;
					case "-g" :
						//TODO

						break;
					case "-c" :
						outStream.writeObject(new String(args[4]));
						outStream.writeObject(new String(args[5]));
						outStream.writeObject(new String(args[6]));
						inStream.readObject();
						break;
					case "-L" :
						outStream.writeObject(new String(args[4]));
						outStream.writeObject(new String(args[5]));
						inStream.readObject();
						break;
					case "-D" :
						outStream.writeObject(new String(args[4]));
						outStream.writeObject(new String(args[5]));
						inStream.readObject();
						break;
					case "-f" :
						outStream.writeObject(new String(args[4])); //Utilizadores s�o separados por virgulas.
						inStream.readObject(); 
						break;
					case "-r" :
						outStream.writeObject(new String(args[4])); //Utilizadores s�o separados por virgulas.
						inStream.readObject(); 
						break;
					default:
						System.err.println("Command Unknown");
					}
				}

				else {
					outStream.writeObject(new String("finished"));
				}
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
