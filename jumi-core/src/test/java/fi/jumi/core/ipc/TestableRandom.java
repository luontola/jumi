// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

import java.util.Random;

public class TestableRandom implements TestRule {

    private final long seed;
    private final Random random;

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
                    throw new ExtraMessageAssertionError(TestableRandom.class.getSimpleName() + " seed was " + seed + "L", t);
                }
            }
        };
    }

    public void resetSeed() {
        random.setSeed(seed);
    }

    public byte nextByte() {
        return (byte) random.nextInt();
    }

    public short nextShort() {
        return (short) random.nextInt();
    }

    public char nextChar() {
        return (char) random.nextInt();
    }

    public int nextInt() {
        return random.nextInt();
    }

    public int nextInt(int exclusiveMax) {
        return random.nextInt(exclusiveMax);
    }

    private static class ExtraMessageAssertionError extends AssertionError {
        private final String extraMessage;
        private final Throwable realException;

        public ExtraMessageAssertionError(String extraMessage, Throwable realException) {
            this.extraMessage = extraMessage;
            this.realException = realException;
            this.setStackTrace(realException.getStackTrace());
        }

        @Override
        public String toString() {
            return extraMessage + "\n" + realException.toString();
        }
    }
}
