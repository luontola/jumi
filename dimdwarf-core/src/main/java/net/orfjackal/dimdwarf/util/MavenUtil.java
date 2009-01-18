/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.util;

import org.slf4j.*;

import javax.annotation.Nullable;
import java.io.*;
import java.util.Properties;

/**
 * @author Esko Luontola
 * @since 15.8.2008
 */
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
