// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;
import java.util.concurrent.Executor;

@Immutable
public class FilterChain implements Executor {

    private final Filter[] chain;

    @Inject
    public FilterChain(Filter[] chain) {
        this.chain = chain;
    }

    public void execute(Runnable command) {
        execute(command, 0);
    }

    private void execute(Runnable command, int currentFilter) {
        if (currentFilter < chain.length) {
            Runnable nextInChain = new FilterRecursion(command, currentFilter + 1);
            chain[currentFilter].filter(nextInChain);
        } else {
            command.run();
        }
    }

    private class FilterRecursion implements Runnable {

        private final Runnable command;
        private final int nextFilter;

        public FilterRecursion(Runnable command, int nextFilter) {
            this.command = command;
            this.nextFilter = nextFilter;
        }

        public void run() {
            execute(command, nextFilter);
        }
    }
}
