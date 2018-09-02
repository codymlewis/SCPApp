import java.math.BigInteger;
import java.security.SecureRandom;
/**
 * Diff-Hellman Library
 *
 * @author Cody Lewis
 * @since 2018-08-28
 */
public class DH {
    /**
     * Generate a random prime at a specified length
     * @param bitLength the number of the bits long the prime should be
     * @return a random prime with length: bitLength
     */
    public static BigInteger genPrime(int bitLength) {
        BigInteger prime = BigInteger.probablePrime(bitLength, new SecureRandom());
        byte b[] = new byte[1];
        b[0] = 2;
        prime = prime.multiply(new BigInteger(b));
        prime = prime.add(BigInteger.ONE);
        return prime;
    }
    /**
     * Create the private key for Diffie Hellman
     * @param prime the published prime
     * @return the user's private key
     */
    public static BigInteger genPrivateKey(BigInteger prime) { // generate the private key
        BigInteger priKey = new BigInteger(prime.bitLength(), new SecureRandom());
        if(priKey.compareTo(BigInteger.ZERO) == 1 && priKey.compareTo(prime) == -1) {
            return priKey;
        }
        return genPrivateKey(prime);
    }
    /**
     * Create the public key for Diffie Hellman
     * @param priKey the user's private key
     * @param prime the published prime
     * @return a public key corresponding to the private key and prime
     */
    public static BigInteger genPublicKey(BigInteger priKey, BigInteger prime) {
        return genGenerator(prime).modPow(priKey, prime);
    }
    /**
     * Generate a primitive root of a prime
     * @param prime a prime
     * @return the primitive root of the prime
     */
    public static BigInteger genGenerator(BigInteger prime) {
        byte b[] = new byte[1];
        b[0] = 2;
        BigInteger generator = new BigInteger(b);
        while(!isPrimRoot(generator, prime) && generator.compareTo(prime) < 1) {
            generator = generator.add(BigInteger.ONE);
        }
        return generator;
    }
    /**
     * Check whether alpha is a primitive root of prime
     * @param alpha the possible primitive root
     * @param prime a prime
     * @return true if alpha is a primitive root of prime else false
     */
    public static boolean isPrimRoot(BigInteger alpha, BigInteger prime) {
        BigInteger phi = prime.subtract(BigInteger.ONE); // phi(prime) is == to prime - 1
        byte b[] = new byte[1];
        b[0] = 2;
        for(BigInteger i = new BigInteger(b); i.compareTo(phi) == -1; i = i.nextProbablePrime()) { // iterate by primes
            if((phi.mod(i)).compareTo(BigInteger.ZERO) != 0) { // check if i is a factor of phi
                continue;
            }
            if(alpha.modPow(phi.divide(i), prime).compareTo(BigInteger.ONE) == 0) { // if alpha^(phi/i) mod prime == 1 then it is not a primRoot
                return false;
            }
        }
        return true;
    }
    /**
     * Calculate the session key
     * @param pubKey the other user's public key
     * @param priKey the callers private key
     * @param prime the agreed prime
     * @return a session key corresponding to the inputs
     */
    public static BigInteger genSessionKey(BigInteger pubKey, BigInteger priKey, BigInteger prime) {
        return pubKey.modPow(priKey, prime);
    }
}
