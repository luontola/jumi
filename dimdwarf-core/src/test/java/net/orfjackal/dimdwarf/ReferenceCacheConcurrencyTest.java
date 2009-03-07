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

package net.orfjackal.dimdwarf;

import com.google.inject.util.ReferenceCache;
import junit.framework.TestCase;

import java.util.*;
import java.util.concurrent.*;

public class ReferenceCacheConcurrencyTest extends TestCase {

    // TODO: upgrade to Guice 2.0 and check that this bug does not appear there
    // http://groups.google.com/group/google-guice/browse_thread/thread/7cdff66a4b7acc9d
    public void testConcurrentGet() throws Exception {
        final ReferenceCache<String, String> cache = new ReferenceCache<String, String>() {
            protected String create(String key) {
                return key;
            }
        };
        ArrayList<Callable<String>> callsToGet = new ArrayList<Callable<String>>();
        for (int i = 0; i < 10; i++) {
            callsToGet.add(new Callable<String>() {
                public String call() throws Exception {
                    return cache.get("foo");
                }
            });
        }
        List<Future<String>> results = Executors.newCachedThreadPool().invokeAll(callsToGet);
        for (Future<String> result : results) {
            assertEquals("foo", result.get());
        }
    }
}
