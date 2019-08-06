package com.jin.env.garbage.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by abc on 2018/3/22.
 */
public class PropertiesUtils {

    private static PropertiesUtils install = null;

    public static Properties properties = new Properties();

    static {
        try {
            properties.load(PropertiesUtils.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private PropertiesUtils() {
    }

    public static PropertiesUtils getInstall(){
        if (install== null) {
            install = new PropertiesUtils();
        }
        return install;
    }

    public static void main(String[] args) {
//        System.out.println(PropertiesUtils.getInstall().properties.getProperty("a"));
//        List<String> names = Arrays.asList("Java", "Scala", "C++", "Haskell", "Lisp");
//        Predicate<String> predicate = (n) -> n.startsWith("J");
//        Predicate<String> predicate1 = (n) -> n.length() >= 4;
//        names.stream().filter(predicate.and(predicate1)).forEach(n -> System.out.println(n));

//        Jedis jedis = new Jedis("47.100.194.27",6379);
//        jedis.set("a","123");
//        System.out.println(jedis.get("a"));
        List<String> list = new ArrayList<>();
        list.add("wanglei");
        list.add("cy");

        list.stream().filter((n) -> n.startsWith("cy")).forEach(n->{
            System.out.println(n);
        });
        System.out.println(Active.DISABLE.getInfo());

    }


    public enum Active{
        DISABLE("bunegyopng"),ENABLE("keyong");
        private String info;
        Active(String info) {
            this.info = info;
        }
        public String getInfo(){
            return info;
        }

    }

}
