// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks;

import com.google.inject.Inject;
import net.orfjackal.dimdwarf.tx.TransactionCoordinator;
import net.orfjackal.dimdwarf.util.Exceptions;
import org.slf4j.*;

import javax.annotation.concurrent.Immutable;

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
