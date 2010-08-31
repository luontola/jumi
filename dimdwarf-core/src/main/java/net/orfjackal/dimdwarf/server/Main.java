// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.services.ServiceStarter;
import net.orfjackal.dimdwarf.util.MavenUtil;
import org.slf4j.*;

import java.io.*;
import java.net.*;
import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static final String APP_CLASSES = "classes";
    public static final String APP_LIBRARIES = "lib";
    public static final String APP_PROPERTIES = "META-INF/app.properties";

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new KillProcessOnUncaughtException());

        logger.info("Dimdwarf {} starting up", getVersion());

        // TODO: parse args properly
        int port = Integer.parseInt(args[1]);
        String applicationDir = args[3];

        List<Module> modules = configureServerModules(port, applicationDir);
        logger.info("Modules configured");

        // TODO: speed up startup by loading classes in parallel
        // Loading the classes is what takes most of the time in startup - on JDK 7 it can be speeded up
        // by loading the classes in parallel. Preliminary tests promise 50% speedup (and 15% slowdown on JDK 6).
        // Doing the following operations in different threads might be able to parallelize the class loading:
        // - create a Guice injector for an empty module (loads Guice's classes)
        // - open a MINA socket acceptor in a random port and close it (loads MINA's classes)
        // - instantiate and run Dimdwarf's modules outside Guice (loads some of Dimdwarf's classes)
        // - create the actual injector with Dimdwarf's modules and return it via a Future (what we really wanted)

        Injector injector = Guice.createInjector(Stage.PRODUCTION, modules);
        logger.info("Modules loaded");

        injector.getInstance(ServiceStarter.class).start();
        logger.info("Server started");
    }

    private static String getVersion() {
        String version = MavenUtil.getPom("net.orfjackal.dimdwarf", "dimdwarf-core").getProperty("version");
        return version != null ? version : "<unknown version>";
    }

    private static List<Module> configureServerModules(int port, String applicationDir) throws Exception {
        List<Module> modules = new ArrayList<Module>();
        modules.add(new ServiceInstallerModule(
                new ControllerModule(),
                new AuthenticatorModule(),
                new NetworkModule(port)
        ));
        modules.add(loadApplicationModule(applicationDir));
        return modules;
    }

    // TODO: extract application loading into a new class

    private static Module loadApplicationModule(String applicationDir) throws Exception {
        logger.info("Opening application directory {}", applicationDir);
        List<File> classpath = new ArrayList<File>();
        classpath.add(new File(applicationDir, APP_CLASSES));
        classpath.addAll(Arrays.asList(listJarsInDirectory(new File(applicationDir, APP_LIBRARIES))));

        URLClassLoader appLoader = new URLClassLoader(asUrls(classpath), Main.class.getClassLoader());
        String appName = getRequiredProperty("dimdwarf.app.name", APP_PROPERTIES, appLoader);
        logger.info("Found application {}", appName);

        String appModule = getRequiredProperty("dimdwarf.app.module", APP_PROPERTIES, appLoader);
        logger.info("Loading application module {}", appModule);
        return (Module) appLoader.loadClass(appModule).newInstance();
    }

    private static String getRequiredProperty(String key, String file, ClassLoader loader) throws IOException {
        InputStream in = loader.getResourceAsStream(file);
        if (in == null) {
            logger.error("Resource does not exist: {}", file);
            System.exit(1);
        }

        Properties p = new Properties();
        try {
            p.load(in);
        } finally {
            in.close();
        }

        String value = p.getProperty(key);
        if (value == null) {
            logger.error("Property {} was not set in {}", key, file);
            System.exit(1);
        }
        return value;
    }

    private static File[] listJarsInDirectory(File dir) {
        return dir.listFiles(new FilenameFilter() {
            public boolean accept(File dir, String name) {
                return name.endsWith(".jar");
            }
        });
    }

    private static URL[] asUrls(List<File> files) throws MalformedURLException {
        URL[] urls = new URL[files.size()];
        for (int i = 0, filesLength = files.size(); i < filesLength; i++) {
            urls[i] = asUrl(files.get(i));
        }
        return urls;
    }

    private static URL asUrl(File file) throws MalformedURLException {
        return file.toURI().toURL();
    }
}
