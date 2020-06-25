//--------------------------------------------------
// Class Main
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng;

public final class Main {
    public static void main(String[] args) {
        try {
            Bootstrapper.startApplication(args);
        } catch (Throwable throwable) {
            System.err.println("FATAL: Bootstrapper Failed!!");

            throwable.printStackTrace();
            System.exit(1);
        }
    }
}
