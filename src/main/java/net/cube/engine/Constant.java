package net.cube.engine;

/**
 * @author pluto
 * @date 2022/5/15
 */
public interface Constant {

    String DOT = "\\.";

    String OPEN_BRACE = "{";

    String CLOSE_BRACE = "}";

    String OPEN_BRACKET = "[";

    String CLOSE_BRACKET = "]";

    String FILE_PATH = Constant.class.getPackage().getName().replace(".", "/");

    String MANIFEST_ENTRY_NAME = "Engine-Module";

}
