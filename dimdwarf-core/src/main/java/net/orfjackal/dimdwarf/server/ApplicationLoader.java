// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.Module;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.net.*;
import java.util.*;

public class ApplicationLoader {
    public static final String CLASSES_DIR = "classes";
    public static final String LIBRARIES_DIR = "lib";
    public static final String CONFIG_FILE = "META-INF/app.properties";
    public static final String APP_NAME = "dimdwarf.app.name";
    public static final String APP_MODULE = "dimdwarf.app.module";

    private final URLClassLoader classLoader;
    private final String applicationName;
    private final String applicationModule;

    public ApplicationLoader(File applicationDir) throws ConfigurationException {
        this(applicationDir, ApplicationLoader.class.getClassLoader());
    }

    public ApplicationLoader(File applicationDir, ClassLoader parent) throws ConfigurationException {
        List<File> classpath = new ArrayList<File>();
        classpath.add(new File(applicationDir, CLASSES_DIR));
        classpath.addAll(Arrays.asList(listJarsInDirectory(new File(applicationDir, LIBRARIES_DIR))));
        classLoader = new URLClassLoader(asUrls(classpath), parent);

        applicationName = getRequiredProperty(APP_NAME, CONFIG_FILE);
        applicationModule = getRequiredProperty(APP_MODULE, CONFIG_FILE);
    }

    public String getApplicationName() {
        return applicationName;
    }

    public String getApplicationModule() {
        return applicationModule;
    }

    public Module newModuleInstance() {
        String clazz = applicationModule;
        try {
            return (Module) classLoader.loadClass(clazz).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Unable to instantiate " + clazz, e);
        }
    }

    public URLClassLoader getClassLoader() {
        return classLoader;
    }

    private String getRequiredProperty(String key, String file) throws ConfigurationException {
        Properties properties = getResourceAsProperties(file);

        String value = properties.getProperty(key);
        if (value == null) {
            throw new ConfigurationException("Property " + key + " was not set in " + file);
        }
        return value;
    }

    private Properties getResourceAsProperties(String file) throws ConfigurationException {
        InputStream in = classLoader.getResourceAsStream(file);
        if (in == null) {
            throw new ConfigurationException("File not found from classpath: " + file);
        }

        Properties p = new Properties();
        try {
            p.load(in);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            IOUtils.closeQuietly(in);
        }
        return p;
    }

    private static File[] listJarsInDirectory(File dir) {
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
    }

    private static URL[] asUrls(List<File> files) {
        URL[] urls = new URL[files.size()];
        for (int i = 0, filesLength = files.size(); i < filesLength; i++) {
            urls[i] = asUrl(files.get(i));
        }
        return urls;
    }

    private static URL asUrl(File file) {
        try {
            return file.toURI().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
