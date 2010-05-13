// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import net.orfjackal.dimdwarf.util.Clock;

public class ScheduledAtFixedRate extends AbstractSchedulingStrategy {
    private static final long serialVersionUID = 1L;

    private final long period;

    public static SchedulingStrategy create(long initialDelay, long period, Clock clock) {
        long scheduledTime = initialDelay + clock.currentTimeMillis();
        return new ScheduledAtFixedRate(scheduledTime, period, clock);
    }

    private ScheduledAtFixedRate(long scheduledTime, long period, Clock clock) {
        super(scheduledTime, clock);
        this.period = period;
    }

    public SchedulingStrategy nextRepeatedRun() {
        long nextScheduledTime = getScheduledTime() + period;
        return new ScheduledAtFixedRate(nextScheduledTime, period, getClock());
    }
}
