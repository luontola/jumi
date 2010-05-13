// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import net.orfjackal.dimdwarf.api.Entity;
import net.orfjackal.dimdwarf.api.internal.EntityObject;

import java.io.Serializable;

@Entity
public class DummyEntity implements DummyInterface, EntityObject, Serializable {
    private static final long serialVersionUID = 1L;

    public Object other;

    public DummyEntity() {
        this(null);
    }

    public DummyEntity(Object other) {
        this.other = other;
    }

    public Object getOther() {
        return other;
    }

    public void setOther(Object other) {
        this.other = other;
    }
}
