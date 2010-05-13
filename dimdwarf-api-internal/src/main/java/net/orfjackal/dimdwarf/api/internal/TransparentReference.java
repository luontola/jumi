// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

public interface TransparentReference {

    Object getEntity$TREF();

    EntityReference<?> getEntityReference$TREF();

    Class<?> getType$TREF();

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
