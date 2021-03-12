package cn.cookiestudio.easy4chess_server.network.packet;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class LoginStatePacket extends Packet{

    private LoginStateEnum state;

    public LoginStatePacket(LoginStateEnum state){
        this.pid = 1;
        this.state = state;
    }

    public LoginStateEnum getState(){
        return this.state;
    }

    public enum LoginStateEnum{
        SUCCESS,
        NO_INFO,
        WRONG_PASSWORD,
    }
}
