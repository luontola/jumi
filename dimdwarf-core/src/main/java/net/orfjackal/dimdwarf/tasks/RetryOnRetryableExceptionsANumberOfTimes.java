// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import net.orfjackal.dimdwarf.tx.Retryable;

import javax.annotation.concurrent.NotThreadSafe;

/**
 * @author Esko Luontola
 * @since 13.12.2008
 */
@NotThreadSafe
public class RetryOnRetryableExceptionsANumberOfTimes implements RetryPolicy {

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
