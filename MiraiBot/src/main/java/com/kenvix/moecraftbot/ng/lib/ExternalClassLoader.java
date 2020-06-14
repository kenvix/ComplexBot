//--------------------------------------------------
// Class PluginClassLoader
//--------------------------------------------------
// Written by Kenvix <i@kenvix.com>
//--------------------------------------------------

package com.kenvix.moecraftbot.ng.lib;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandlerFactory;
import java.nio.file.Path;
import java.util.ArrayList;

public class ExternalClassLoader extends URLClassLoader {
    public ExternalClassLoader(URL[] urls) {
        super(urls);
    }

    public ExternalClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(urls, parent, factory);
    }

    public ExternalClassLoader(URL[] urls, ClassLoader parent) {
        super(urls, parent);
    }

    public ExternalClassLoader(URL url) {
        super(new URL[] { url });
    }

    public ExternalClassLoader(URL url, ClassLoader parent, URLStreamHandlerFactory factory) {
        super(new URL[] { url }, parent, factory);
    }

    public ExternalClassLoader(URL url, ClassLoader parent) {
        super(new URL[] { url }, parent);
    }

    @Override
    public void addURL(URL url) {
        super.addURL(url);
    }

    public static ExternalClassLoader getJarLoaderFromFile(File jarFile) throws MalformedURLException {
        return new ExternalClassLoader(jarFile.toURI().toURL());
    }

    public static ExternalClassLoader getJarLoaderFromFilePath(Path jarFile) throws MalformedURLException {
        return new ExternalClassLoader(jarFile.normalize().toUri().toURL());
    }

    public static ExternalClassLoader getJarLoaderFromDirectory(File jarDir) throws MalformedURLException, FileNotFoundException {
        File[] files = jarDir.listFiles();

        if (files == null)
            throw new FileNotFoundException();

        ArrayList<URL> list = new ArrayList<>();
        for (File jar : files) {
            list.add(jar.toURI().toURL());
        }

        return new ExternalClassLoader(list.toArray(new URL[0]));
    }

    public static ExternalClassLoader getJarLoaderFromDirectoryPath(Path jarFile) throws MalformedURLException, FileNotFoundException {
        return getJarLoaderFromDirectory(jarFile.toFile());
    }
}
