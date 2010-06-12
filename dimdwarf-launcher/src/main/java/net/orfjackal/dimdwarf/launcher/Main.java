// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.launcher;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

/**
 * Adds to classpath automatically all libraries which are part of the application,
 * and then runs the application via its main method.
 * <p/>
 * This launcher should not contain any application logic (such as reading config files
 * or parsing command line arguments), but it should delegate everything to the actual
 * application. Also library dependencies should be minimized (preferably just the JRE).
 * <p/>
 * Also the application should be started in the same JVM as this class. This is to
 * make it easier for IDEs to debug the application. If it were launched in a separate
 * JVM, attaching a debugger or profiler to it would be much harder.
 * <p/>
 * Due to the previous requirement, this class cannot be used to set JVM options. They
 * need to be set possibly in a shell script which runs this launcher.
 */
public class Main {
    private static final File LIBRARIES_DIR = new File("lib");
    private static final String ACTUAL_MAIN = "net.orfjackal.dimdwarf.server.Main";

    // TODO: AOP bytecode manipulation? perhaps first loading the AOP libraries from a lib/boot/ directory
    // TODO: remove/modify the old startup scripts

    public static void main(String[] args) throws Exception {
        URL[] libraries = asUrls(listJarsInDirectory(LIBRARIES_DIR));
        URLClassLoader loader = new URLClassLoader(libraries);
        Class<?> mainClass = loader.loadClass(ACTUAL_MAIN);
        invokeMainMethod(mainClass, args);
    }

    private static void invokeMainMethod(Class<?> mainClass, String[] args) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method main = mainClass.getMethod("main", String[].class);
        main.invoke(null, (Object) args);
    }

    private static File[] listJarsInDirectory(File dir) {
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
    }

    private static URL[] asUrls(File[] files) throws MalformedURLException {
        URL[] urls = new URL[files.length];
        for (int i = 0, filesLength = files.length; i < filesLength; i++) {
            urls[i] = files[i].toURI().toURL();
        }
        return urls;
    }
}
