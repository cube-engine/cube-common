package net.cube.engine;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author pluto
 * @date 2022/5/15
 */
public class TypeConverterManager {

    private static volatile TypeConverterManager INSTANCE;

    private volatile Map<String, TypeConverter<?>> registry = new ConcurrentHashMap<>(16);

    private TypeConverterManager() {
    }

    public static TypeConverterManager getInstance() {
        if (INSTANCE == null ) {
            synchronized (TypeConverterManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new TypeConverterManager();
                }
            }
        }
        return INSTANCE;
    }

    public TypeConverter<?> getConverter(String javaType) {
        return registry.getOrDefault(javaType, registry.get(Object.class.getName()));
    }

    public TypeConverter<?> getConverter(Class<?> javaTypeClazz) {
        return registry.getOrDefault(javaTypeClazz.getName(), registry.get(Object.class.getName()));
    }

    /**
     * A type converter converts to a source object to a target of type {@code T}.
     * <p>
     *     Implementations of this interface are thread-safe and can be shared.
     * </p>
     * @param <T>
     */
    public interface TypeConverter<T> {

        /**
         * Convert the source object to target type {@code T}.
         * @param origin the source object to convert
         * @return the converted object, which must be an instance of {@code T} (potentially {@code null})
         */
        T convert(Object origin);
    }

}
