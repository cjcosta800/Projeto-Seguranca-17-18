import javax.crypto.*;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;

public class ServerSecurity {

    private static final int AES_KEY_SIZE_BITS = 128;
    private static final int AES_KEY_SIZE_BYTES = 16;
    private static final String SERVER_CERTIFICATE_ALIAS = "server";
    private final KeyStore kstore = KeyStore.getInstance("JKS");

    private KeyGenerator AESKeyGen;
    private String currentUser;

    public ServerSecurity(String currentUser) throws NoSuchAlgorithmException,
            KeyStoreException, IOException, CertificateException {
        this.currentUser = currentUser;
        this.AESKeyGen = KeyGenerator.getInstance("AES");
        this.kstore.load(new FileInputStream(ServerPaths.KEYSTORE_FILE), "grupo026".toCharArray());
        // tem de ser alterado para uma pw por arg
    }

    /**
     * Given a clear array and a filename (to get the cipher key), this will cipher the clear 'text'
     * @param clear
     * @param filename
     * @return ciphered text
     */
    public byte[] cipher(byte[] clear, String filename) {

        try {
            Key fileKey = loadKeyFromFile(filename);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, fileKey);

            return cipher.doFinal(clear);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Given a ciphered byte array and a file name (to get the key) it will decipher the
     * byte array
     * @param ciphered
     * @param filename
     * @return clear byte array
     */
    public byte[] decipher(byte[] ciphered, String filename) {

        try {
            Key fileKey = loadKeyFromFile(filename);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, fileKey);

            return cipher.doFinal(ciphered);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Generates an AES key and saves it on a filename.key
     * @param filename name of the file that will have a correspondent key file
     * @return
     */
    public void generateAESKey(String filename) {

        AESKeyGen.init(AES_KEY_SIZE_BITS);
        SecretKey key = AESKeyGen.generateKey();

        saveKey(key, filename);
    }

    /**
     * Saves a key to a filename.key (i.e. example.jpg.key)
     * @param key key to save
     * @param filename original file name
     * @throws IOException in case a file can't be created or written to
     */
    private void saveKey(SecretKey key, String filename)  {

        try {
            FileOutputStream keyFile = new FileOutputStream(ServerPaths.SERVER_PATH + ServerPaths.FILE_SEPARATOR +
                    currentUser + ServerPaths.FILE_SEPARATOR + filename + ".key");
            ObjectOutputStream writeKeyFile = new ObjectOutputStream(keyFile);

            Certificate serverCert = this.kstore.getCertificate(SERVER_CERTIFICATE_ALIAS);
            Cipher cipherPublicKey = Cipher.getInstance("RSA");
            cipherPublicKey.init(Cipher.WRAP_MODE, serverCert);
            byte[] wrapKey = cipherPublicKey.wrap(key);

            writeKeyFile.writeObject(wrapKey);
            writeKeyFile.close();
            keyFile.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        }
    }

    /**
     * Given a filename it will get the bytes from a filename.key file
     * @param filename
     * @return bytes from a filename.key file
     */
    private Key loadKeyFromFile(String filename) {
        FileInputStream kis;
        ObjectInputStream ois;
        byte[] key = new byte[AES_KEY_SIZE_BYTES];

        try {
            Key privateServerKey = kstore.getKey(SERVER_CERTIFICATE_ALIAS, "grupo026".toCharArray());

            Cipher unwrapper = Cipher.getInstance("RSA");
            unwrapper.init(Cipher.UNWRAP_MODE, privateServerKey);

            kis = new FileInputStream(ServerPaths.SERVER_PATH + ServerPaths.FILE_SEPARATOR +
                    currentUser + ServerPaths.FILE_SEPARATOR + filename + ".key");
            ois = new ObjectInputStream(kis);
            byte[] keyEncoded = new byte[256];
            keyEncoded = (byte[]) ois.readObject();
            kis.close();
            ois.close();

            return unwrapper.unwrap(keyEncoded, "AES", Cipher.SECRET_KEY);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }


}
