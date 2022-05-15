package net.cube.engine;

import static net.cube.engine.Constant.CLOSE_BRACE;
import static net.cube.engine.Constant.OPEN_BRACE;

/**
 * @author pluto
 * @date 2022/5/15
 */
public class PlaceholderHelper {

    /**
     *
     * @param text
     * @param handler
     * @return
     */
    public String parse(String text, PlaceholderTokenHandler handler) {
        return parse(text, OPEN_BRACE, CLOSE_BRACE, handler);
    }

    /**
     *
     * @param text
     * @param beginSymbol
     * @param endSymbol
     * @param handler
     * @return
     */
    public String parse(String text, String beginSymbol, String endSymbol, PlaceholderTokenHandler handler) {
        StringBuilder buffer = new StringBuilder();
        if (text != null) {
            int start;
            String result = text;
            do {
                start = result.indexOf(beginSymbol);
                int end = result.indexOf(endSymbol);
                if ((end > start) && (start != -1)) {
                    buffer.append(result.substring(0, start));
                    String content = result.substring(start + beginSymbol.length(), end);
                    buffer.append(handler.handleToken(content));
                    result = result.substring(end + endSymbol.length());
                } else {
                    if (end <= -1) {
                        break;
                    }
                    buffer.append(result.substring(0, end)).append(endSymbol);
                    result = result.substring(end + endSymbol.length());
                }
            } while (start > -1);
            buffer.append(result);
        }
        return buffer.toString();
    }

    /**
     * 占位符处理器
     * <p>
     *     用于占位符解析后的处理
     *     通过{@link PlaceholderHelper}解析出占位符后，如果需要对占位符进行后续处理，需要用到此接口的实现。
     * </p>
     */
    public interface PlaceholderTokenHandler {

        /**
         *
         * @param token
         * @return
         */
        Object handleToken(String token);

    }

}
