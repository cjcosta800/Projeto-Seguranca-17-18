import javax.crypto.*;
import javax.crypto.spec.PBEKeySpec;
import java.io.*;
import java.security.*;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;

public class ServerSecurity {

    private static final int AES_KEY_SIZE_BITS = 128;
    private static final int AES_KEY_SIZE_BYTES = 16;
    private static final String SERVER_CERTIFICATE_ALIAS = "server";
    private final KeyStore kstore = KeyStore.getInstance("JKS");
    private static final int NUMBER_OF_ITERATIONS = 20;
    private static final Random RANDOM = new SecureRandom();

    private KeyGenerator AESKeyGen;
    private String currentUser;
    private String keyStoresPassword;

    public ServerSecurity(String currentUser, String keyStoresPassword) throws NoSuchAlgorithmException,
            KeyStoreException, IOException, CertificateException {
        this.currentUser = currentUser;
        this.keyStoresPassword = keyStoresPassword;
        this.AESKeyGen = KeyGenerator.getInstance("AES");
        this.kstore.load(new FileInputStream(ServerPaths.KEYSTORE_FILE), keyStoresPassword.toCharArray());
        // tem de ser alterado para uma pw por arg
    }

    /**
     * Given a clear array and a filename (to get the cipher key), this will cipher the clear 'text'
     * @param clear
     * @param filename
     * @return ciphered text
     */
    public byte[] cipher(byte[] clear, String userPath,String filename) {

        try {
            Cipher cipher = getFileCipher(userPath, filename);

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
     *
     * @param filename
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public Cipher getFileCipher(String userPath, String filename) throws
            InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException {
        Key fileKey = loadKeyFromFile(userPath, filename);

        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, fileKey);
        return cipher;
    }

    /**
     *
     * @param filename
     * @return
     * @throws InvalidKeyException
     * @throws NoSuchPaddingException
     * @throws NoSuchAlgorithmException
     */
    public Cipher getFileDecipher(String userPath, String filename) throws
            InvalidKeyException, NoSuchPaddingException, NoSuchAlgorithmException{
        Key fileKey = loadKeyFromFile(userPath, filename);

        Cipher decipher = Cipher.getInstance("AES");
        decipher.init(Cipher.DECRYPT_MODE, fileKey);
        return decipher;
    }

    /**
     * Given a ciphered byte array and a file name (to get the key) it will decipher the
     * byte array
     * @param ciphered
     * @param filename
     * @return clear byte array
     */
    public byte[] decipher(byte[] ciphered, String userPath, String filename) {

        try {
            Cipher cipher = getFileDecipher(userPath, filename);

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
    private Key loadKeyFromFile(String userPath, String filename) {
        FileInputStream kis;
        ObjectInputStream ois;
        byte[] key = new byte[AES_KEY_SIZE_BYTES];

        try {
            Key privateServerKey = kstore.getKey(SERVER_CERTIFICATE_ALIAS, keyStoresPassword.toCharArray());

            Cipher unwrapper = Cipher.getInstance("RSA");
            unwrapper.init(Cipher.UNWRAP_MODE, privateServerKey);

            kis = new FileInputStream(userPath + filename + ".key");
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

    /**
     *
     * @param filename
     */
    public byte[] signFile(byte[] toSign, String filename) {
        try {
            PrivateKey privateServerKey = (PrivateKey) kstore.getKey(SERVER_CERTIFICATE_ALIAS, keyStoresPassword.toCharArray());
            Signature sign = Signature.getInstance("SHA1withRSA");
            sign.initSign(privateServerKey);

            String filePath = ServerPaths.SERVER_PATH + currentUser +
                    ServerPaths.FILE_SEPARATOR + filename;

            sign.update(toSign);

            return sign.sign();

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnrecoverableKeyException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param signature
     */
    public void saveSign(byte[] signature, String filename) {
        String filePath = ServerPaths.SERVER_PATH + currentUser +
                ServerPaths.FILE_SEPARATOR + filename + ".sig";

        try {
            FileOutputStream fos = new FileOutputStream(new File(filePath));
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(signature);
            oos.flush();
            oos.close();
            fos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     *
     * @param filename
     * @return
     */
    public byte[] loadSign(String userPath, String filename) {
        String filePath = userPath + filename + ".sig";

        try {
            FileInputStream fis = new FileInputStream(new File (filePath));
            ObjectInputStream ois = new ObjectInputStream(fis);

            byte[] result = (byte[]) ois.readObject();
            ois.close();
            fis.close();

            return result;

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     *
     * @param filename
     * @param data
     * @return
     */
    public boolean verifySignature(String filename, String userPath, byte[] data) {
        try {
            Certificate cert = kstore.getCertificate(SERVER_CERTIFICATE_ALIAS);
            PublicKey publicKey = cert.getPublicKey();

            Signature sign = Signature.getInstance("SHA1withRSA");
            sign.initVerify(publicKey);

            byte[] signature = loadSign(userPath, filename);
            sign.update(data);

            return sign.verify(signature);

        } catch (KeyStoreException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (SignatureException e) {
            e.printStackTrace();
        }

        return false;
    }

    public static SecretKey secretKeyGenerator(String password, byte[] salt) {
        try {
            PBEKeySpec passSpec = new PBEKeySpec(password.toCharArray(), salt, NUMBER_OF_ITERATIONS);
            SecretKeyFactory secPass = SecretKeyFactory.getInstance("PBEWithHmacSHA256AndAES_128");
            return secPass.generateSecret(passSpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new AssertionError("Error hashing the password: " + e.getMessage(), e);
        }
    }

    //necessario tratar de possiveis erros a fazer o salted hash?private
    public static byte[] getSalt() {

        byte[] salt = new byte[32];
        RANDOM.nextBytes(salt);
        return salt;
    }

    public static byte[] getMac(SecretKey key, byte[] passByte){

        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(key);
            mac.update(passByte);
            return mac.doFinal();
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new AssertionError("Error creating MAC" + e.getMessage(), e);
        }
    }

    public static boolean compareMac(byte[] mac, byte[] otherMac) {

        if (mac.length != otherMac.length)
            return false;

        int count = 0;

        while (count < mac.length) {
            if (mac[count] != otherMac[count])
                return false;
            count++;
        }
        return true;
    }


    public CipherOutputStream createCipherOutputStream(FileOutputStream fos, String userPath, String photoName)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = getFileCipher(userPath, photoName);
        return new CipherOutputStream(fos, cipher);
    }

    public CipherInputStream createCipherInputStream(FileInputStream fis, String userPath, String photoName)
            throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = getFileDecipher(userPath, photoName);
        return new CipherInputStream(fis, cipher);
    }
}
