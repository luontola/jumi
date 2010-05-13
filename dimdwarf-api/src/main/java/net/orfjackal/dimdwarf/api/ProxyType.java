// Copyright © 2008-2010 Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.api;

/**
 * The kind of proxy to generate for given a target class.
 */
public enum ProxyType {

    /**
     * The proxy will extend {@link Object} and implement the same interfaces as the target class. This is the safest
     * option, but requires that the class has interfaces and is always used through its interfaces. Otherwise an
     * exception will happen at runtime, when some objects are deserialized or when trying to cast the proxy to the
     * type of the target class.
     */
    INTERFACE,

    /**
     * The proxy will extend the target class. This way the class does not need to be used through interfaces, but the
     * programmer needs to be aware of some restrictions to avoid bug traps:
     * <ul>
     * <li>The target class should not have any publicly accessible fields, because the proxy can not intercept field
     * access.</li>
     * <li>Neither should the class access the private fields of another instance of the same class (unfortunately Java
     * has only a class-private and not an instance-private scope), because it could very well be a proxy instead of
     * the actual object. The fields of the proxy are not initialized, so they will have a default value ({@code null},
     * {@code 0}, {@code false}).</li>
     * <li>Neither should the class be final or have any final methods, because the proxy won't be able to override
     * them otherwise. A final class will produce an exception when trying to create a proxy, but final fields may be
     * ignored silenty and produce unspecified behaviour.</li>
     * <li>The constructor of the target class will not be called when the proxy (which is its subclass) is created,
     * so the class does not need to have an accessible default constructor.</li>
     * </ul>
     */
    CLASS
}
