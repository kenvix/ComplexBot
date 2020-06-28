//--------------------------------------------------
// Class ComplexBotConfig
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot;

import java.util.List;

public class ComplexBotConfig {
    public Bot bot;
    public Mirai mirai;

    public static class Bot {
        public String name;
        public String password;
        public long qq;
        public List<Long> administratorIds;
        public boolean acceptAllFriendRequest;
        public boolean acceptAllGroupInvitation;
    }

    public static class Mirai {
        public String authKey;
        public int port;
    }
}
