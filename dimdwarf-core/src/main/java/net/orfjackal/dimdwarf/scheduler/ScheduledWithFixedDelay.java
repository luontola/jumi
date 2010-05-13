// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
