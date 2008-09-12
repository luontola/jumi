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

package net.orfjackal.dimdwarf.entities.tref;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.aop.AddEqualsAndHashCodeMethodsForEntities;
import net.orfjackal.dimdwarf.aop.TransformationTestClassLoader;
import net.orfjackal.dimdwarf.aop.agent.AbstractTransformationChain;
import net.orfjackal.dimdwarf.api.impl.EntityReference;
import net.orfjackal.dimdwarf.api.impl.IEntity;
import net.orfjackal.dimdwarf.context.Context;
import net.orfjackal.dimdwarf.context.ThreadContext;
import net.orfjackal.dimdwarf.entities.DummyEntity;
import net.orfjackal.dimdwarf.entities.DummyObject;
import net.orfjackal.dimdwarf.entities.EntityReferenceImpl;
import net.orfjackal.dimdwarf.entities.ReferenceFactory;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class GeneratingEqualsAndHashCodeForEntitiesSpec extends Specification<Object> {

    private int referencesCreated = 0;
    private Object target;

    public void create() throws Exception {
        ReferenceFactory factory = new ReferenceFactory() {
            public <T> EntityReference<T> createReference(T entity) {
                referencesCreated++;
                return new EntityReferenceImpl<T>(BigInteger.ONE, entity);
            }
        };
        ThreadContext.setUp(new Context(ReferenceFactory.class, factory));
    }

    public void destroy() throws Exception {
        ThreadContext.tearDown();
    }

    private static Object newInstrumentedInstance(Class<?> cls) throws Exception {
        ClassLoader loader = new TransformationTestClassLoader(cls.getName(), new AbstractTransformationChain() {
            protected ClassVisitor getAdapters(ClassVisitor cv) {
                cv = new CheckClassAdapter(cv);
                cv = new AddEqualsAndHashCodeMethodsForEntities(cv);
                return cv;
            }
        });
        return loader.loadClass(cls.getName()).newInstance();
    }


    public class AnInstrumentedNormalObject {

        public Object create() throws Exception {
            target = newInstrumentedInstance(DummyObject.class);
            return null;
        }

        public void doesNotDelegateItsEqualsMethod() {
            target.equals(new Object());
            specify(referencesCreated, should.equal(0));
        }

        public void doesNotDelegateItsHashCodeMethod() {
            target.hashCode();
            specify(referencesCreated, should.equal(0));
        }
    }

    public class AnInstrumentedEntityWithNoEqualsAndHashCodeMethods {

        public Object create() throws Exception {
            target = newInstrumentedInstance(DummyEntity.class);
            return null;
        }

        public void delegatesItsEqualsMethodToEntityIdentity() {
            target.equals(new Object());
            specify(referencesCreated, should.equal(1));
        }

        public void delegatesItsHashCodeMethodToEntityIdentity() {
            target.hashCode();
            specify(referencesCreated, should.equal(1));
        }
    }

    public class AnInstrumentedEntityWithACustomEqualsMethod {

        public Object create() throws Exception {
            target = newInstrumentedInstance(EntityWithEquals.class);
            return null;
        }

        public void doesNotDelegateItsEqualsMethod() {
            target.equals(new Object());
            specify(referencesCreated, should.equal(0));
        }

        public void delegatesItsHashCodeMethodToEntityIdentity() {
            target.hashCode();
            specify(referencesCreated, should.equal(1));
        }
    }

    public class AnInstrumentedEntityWithACustomHashCodeMethod {

        public Object create() throws Exception {
            target = newInstrumentedInstance(EntityWithHashCode.class);
            return null;
        }

        public void delegatesItsEqualsMethodToEntityIdentity() {
            target.equals(new Object());
            specify(referencesCreated, should.equal(1));
        }

        public void doesNotDelegateItsHashCodeMethod() {
            target.hashCode();
            specify(referencesCreated, should.equal(0));
        }
    }


    public static class EntityWithEquals implements IEntity {
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    public static class EntityWithHashCode implements IEntity {
        public int hashCode() {
            return super.hashCode();
        }
    }
}
