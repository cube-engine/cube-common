package net.cube.engine;

import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

/**
 * @author pluto
 * @date 2022/5/31
 */
public class FileHelper {

    public static final byte[] JAR_MAGIC = {'P', 'K', 3, 4};

    public static final String PROTOCOL_FILE = "file";

    public static final String PROTOCOL_JAR = "jar";

    private FileHelper(){
    }

    public static boolean isJar(URL url) {
        String protocolName = url.getProtocol();
        if (PROTOCOL_JAR.equalsIgnoreCase(protocolName)) {
            return true;
        }
        byte[] buffer = new byte[JAR_MAGIC.length];
        try (InputStream is = url.openStream()) {
            is.read(buffer, 0, JAR_MAGIC.length);
            if (Arrays.equals(buffer, JAR_MAGIC)) {
                return true;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return false;
    }

}
