package utils;

import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

public class RandomStringGenerator {

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String lower = upper.toLowerCase(Locale.ROOT);
    public static final String digits = "0123456789";
    public static final String alphanum = upper + lower + digits;
    public static final Random random = new SecureRandom();

    public static String generate(int length) {
        if (length < 1) throw new IllegalArgumentException();

        char[] symbols = alphanum.toCharArray();
        char[] buf = new char[length];

        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

}
