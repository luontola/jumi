// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.EntityId;
import net.orfjackal.dimdwarf.db.Blob;
import net.orfjackal.dimdwarf.entities.dao.EntityDao;
import net.orfjackal.dimdwarf.serial.ObjectSerializer;
import net.orfjackal.dimdwarf.tasks.TaskScoped;

import javax.annotation.concurrent.NotThreadSafe;
import javax.inject.*;

@TaskScoped
@NotThreadSafe
public class EntityRepository implements EntitiesPersistedInDatabase {

    private final EntityDao database;
    private final ObjectSerializer serializer;
    private final Provider<EntitySerializationFilter> filter;

    @Inject
    public EntityRepository(EntityDao database, ObjectSerializer serializer, Provider<EntitySerializationFilter> filter) {
        this.database = database;
        this.serializer = serializer;
        this.filter = filter;
    }

    public boolean exists(EntityId id) {
        return database.exists(id);
    }

    public Object read(EntityId id) {
        Blob bytes = database.read(id);
        if (bytes.equals(Blob.EMPTY_BLOB)) {
            throw new EntityNotFoundException("id=" + id);
        }
        return serializer.deserialize(bytes, filter.get());
    }

    public void update(EntityId id, Object entity) {
        Blob newData = serializer.serialize(entity, filter.get());
        if (hasBeenModified(id, newData)) {
            database.update(id, newData);
        }
    }

    private boolean hasBeenModified(EntityId id, Blob newData) {
        // TODO: Compare with bytes from reserializing the original bytes, and not with the original bytes. See http://www.projectdarkstar.com/forum/?topic=1328.msg9107#msg9107
        Blob oldData = database.read(id);
        return !oldData.equals(newData);
    }
}
