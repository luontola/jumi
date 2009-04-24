/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
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

package net.orfjackal.dimdwarf.util;

import jdave.*;
import org.mockito.*;

/**
 * @author Esko Luontola
 * @since 24.4.2009
 */
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
