// Copyright © 2011-2014, Esko Luontola <www.orfjackal.net>
// This software is released under the Apache License 2.0.
// The license text is at http://www.apache.org/licenses/LICENSE-2.0

package fi.jumi.core.config;

import fi.jumi.core.util.Boilerplate;

import javax.annotation.concurrent.Immutable;
import java.lang.reflect.Method;
import java.util.Properties;

@Immutable
class SystemProperty {

    // TODO: make generic and decouple from DaemonConfiguration?

    private final String systemProperty;
    private final DaemonConfiguration defaults;
    private final Method getter;
    private final Method setter;
    private final Class<?> type;

    public SystemProperty(String beanProperty, String systemProperty, DaemonConfiguration defaults) {
        this.systemProperty = systemProperty;
        this.defaults = defaults;
        try {
            getter = DaemonConfiguration.class.getMethod(getterName(beanProperty));
            type = getter.getReturnType();
            setter = DaemonConfigurationBuilder.class.getMethod(setterName(beanProperty), type);
        } catch (NoSuchMethodException e) {
            throw Boilerplate.rethrow(e);
        }
    }

    public void toSystemProperty(DaemonConfiguration source, Properties target) {
        Object value = get(source);
        Object defaultValue = get(defaults);

        if (!value.equals(defaultValue)) {
            target.setProperty(systemProperty, String.valueOf(value));
        }
    }

    public void parseSystemProperty(DaemonConfigurationBuilder target, Properties source) {
        String value = source.getProperty(systemProperty);
        if (value != null) {
            set(target, value);
        }
    }

    private Object get(DaemonConfiguration source) {
        try {
            return getter.invoke(source);
        } catch (Exception e) {
            throw Boilerplate.rethrow(e);
        }
    }

    private void set(DaemonConfigurationBuilder target, String value) {
        try {
            setter.invoke(target, parse(type, value));
        } catch (Exception e) {
            throw Boilerplate.rethrow(e);
        }
    }

    private static Object parse(Class<?> type, String value) {
        if (type == int.class) {
            return Integer.parseInt(value);
        }
        if (type == long.class) {
            return Long.parseLong(value);
        }
        if (type == boolean.class) {
            return Boolean.parseBoolean(value);
        }
        throw new IllegalArgumentException("unsupported type: " + type);
    }

    private static String getterName(String beanProperty) {
        return "get" + capitalize(beanProperty);
    }

    private static String setterName(String beanProperty) {
        return "set" + capitalize(beanProperty);
    }

    private static String capitalize(String s) {
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}
