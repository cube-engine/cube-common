package net.cube.engine;

import static net.cube.engine.Constant.DOT;

/**
 * @author pluto
 * @date 2022/5/15
 */
public class KeyGenHelper {

    public static String genObjectPath(String key, String... suffix) {
        StringBuilder keyBuff = new StringBuilder(
                key == null || "".equals(key) ? "" : key
        );
        if (suffix == null || suffix.length == 0) {
            return keyBuff.toString();
        }
        for (String sfx : suffix) {
            if (sfx == null || "".equals(sfx)) {
                continue;
            }
            if (keyBuff.length() > 0) {
                keyBuff.append(DOT);
            }
            keyBuff.append(sfx);
        }
        return keyBuff.toString();
    }

    public static String genCamelKey(String... index) {
        if (index == null || index.length == 0) {
            return null;
        }
        StringBuilder buff = new StringBuilder(16);
        for (int i = 0; i < index.length; i++) {
            String v = index[0];
            if (v == null || "".equals(v)) {
                continue ;
            }
            buff.append(toFirstUpperCase(index[i]));
        }
        return toFirstLowerCase(buff.toString());
    }

    private static String toFirstLowerCase(String name) {
        return name == null ? null : "".equals(name) ? "" : name.substring(0, 1).toLowerCase().concat(name.substring(1));
    }

    private static String toFirstUpperCase(String name) {
        return name == null ? null : "".equals(name) ? "" : name.substring(0, 1).toUpperCase().concat(name.substring(1));
    }

}
