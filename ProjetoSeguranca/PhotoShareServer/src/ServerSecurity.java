import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ServerSecurity {

    private static final int AES_KEY_SIZE = 128;
    private static final String CIPHER_AES_TRANSFORMATION = "AES";

    private KeyGenerator AESKeyGen;
    private String currentUser;

    public ServerSecurity(String currentUser) throws NoSuchAlgorithmException {
        this.currentUser = currentUser;
        this.AESKeyGen = KeyGenerator.getInstance("AES");
    }

    public Cipher getCipher(String filename) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, IOException {

        Cipher c = Cipher.getInstance(CIPHER_AES_TRANSFORMATION);
        SecretKey generatedKey = generateKey();
        c.init(Cipher.ENCRYPT_MODE, generatedKey);

        saveKey(generatedKey, filename);

        return c;
    }

    public Cipher getDecipher(String filename) {
        // TODO
        return null;
    }

    /**
     *
     * @return
     */
    private SecretKey generateKey() {
        AESKeyGen.init(AES_KEY_SIZE);
        return AESKeyGen.generateKey();
    }

    /**
     * Saves a key to a filename.key (i.e. example.jpg.key)
     * TODO implement hybrid cryptography to save the file with server's public key
     * @param key key to save
     * @param filename original file name
     * @throws IOException in case a file can't be created or written to
     */
    private void saveKey(SecretKey key, String filename) throws IOException {
        byte[] keyEncoded = key.getEncoded();
        FileOutputStream keyFile = new FileOutputStream(ServerPaths.SERVER_PATH + ServerPaths.FILE_SEPARATOR +
                currentUser + ServerPaths.FILE_SEPARATOR + filename + ".key");
        ObjectOutputStream writeKeyFile = new ObjectOutputStream(keyFile);
        writeKeyFile.write(keyEncoded);
        writeKeyFile.close();
    }


}
