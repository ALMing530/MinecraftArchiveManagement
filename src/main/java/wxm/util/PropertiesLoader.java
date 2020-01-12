package wxm.util;

import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
    static Properties properties = new Properties();
    public static void load(String propLocation){
        try {
            properties.load(PropertiesLoader.class.getResourceAsStream(propLocation));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static String getValue(String key){
        return properties.getProperty(key);
    }
}
