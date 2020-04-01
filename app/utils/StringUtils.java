package utils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Random;

public class StringUtils {

    public static final String upper = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static final String lower = upper.toLowerCase(Locale.ROOT);
    public static final String digits = "0123456789";
    public static final String alphanum = upper + lower + digits;
    public static final Random random = new SecureRandom();

    public static String generateRandomString(int length) {
        if (length < 1) throw new IllegalArgumentException();

        char[] symbols = alphanum.toCharArray();
        char[] buf = new char[length];

        for (int idx = 0; idx < buf.length; ++idx)
            buf[idx] = symbols[random.nextInt(symbols.length)];
        return new String(buf);
    }

    public static String encodeUriComponent(String input) {
        String result;

        try {
            result = URLEncoder.encode(input, "UTF-8")
                .replaceAll("\\+", "%20")
                .replaceAll("\\%21", "!")
                .replaceAll("\\%27", "'")
                .replaceAll("\\%28", "(")
                .replaceAll("\\%29", ")")
                .replaceAll("\\%7E", "~");
        } catch (UnsupportedEncodingException e) {
            result = input;
        }

        return result;
    }

    public static String decodeURIComponent(String input) {
        if (input == null) {
            return null;
        }

        String result;

        try {
            result = URLDecoder.decode(input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            result = input;
        }

        return result;
  }

}
