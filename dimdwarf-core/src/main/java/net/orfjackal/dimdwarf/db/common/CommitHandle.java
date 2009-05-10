// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.db.common;

import javax.annotation.WillClose;

/**
 * @author Esko Luontola
 * @since 18.11.2008
 */
public interface CommitHandle {

    @WillClose
    void commit();

    @WillClose
    void rollback();
}
