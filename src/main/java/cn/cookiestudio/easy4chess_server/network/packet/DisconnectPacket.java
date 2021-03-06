package cn.cookiestudio.easy4chess_server.network.packet;

import cn.cookiestudio.easy4chess_server.user.User;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class DisconnectPacket extends Packet {

    @JsonSerialize
    @JsonDeserialize
    private User user;

    public DisconnectPacket(User user) {
        this.pid = 4;
        this.user = user;
    }

    public DisconnectPacket() {
        this.pid = 4;
    }

    public User getUser() {
        return user;
    }
}
