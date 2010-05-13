// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import net.orfjackal.dimdwarf.tx.Retryable;

import javax.annotation.concurrent.NotThreadSafe;

@NotThreadSafe
public class RetryOnRetryableExceptionsANumberOfTimes implements RetryPolicy {

    // FIXME: RetryOnRetryableExceptionsANumberOfTimes will be removed/refactored in new architecture

    private final int maxRetries;
    private boolean retryable = true;
    private int failures = 0;

    public RetryOnRetryableExceptionsANumberOfTimes(int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public void taskHasFailed(Throwable t) {
        retryable = (t instanceof Retryable)
                && ((Retryable) t).mayBeRetried();
        failures++;
    }

    // TODO: make this class stateless? - change method to shouldRetry(Throwable t, int previousRetries, long startTime)
    public boolean shouldRetry() {
        return retryable && failures <= maxRetries;
    }
}
