import java.io.File;

class ServerPaths {
    public static final String FILE_SEPARATOR = File.separator;
    public static final String SERVER_PATH = "PhotoShare" + FILE_SEPARATOR;
    public static final String PASSWORD_FILE = SERVER_PATH + "passwords.txt";
    public static final String TEMP_PASSWORD_FILE = SERVER_PATH + "temp_password.txt";
    public static final String ADMIN_FILE = SERVER_PATH + "Utils" + FILE_SEPARATOR + "adminPass.txt";
    public static final String KEYSTORE_FILE = SERVER_PATH + "KeyStore.jks";
    public static final String SSLKEYSTORE_FILE = "src" + FILE_SEPARATOR + "SSLServer.keyStore";
}
