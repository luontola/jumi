// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObjectId;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 4.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityIdFactorySpec extends Specification<Object> {

    private static final long LARGEST_USED_ID = 42;

    private EntityIdFactoryImpl factory;

    public void create() throws Exception {
        factory = new EntityIdFactoryImpl(LARGEST_USED_ID);
    }


    public class AnEntityIdFactory {

        public void startsFromTheNextUnusedId() {
            EntityId nextUnused = new EntityObjectId(LARGEST_USED_ID + 1);
            EntityId id1 = factory.newId();
            specify(id1, should.equal(nextUnused));
        }

        public void incrementsTheIdOnEveryCall() {
            EntityId id1 = factory.newId();
            EntityId id2 = factory.newId();
            specify(id2, should.not().equal(id1));
        }
    }
}
