// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.gc;

import javax.annotation.Nullable;
import java.util.EventListener;

/**
 * @author Esko Luontola
 * @since 30.11.2008
 */
public interface MutatorListener<T> extends EventListener {

    void onNodeCreated(T node);

    void onReferenceCreated(@Nullable T source, T target);

    void onReferenceRemoved(@Nullable T source, T target);
}
