// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.db.DatabaseTable;
import net.orfjackal.dimdwarf.entities.dao.BindingDao;

import javax.annotation.concurrent.Immutable;
import javax.inject.Inject;

@Immutable
public class BindingRepository implements DatabaseTable<String, Object> {

    private final BindingDao bindings;
    private final ConvertEntityToEntityId entityToId;

    @Inject
    public BindingRepository(BindingDao bindings, ConvertEntityToEntityId entityToId) {
        this.bindings = bindings;
        this.entityToId = entityToId;
    }

    public boolean exists(String binding) {
        return bindings.exists(binding);
    }

    public Object read(String binding) {
        return entityToId.back(bindings.read(binding));
    }

    public void update(String binding, Object entity) {
        bindings.update(binding, entityToId.forth(entity));
    }

    public void delete(String binding) {
        bindings.delete(binding);
    }

    public String firstKey() {
        return bindings.firstKey();
    }

    public String nextKeyAfter(String currentKey) {
        return bindings.nextKeyAfter(currentKey);
    }
}
