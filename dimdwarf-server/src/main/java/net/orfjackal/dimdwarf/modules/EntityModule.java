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

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.google.inject.Provider;
import net.orfjackal.dimdwarf.entities.*;
import net.orfjackal.dimdwarf.entities.tref.ReplaceEntitiesWithTransparentReferences;
import net.orfjackal.dimdwarf.entities.tref.TransparentReferenceFactory;
import net.orfjackal.dimdwarf.entities.tref.TransparentReferenceFactoryImpl;
import static net.orfjackal.dimdwarf.modules.DatabaseModule.databaseTable;
import static net.orfjackal.dimdwarf.modules.DatabaseModule.databaseTableConnection;
import net.orfjackal.dimdwarf.serial.*;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 13.9.2008
 */
public class EntityModule extends AbstractModule {

    protected void configure() {

        bind(EntityManagerImpl.class);
        bind(EntityLoader.class).to(EntityManagerImpl.class);
        bind(ReferenceFactory.class).to(EntityManagerImpl.class);
        bind(TransparentReferenceFactory.class).to(TransparentReferenceFactoryImpl.class);
        bind(EntityIdFactory.class)
                .toProvider(new Provider<EntityIdFactory>() {
                    public EntityIdFactory get() {
                        return new EntityIdFactoryImpl(BigInteger.ZERO); // TODO: import from database
                    }
                });

        bind(EntityStorage.class).to(EntityStorageImpl.class);
        bind(databaseTableConnection())
                .annotatedWith(EntitiesTable.class)
                .toProvider(databaseTable("entities"));

        bind(BindingManager.class).to(BindingManagerImpl.class);
        bind(databaseTableConnection())
                .annotatedWith(BindingsTable.class)
                .toProvider(databaseTable("bindings"));

        bind(ObjectSerializer.class).to(ObjectSerializerImpl.class);
        bind(SerializationListener[].class).toProvider(new SerializationListenerListProvider());
        bind(SerializationReplacer[].class).toProvider(new SerializationReplacerListProvider());
    }

    private static class SerializationListenerListProvider implements Provider<SerializationListener[]> {
        @Inject Provider<CheckInnerClassSerialized> listeneter1;
        @Inject Provider<CheckDirectlyReferredEntitySerialized> listeneter2;
        @Inject Provider<InjectObjectsOnDeserialization> listener3;

        public SerializationListener[] get() {
            return new SerializationListener[]{listeneter1.get(), listeneter2.get(), listener3.get()};
        }
    }

    private static class SerializationReplacerListProvider implements Provider<SerializationReplacer[]> {
        @Inject Provider<ReplaceEntitiesWithTransparentReferences> replacer1;

        public SerializationReplacer[] get() {
            return new SerializationReplacer[]{replacer1.get()};
        }
    }
}
