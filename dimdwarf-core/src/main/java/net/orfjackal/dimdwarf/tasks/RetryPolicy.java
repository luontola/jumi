// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

/**
 * @author Esko Luontola
 * @since 13.12.2008
 */
public interface RetryPolicy {

    void taskHasFailed(Throwable t);

    boolean shouldRetry();
}
