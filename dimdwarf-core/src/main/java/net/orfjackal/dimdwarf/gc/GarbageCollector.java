// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

import net.orfjackal.dimdwarf.tasks.util.IncrementalTask;

import java.util.List;

/**
 * @author Esko Luontola
 * @since 30.11.2008
 */
public interface GarbageCollector<T> {

    List<? extends IncrementalTask> getCollectorStagesToExecute();

    MutatorListener<T> getMutatorListener();

    Enum<?> getColor(T node);
}
