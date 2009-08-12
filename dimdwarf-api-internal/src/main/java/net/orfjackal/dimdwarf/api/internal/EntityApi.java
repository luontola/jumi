// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api.internal;

/**
 * @author Esko Luontola
 * @since 12.8.2009
 */
public interface EntityApi {

    boolean isEntity(Object obj);

    boolean isTransparentReference(Object obj);
}
