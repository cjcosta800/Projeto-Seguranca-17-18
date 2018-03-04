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

    }

}
