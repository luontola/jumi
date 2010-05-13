// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.server;

import net.orfjackal.dimdwarf.util.MavenUtil;
import org.slf4j.*;

import java.io.IOException;

public class Startup {
    private static final Logger logger = LoggerFactory.getLogger(Startup.class);

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
