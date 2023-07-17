package sk.stuba.fei.uim.vsa.pr2;

import at.favre.lib.crypto.bcrypt.BCrypt;
import at.favre.lib.crypto.bcrypt.LongPasswordStrategies;

public class BCryptService {

    public static final int BCRYPT_COST = 13;
    public static final BCrypt.Version VERSION = BCrypt.Version.VERSION_2A;

    public static String hash(String value) {
        return BCrypt.with(LongPasswordStrategies.hashSha512(VERSION)).hashToString(BCRYPT_COST, value.toCharArray());
    }

    public static boolean verify(String value, String hash) {
        return BCrypt.verifyer(VERSION, LongPasswordStrategies.hashSha512(VERSION)).verifyStrict(value.toCharArray(), hash.toCharArray()).verified;
    }

}
