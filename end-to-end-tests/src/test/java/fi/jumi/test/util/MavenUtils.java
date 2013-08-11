// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import org.w3c.dom.*;

import javax.xml.xpath.*;
import java.nio.file.Path;
import java.util.*;

public class MavenUtils {

    public static List<String> getRuntimeDependencies(Path pomFile) throws Exception {
        return getRuntimeDependencies(XmlUtils.parseXml(pomFile));
    }

    public static List<String> getRuntimeDependencies(Document pom) throws XPathExpressionException {
        NodeList nodes = (NodeList) XmlUtils.xpath(
                "/project/dependencies/dependency[not(scope) or scope='compile' or scope='runtime']",
                pom, XPathConstants.NODESET);

        List<String> results = new ArrayList<>();
        for (int i = 0; i < nodes.getLength(); i++) {
            Node dependency = nodes.item(i);

            String groupId = XmlUtils.xpath("groupId", dependency);
            String artifactId = XmlUtils.xpath("artifactId", dependency);
            results.add(groupId + ":" + artifactId);
        }
        return results;
    }
}
