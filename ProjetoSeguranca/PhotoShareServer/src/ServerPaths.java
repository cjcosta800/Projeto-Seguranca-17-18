import java.io.File;

class ServerPaths {
    public static final String FILE_SEPARATOR = File.separator;
    public static final String SERVER_PATH = "PhotoShareServer" + FILE_SEPARATOR;
    public static final String PASSWORD_FILE = SERVER_PATH + FILE_SEPARATOR + "passwords.txt";
    public static final String SSLKEYSTORE_FILE = "src" + FILE_SEPARATOR + "SSLServer.keyStore";
}
