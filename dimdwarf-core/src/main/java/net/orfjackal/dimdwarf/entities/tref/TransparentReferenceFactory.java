// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.tref;

import net.orfjackal.dimdwarf.api.internal.*;


/**
 * @author Esko Luontola
 * @since 26.1.2008
 */
public interface TransparentReferenceFactory {

    /**
     * Creates a proxy for the specified entity, so that the proxy will have the same
     * external interface as the proxied entity, but the proxy itself does not need
     * to be wrapped into an {@link EntityReference}.
     */
    TransparentReference createTransparentReference(Object entity);

    /**
     * Creates a proxy from a {@link TransparentReferenceBackend} which is not yet proxied.
     * This is needed only during deserialization and should not be called elsewhere.
     */
    TransparentReference newProxy(TransparentReferenceBackend tref);
}
