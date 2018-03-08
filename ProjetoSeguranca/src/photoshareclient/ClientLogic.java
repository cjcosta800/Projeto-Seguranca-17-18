package photoshareclient;

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
        currUser
    }

    public void followLocalUser(String users) {

        String[] usersList = users.split(",");
        String followersPath = "./src/photoshareserver/Photos/" + user + "/followers.txt";
        File followers = new File(followersPath);
        if (!followers.isFile) {
            System.out.println("Ficheiro não existe!");
            return;
        }

        BufferedReader buffReader = new BufferedReader(new FileReader(followers);
        PrintWriter pw = new PrintWriter(new FileWriter(followers,true));

        while((String user = buffReader.readLine()) != null) {
            for (int i = 0; i < usersList.length; i++) {
                if (!user.trim().equals(usersList[i])) {
                    pw.println(user);
                    pw.flush();
                }
            }
        }
        pw.close();
        buffReader.close();

        }

    public void unfollowLocalUser(String users) { //FALTAM OS ERROS
        String[] usersList = users.split(",");
        String followersPath = "./src/photoshareserver/Photos/" + user + "/followers.txt";
        File followers = new File(followersPath);
        if (!followers.isFile) {
            System.out.println("Ficheiro não existe!");
            return;
        }

        File aux = new File (followersPath + ".tmp");
        BufferedReader buffReader = new BufferedReader(new FileReader(followers);
        PrintWriter pw = new PrintWriter(new FileWriter(aux));

        while((String user = buffReader.readLine()) != null) {
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
        }

        //LER FICHEIRO TODO, PROCURAR USERS A REMOVER, NAO ABRIR COM PARAMETRO A TRUE, ESCREVER DE NOVO O FICHEIRO SEM INCLUIR O USER QUE ACABAMOS DE REMOVER.
        //REESCREVER O FICHEIRO SEM O USER QUE SE VAI EXCLUIR.
    }
}
