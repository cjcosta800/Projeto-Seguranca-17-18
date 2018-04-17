import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class ServerSecurity {

    private static final int AES_KEY_SIZE_BITS = 128;
    private static final int AES_KEY_SIZE_BYTES = 16;

    private KeyGenerator AESKeyGen;
    private String currentUser;

    public ServerSecurity(String currentUser) throws NoSuchAlgorithmException {
        this.currentUser = currentUser;
        this.AESKeyGen = KeyGenerator.getInstance("AES");
    }

    /**
     * Given a clear array and a filename (to get the cipher key), this will cipher the clear 'text'
     * @param clear
     * @param filename
     * @return ciphered text
     */
    public byte[] cipher(byte[] clear, String filename) throws NoSuchPaddingException,
            NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {

        byte[] keyByted = getByteKeyFromFile(filename);
        SecretKeySpec keySpec = new SecretKeySpec(keyByted, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        return cipher.doFinal(clear);
    }

    /**
     * Given a ciphered byte array and a file name (to get the key) it will decipher the
     * byte array
     * @param ciphered
     * @param filename
     * @return clear byte array
     */
    public byte[] decipher(byte[] ciphered, String filename) throws BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {

        byte[] keyByted = getByteKeyFromFile(filename);
        SecretKeySpec keySpec = new SecretKeySpec(keyByted, "AES");

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        return cipher.doFinal(ciphered);
    }

    /**
     * Generates an AES key and saves it on a filename.key
     * @param filename name of the file that will have a correspondent key file
     * @return
     */
    public void generateAESKey(String filename) throws IOException {
        AESKeyGen.init(AES_KEY_SIZE_BITS);
        SecretKey key = AESKeyGen.generateKey();

        saveKey(key, filename);
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

    /**
     * Given a filename it will get the bytes from a filename.key file
     * @param filename
     * @return bytes from a filename.key file
     */
    private byte[] getByteKeyFromFile(String filename) {
        FileInputStream kis;
        ObjectInputStream ois;
        byte[] key = new byte[AES_KEY_SIZE_BYTES];
        int resultFromRead;

        try {
            kis = new FileInputStream(ServerPaths.SERVER_PATH + ServerPaths.FILE_SEPARATOR +
                    currentUser + ServerPaths.FILE_SEPARATOR + filename + ".key");
            ois = new ObjectInputStream(kis);

            resultFromRead = ois.read(key);
            if(resultFromRead == -1) {
                throw new IOException("Error while reading key from file (read = -1)");
            }

            ois.close();

            return key;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }


}
