/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
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
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
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

package net.orfjackal.dimdwarf.context;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Esko Luontola
 * @since 12.11.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class FilterChainSpec extends Specification<Object> {

    private FilterChain chain;
    private Runnable command = new DummyCommand();
    private List<String> executionOrder = new ArrayList<String>();

    public class WhenThereAreNoFilters {

        public void create() {
            chain = new FilterChain(new Filter[0]);
            chain.execute(command);
        }

        public void theCommandIsExecuted() {
            specify(executionOrder, should.containInOrder("command"));
        }
    }

    public class WhenThereIsOneFilter {

        public void create() {
            chain = new FilterChain(new Filter[]{
                    new DummyFilter("A")
            });
            chain.execute(command);
        }

        public void theFilterIsExecutedAroundTheCommand() {
            specify(executionOrder, should.containInOrder(
                    "enter A",
                    "command",
                    "exit A"
            ));
        }
    }

    public class WhenThereAreManyFilters {

        public void create() {
            chain = new FilterChain(new Filter[]{
                    new DummyFilter("A"),
                    new DummyFilter("B")
            });
            chain.execute(command);
        }

        public void theFiltersAreNestedAroundTheCommandInTheDeclaredOrder() {
            specify(executionOrder, should.containInOrder(
                    "enter A",
                    "enter B",
                    "command",
                    "exit B",
                    "exit A"
            ));
        }
    }


    private class DummyCommand implements Runnable {
        public void run() {
            executionOrder.add("command");
        }
    }

    private class DummyFilter implements Filter {
        private final String name;

        public DummyFilter(String name) {
            this.name = name;
        }

        public void filter(Runnable next) {
            executionOrder.add("enter " + name);
            next.run();
            executionOrder.add("exit " + name);
        }
    }
}
