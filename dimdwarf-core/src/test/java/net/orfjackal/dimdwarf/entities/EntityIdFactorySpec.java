// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObjectId;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class EntityIdFactorySpec extends Specification<Object> {

    private static final long LARGEST_USED_ID = 42;

    private EntityIdFactory factory;

    public void create() throws Exception {
        factory = new EntityIdFactory(LARGEST_USED_ID);
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
