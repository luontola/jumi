// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop.conf;

import net.orfjackal.dimdwarf.aop.AopApi;

public class DimdwarfAopApi implements AopApi {

    public String getEntityAnnotation() {
        return "net/orfjackal/dimdwarf/api/Entity";
    }

    public String getEntityInterface() {
        return "net/orfjackal/dimdwarf/api/internal/EntityObject";
    }

    public String getEntityHelperClass() {
        return "net/orfjackal/dimdwarf/entities/tref/EntityHelper";
    }
}
