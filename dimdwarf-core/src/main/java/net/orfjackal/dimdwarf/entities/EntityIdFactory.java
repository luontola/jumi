// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 31.8.2008
 */
public interface EntityIdFactory {

    BigInteger newId();
}
