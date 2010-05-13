// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.scheduler;

import java.io.Serializable;

public class DummyTask implements Runnable, Serializable {
    private static final long serialVersionUID = 1L;

    private final String dummyId;

    public DummyTask(String dummyId) {
        this.dummyId = dummyId;
    }

    public String getDummyId() {
        return dummyId;
    }

    public void run() {
    }

    public boolean equals(Object obj) {
        if (obj instanceof DummyTask) {
            DummyTask other = (DummyTask) obj;
            return dummyId.equals(other.dummyId);
        }
        return false;
    }
}
