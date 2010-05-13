// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

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
