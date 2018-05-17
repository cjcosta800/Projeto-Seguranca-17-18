import java.io.File;

public class ClientPaths {
    public static final String FILE_SEPARATOR = File.separator;
    public static final String CLIENT_PATH = "PhotoShareClient" + FILE_SEPARATOR;
    public static final String SSLTRUSTSTORE_FILE = "src" + FILE_SEPARATOR + "SSLClient.keyStore";
    public static final int BUFFER_SIZE = 16 * 1024;
}
