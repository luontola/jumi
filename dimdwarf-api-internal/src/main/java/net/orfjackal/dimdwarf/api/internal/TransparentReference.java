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

package net.orfjackal.dimdwarf.api.internal;

/**
 * @author Esko Luontola
 * @since 31.1.2008
 */
public interface TransparentReference {

    Object getEntity();

    EntityReference<?> getEntityReference();

    Class<?> getType();

    /**
     * Returns {@code true} when (1) the other object is a transparent reference to the same entity
     * as this refers to, or (2) the other object is the same entity itself.
     * <p/>
     * This method and {@link EntityObject#equals} must follow the same contract.
     */
    boolean equals(Object obj);

    /**
     * Returns a hashCode which is remains the same through the whole lifecycle of the entity
     * (i.e. from its creation until its removal from the database).
     * <p/>
     * This method and {@link EntityObject#hashCode} must follow the same contract.
     */
    int hashCode();

    /**
     * The proxy will delegate to this method, so that the {@link TransparentReference} implementation
     * instead of the proxy will be serialized.
     */
    Object writeReplace();
}
