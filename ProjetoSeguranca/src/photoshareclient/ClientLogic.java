package photoshareclient;

import java.net.Socket;

public class ClientLogic {

    private Socket socket;

    public ClientLogic(Socket socket) {
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
        currUser
    }

    public void followLocalUser(String users) {
        String[] usersList = users.split(",");

    }

    public void unfollowLocalUser(String users) {
        String[] usersList = users.split(",");

    }
}
