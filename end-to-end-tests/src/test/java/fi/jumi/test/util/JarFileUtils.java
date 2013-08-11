// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.test.util;

import com.google.common.collect.AbstractIterator;
import org.objectweb.asm.tree.ClassNode;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.jar.*;

import static org.junit.Assert.*;

public class JarFileUtils {

    public static Iterable<ClassNode> classesIn(final Path jarFile) {
        return () -> {
            try {
                return new ClassNodeIterator(jarFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static void walkZipFile(Path jarFile, SimpleFileVisitor<Path> visitor) throws Exception {
        URI uri = new URI("jar", jarFile.toUri().toString(), null);
        HashMap<String, String> env = new HashMap<>();
        try (FileSystem fs = FileSystems.newFileSystem(uri, env)) {
            Files.walkFileTree(fs.getPath("/"), visitor);
        }
    }

    public static Properties getProperties(Path jarFile, String resource) throws IOException {
        URLClassLoader cl = new URLClassLoader(new URL[]{jarFile.toUri().toURL()});
        try (InputStream in = cl.getResourceAsStream(resource)) {
            assertNotNull("resource not found: " + resource, in);

            Properties p = new Properties();
            p.load(in);
            return p;
        }
    }

    public static void checkAllClasses(Path jarFile, CompositeMatcher<ClassNode> matcher) {
        for (ClassNode classNode : classesIn(jarFile)) {
            matcher.check(classNode);
        }
        try {
            matcher.rethrowErrors();
        } catch (AssertionError e) {
            // XXX: get the parameterized runner improved so that it would be easier to see which of the parameters broke a test
            System.err.println("Found errors in " + jarFile);
            throw e;
        }
    }

    public static void assertContainsOnly(Path jarFile, List<String> expected) throws Exception {
        walkZipFile(jarFile, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
                assertTrue(jarFile + " contained a not allowed entry: " + file,
                        isWhitelisted(file, expected));
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private static boolean isWhitelisted(Path file, List<String> whitelist) {
        boolean allowed = false;
        for (String s : whitelist) {
            allowed |= file.startsWith("/" + s);
        }
        return allowed;
    }


    private static class ClassNodeIterator extends AbstractIterator<ClassNode> {

        private final JarInputStream in;

        public ClassNodeIterator(Path jarFile) throws IOException {
            // TODO: iterate this JAR using FileSystem instead of JarInputStream?
            // http://docs.oracle.com/javase/7/docs/technotes/guides/io/fsp/zipfilesystemprovider.html
            in = new JarInputStream(Files.newInputStream(jarFile));
        }

        @Override
        protected ClassNode computeNext() {
            try {
                JarEntry entry;
                while ((entry = in.getNextJarEntry()) != null) {
                    if (!isClassFile(entry)) {
                        continue;
                    }
                    return AsmUtils.readClass(in);
                }
                in.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return endOfData();
        }

        private static boolean isClassFile(JarEntry entry) {
            return !entry.isDirectory() && entry.getName().endsWith(".class");
        }
    }
}
