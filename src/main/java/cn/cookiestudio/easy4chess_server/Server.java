package cn.cookiestudio.easy4chess_server;

import cn.cookiestudio.easy4chess_server.network.ServerUdp;
import cn.cookiestudio.easy4chess_server.network.listener.DefaultListener;
import cn.cookiestudio.easy4chess_server.network.listener.ListenerManager;
import cn.cookiestudio.easy4chess_server.player.Player;
import cn.cookiestudio.easy4chess_server.player.PlayerDataConfig;
import cn.cookiestudio.easy4chess_server.scheduler.Scheduler;
import cn.cookiestudio.easy4chess_server.utils.Config;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;

public class Server {

    private static ObjectMapper JACKSON_JSON_MAPPER = new ObjectMapper();
    private static Gson GSON = new Gson();
    private static YAMLMapper YAML_MAPPER = new YAMLMapper();

     static{//init jackson json
        JACKSON_JSON_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JACKSON_JSON_MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        JACKSON_JSON_MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        JACKSON_JSON_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
     }

    private static Server instance = null;
    private int serverTPS = 20;
    private Path serverPath = Paths.get(System.getProperty("user.dir"));
    private Logger logger = LogManager.getLogger(LogManager.ROOT_LOGGER_NAME);
    private Scheduler scheduler;
    private ListenerManager listenerManager = new ListenerManager();
    private ServerUdp serverUdp;
    private Config serverSets;
    private PlayerDataConfig userData;
    private InetSocketAddress serverAddress;
    private HashMap<String, Player> users = new HashMap<>();
    public static void main(String[] args) {
        new Server();
    }

    public Server(){
        this.logger.info("Server starting...");
        instance = this;

        this.loadServerYml();
        this.getLogger().info("Successfully loaded server info");

        this.userData = new PlayerDataConfig();

        this.scheduler = new Scheduler();
        this.scheduler.start();
        this.getLogger().info("Successfully started scheduler");
        try {
            this.serverAddress = new InetSocketAddress(InetAddress.getByName((String)this.serverSets.get("ip")), (int)this.serverSets.get("port"));
            this.serverUdp = new ServerUdp(this.serverAddress);
            this.getLogger().info("Successfully started udp service");
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.registerDefaultListener();
        this.getLogger().info("Successfully registered default listener");
    }

    public ListenerManager getListenerManager() {
        return listenerManager;
    }

    public Path getServerPath() {
        return serverPath;
    }

    public HashMap<String, Player> getPlayers() {
        return users;
    }

    public void addPlayer(Player player){
        this.users.put(player.getPlayerName(), player);
    }

    public boolean removeUser(Player player){
        if (this.users.containsKey(player.getPlayerName())) return false;
        this.users.remove(player.getPlayerName());
        return true;
    }

    public ServerUdp getServerUdp() {
        return serverUdp;
    }

    public PlayerDataConfig getUserData() {
        return userData;
    }

    public Config getServerSets() {
        return serverSets;
    }

    public InetSocketAddress getServerAddress() {
        return serverAddress;
    }

    public Scheduler getScheduler() {
        return scheduler;
    }

    public static ObjectMapper getJacksonJsonMapper() {
        return JACKSON_JSON_MAPPER;
    }

    public static Gson getGSON() {
        return GSON;
    }

    public static YAMLMapper getYamlMapper() {
        return YAML_MAPPER;
    }

    public static Server getInstance(){
        return instance;
    }

    public int getServerTPS() {
        return serverTPS;
    }

    public Logger getLogger(){
        return this.logger;
    }

    public void stop(int status){
        if (status == 0){
            logger.info("Server is closed");
            stop$1();
            System.exit(0);
        }else{
            logger.fatal("Server is crashed");
            stop$1();
            System.exit(1);
        }
    }

    private void stop$1(){
        this.serverUdp.close();
    }

    private void loadServerYml(){
        Path ymlPath = Paths.get(this.serverPath.toString(), "server.yml");
        if (!Files.exists(ymlPath)){
            logger.error("Can't find server.yml,creating new file....");
            try {
                Files.copy(Server.class.getClassLoader().getResourceAsStream("server.yml"),ymlPath);
            } catch (IOException e) {
                e.printStackTrace();
                logger.fatal("Can't create file,server will crash...");
                this.stop(1);
            }
        }
        serverSets = new Config(ymlPath);
    }

    private void registerDefaultListener(){
        this.getListenerManager().registerListener(new DefaultListener());
    }
}
