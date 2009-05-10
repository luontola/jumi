// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import jdave.*;
import jdave.junit4.JDaveRunner;
import net.orfjackal.dimdwarf.aop.conf.*;
import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.*;
import net.orfjackal.dimdwarf.entities.DummyObject;
import org.junit.runner.RunWith;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.util.CheckClassAdapter;

/**
 * @author Esko Luontola
 * @since 9.9.2008
 */
@RunWith(JDaveRunner.class)
@Group({"fast"})
public class AddMarkerInterfaceForEntitiesSpec extends Specification<Object> {

    private Object target;

    private static Object newInstrumentedInstance(Class<?> cls) throws Exception {
        ClassLoader loader = new TransformationTestClassLoader(cls.getName(), new AbstractTransformationChain() {
            protected ClassVisitor getAdapters(ClassVisitor cv) {
                cv = new CheckClassAdapter(cv);
                cv = new AddMarkerInterfaceForEntities(new DimdwarfAopApi(), cv);
                return cv;
            }
        });
        return loader.loadClass(cls.getName()).newInstance();
    }


    public class AClassWithNoAnnotations {

        public void create() throws Exception {
            target = newInstrumentedInstance(DummyObject.class);
        }

        public void isNotTransformed() {
            specify(Entities.isEntity(target), should.equal(false));
        }
    }

    public class AClassWithTheEntityAnnotation {

        public void create() throws Exception {
            target = newInstrumentedInstance(AnnotatedEntity.class);
        }

        public void isTransformedToAnEntity() {
            specify(Entities.isEntity(target));
        }
    }

    public class AClassWithTheEntityAnnotationAndMarkerInterface {

        public void doesNotHaveTheSameInterfaceAddedTwise() throws Exception {
            // The class loader will throw ClassFormatError if the same interface is declared twise.
            newInstrumentedInstance(AnnotatedEntityWithMarkerInterface.class);
        }
    }


    @Entity
    public static class AnnotatedEntity {
    }

    @Entity
    public static class AnnotatedEntityWithMarkerInterface implements EntityObject {
    }
}
