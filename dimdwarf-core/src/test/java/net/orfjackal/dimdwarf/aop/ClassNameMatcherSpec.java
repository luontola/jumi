// Copyright © 2008-2009, Esko Luontola. All Rights Reserved.
// This software is released under the MIT License.
// The license may be viewed at http://dimdwarf.sourceforge.net/LICENSE

package net.orfjackal.dimdwarf.aop;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

@RunWith(JDaveRunner.class)
@Group({"fast"})
public class ClassNameMatcherSpec extends Specification<Object> {

    private ClassNameMatcher matcher;

    public class WhenMatchingASingleClass {

        public void create() {
            matcher = new ClassNameMatcher("x.Foo");
        }

        public void thatClassIsMatched() {
            specify(matcher.matches("x.Foo"));
        }

        public void otherClassesAreNotMached() {
            specify(!matcher.matches("x.Bar"));
        }
    }

    public class WhenMatchingAllClassesInAPackage {

        public void create() {
            matcher = new ClassNameMatcher("x.*");
        }

        public void classesInThatPackageAreMatched() {
            specify(matcher.matches("x.Foo"));
        }

        public void classesInSubpackagesAreNotMatched() {
            specify(!matcher.matches("x.y.Foo"));
        }

        public void classesInOtherPackagesAreNotMatched() {
            specify(!matcher.matches("y.Foo"));
        }

        public void classesInPackagesWithTheSamePrefixAreNotMatched() {
            specify(!matcher.matches("xx.Foo"));
        }
    }

    public class WhenMatchingAllClassesInSubpackages {

        public void create() {
            matcher = new ClassNameMatcher("x.**");
        }

        public void classesInThatPackageAreMatched() {
            specify(matcher.matches("x.Foo"));
        }

        public void classesInSubpackagesAreMatched() {
            specify(matcher.matches("x.y.Foo"));
        }

        public void classesInOtherPackagesAreNotMatched() {
            specify(!matcher.matches("y.Foo"));
        }

        public void classesInPackagesWithTheSamePrefixAreNotMatched() {
            specify(!matcher.matches("xx.Foo"));
        }
    }
}
