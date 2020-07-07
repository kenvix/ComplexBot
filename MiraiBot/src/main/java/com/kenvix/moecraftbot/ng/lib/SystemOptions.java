//--------------------------------------------------
// Class SystemOptions
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib;

import java.util.List;
import java.util.Set;

public class SystemOptions {
    public Proxy proxy;
    public Bot bot;
    public Auth auth;
    public System system;
    public Database database;
    public HTTP http;
    public MessagePush messagePush;
    public MongoDB mongo;

    public static class Database {
        public String type;
        public String name;
        public String user;
        public String password;
        public String host;
        public int port;
        public boolean needAuth;
    }

    public static class MongoDB {
        public String authSource;
        public String name;
        public String user;
        public String password;
        public String host;
        public int port;
        public boolean needAuth;
    }

    public static class Proxy {
        public boolean enable;
        public String host;
        public String port;
        public boolean auth;
        public String username;
        public String password;
        public Scope scope;
        public Type type;

        public static class Scope {
            public boolean bot;
            public boolean driver;
            public boolean other;
        }

        public enum Type {
            http, socks5, socks4
        }
    }

    public static class Bot {
        public Set<String> commandPrefix;
        public int commandPrefixLength;
        public String helpMessage;
    }

    public static class Auth {
        public Long[] groups;
        public Long[] admins;
        public String successMessage;
        public String welcomeMessage;
        public boolean disallowOneTgAccountToMultipleSiteUser = true;
        public boolean disallowSiteUserToMultipleOneTgAccount = true;
    }

    public static class System {
        public long cacheSize = 1000000000L;
        public int threadPoolMaxSize = 10;
    }

    public static class HTTP {
        public boolean enable = false;
        public int port = 54121;
        public String host = "0.0.0.0";
        public String corsOrigin = "";
    }

    public static class MessagePush {
        public String key = "";
    }
}
