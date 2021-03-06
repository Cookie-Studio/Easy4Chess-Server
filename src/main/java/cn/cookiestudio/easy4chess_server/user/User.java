package cn.cookiestudio.easy4chess_server.user;

import cn.cookiestudio.easy4chess_server.Server;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;

public class User {

    @JsonDeserialize
    @JsonSerialize
    private String userName;
    @JsonDeserialize
    @JsonSerialize
    private String password;
    @JsonDeserialize
    @JsonSerialize
    private int winCount;
    @JsonDeserialize
    @JsonSerialize
    private int loseCount;
    @JsonDeserialize
    @JsonSerialize
    private InetSocketAddress address;

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public int getWinCount() {
        return winCount;
    }

    public InetSocketAddress getAddress() {
        return address;
    }

    public int getLoseCount() {
        return loseCount;
    }

    public User(String userName, String password, InetSocketAddress address){
        this.userName = userName;
        this.password = password;
        this.address = address;
    }

    public void sendData(byte[] data){
        Server.getInstance().getServerUdp().sendData(data, this.address);
    }
}
