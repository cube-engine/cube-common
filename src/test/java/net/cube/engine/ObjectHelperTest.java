package net.cube.engine;

import lombok.Data;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author pluto
 * @date 2022/5/15
 */
public class ObjectHelperTest {

    @Test
    public void test() {
        Map<String, Object> map = new HashMap<>(16);
        List list = new ArrayList<>();
        list.add("aaa");
        map.put("a", list);
        System.out.println(ObjectHelper.getProperty(map, "a[0]"));
        ObjectHelper.setProperty(map, "a[0]", "bbb");
        System.out.println("========" + ObjectHelper.getProperty(list, "Agent.Person"));

        TestObject testObject = new TestObject();
        ObjectHelper.setProperty(testObject, "name", "liilb");
        ObjectHelper.setProperty(testObject, "name1", "liilb1");
        ObjectHelper.setProperty(testObject, "name2", "liilb2");
        ObjectHelper.setProperty(testObject, "name3", "liilb3");

        ObjectHelper.setProperty(testObject, "testObject1.name", "liilb");
        ObjectHelper.setProperty(testObject, "testObject1.name1", "liilb1");
        ObjectHelper.setProperty(testObject, "testObject1.name2", "liilb2");
        ObjectHelper.setProperty(testObject, "testObject1.name3", "liilb3");

        System.out.println(ObjectHelper.getProperty(testObject, "name"));
        System.out.println(ObjectHelper.getProperty(testObject, "name1"));
        System.out.println(ObjectHelper.getProperty(testObject, "name2"));
        System.out.println(ObjectHelper.getProperty(testObject, "name3"));

        System.out.println(ObjectHelper.getProperty(testObject, "testObject1.name"));
        System.out.println(ObjectHelper.getProperty(testObject, "testObject1.name1"));
        System.out.println(ObjectHelper.getProperty(testObject, "testObject1.name2"));
        System.out.println(ObjectHelper.getProperty(testObject, "testObject1.name3"));
    }

    @Data
    class TestObject {
        private String name;

        protected String name1;

        public String name2;

        String name3;

        private TestObject1 testObject1 = new TestObject1();
    }

    @Data
    class TestObject1 {
        public String name;

        private String name1;

        protected String name2;

        String name3;
    }

}
