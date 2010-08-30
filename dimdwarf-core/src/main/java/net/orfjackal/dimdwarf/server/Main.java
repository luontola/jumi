// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.modules.*;
import net.orfjackal.dimdwarf.services.ServiceStarter;
import net.orfjackal.dimdwarf.util.MavenUtil;
import org.slf4j.*;

import java.util.*;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        Thread.setDefaultUncaughtExceptionHandler(new KillProcessOnUncaughtException());

        logger.info("Dimdwarf {} starting up", getVersion());

        // TODO: parse args properly
        int port = Integer.parseInt(args[1]);
        String applicationDir = args[3];

        List<Module> modules = configureServerModules(port);
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

    private static List<Module> configureServerModules(int port) {
        List<Module> modules = new ArrayList<Module>();
        modules.add(new ServiceInstallerModule(
                new ControllerModule(),
                new AuthenticatorModule(),
                new NetworkModule(port)
        ));
        return modules;
    }

    private static String getVersion() {
        String version = MavenUtil.getPom("net.orfjackal.dimdwarf", "dimdwarf-core").getProperty("version");
        return version != null ? version : "<unknown version>";
    }
}
