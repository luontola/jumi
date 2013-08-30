// Copyright © 2011-2013, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.ipc;

import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class ProgressiveSleepWaitStrategyTest {

    private final List<String> spy = new ArrayList<>();
    private ProgressiveSleepWaitStrategy waitStrategy;

    @Test
    public void at_first_yields() {
        waitStrategy = new SpiedProgressiveSleepWaitStrategy(3, 1, 10);

        for (int i = 0; i < 3; i++) {
            waitStrategy.await();
        }

        assertThat(spy, is(asList("yield", "yield", "yield")));
    }

    @Test
    public void sleeps_progressively_longer_times() {
        waitStrategy = new SpiedProgressiveSleepWaitStrategy(0, 1, 10);

        for (int i = 0; i < 3; i++) {
            waitStrategy.await();
        }

        assertThat(spy, is(asList("sleep 1", "sleep 2", "sleep 3")));
    }

    @Test
    public void the_initial_sleep_time_is_configurable() {
        waitStrategy = new SpiedProgressiveSleepWaitStrategy(0, 5, 10);

        for (int i = 0; i < 3; i++) {
            waitStrategy.await();
        }

        assertThat(spy, is(asList("sleep 5", "sleep 6", "sleep 7")));
    }

    @Test
    public void sleeps_at_most_the_maximum_sleep_time() {
        waitStrategy = new SpiedProgressiveSleepWaitStrategy(0, 1, 10);
        for (int i = 0; i < 9; i++) {
            waitStrategy.await();
        }
        spy.clear();

        for (int i = 0; i < 3; i++) {
            waitStrategy.await();
        }

        assertThat(spy, is(asList("sleep 10", "sleep 10", "sleep 10")));
    }

    @Test
    public void on_reset_goes_back_to_beginning() {
        waitStrategy = new SpiedProgressiveSleepWaitStrategy(1, 1, 10);
        for (int i = 0; i < 9; i++) {
            waitStrategy.await();
        }
        spy.clear();

        waitStrategy.reset();
        for (int i = 0; i < 3; i++) {
            waitStrategy.await();
        }

        assertThat(spy, is(asList("yield", "sleep 1", "sleep 2")));
    }


    private class SpiedProgressiveSleepWaitStrategy extends ProgressiveSleepWaitStrategy {

        public SpiedProgressiveSleepWaitStrategy(int yieldCount, int initialSleepMillis, int maxSleepMillis) {
            super(yieldCount, initialSleepMillis, maxSleepMillis);
        }

        @Override
        protected void yield() {
            spy.add("yield");
        }

        @Override
        protected void sleep(int millis) {
            spy.add("sleep " + millis);
        }
    }
}
