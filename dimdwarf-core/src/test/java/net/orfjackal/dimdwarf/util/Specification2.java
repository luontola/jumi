// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.util;

import jdave.*;
import org.mockito.*;

public abstract class Specification2 extends Specification<Object> {

    private static final String MOCK_METHOD_DISABLED =
            "Use the mockito() or jmock() methods or the @Mock annotation instead";

    protected Specification2() {
        MockitoAnnotations.initMocks(this);
        addListener(new DefaultLifecycleListener() {
            public void afterContextInstantiation(Object contextInstance) {
                MockitoAnnotations.initMocks(contextInstance);
            }
        });
    }

    /**
     * @see org.mockito.Mockito#mock(java.lang.Class<T>)
     */
    public static <T> T mockito(Class<T> classToMock) {
        return Mockito.mock(classToMock);
    }

    /**
     * @see org.mockito.Mockito#mock(java.lang.Class<T>)
     */
    public static <T> T mockito(Class<T> classToMock, String name) {
        return Mockito.mock(classToMock, name);
    }

    /**
     * @see org.mockito.Mockito#mock(java.lang.Class<T>)
     */
    public static <T> T mockito(Class<T> classToMock, ReturnValues returnValues) {
        return Mockito.mock(classToMock, returnValues);
    }

    /**
     * @see jdave.mock.MockSupport#mock(java.lang.Class<T>, java.lang.String)
     */
    public <T> T jmock(Class<T> typeToMock, String name) {
        return super.mock(typeToMock, name);
    }

    /**
     * @see jdave.mock.MockSupport#mock(java.lang.Class<T>, java.lang.String)
     */
    public <T> T jmock(Class<T> typeToMock) {
        return super.mock(typeToMock);
    }

    public <T> T mock(Class<T> typeToMock, String name) {
        throw new UnsupportedOperationException(MOCK_METHOD_DISABLED);
    }

    public <T> T mock(Class<T> typeToMock) {
        throw new UnsupportedOperationException(MOCK_METHOD_DISABLED);
    }
}
