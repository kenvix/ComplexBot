//--------------------------------------------------
// Class Main
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng;

import com.kenvix.utils.tools.BlockingTools;

import javax.swing.*;

public final class Main {
    public static void main(String[] args) {
        try {
            Bootstrapper.startApplication(args);

            BlockingTools.makeCurrentThreadPermanentlyBlocked();
        } catch (Throwable throwable) {
            System.err.println(BuildConfig.APPLICATION_NAME + " Bootstrap Failed!!");
            throwable.printStackTrace();

            if (BuildConfig.IS_RELEASE_BUILD && System.console() == null)
                JOptionPane.showMessageDialog(null, throwable.toString(),
                        BuildConfig.APPLICATION_NAME + " - Bootstrap Failed", JOptionPane.ERROR_MESSAGE);

            System.exit(1);
        }
    }
}
