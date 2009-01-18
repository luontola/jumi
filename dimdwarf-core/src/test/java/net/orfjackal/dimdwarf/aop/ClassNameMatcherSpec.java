/*
 * This file is part of Dimdwarf Application Server <http://dimdwarf.sourceforge.net/>
 *
 * Copyright (c) 2008-2009, Esko Luontola. All Rights Reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the following conditions are met:
 *
 *     * Redistributions of source code must retain the above copyright notice,
 *       this list of conditions and the following disclaimer.
 *
 *     * Redistributions in binary form must reproduce the above copyright notice,
 *       this list of conditions and the following disclaimer in the documentation
 *       and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.orfjackal.dimdwarf.aop;

import jdave.*;
import jdave.junit4.JDaveRunner;
import org.junit.runner.RunWith;

/**
 * @author Esko Luontola
 * @since 27.12.2008
 */
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
