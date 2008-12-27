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

package net.orfjackal.dimdwarf.aop;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.aop.conf.*;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityReference;
import net.orfjackal.dimdwarf.context.*;
import net.orfjackal.dimdwarf.entities.*;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

import java.lang.instrument.ClassFileTransformer;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class AddEqualsAndHashCodeMethodsForEntitiesSpec extends Specification<Object> {

    private int entityHelperCalled = 0;
    private Object target;

    public void create() throws Exception {
        // the EntityHelper class will call ReferenceFactory on every equals() and hashCode() operation
        ReferenceFactory factory = new ReferenceFactory() {
            public <T> EntityReference<T> createReference(T entity) {
                entityHelperCalled++;
                return new EntityReferenceImpl<T>(BigInteger.ONE, entity);
            }
        };
        ThreadContext.setUp(new FakeContext().with(ReferenceFactory.class, factory));
    }

    public void destroy() throws Exception {
        ThreadContext.tearDown();
    }

    private static Object newInstrumentedInstance(Class<?> cls) throws Exception {
        return instrumentClass(cls).newInstance();
    }

    private static Class<?> instrumentClass(Class<?> cls) throws ClassNotFoundException {
        final AopApi api = new DimdwarfAopApi();
        ClassFileTransformer transformer = new AbstractTransformationChain() {
            protected ClassVisitor getAdapters(ClassVisitor cv) {
                cv = new CheckClassAdapter(cv);
                cv = new AddEqualsAndHashCodeMethodsForEntities(api, cv);
                cv = new AddMarkerInterfaceForEntities(api, cv);
                return cv;
            }
        };
        ClassLoader loader = new TransformationTestClassLoader(cls.getPackage().getName() + ".*", transformer);
        return loader.loadClass(cls.getName());
    }


    public class ANormalObject {

        public void create() throws Exception {
            target = newInstrumentedInstance(DummyObject.class);
        }

        public void doesNotDelegateItsEqualsMethod() {
            target.equals(new Object());
            specify(entityHelperCalled, should.equal(0));
        }

        public void doesNotDelegateItsHashCodeMethod() {
            target.hashCode();
            specify(entityHelperCalled, should.equal(0));
        }
    }

    public class AnEntityWithNoEqualsAndHashCodeMethods {

        public void create() throws Exception {
            target = newInstrumentedInstance(DummyEntity.class);
        }

        public void delegatesItsEqualsMethodToEntityHelper() {
            target.equals(new Object());
            specify(entityHelperCalled, should.equal(1));
        }

        public void delegatesItsHashCodeMethodToEntityHelper() {
            target.hashCode();
            specify(entityHelperCalled, should.equal(1));
        }
    }

    public class AnEntityWithACustomEqualsMethod {

        public void create() throws Exception {
            target = newInstrumentedInstance(EntityWithEquals.class);
        }

        public void doesNotDelegateItsEqualsMethod() {
            target.equals(new Object());
            specify(entityHelperCalled, should.equal(0));
        }

        public void delegatesItsHashCodeMethodToEntityHelper() {
            target.hashCode();
            specify(entityHelperCalled, should.equal(1));
        }
    }

    public class AnEntityWithACustomHashCodeMethod {

        public void create() throws Exception {
            target = newInstrumentedInstance(EntityWithHashCode.class);
        }

        public void delegatesItsEqualsMethodToEntityHelper() {
            target.equals(new Object());
            specify(entityHelperCalled, should.equal(1));
        }

        public void doesNotDelegateItsHashCodeMethod() {
            target.hashCode();
            specify(entityHelperCalled, should.equal(0));
        }
    }

    public class ASubclassOfAnAlreadyInstrumentedEntity {

        public void create() throws Exception {
            target = newInstrumentedInstance(SubclassOfEntityWithHashCode.class);
        }

        public void isNotInstrumentedASecondTime() {
            // If the subclasses would be instrumented, then it could override a custom
            // equals/hashCode method in the superclass. 
            target.hashCode();
            specify(entityHelperCalled, should.equal(0));
        }
    }

    public class AnInterfaceWhichIsAccidentallyMarkedAsAnEntity {

        public void shouldNotBeInstrumented() throws Exception {
            // If methods are added to an interface, the class loader will throw a ClassFormatError
            specify(instrumentClass(InterfaceMarkedAsEntity.class), should.not().equal(null));
        }
    }


    @Entity
    public static class EntityWithEquals {
        public boolean equals(Object obj) {
            return super.equals(obj);
        }
    }

    @Entity
    public static class EntityWithHashCode {
        public int hashCode() {
            return super.hashCode();
        }
    }

    public static class SubclassOfEntityWithHashCode extends EntityWithHashCode {
    }

    @Entity
    public static interface InterfaceMarkedAsEntity {
    }
}

// TODO: when calling the equals/hashCode method of a transparent reference proxy whose target class has
// custom equals/hashCode methods, the method call should be delegated to the actual entity and not the proxy
