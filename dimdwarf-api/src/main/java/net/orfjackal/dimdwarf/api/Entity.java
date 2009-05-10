// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api;

import java.lang.annotation.*;

/**
 * Classes annotated with {@link Entity} will maintain their identity when stored in the database.
 * <p/>
 * By default, entities should be referred only through their interfaces, because the system will replace
 * entities with proxy objects when they are persisted in the database. To create proxy objects which extend
 * the concrete class of the entity (instead of only implementing its interfaces), specify {@link ProxyType#CLASS}
 * as a value for this annotation.
 * <p/>
 * See pages 31-36 of the book
 * <a href="http://www.infoq.com/minibooks/domain-driven-design-quickly">Domain Driven Design Quickly</a>
 * to learn more about the difference between entities and value objects.
 *
 * @author Esko Luontola
 * @since 9.9.2008
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Entity {

    ProxyType value() default ProxyType.INTERFACE;
}
