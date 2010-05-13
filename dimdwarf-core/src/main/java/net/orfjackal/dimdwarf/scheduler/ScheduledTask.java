// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import javax.annotation.CheckReturnValue;
import java.util.concurrent.TimeUnit;

public interface ScheduledTask {

    @CheckReturnValue
    Runnable startScheduledRun();

    long getScheduledTime();

    long getDelay(TimeUnit unit);

    boolean isDone();

    boolean isCancelled();

    void setCancelled();
}
