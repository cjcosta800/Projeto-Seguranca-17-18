import java.io.IOException;
import java.net.Socket;

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
		
		Socket socket = startClient(ip,port);

		switch (args[3]) {
		case "-a" : 
			addCopyPhotos(args[4]); //S� passa uma foto de cada vez
			break;
		case "-l" :
			userPhotoList(args[4]);
			break;
		case "-i" :
			checkPhotoLikes(args[4],args[5]);
			break;
		case "-g" :
			pullAllPhotos(args[4]);
			break;
		case "-c" :
			commentPhoto(args[4],args[5],args[6]);
			break;
		case "-L" :
			likePhoto(args[4],args[5]);
			break;
		case "-D" :
			dislikePhoto(args[4],args[5]);
			break;
		case "-f" :
			addFollowers(args[4]); //Utilizadores s�o separados por virgulas.
			break;
		case "-r" :
			removeFollowers(args[4]); //Utilizadores s�o separados por virgulas.
			break;
		default:
			System.err.println("Command Unknown");
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

	private static void addCopyPhotos(String string) {
		// TODO Auto-generated method stub

	}

	private static void userPhotoList(String string) {
		// TODO Auto-generated method stub

	}

	private static void checkPhotoLikes(String string, String string2) {
		// TODO Auto-generated method stub

	}

	private static void pullAllPhotos(String string) {
		// TODO Auto-generated method stub

	}

	private static void commentPhoto(String string, String string2, String string3) {
		// TODO Auto-generated method stub

	}

	private static void likePhoto(String string, String string2) {
		// TODO Auto-generated method stub

	}

	private static void dislikePhoto(String string, String string2) {
		// TODO Auto-generated method stub

	}

	private static void addFollowers(String string) {
		// TODO Auto-generated method stub

	}

	private static void removeFollowers(String string) {
		// TODO Auto-generated method stub

	}



}
