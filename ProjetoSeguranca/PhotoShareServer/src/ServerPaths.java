import java.io.File;

class ServerPaths {
    public static final String FILE_SEPARATOR = File.separator;
    public static final String SERVER_PATH = "PhotoShare" + File.separator;
    public static final String PASSWORD_FILE = SERVER_PATH + "passwords.txt";
    public static final String KEYSTORE_FILE = "src" + FILE_SEPARATOR + "KeyStore.jks";
    public static final String SSLKEYSTORE_FILE = "src" + FILE_SEPARATOR + "SSLServer.keyStore";
    public static final String TEMP_PASSWORD_FILE = SERVER_PATH + "temp_password.txt";
    public static final String ADMIN_PASSWORD = SERVER_PATH + "adminPass.txt";
    public static final int BUFFER_SIZE = 16 * 1024;
}
