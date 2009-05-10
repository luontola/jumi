// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

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
