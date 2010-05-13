// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.slf4j.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Properties;

public class MavenUtil {
    private static final Logger logger = LoggerFactory.getLogger(MavenUtil.class);

    private MavenUtil() {
    }

    public static Properties getPom(String groupId, String artifactId) {
        Properties p = new Properties();
        InputStream in = MavenUtil.class.getResourceAsStream("/META-INF/maven/" + groupId + "/" + artifactId + "/pom.properties");
        try {
            tryLoad(p, in);
        } catch (IOException e) {
            logger.warn("Unable to read pom.properties of artifact " + groupId + ":" + artifactId, e);
        }
        return p;
    }

    private static void tryLoad(Properties p, @Nullable InputStream in) throws IOException {
        try {
            if (in != null) {
                p.load(in);
            }
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
}
