// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.tasks.util;

import java.util.Collection;

public interface IncrementalTask {

    Collection<? extends IncrementalTask> step();
}
