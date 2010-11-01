// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import org.hamcrest.*;

import static org.hamcrest.MatcherAssert.assertThat;

// TODO: implement SelfDescribing here instead of the subclasses?
public abstract class AsynchronousSink<T> implements SelfDescribing {

    private final Object lock = new Object();
    private final long timeout;

    public AsynchronousSink(long timeout) {
        this.timeout = timeout;
    }

    public void assertEventually(final Matcher<?> matcher) {
        synchronized (lock) {
            assertThat(this, new TypeSafeMatcher<AsynchronousSink<T>>() {
                protected boolean matchesSafely(AsynchronousSink<T> item) {
                    return AsynchronousSink.this.matches(matcher);
                }

                public void describeTo(Description description) {
                    description.appendDescriptionOf(matcher);
                }

                protected void describeMismatchSafely(AsynchronousSink<T> item, Description mismatchDescription) {
                    mismatchDescription.appendDescriptionOf(item);
                }
            });
        }
    }

    public final void append(T event) {
        synchronized (lock) {
            doAppend(event);
            lock.notifyAll();
        }
    }

    protected abstract void doAppend(T event);

    public final boolean matches(Matcher<?> matcher) {
        // TODO: consider moving the code from this class to an external assertEventually method
        Timeout timeout = new Timeout(this.timeout);

        synchronized (lock) {
            while (!matcher.matches(getContent())) {
                if (timeout.hasTimedOut()) {
                    return false;
                }
                timeout.waitUntilTimeout(lock);
            }
            return true;
        }
    }

    protected abstract T getContent();
}
