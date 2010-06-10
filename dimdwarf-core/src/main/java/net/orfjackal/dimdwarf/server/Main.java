// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import net.orfjackal.dimdwarf.util.MavenUtil;
import org.slf4j.*;

import java.io.IOException;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws IOException {
        logger.info("Dimdwarf {} starting up", getVersion());

        // start up server etc.
        //Guice.createInjector(Stage.PRODUCTION, ...)

        logger.info("Shutting down");
    }

    private static String getVersion() throws IOException {
        String version = MavenUtil.getPom("net.orfjackal.dimdwarf", "dimdwarf-core").getProperty("version");
        return version != null ? version : "<unknown version>";
    }
}
