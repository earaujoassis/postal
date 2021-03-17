package utils;

import java.net.URL;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.net.MalformedURLException;
import java.io.BufferedReader;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Map;
import java.util.HashMap;
import java.util.StringJoiner;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHelper {

    private final static Logger logger = LoggerFactory.getLogger(HttpHelper.class);

    public static String doPost(String _url, Map<String, String> header, Map<String, String> formData)
    throws HttpHelperException {
        StringBuffer content = new StringBuffer();
        byte[] output;
        int outputLength;
        URL url;
        HttpURLConnection conn;
        BufferedReader input;
        String inputLine;
        int status;

        logger.info(String.format("Attempting connection to %s", _url));
        try {
            output = urlEncodedForm(formData);
            outputLength = output.length;
        } catch (UnsupportedEncodingException err) {
            throw new HttpHelperException("error processing data form: unsupported encoding");
        }

        try {
            url = new URL(_url);
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(false);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");

            for (Map.Entry<String,String> entry : header.entrySet()) {
                logger.info(String.format("Setting header[%s]=%s", entry.getKey(), entry.getValue()));
                conn.setRequestProperty(entry.getKey(), entry.getValue());
            }

            conn.setFixedLengthStreamingMode(outputLength);
            conn.setConnectTimeout(2500);
            conn.setReadTimeout(2500);
            conn.connect();
        } catch (MalformedURLException err) {
            err.printStackTrace();
            throw new HttpHelperException("malformed URL for connection");
        } catch (IOException err) {
            err.printStackTrace();
            throw new HttpHelperException("could not create a connection from URL");
        }

        try (OutputStream outputStream = conn.getOutputStream()) {
            logger.info(String.format("Sending data form: %s", new String(output)));
            outputStream.write(output);
            outputStream.flush();
            outputStream.close();
        } catch (IOException err) {
            err.printStackTrace();
            throw new HttpHelperException("could not write POST form");
        }

        try {
            status = conn.getResponseCode();
            if (status != 200) {
                throw new HttpHelperException("HTTP response is not OK: " + conn.getResponseMessage());
            }
            input = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            while ((inputLine = input.readLine()) != null) {
                content.append(inputLine);
            }
            input.close();
            conn.disconnect();
        } catch (IOException err) {
            err.printStackTrace();
            throw new HttpHelperException("HTTP connection failed");
        }

        return content.toString();
    }

    private static byte[] urlEncodedForm(Map<String, String> arguments)
    throws UnsupportedEncodingException {
        StringJoiner sj = new StringJoiner("&");

        for (Map.Entry<String,String> entry : arguments.entrySet()) {
            sj.add(entry.getKey() + "=" + entry.getValue());
        }

        return sj.toString().getBytes(StandardCharsets.UTF_8);
    }

}
