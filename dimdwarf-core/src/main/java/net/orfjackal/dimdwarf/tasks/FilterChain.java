/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.Inject;

import javax.annotation.concurrent.Immutable;
import java.util.concurrent.Executor;

/**
 * @author Esko Luontola
 * @since 12.11.2008
 */
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
