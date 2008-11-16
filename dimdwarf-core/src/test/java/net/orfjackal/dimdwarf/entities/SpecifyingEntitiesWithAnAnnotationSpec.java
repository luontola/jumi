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

package net.orfjackal.dimdwarf.entities;

import jdave.Group;
import jdave.Specification;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.aop.DimdwarfApi;
import net.orfjackal.dimdwarf.aop.MarkAsEntitiesAllClassesAnnotatedWith;
import net.orfjackal.dimdwarf.aop.TransformationTestClassLoader;
import net.orfjackal.dimdwarf.aop.agent.AbstractTransformationChain;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.Entities;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class SpecifyingEntitiesWithAnAnnotationSpec extends Specification<Object> {

    private Object target;

    private static Object newInstrumentedInstance(Class<?> cls) throws Exception {
        ClassLoader loader = new TransformationTestClassLoader(cls.getName(), new AbstractTransformationChain() {
            protected ClassVisitor getAdapters(ClassVisitor cv) {
                cv = new CheckClassAdapter(cv);
                cv = new MarkAsEntitiesAllClassesAnnotatedWith(DimdwarfApi.ENTITY_ANNOTATION, cv);
                return cv;
            }
        });
        return loader.loadClass(cls.getName()).newInstance();
    }


    public class AClassWithNoAnnotations {

        public Object create() throws Exception {
            target = newInstrumentedInstance(DummyObject.class);
            return null;
        }

        public void isNotTransformed() {
            specify(Entities.isEntity(target), should.equal(false));
        }
    }

    public class AClassWithTheEntityAnnotation {

        public Object create() throws Exception {
            target = newInstrumentedInstance(AnnotatedEntity.class);
            return null;
        }

        public void isTransformedToAnEntity() {
            specify(Entities.isEntity(target));
        }
    }


    @Entity
    public static class AnnotatedEntity {

    }
}
