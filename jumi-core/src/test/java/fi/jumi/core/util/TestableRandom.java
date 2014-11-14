// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.util;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.lang.reflect.Field;
import java.util.*;

public class TestableRandom implements TestRule {

    private final long seed;
    private final Random random;
    private final List<Object> log = new ArrayList<>();

    public TestableRandom() {
        this(System.currentTimeMillis());
    }

    public TestableRandom(long seed) {
        this.seed = seed;
        this.random = new Random(seed);
    }

    @Override
    public Statement apply(Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                } catch (Throwable t) {
                    t.addSuppressed(new DebugInformation(getDebugInformation()));
                    throw t;
                }
            }
        };
    }

    public void resetSeed() {
        info("reset seed");
        random.setSeed(seed);
    }

    public byte nextByte() {
        return log((byte) random.nextInt());
    }

    public short nextShort() {
        return log((short) random.nextInt());
    }

    public char nextChar() {
        return log((char) random.nextInt());
    }

    public int nextInt() {
        return log(random.nextInt());
    }

    public int nextInt(int exclusiveMax) {
        return log(random.nextInt(exclusiveMax));
    }

    public long nextLong() {
        return log(random.nextLong());
    }


    // debug information

    public void info(String message) {
        log.add(new Info(message));
    }

    private <T> T log(T generatedValue) {
        log.add(generatedValue);
        return generatedValue;
    }

    private String getDebugInformation() {
        String message = TestableRandom.class.getSimpleName() + " seed was " + seed + "L";
        for (Object obj : log) {
            if (obj instanceof Info) {
                message += "\n- " + obj;
            } else {
                message += "\n- (" + formatType(obj) + ") " + obj;
            }
        }
        return message;
    }

    private static String formatType(Object obj) {
        try {
            Field f = obj.getClass().getField("TYPE");
            Class<?> primitiveType = (Class<?>) f.get(null);
            return primitiveType.toString();
        } catch (NoSuchFieldException e) {
            return obj.getClass().getSimpleName();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private static class Info {
        private final String message;

        public Info(String message) {
            this.message = message;
        }

        @Override
        public String toString() {
            return message;
        }
    }

    private static class DebugInformation extends RuntimeException {
        public DebugInformation(String message) {
            super("\n" + message);
        }
    }
}
