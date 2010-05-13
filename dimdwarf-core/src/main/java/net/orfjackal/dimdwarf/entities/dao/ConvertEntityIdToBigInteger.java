// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.dao;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.api.internal.EntityObjectId;
import net.orfjackal.dimdwarf.db.Converter;

import javax.annotation.Nullable;
import java.math.BigInteger;

public class ConvertEntityIdToBigInteger implements Converter<EntityId, BigInteger> {

    @Nullable
    public EntityId back(@Nullable BigInteger value) {
        if (value == null) {
            return null;
        }
        return new EntityObjectId(value.longValue());
    }

    @Nullable
    public BigInteger forth(@Nullable EntityId value) {
        if (value == null) {
            return null;
        }
        return value.toBigInteger();
    }
}
