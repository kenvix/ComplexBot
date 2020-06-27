//--------------------------------------------------
// Class ComplexBotConfig
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.complexbot;

public class ComplexBotConfig {
    public Bot bot;
    public Mirai mirai;

    public static class Bot {
        public String name;
        public String password;
        public long qq;
    }

    public static class Mirai {
        public String authKey;
        public int port;
    }
}
