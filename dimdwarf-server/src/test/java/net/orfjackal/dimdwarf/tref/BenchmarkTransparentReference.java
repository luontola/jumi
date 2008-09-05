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

package net.orfjackal.dimdwarf.tref;

import net.orfjackal.dimdwarf.api.internal.EntityReference;
import static net.orfjackal.dimdwarf.tref.BenchmarkTransparentReference.Strategy.CGLIB_PROXYING;
import static net.orfjackal.dimdwarf.tref.BenchmarkTransparentReference.Strategy.JDK_PROXYING;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Esko Luontola
 * @since 24.1.2008
 */
public class BenchmarkTransparentReference {

    // TODO: is this anymore needed? should this be converted to use the Benchmark class?

    enum Strategy {
        JDK_PROXYING, CGLIB_PROXYING
    }

    public static final int MILLIS_TO_NANOS = 1000 * 1000;

    public static final int REPEATS = 10;

    public static final int CREATE_ITERATIONS = 10 * 1000 * 1000;
    public static final int DESERIAL_ITERATIONS = 10 * 1000;
    public static final int CALL_ITERATIONS = 1000 * 1000 * 1000;

    //private static Strategy current = JDK_PROXYING;
    private static Strategy current = CGLIB_PROXYING;
    private static TransparentReferenceFactory factory;
    private static int junk = 0;

    public static void main(String[] args) throws Exception {
//        MockAppContext.install();

        System.out.println("Current strategy: " + current + "\n");
        if (current == JDK_PROXYING) {
//            factory = new TransparentReferenceJdkProxyFactory();
        } else if (current == CGLIB_PROXYING) {
            factory = new TransparentReferenceCglibProxyFactory(AppContext.getDataManager());
        }

        for (int i = 0; i < REPEATS; i++) {
            long createRef = createReference(CREATE_ITERATIONS);
            long createProxy = createTransparentReference(CREATE_ITERATIONS);
            long deserialRef = deserializeReference(DESERIAL_ITERATIONS);
            long deserialProxy = deserializeTransparentReference(DESERIAL_ITERATIONS);
            long callDirect = callMethod(CALL_ITERATIONS);
            long callRef = callRefMethod(CALL_ITERATIONS);
            long callProxy = callProxyMethod(CALL_ITERATIONS / 10);

            System.out.println(result("create ref", createRef, CREATE_ITERATIONS));
            System.out.println(result("create proxy", createProxy, CREATE_ITERATIONS));
            System.out.println(result("deser ref", deserialRef, DESERIAL_ITERATIONS));
            System.out.println(result("deser proxy", deserialProxy, DESERIAL_ITERATIONS));
            System.out.println(result("call direct", callDirect, CALL_ITERATIONS));
            System.out.println(result("call ref", callRef, CALL_ITERATIONS));
            System.out.println(result("call proxy", callProxy, CALL_ITERATIONS / 10));
            System.out.println();
        }

        // prevent JIT compiler from optimizing away all benchmarks
        System.out.println("(Junk: " + junk + ")");

//        MockAppContext.uninstall();
    }

    private static String result(String name, long totalMillis, int iterations) {
        double oneNanos = totalMillis * (((double) MILLIS_TO_NANOS) / iterations);
        return name + ": \t" + oneNanos + " ns  \t(total " + totalMillis + " ms)";
    }

    // create

    private static long createReference(int iterations) {
        DummyManagedObject managedObject = new DummyManagedObject();

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            Object reference = AppContext.getDataManager().createReference(managedObject);
            junk += (reference == null) ? 1 : 2;
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static long createTransparentReference(int iterations) {
        DummyManagedObject managedObject = new DummyManagedObject();

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            Object reference = factory.createTransparentReference(managedObject);
            junk += (reference == null) ? 1 : 2;
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    // deserialize

    private static long deserializeReference(int iterations) throws IOException, ClassNotFoundException {
        Object reference = AppContext.getDataManager().createReference(new DummyManagedObject());
        byte[] bytes = TransparentReferenceTest.serializeObject(reference);

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            Object o = TransparentReferenceTest.deserializeObject(bytes);
            junk += (o == null) ? 1 : 2;
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static long deserializeTransparentReference(int iterations) throws IOException, ClassNotFoundException {
        Object reference = factory.createTransparentReference(new DummyManagedObject());
        byte[] bytes = TransparentReferenceTest.serializeObject(reference);

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            Object o = TransparentReferenceTest.deserializeObject(bytes);
            junk += (o == null) ? 1 : 2;
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    // call

    private static long callMethod(int iterations) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        DummyInterface1 object = new DummyManagedObject();
//        Method method = DummyInterface1.class.getMethod("dummyMethod");

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
//            dummy += (Integer) method.invoke(object);
            junk += object.dummyMethod();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static long callRefMethod(int iterations) {
        EntityReference<DummyManagedObject> reference = AppContext.getDataManager().createReference(new DummyManagedObject());

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            DummyInterface1 object = reference.get();
            junk += object.dummyMethod();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }

    private static long callProxyMethod(int iterations) {
        DummyInterface1 reference = (DummyInterface1) factory.createTransparentReference(new DummyManagedObject());

        long start = System.currentTimeMillis();
        for (int i = 0; i < iterations; i++) {
            junk += reference.dummyMethod();
        }
        long end = System.currentTimeMillis();
        return end - start;
    }
}
