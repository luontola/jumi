// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import net.orfjackal.dimdwarf.util.Clock;

public class ScheduledWithFixedDelay extends AbstractSchedulingStrategy {
    private static final long serialVersionUID = 1L;

    private final long delay;

    public static SchedulingStrategy create(long initialDelay, long delay, Clock clock) {
        long scheduledTime = initialDelay + clock.currentTimeMillis();
        return new ScheduledWithFixedDelay(scheduledTime, delay, clock);
    }

    private ScheduledWithFixedDelay(long scheduledTime, long delay, Clock clock) {
        super(scheduledTime, clock);
        this.delay = delay;
    }

    public SchedulingStrategy nextRepeatedRun() {
        long nextScheduledTime = getClock().currentTimeMillis() + delay;
        return new ScheduledWithFixedDelay(nextScheduledTime, delay, getClock());
    }
}
