import java.util.Base64;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
/**
 * AES encryption wrapper library
 *
 * @author Cody Lewis
 * @since 2018-08-29
 */
public class AES {
    private static final String CIPHER_ALGORITHM = "AES/ECB/PKCS5Padding";
    /**
     * Hash the input key
     * @param key a secret key
     * @return the hash of the secret key
     */
    private static byte[] hashKey(byte[] key) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(key);
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            return new byte[0];
        }
    }
    /**
     * Perform AES encryption
     * @param sessionKey a secret key
     * @param message the plaintext
     * @return cipher-text of the message
     */
    public static String[] encrypt(byte[] sessionKey, String message) {
        try {
            // set up cipher
            Cipher c = Cipher.getInstance(CIPHER_ALGORITHM);
            SecretKeySpec secret = new SecretKeySpec(hashKey(sessionKey), "AES");
            c.init(c.ENCRYPT_MODE, secret);
            // produce ciphertext
            int len = message.length() / 16 + ((message.length() % 16 == 0) ? 0 : 1);
            String[] ciphertext = new String[len];
            String[] result = new String[len];
            for(int i = 0; i < len; ++i) {
                ciphertext[i] = (i + 1) * 16 > message.length() ? message.substring(i * 16) : message.substring(i * 16, (i + 1) * 16);
                result[i] = Base64.getEncoder().encodeToString(c.doFinal(ciphertext[i].getBytes("UTF-8")));
            }
            return result;
        } catch(Exception e) {
            System.err.println("Error: " + e.getMessage());
            return new String[0];
        }
    }
    /**
     * Perform AES decryption
     * @param sessionKey a secret key
     * @param ciphertext some AES encrypted text
     * @return the plain-text
     */
    public static String decrypt(byte[] sessionKey, String ciphertext) {
        try {
            // set up cipher
            Cipher c = Cipher.getInstance(CIPHER_ALGORITHM);
            SecretKeySpec secret = new SecretKeySpec(hashKey(sessionKey), "AES");
            c.init(c.DECRYPT_MODE, secret);
            // produce plaintext
            return new String(c.doFinal(Base64.getDecoder().decode(ciphertext.substring(0, ciphertext.indexOf("\n") == -1 ? ciphertext.length() : ciphertext.indexOf("\n")))));
        } catch(Exception e) {
            System.err.println("Error: " + e.getMessage());
            return new String();
        }
    }
}