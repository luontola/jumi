// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.modules;

import com.google.inject.*;
import net.orfjackal.dimdwarf.api.EntityInfo;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.entities.dao.*;
import net.orfjackal.dimdwarf.entities.tref.*;
import net.orfjackal.dimdwarf.gc.entities.*;
import static net.orfjackal.dimdwarf.modules.DatabaseModule.*;
import net.orfjackal.dimdwarf.serial.*;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class EntityModule extends AbstractModule {

    protected void configure() {
        bind(EntityManager.class).to(EntityManagerImpl.class);
        bind(ReferenceFactory.class).to(ReferenceFactoryImpl.class);
        bind(EntityInfo.class).to(EntityInfoImpl.class);
        bind(TransparentReferenceFactory.class).to(TransparentReferenceFactoryImpl.class);

        bind(EntityIdFactory.class).to(EntityIdFactoryImpl.class);
        bind(BigInteger.class)
                .annotatedWith(MaxEntityId.class)
                .toInstance(BigInteger.ZERO); // TODO: import from database

        bind(EntityRepository.class).to(GcAwareEntityRepository.class);
        bind(databaseTableConnectionWithMetadata())
                .annotatedWith(EntitiesTable.class)
                .toProvider(databaseTableWithMetadata("entities"));

        bind(BindingRepository.class).to(GcAwareBindingRepository.class);
        bind(databaseTableConnection())
                .annotatedWith(BindingsTable.class)
                .toProvider(databaseTable("bindings"));

        bind(ObjectSerializer.class).to(ObjectSerializerImpl.class);
        bind(SerializationListener[].class).toProvider(SerializationListenerListProvider.class);
        bind(SerializationReplacer[].class).toProvider(SerializationReplacerListProvider.class);
    }

    private static class SerializationListenerListProvider implements Provider<SerializationListener[]> {
        @Inject public CheckInnerClassSerialized listener1;
        @Inject public CheckDirectlyReferredEntitySerialized listener2;
        @Inject public InjectObjectsOnDeserialization listener3;
        @Inject public EntityReferenceListener listener4;

        public SerializationListener[] get() {
            return new SerializationListener[]{listener1, listener2, listener3, listener4};
        }
    }

    private static class SerializationReplacerListProvider implements Provider<SerializationReplacer[]> {
        @Inject public ReplaceEntitiesWithTransparentReferences replacer1;

        public SerializationReplacer[] get() {
            return new SerializationReplacer[]{replacer1};
        }
    }
}
