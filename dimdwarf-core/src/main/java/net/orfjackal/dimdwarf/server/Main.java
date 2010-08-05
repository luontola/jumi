// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import com.google.inject.*;
import net.orfjackal.dimdwarf.modules.CommonModules;
import net.orfjackal.dimdwarf.util.MavenUtil;
import org.slf4j.*;

import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException, InterruptedException {
        logger.info("Dimdwarf {} starting up", getVersion());

        // TODO: speed up startup by loading classes in parallel
        // Loading the classes is what takes most of the time in startup - on JDK 7 it can be speeded up
        // by loading the classes in parallel. Preliminary tests promise 50% speedup (and 15% slowdown on JDK 6).
        // Doing the following operations in different threads might be able to parallelize the class loading:
        // - create a Guice injector for an empty module (loads Guice's classes)
        // - open a MINA socket acceptor in a random port and close it (loads MINA's classes)
        // - instantiate and run Dimdwarf's modules outside Guice (loads some of Dimdwarf's classes)
        // - create the actual injector with Dimdwarf's modules and return it via a Future (what we really wanted)

        Injector injector = Guice.createInjector(Stage.PRODUCTION, new CommonModules());
        logger.info("Modules loaded");

        ServerBootstrap server = injector.getInstance(ServerBootstrap.class);
        logger.info("Bootstrapper created");

        server.configure(args);
        server.start();
        logger.info("Server started");
    }

    private static String getVersion() {
        String version = MavenUtil.getPom("net.orfjackal.dimdwarf", "dimdwarf-core").getProperty("version");
        return version != null ? version : "<unknown version>";
    }
}
