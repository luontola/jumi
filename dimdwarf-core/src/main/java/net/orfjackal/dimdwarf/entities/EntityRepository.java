// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.db.DatabaseTable;

import javax.annotation.Nonnull;
import java.math.BigInteger;

/**
 * @author Esko Luontola
 * @since 31.8.2008
 */
public interface EntityRepository extends DatabaseTable<BigInteger, Object> {

    @Nonnull
    Object read(BigInteger id) throws EntityNotFoundException;

    void update(BigInteger id, Object entity);

    void delete(BigInteger id);
}
