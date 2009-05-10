// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.util.Exceptions;
import org.slf4j.*;

import javax.annotation.concurrent.Immutable;

/**
 * @author Esko Luontola
 * @since 12.11.2008
 */
@Immutable
public class TransactionFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(TransactionFilter.class);

    private final TransactionCoordinator tx;

    @Inject
    public TransactionFilter(TransactionCoordinator tx) {
        this.tx = tx;
    }

    public void filter(Runnable nextInChain) {
        try {
            nextInChain.run();
            tx.prepareAndCommit();
        } catch (Throwable t) {
            logger.info("Task failed, rolling back its transaction", t);
            tx.rollback();
            throw Exceptions.throwAsUnchecked(t);
        }
    }
}
