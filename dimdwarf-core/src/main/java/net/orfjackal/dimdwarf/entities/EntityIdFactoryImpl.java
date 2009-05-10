// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import com.google.inject.*;

import javax.annotation.concurrent.ThreadSafe;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 4.9.2008
 */
@Singleton
@ThreadSafe
public class EntityIdFactoryImpl implements EntityIdFactory {

    // using java.util.concurrent.atomic.AtomicLong would also be an option

    private BigInteger nextId;

    @Inject
    public EntityIdFactoryImpl(@MaxEntityId BigInteger largestUsedId) {
        nextId = largestUsedId.add(BigInteger.ONE);
    }

    public synchronized BigInteger newId() {
        BigInteger currentId = nextId;
        nextId = nextId.add(BigInteger.ONE);
        return currentId;
    }
}
