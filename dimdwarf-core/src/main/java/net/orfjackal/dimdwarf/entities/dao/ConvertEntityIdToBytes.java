// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities.dao;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.*;

import javax.annotation.Nullable;

public class ConvertEntityIdToBytes implements Converter<EntityId, Blob> {

    // TODO: get rid of the conversion to BigInteger, convert EntityId straight to bytes
    private final ConvertEntityIdToBigInteger keys1 = new ConvertEntityIdToBigInteger();
    private final ConvertBigIntegerToBytes keys2 = new ConvertBigIntegerToBytes();

    @Nullable
    public EntityId back(@Nullable Blob value) {
        return keys1.back(keys2.back(value));
    }

    @Nullable
    public Blob forth(@Nullable EntityId value) {
        return keys2.forth(keys1.forth(value));
    }
}
