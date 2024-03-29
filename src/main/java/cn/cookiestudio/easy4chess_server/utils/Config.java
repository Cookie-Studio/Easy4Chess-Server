package cn.cookiestudio.easy4chess_server.utils;

import cn.cookiestudio.easy4chess_server.Server;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.dataformat.javaprop.JavaPropsMapper;
import org.apache.logging.log4j.Level;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

/**
 * copyed from cloudburst
 * author: MagicDroidX
 * Nukkit
 */
public class Config {

    public static final int DETECT = -1; //Detect by file extension
    public static final int PROPERTIES = 0; // .properties
    public static final int CNF = Config.PROPERTIES; // .cnf
    public static final int JSON = 1; // .js, .json
    public static final int YAML = 2; // .yml, .yaml
    public static final int ENUM = 5; // .txt, .list, .enum
    public static final int ENUMERATION = Config.ENUM;
    private static final JavaPropsMapper JAVA_PROPS_MAPPER = new JavaPropsMapper();

    static {
        SimpleModule module = new SimpleModule();
        module.addAbstractTypeMapping(Map.class, ConfigSection.class);
        Server.getJacksonJsonMapper().registerModule(module);
        Server.getYamlMapper().registerModule(module);
        JAVA_PROPS_MAPPER.registerModule(module);
    }

    //private LinkedHashMap<String, Object> config = new LinkedHashMap<>();
    private ConfigSection config = new ConfigSection();
    private File file;
    private boolean correct = false;
    private int type = Config.DETECT;

    public static final Map<String, Integer> format = new TreeMap<>();

    static {
        format.put("properties", Config.PROPERTIES);
        format.put("con", Config.PROPERTIES);
        format.put("conf", Config.PROPERTIES);
        format.put("config", Config.PROPERTIES);
        format.put("js", Config.JSON);
        format.put("json", Config.JSON);
        format.put("yml", Config.YAML);
        format.put("yaml", Config.YAML);
        //format.put("sl", Config.SERIALIZED);
        //format.put("serialize", Config.SERIALIZED);
        format.put("txt", Config.ENUM);
        format.put("list", Config.ENUM);
        format.put("enum", Config.ENUM);
    }

    /**
     * Constructor for Config instance with undefined file object
     *
     * @param type - Config type
     */
    public Config(int type) {
        this.type = type;
        this.correct = true;
        this.config = new ConfigSection();
    }

    /**
     * Constructor for Config (YAML) instance with undefined file object
     */
    public Config() {
        this(Config.YAML);
    }

    public Config(String file) {
        this(file, Config.DETECT);
    }

    public Config(File file) {
        this(file.toString(), Config.DETECT);
    }

    public Config(String file, int type) {
        this(file, type, new ConfigSection());
    }

    public Config(File file, int type) {
        this(file.toString(), type, new ConfigSection());
    }

    public Config(String file, int type, ConfigSection defaultMap) {
        this.load(file, type, defaultMap);
    }

    public Config(File file, int type, ConfigSection defaultMap) {
        this.load(file.toString(), type, defaultMap);
    }

    public Config(Path path){
        this(path.toString());
    }

    public void reload() {
        this.config.clear();
        this.correct = false;
        if (this.file == null) throw new IllegalStateException("Failed to reload Config. File object is undefined.");
        this.load(this.file.toString(), this.type);

    }

    public boolean load(String file) {
        return this.load(file, Config.DETECT);
    }

    public boolean load(String file, int type) {
        return this.load(file, type, new ConfigSection());
    }

    @SuppressWarnings("unchecked")
    public boolean load(String file, int type, ConfigSection defaultMap) {
        this.correct = true;
        this.type = type;
        this.file = new File(file);
        if (!this.file.exists()) {
            try {
                this.file.getParentFile().mkdirs();
                this.file.createNewFile();
            } catch (IOException e) {
                Server.getInstance().getLogger().error("Could not create Config " + this.file.toString(), e);
            }
            this.config = defaultMap;
            this.save();
        } else {
            if (this.type == Config.DETECT) {
                String extension = "";
                if (this.file.getName().lastIndexOf(".") != -1 && this.file.getName().lastIndexOf(".") != 0) {
                    extension = this.file.getName().substring(this.file.getName().lastIndexOf(".") + 1);
                }
                if (format.containsKey(extension)) {
                    this.type = format.get(extension);
                } else {
                    this.correct = false;
                }
            }
            if (this.correct) {
                String content = "";
                try {
                    content = Utils.readFile(this.file);
                } catch (IOException e) {
                    Server.getInstance().getLogger().throwing(Level.ERROR, e);
                }
                this.parseContent(content);
                if (!this.correct) return false;
                if (this.setDefault(defaultMap) > 0) {
                    this.save();
                }
            } else {
                return false;
            }
        }
        return true;
    }

    public boolean load(InputStream inputStream) {
        if (inputStream == null) return false;
        if (this.correct) {
            String content;
            try {
                content = Utils.readFile(inputStream);
            } catch (IOException e) {
                Server.getInstance().getLogger().throwing(Level.ERROR, e);
                return false;
            }
            this.parseContent(content);
        }
        return correct;
    }

    public boolean check() {
        return this.correct;
    }

    public boolean isCorrect() {
        return correct;
    }

    /**
     * Save configuration into provided file. Internal file object will be set to new file.
     *
     * @param file
     * @param async
     * @return
     */
    public boolean save(File file, boolean async) {
        this.file = file;
        return save(async);
    }

    public boolean save(File file) {
        this.file = file;
        return save();
    }

    public boolean save() {
        return this.save(false);
    }

    public boolean save(Boolean async) {
        if (this.file == null) throw new IllegalStateException("Failed to save Config. File object is undefined.");
        if (this.correct) {
            String content;
            if (this.type == ENUM) {
                StringBuilder builder = new StringBuilder();
                for (Object o : this.config.entrySet()) {
                    Map.Entry<?, ?> entry = (Map.Entry<?, ?>) o;
                    builder.append(entry.getKey()).append("\r\n");
                }
                content = builder.toString();
            } else {
                ObjectMapper mapper;
                switch (this.type) {
                    case PROPERTIES:
                        mapper = JAVA_PROPS_MAPPER;
                        break;
                    case JSON:
                        mapper = Server.getJacksonJsonMapper();
                        break;
                    case YAML:
                        mapper = Server.getYamlMapper();
                        break;
                    default:
                        throw new UnsupportedOperationException("Invalid config type " + type);
                }
                try {
                    content = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(this.config);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            }
            if (async) {
                Server.getInstance().getScheduler().schedulerImmediateAsyncRunnable(() -> {
                try {
                    Utils.writeFile(file, content);
                } catch (IOException e) {
                    Server.getInstance().getLogger().throwing(Level.ERROR, e);
                }});
            } else {
                try {
                    Utils.writeFile(this.file, content);
                } catch (IOException e) {
                    Server.getInstance().getLogger().throwing(Level.ERROR, e);
                }
            }
            return true;
        } else {
            return false;
        }
    }

    public void set(final String key, Object value) {
        this.config.set(key, value);
    }

    public Object get(String key) {
        return this.get(key, null);
    }

    public <T> T get(String key, T defaultValue) {
        return this.correct ? this.config.get(key, defaultValue) : defaultValue;
    }

    public ConfigSection getSection(String key) {
        return this.correct ? this.config.getSection(key) : new ConfigSection();
    }

    public boolean isSection(String key) {
        return config.isSection(key);
    }

    public ConfigSection getSections(String key) {
        return this.correct ? this.config.getSections(key) : new ConfigSection();
    }

    public ConfigSection getSections() {
        return this.correct ? this.config.getSections() : new ConfigSection();
    }

    public int getInt(String key) {
        return this.getInt(key, 0);
    }

    public int getInt(String key, int defaultValue) {
        return this.correct ? this.config.getInt(key, defaultValue) : defaultValue;
    }

    public boolean isInt(String key) {
        return config.isInt(key);
    }

    public long getLong(String key) {
        return this.getLong(key, 0);
    }

    public long getLong(String key, long defaultValue) {
        return this.correct ? this.config.getLong(key, defaultValue) : defaultValue;
    }

    public boolean isLong(String key) {
        return config.isLong(key);
    }

    public double getDouble(String key) {
        return this.getDouble(key, 0);
    }

    public double getDouble(String key, double defaultValue) {
        return this.correct ? this.config.getDouble(key, defaultValue) : defaultValue;
    }

    public boolean isDouble(String key) {
        return config.isDouble(key);
    }

    public String getString(String key) {
        return this.getString(key, "");
    }

    public String getString(String key, String defaultValue) {
        return this.correct ? this.config.getString(key, defaultValue) : defaultValue;
    }

    public boolean isString(String key) {
        return config.isString(key);
    }

    public boolean getBoolean(String key) {
        return this.getBoolean(key, false);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        return this.correct ? this.config.getBoolean(key, defaultValue) : defaultValue;
    }

    public boolean isBoolean(String key) {
        return config.isBoolean(key);
    }

    public <T> List<T> getList(String key) {
        return this.getList(key, null);
    }

    public <T> List<T> getList(String key, List<T> defaultList) {
        return this.correct ? this.config.getList(key, defaultList) : defaultList;
    }

    public boolean isList(String key) {
        return config.isList(key);
    }

    public List<String> getStringList(String key) {
        return config.getStringList(key);
    }

    public List<Integer> getIntegerList(String key) {
        return config.getIntegerList(key);
    }

    public List<Boolean> getBooleanList(String key) {
        return config.getBooleanList(key);
    }

    public List<Double> getDoubleList(String key) {
        return config.getDoubleList(key);
    }

    public List<Float> getFloatList(String key) {
        return config.getFloatList(key);
    }

    public List<Long> getLongList(String key) {
        return config.getLongList(key);
    }

    public List<Byte> getByteList(String key) {
        return config.getByteList(key);
    }

    public List<Character> getCharacterList(String key) {
        return config.getCharacterList(key);
    }

    public List<Short> getShortList(String key) {
        return config.getShortList(key);
    }

    public List<Map> getMapList(String key) {
        return config.getMapList(key);
    }

    public void setAll(LinkedHashMap<String, Object> map) {
        this.config = new ConfigSection(map);
    }

    public void setAll(ConfigSection section) {
        this.config = section;
    }

    public boolean exists(String key) {
        return config.exists(key);
    }

    public boolean exists(String key, boolean ignoreCase) {
        return config.exists(key, ignoreCase);
    }

    public void remove(String key) {
        config.remove(key);
    }

    public Map<String, Object> getAll() {
        return this.config.getAllMap();
    }

    /**
     * Get root (main) config section of the Config
     *
     * @return
     */
    public ConfigSection getRootSection() {
        return config;
    }

    public int setDefault(LinkedHashMap<String, Object> map) {
        return setDefault(new ConfigSection(map));
    }

    public int setDefault(ConfigSection map) {
        int size = this.config.size();
        this.config = this.fillDefaults(map, this.config);
        return this.config.size() - size;
    }


    private ConfigSection fillDefaults(ConfigSection defaultMap, ConfigSection data) {
        for (String key : defaultMap.keySet()) {
            if (!data.containsKey(key)) {
                data.put(key, defaultMap.get(key));
            }
        }
        return data;
    }

    private void parseList(String content) {
        content = content.replace("\r\n", "\n");
        for (String v : content.split("\n")) {
            if (v.trim().isEmpty()) {
                continue;
            }
            config.put(v, true);
        }
    }

    private String writeProperties() {
        String content = "#Properties Config file\r\n#" + new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").format(new Date()) + "\r\n";
        for (Object o : this.config.entrySet()) {
            Map.Entry entry = (Map.Entry) o;
            Object v = entry.getValue();
            Object k = entry.getKey();
            if (v instanceof Boolean) {
                v = (Boolean) v ? "on" : "off";
            }
            content += k + "=" + v + "\r\n";
        }
        return content;
    }

    private void parseProperties(String content) {
        for (final String line : content.split("\n")) {
            if (Pattern.compile("[a-zA-Z0-9\\-_.]*+=+[^\\r\\n]*").matcher(line).matches()) {
                final int splitIndex = line.indexOf('=');
                if (splitIndex == -1) {
                    continue;
                }
                final String key = line.substring(0, splitIndex);
                final String value = line.substring(splitIndex + 1);
                final String valueLower = value.toLowerCase();
                if (this.config.containsKey(key) && Server.getInstance().getLogger().isDebugEnabled()) {
                    Server.getInstance().getLogger().debug("[Config] Repeated property " + key + " on file " + this.file.toString());
                }
                switch (valueLower) {
                    case "on":
                    case "true":
                    case "yes":
                        this.config.put(key, true);
                        break;
                    case "off":
                    case "false":
                    case "no":
                        this.config.put(key, false);
                        break;
                    default:
                        this.config.put(key, value);
                        break;
                }
            }
        }
    }

    private void parseContent(String content) {
        if (type == ENUM) {
            this.parseList(content);
        } else {
            ObjectMapper mapper;
            switch (this.type) {
                case Config.PROPERTIES:
                    mapper = JAVA_PROPS_MAPPER;
                    break;
                case Config.JSON:
                    mapper = Server.getJacksonJsonMapper();
                    break;
                case Config.YAML:
                    mapper = Server.getYamlMapper();
                    break;
                default:
                    this.correct = false;
                    return;
            }
            try {
                this.config = mapper.readValue(content, ConfigSection.class);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public Set<String> getKeys() {
        if (this.correct) return config.getKeys();
        return new HashSet<>();
    }

    public Set<String> getKeys(boolean child) {
        if (this.correct) return config.getKeys(child);
        return new HashSet<>();
    }
}
