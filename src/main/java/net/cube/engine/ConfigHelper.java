package net.cube.engine;

import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Provides utility methods for working with config.
 * @author pluto
 * @date   2022/5/15
 */
public class ConfigHelper {

    public static final String CONFIG_FILE_NAME_PROP = "CONFIG_FILE_PATH";

    private static final ConfigHelper INSTANCE = new ConfigHelper();

    private ConfigHelper() {
        init();
    }

    private volatile boolean initialized = false;

    private Lock lock = new ReentrantLock();

    protected Properties config;

    private void init() {
        InputStream is = null;
        if (initialized) {
            return ;
        }
        if (lock.tryLock()) {
            try {
                if (initialized) {
                    return ;
                }
                String configFilePath = System.getProperty(CONFIG_FILE_NAME_PROP);
                if (configFilePath == null || "".equals(configFilePath)) {
                    throw new RuntimeException("Con not found config file in system properties.");
                }
                final URL url = this.getClass().getClassLoader().getResource(configFilePath);
                if (url == null) {
                    return ;
                }
                Yaml yaml = new Yaml();
                is = url.openStream();
                config = yaml.loadAs(is, Properties.class);
                initialized = true;
            } catch (IOException e) {
               throw new RuntimeException("Configuration initialized error.", e);
            } finally {
                if (is != null) {
                    try {
                        is.close();
                        is = null;
                    } catch (IOException ignored) {
                    }
                }
                lock.unlock();
            }
        }
    }

    /**
     * Get the static instance.
     * @return instance of this class.
     */
    public static ConfigHelper getInstance() {
        return INSTANCE;
    }

    /**
     * Get all configuration entries.
     * @return properties for configuration
     */
    public Properties getAllConfig() {
        return config;
    }

    /**
     * Get entry by name.
     * @param name of specified entry
     * @return value of specified entry
     */
    public Object getConfig(String name) {
        return getConfig(name, null);
    }

    /**
     * Get entry by name.
     * If no entry in current configuration, return defaultValue.
     * @param name name for specified entry
     * @param defaultValue if entry not exist.
     * @return value of specified entry
     */
    public Object getConfig(String name, Object defaultValue) {
        return config.getOrDefault(name, defaultValue);
    }

}
