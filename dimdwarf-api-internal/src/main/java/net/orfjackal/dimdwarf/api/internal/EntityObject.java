// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

/**
 * Marker interface equivalent to Darkstar's ManagedObject.
 * <p/>
 * For performance and Darkstar compatibility reasons the system uses internally only this interface,
 * and not the {@link @Entity} annotation. Classes annotated with {@code @Entity} will be automatically
 * transformed to implement this interface.
 */
public interface EntityObject {
}
