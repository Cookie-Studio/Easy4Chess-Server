package cn.cookiestudio.easy4chess_server.network.listener;

import cn.cookiestudio.easy4chess_server.Server;
import cn.cookiestudio.easy4chess_server.network.packet.*;
import cn.cookiestudio.easy4chess_server.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import java.io.IOException;
import java.net.DatagramPacket;

public class DefaultListener implements Listener{
    @PacketHandler
    public void onRequestLogin(RequestLoginPacket packet) throws IOException {
        User user = new User(packet.getUserName(),packet.getPassword(),packet.getAddress());
        //check if exist
        if (!Server.getInstance().getUserData().containUser(packet.getUserName())) {
            byte[] b = Server.getGSON().toJson(new LoginStatePacket(LoginStatePacket.LoginStateEnum.NO_INFO)).getBytes();
            Server.getInstance().getServerUdp().getUdpSocket().send(new DatagramPacket(b,0,b.length,user.getAddress()));
        }
        //check password
        if (!Server.getInstance().getUserData().verifyPassword(user.getUserName(),user.getPassword())){
            byte[] b = Server.getGSON().toJson(new LoginStatePacket(LoginStatePacket.LoginStateEnum.WRONG_PASSWORD)).getBytes();
            Server.getInstance().getServerUdp().getUdpSocket().send(new DatagramPacket(b,0,b.length,user.getAddress()));
        }

        Server.getInstance().addUser(user);
        //send success packet
        byte[] b = Server.getGSON().toJson(new LoginStatePacket(LoginStatePacket.LoginStateEnum.SUCCESS)).getBytes();
        Server.getInstance().getServerUdp().getUdpSocket().send(new DatagramPacket(b,0,b.length,user.getAddress()));
    }

    @PacketHandler
    public void onRequestServerInfo(RequestServerInfoPacket packet) throws IOException {
        ServerInfoPacket packet1 = new ServerInfoPacket(Server.getInstance());
        Server.getInstance().getServerUdp().sendData(Server.getGSON().toJson(packet1).getBytes(),packet.getAddress());
    }

    @PacketHandler
    public void onDisconnect(DisconnectPacket packet){
        //for future code...

        //remove user
        Server.getInstance().removeUser(packet.getUser());
    }

    @PacketHandler
    public void onRegisterInfo(RegisterInfoPacket packet) throws JsonProcessingException {
        RegisterInfoStatePacket result = new RegisterInfoStatePacket(Server.getInstance().getUserData().writeUserData(packet.getUserName(),packet.getPassword()));
        Server.getInstance().getServerUdp().sendData(Server.getGSON().toJson(result).getBytes(),packet.getAddress());
    }
}
