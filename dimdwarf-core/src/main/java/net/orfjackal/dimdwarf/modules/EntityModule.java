/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 *     * Neither the name of the copyright holder nor the names of its contributors
 *       may be used to endorse or promote products derived from this software
 *       without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

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
        @Inject public CheckInnerClassSerialized listeneter1;
        @Inject public CheckDirectlyReferredEntitySerialized listeneter2;
        @Inject public InjectObjectsOnDeserialization listener3;

        public SerializationListener[] get() {
            return new SerializationListener[]{listeneter1, listeneter2, listener3};
        }
    }

    private static class SerializationReplacerListProvider implements Provider<SerializationReplacer[]> {
        @Inject public ReplaceEntitiesWithTransparentReferences replacer1;

        public SerializationReplacer[] get() {
            return new SerializationReplacer[]{replacer1};
        }
    }
}
