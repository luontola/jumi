// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.util.Clock;

import java.io.Serializable;
import java.util.concurrent.TimeUnit;

public abstract class AbstractSchedulingStrategy implements Serializable, SchedulingStrategy {
    private static final long serialVersionUID = 1L;

    private final long scheduledTime;
    private transient Clock clock;

    protected AbstractSchedulingStrategy(long scheduledTime, Clock clock) {
        this.scheduledTime = scheduledTime;
        this.clock = clock;
    }

    @Inject
    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public long getScheduledTime() {
        return scheduledTime;
    }

    public long getDelay(TimeUnit unit) {
        return unit.convert(scheduledTime - clock.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }

    protected Clock getClock() {
        return clock;
    }
}
