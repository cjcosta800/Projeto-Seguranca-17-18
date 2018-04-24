import java.io.File;

class ServerPaths {
    public static final String FILE_SEPARATOR = File.separator;
    public static final String SERVER_PATH = "PhotoShare" + File.separator;
    public static final String PASSWORD_FILE = SERVER_PATH + "passwords.txt";
    public static final String KEYSTORE_FILE = SERVER_PATH + "KeyStore.jks";
    public static final String SSLKEYSTORE_FILE = "src" + FILE_SEPARATOR + "SSLServer.keyStore";
}
