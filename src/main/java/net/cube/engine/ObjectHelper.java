package net.cube.engine;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import static net.cube.engine.Constant.*;

/**
 * @author pluto
 * @date 2022/5/15
 */
public class ObjectHelper {

    private static final Pattern ARRAY_PATH_PATTERN = Pattern.compile("\\w+(\\[(\\d+|\\-[1])\\])$");

    private static final Pattern ARRAY_INDEX_PATTERN = Pattern.compile("^\\d+$");

    private ObjectHelper() {
    }

    public static void setProperty(Object obj, String path, Object value, boolean createWhenParentNotExist) {
        if (obj == null) {
            throw new IllegalArgumentException("Input object is null.");
        }
        List<String> pathStack = new LinkedList<>();
        String[] pathArray = path.split(DOT);
        if (pathArray.length == 0) {
            pathStack.add(path);
        } else {
            pathStack.addAll(Arrays.asList(pathArray));
        }
        Object o = obj;
        String entry = pathStack.get(0);
        int i = 0;
        while (i < pathStack.size() - 1 && o != null) {
            entry = pathStack.get(i++);
            Object _o;
            if (isArrayPath(entry)) {
                String parent = getArrayParentPath(entry);
                _o = getProperty1(o, parent);
                setProperty2(o, _o, createWhenParentNotExist, path, parent);
            }
            _o = getProperty1(o, entry);
            if (_o == null && createWhenParentNotExist) {
                _o = new HashMap<>(16);
                setProperty1(o, entry, _o);
            }
            o = _o;
        }
        if (o == null) {
            throw new NoSuchElementException(entry + " not exist.");
        }
        entry = pathStack.get(pathStack.size() - 1);
        if (isArrayPath(entry)) {
            String parent = getArrayParentPath(entry);
            Object parentValue = getProperty1(o, parent);
            setProperty2(o, parentValue, createWhenParentNotExist, entry, parent);
        }
        setProperty1(o, entry, value);
    }

    public static void setProperty(Object obj, String path, Object value, Class<?> valueType) {
        Object convertedValue = TypeConverterManager.getInstance().getConverter(valueType.getName()).convert(value);
        setProperty(obj, path, convertedValue, true);
    }

    public static void setProperty(Object obj, String path, Object value) {
        setProperty(obj, path, value, true);
    }

    public static Object getProperty(Object obj, String path) {
        List<String> pathStack = new LinkedList<>();
        String[] pathArray = path.split(DOT);
        if (pathArray.length == 0) {
            pathStack.add(path);
        } else {
            pathStack.addAll(Arrays.asList(pathArray));
        }
        Object o = obj;
        while (!pathStack.isEmpty() && o != null) {
            String name = pathStack.remove(0);
            o = getProperty1(o, name);
        }
        return o;
    }

    public static <T> T getProperty(Object obj, String path, Class<T> clazz) {
        return getProperty(obj, path, clazz.getName());
    }

    public static <T> T getProperty(Object obj, String path, String nativeTypeName) {
        Object value = getProperty(obj, path);
        if (value == null) {
            return null;
        }
        return (T)TypeConverterManager.getInstance().getConverter(nativeTypeName).convert(value);
    }

    public static String getParentPath(String path) {
        int lastDotIndex = path.lastIndexOf(DOT);
        if (lastDotIndex <= 0) {
            return "";
        }
        return path.substring(0, lastDotIndex);
    }

    @Deprecated
    public static Field findField(Class<?> clazz, String path) throws Exception {
        Field field;
        try {
            field = clazz.getDeclaredField(path);
        } catch (NoSuchFieldException e) {
            Class<?> superClazz = clazz.getSuperclass();
            do {
                try {
                    field = superClazz.getDeclaredField(path);
                } catch (NoSuchFieldException ex) {
                    field = null;
                }
                if (field != null) {
                    int modifiers = field.getModifiers();
                    boolean valid = Modifier.isProtected(modifiers) ||
                            Modifier.isPublic(modifiers);
                    if (valid) {
                        break ;
                    }
                }
            } while (!Object.class.equals(superClazz));
        }
        if (field == null) {
            throw new NoSuchFieldException("No such field named [" + path + "] in " + clazz.getName());
        }
        return field;
    }

    private static Method findGetterMethod(Class<?> clazz, String path) throws Exception {
        String methodName = KeyGenHelper.genCamelKey("get", path);
        Method method;
        try {
            method = clazz.getDeclaredMethod(methodName);
        } catch (NoSuchMethodException e) {
            Class<?> superClazz = clazz.getSuperclass();
            do {
                try {
                    method = superClazz.getDeclaredMethod(methodName);
                } catch (NoSuchMethodException ex) {
                    method = null;
                }
                if (method != null) {
                    int modifiers = method.getModifiers();
                    boolean valid = Modifier.isProtected(modifiers) ||
                            Modifier.isPublic(modifiers);
                    if (valid) {
                        break ;
                    }
                }
            } while (!Object.class.equals(superClazz));
        }
        if (method == null) {
            throw new NoSuchMethodException("No such getter named [" + methodName + "] in " + clazz.getName());
        }
        return method;
    }

    public static Method findSetterMethod(Class<?> clazz, String path) throws Exception {
        String methodName = KeyGenHelper.genCamelKey("set", path);
        Method method = null;
        do {
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method declaredMethod : declaredMethods) {
                if (declaredMethod.getName().equals(methodName)) {
                    method = declaredMethod;
                    break;
                }
            }
            if (method != null) {
                int modifiers = method.getModifiers();
                boolean valid = Modifier.isProtected(modifiers) ||
                        Modifier.isPublic(modifiers);
                if (valid) {
                    break ;
                }
            }
            clazz = clazz.getSuperclass();
        } while (!Object.class.equals(clazz));
        if (method == null) {
            throw new NoSuchMethodException("No such getter named [" + methodName + "] in " + clazz.getName());
        }
        return method;
    }

    public static boolean isArray(Object obj) {
        return obj != null && obj.getClass().isArray();
    }

    public static boolean isCollection(Object obj) {
        return obj instanceof Collection;
    }

    @SuppressWarnings("unchecked")
    public static <T> T toObject(Object obj, Class<T> clazz) {
        return clazz.isInstance(obj) ? (T)obj :
                (T)TypeConverterManager.getInstance().getConverter(clazz.getName()).convert(obj);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> toCollection(Object obj) {
        return (Collection<T>) toCollection(obj, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> Collection<T> toCollection(Object obj, Class<T> clazz) {
        Collection<T> c;
        if (isArray(obj)) {
            Class<?> cClazz = ((Object[])obj).getClass().getComponentType();
            c = new LinkedList<>();
            int len = ((Object[])obj).length;
            for (int i = 0; i < len; i++) {
                Object item = ((Object[]) obj)[i];
                c.add(
                        item == null ?
                                null :
                                (
                                        clazz.equals(cClazz) || clazz.isAssignableFrom(cClazz) ?
                                                (T) item :
                                                (T) TypeConverterManager.getInstance().getConverter(clazz.getName()).convert(item)
                                )
                );
            }
        } else if (isCollection(obj)) {
            Class<?> cClazz = ((Collection)obj).getClass().getComponentType();
            if (cClazz == null) {
                cClazz = Object.class;
            }
            if (clazz.equals(cClazz) || clazz.isAssignableFrom(cClazz)) {
                c = (Collection<T>) obj;
            } else {
                c = new LinkedList<>();
                for (Object item : (Collection<Object>)obj) {
                    c.add(
                            item == null ?
                                    null :
                                    (T) TypeConverterManager.getInstance().getConverter(clazz.getName()).convert(item)
                    );
                }
            }
        } else {
            Class<?> cClazz;
            if (obj == null) {
                c = new LinkedList<>();
            } else {
                cClazz = obj.getClass();
                c = new LinkedList<>();
                if (cClazz.equals(clazz) || clazz.isAssignableFrom(cClazz)) {
                    c.add((T)obj);
                } else {
                    c.add((T) TypeConverterManager.getInstance().getConverter(clazz.getName()).convert(obj));
                }
            }
        }
        return c;
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Object obj) {
        return (T[])toArray(obj, Object.class);
    }

    @SuppressWarnings("unchecked")
    public static <T> T[] toArray(Object obj, Class<T> clazz) {
        T[] a;
        if (isArray(obj)) {
            Class<?> aClazz = ((Object[])obj).getClass().getComponentType();
            if (clazz.equals(aClazz) || clazz.isAssignableFrom(aClazz)) {
                a = (T[])obj;
            } else {
                int len = ((Object[])obj).length;
                a = (T[]) Array.newInstance(clazz, len);
                for (int i = 0; i < len; i++) {
                    a[i] = ((Object[])obj)[i] == null ?
                            null :
                            (T) TypeConverterManager.getInstance().getConverter(clazz.getName()).convert(((Object[])obj)[i]);
                }
            }
        } else if (isCollection(obj)) {
            Class<?> aClazz = ((Collection)obj).getClass().getComponentType();
            if (aClazz == null) {
                aClazz = Object.class;
            }
            a = (T[])Array.newInstance(clazz, ((Collection)obj).size());
            Iterator<?> iter = ((Collection)obj).iterator();
            int i = 0;
            if (clazz.equals(aClazz) || clazz.isAssignableFrom(aClazz)) {
                while (iter.hasNext()) {
                    a[i++] = (T)iter.next();
                }
            } else {
                while (iter.hasNext()) {
                    Object item = iter.next();
                    a[i++] = item == null ? null :
                            (T) TypeConverterManager.getInstance().getConverter(clazz.getName()).convert(item);
                }
            }
        } else {
            Class<?> aClazz;
            if (obj == null) {
                a = (T[])Array.newInstance(clazz, 0);
            } else {
                aClazz = obj.getClass();
                a = (T[])Array.newInstance(clazz, 1);
                if (aClazz.equals(clazz) || clazz.isAssignableFrom(aClazz)) {
                    a[0] = (T)obj;
                } else {
                    a[0] = (T) TypeConverterManager.getInstance().getConverter(clazz.getName()).convert(obj);
                }
            }
        }
        return a;
    }

    @SuppressWarnings("unchecked")
    private static Object getProperty1(Object obj, String path) {
        Object value;
        if (isArrayPath(path)) {
            String parent = getArrayParentPath(path);
            Object parentValue = getProperty1(obj, parent);
            Collection wrapper;
            if (parentValue == null) {
                return null;
            } else if (isArray(parentValue)) {
                wrapper = Arrays.asList((Object[])parentValue);
            } else if (isCollection(parentValue)) {
                wrapper = (Collection)parentValue;
            } else {
                throw new RuntimeException(
                        "Illegal data type. Expect Array or Collection, actual "
                        + parentValue.getClass().getName()
                );
            }
            int index = getIndexFromArrayPath(path);
            if (index == -1) {
                index = wrapper.size();
            }
            if (index > wrapper.size()) {
                throw new ArrayIndexOutOfBoundsException(
                        "Index " + index + " is out of the bound " + wrapper.size()
                );
            }
            if (index == wrapper.size()) {
                wrapper.add(new HashMap<>(16));
            }
            List<Object> wrapperNew = new LinkedList<>(wrapper);
            value = wrapperNew.get(index);
        } else {
            if (isArray(obj)) {
                value = getMultiObjectProperty((Object[])obj, path);
            } else if (isCollection(obj)) {
                Collection<?> collection = (Collection<?>) obj;
                value = getMultiObjectProperty(collection.toArray(), path);
            } else {
                value = getSingleObjectProperty(obj, path);
            }
        }
        return value;
    }

    private static Object[] getMultiObjectProperty(Object[] objects, String path) {
        Object[] values = new Object[objects.length];
        try {
            for (int i = 0; i < objects.length; i++) {
                values[i] = getSingleObjectProperty(objects[i], path);
            }
        } catch (Exception e) {
            values = null;
        }
        return values;
    }

    private static Object getSingleObjectProperty(Object obj, String path) {
        Object value;
        if (obj instanceof Map) {
            value = ((Map)obj).get(path);
        } else {
            try {
                Method getter = findGetterMethod(obj.getClass(), path);
                value = getter.invoke(obj);
            } catch (Exception e) {
                value = null;
            }
            return value;
        }
        return value;
    }

    private static void setProperty1(Object obj, String path, Object value) {
        if (isArrayPath(path)) {
            String parent = getArrayParentPath(path);
            Object parentValue = getProperty1(obj, parent);
            List opList;
            boolean neededToArray = false;
            if (parentValue == null) {
                throw new NoSuchElementException("Illegal path " + path + ". Value of parent path not exist");
            }
            if (isArray(parentValue)) {
                opList = new LinkedList<>(Arrays.asList(parent));
                neededToArray = true;
            } else if (isCollection(parentValue)) {
                opList = new LinkedList<>((Collection)parentValue);
            } else {
                throw new RuntimeException("Illegal type of parent.");
            }
            int index = getIndexFromArrayPath(path);
            if (index == -1) {
                index = ((List)parentValue).size();
            }
            if (index > opList.size()) {
                throw new ArrayIndexOutOfBoundsException(
                        "Index " + index + " is out of the bound " + opList.size()
                );
            }
            if (index < opList.size()) {
                opList.remove(index);
            }
            opList.add(index, value);
            if (neededToArray) {
                value = opList.toArray();
            } else {
                value = opList;
            }
            path = parent;
        }
        if (obj instanceof Map) {
            ((Map)obj).put(path, value);
        } else {
            try {
                Method setter = findSetterMethod(obj.getClass(), path);
                if (setter == null) {
                    throw new NoSuchMethodException();
                }
                setter.invoke(obj, value);
            } catch (Exception e) {
                throw new RuntimeException("Path[" + path + "] set error.", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static void setProperty2(Object obj, Object parentValue, boolean createWhenParentNotExist,
                                     String path, String parent) {
        if (parentValue == null && createWhenParentNotExist) {
            parentValue = new LinkedList<>();
            int index = getIndexFromArrayPath(path);
            if (index == -1) {
                index = 0;
            }
            if (index > 0) {
                for (int j = 0; j < index; j++) {
                    ((List)parentValue).add(j, new HashMap<>(16));
                }
            }
            setProperty1(obj, parent, parentValue);
        }
    }

    private static boolean isArrayPath(String path) {
        return path != null && !"".equals(path) && ARRAY_PATH_PATTERN.matcher(path).matches();
    }

    private static String getArrayParentPath(String arrayPath) {
        int obIndex = arrayPath.indexOf(OPEN_BRACKET);
        return arrayPath.substring(0, obIndex);
    }

    private static int getIndexFromArrayPath(String arrayPath) {
        if (isArrayPath(arrayPath)) {
            int obIndex = arrayPath.indexOf(OPEN_BRACKET);
            int cbIndex = arrayPath.indexOf(CLOSE_BRACKET);
            String arrayIndex = arrayPath.substring(obIndex + 1, cbIndex);
            if (ARRAY_INDEX_PATTERN.matcher(arrayIndex).matches()) {
                return Integer.parseInt(arrayIndex, 10);
            }
        }
        return -1;
    }

    public static boolean isEmpty(Object obj) {
        return obj == null || (
                isArray(obj) ? Array.getLength(obj) == 0 : (
                        isCollection(obj) ? toCollection(obj).isEmpty() : (
                                obj instanceof Map ? ((Map<?, ?>) obj).isEmpty() : (
                                        "".equals(obj)
                                )
                        )
                )
        );
    }

}
