// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.entities;

import java.io.Serializable;

/**
 * @author Esko Luontola
 * @since 4.9.2008
 */
public class DummyObject implements DummyInterface, Serializable {
    private static final long serialVersionUID = 1L;

    public Object other;

    public Object getOther() {
        return other;
    }

    public void setOther(Object other) {
        this.other = other;
    }
}
