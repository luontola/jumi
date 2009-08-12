// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.internal.EntityReference;

/**
 * @author Esko Luontola
 * @since 5.9.2008
 */
public interface EntityReferenceFactory {

    <T> EntityReference<T> createReference(T entity);
}
