---
layout: page
---
{% include JB/setup %}

Common test runner for the JVM. Natively supports running tests in parallel, class loader caching, test order priorization and other measures to run tests faster. Contains the full stack for running tests (the responsibilities which used to be spread over build tools, IDEs and test runners) in order to overcome limitations of previously existing tools. Will overcome JUnit's test runner's limitations to better support all testing frameworks on the JVM.

Jumi is not only open source, but also open development. The process of how Jumi is developed is being screencasted at <http://www.orfjackal.net/lets-code> The long-term roadmap is in [ROADMAP.txt](https://github.com/orfjackal/jumi/blob/master/ROADMAP.txt) and the short-term task list is in [TODO.txt](https://github.com/orfjackal/jumi/blob/master/TODO.txt).

- Mailing list: <https://groups.google.com/d/forum/jumi-test-runner>
- Downloads: [fi.jumi in Maven Central](http://search.maven.org/#search%7Cga%7C1%7Cg%3A%22fi.jumi%22) ([Release Notes](https://github.com/orfjackal/jumi/blob/master/RELEASE-NOTES.md))
- Source code:
    - <https://github.com/orfjackal/jumi>
    - <https://github.com/orfjackal/jumi-scripts>
    - <https://github.com/orfjackal/sbt-jumi>
- License: [Apache License 2.0](http://www.apache.org/licenses/LICENSE-2.0)
- Developer: [Esko Luontola](https://github.com/orfjackal) ([@orfjackal](http://twitter.com/orfjackal))


Main Features
-------------

- **Runs Tests in Parallel** - Faster feedback by distributing the load over all your CPU cores. A testing framework that runs on Jumi can easily run even each test method in its own thread.

- **Accurate Test Output** - When a test prints something to System.out/err, Jumi will show the output exactly inside that test in the results (unlike JUnit and many IDEs). Also when multiple tests are run in parallel, the output from each of them is isolated from each other.

- **Backward Compatible with JUnit** - Runs your existing JUnit tests and also tests for the numerous other testing frameworks that support the JUnit test runner.

- **Expressive Execution Model** - Supports better all testing frameworks. Unlike with JUnit, with Jumi it's not necessary for a testing framework to know what tests exist before it executes the tests. Also tests may consist of multiple steps ("nested tests"), instead of just a flat JUnit-like organization.

[Continue to the documentation](https://github.com/orfjackal/jumi/wiki) to find out how to run your tests on Jumi.


Articles
--------

[Faster JUnit Tests with Jumi Test Runner and Class Loader Caching](http://blog.orfjackal.net/2013/02/faster-junit-tests-with-jumi-test.html) (2013-02-12)  
*Benchmarks of Jumi's performance, especially after some optimizations have been implemented.*

[Announcing the First Release of the Jumi Test Runner for the JVM](http://www.youtube.com/watch?v=Ggi6yutRZ9Y) (2012-09-19)  
*Motivation and benefits of the Jumi test runner, and how testing framework, build tool and IDE developers can get started on implementing Jumi support.*


What's compatible with Jumi?
------------------

Jumi has just recently been released and it's missing some important features, but it's already at a stage where early adopters can start using it and testing framework developers can implement support for it. Here is a list of frameworks and tools that already have Jumi support:

- [JUnit](http://www.junit.org/) and **any testing frameworks that run on JUnit**, through Jumi's JUnit backward compatibility (parallelism is limited to test class level)
- [Specsy](http://specsy.org/), a testing framework for Scala, Groovy, Java and easily any other JVM-based language


Project Goals
-------------

- **Reliability** - A test runner is the most important tool of a software developer, second only to a compiler. Thus it should have [zero bugs](http://jamesshore.com/Agile-Book/no_bugs.html). In the unlikely case that you find a bug from Jumi, you will be richly rewarded.

- **Speed** - When tests are run after each change to a line of code, many times per minute, [speed matters](http://agileinaflash.blogspot.com/2009/02/first.html). Jumi will take any measures necessary (which don't compromise reliability) to give faster feedback.

- **Usability** - Oftentimes, high usability in the user interface affects also design decisions at the system's lowest implementation levels. By covering the whole stack of running tests, from UI to class loading, Jumi will be able to maximize its usability.

- **Compatibility** - Jumi attempts to become the next *de facto* test runner on the JVM. Thus it needs to integrate well with all tools and testing frameworks. We take backward compatibility seriously and will run any [consumer contract tests](http://martinfowler.com/articles/consumerDrivenContracts.html) from framework and tool developers. Any incompatible changes will be done carefully over a transition period.

- **Simplicity** - The system should be as simple as possible, but no simpler. Adding new features should be done with great criticism and unnecessary features should be removed. To achieve high reliability, [simplicity](http://www.jbrains.ca/permalink/the-four-elements-of-simple-design) in implementation is critical.

More details on the project's motivation can be read from its original announcements at the [junit](http://tech.groups.yahoo.com/group/junit/message/22933) and [scala-tools](http://scala-programming-language.1934581.n4.nabble.com/scala-tools-Common-Test-Runner-for-JVM-td2536290.html) mailing lists.


License
-------

Copyright © 2011-{{ site.time | date: "%Y" }} Esko Luontola <[www.orfjackal.net](http://www.orfjackal.net/)>  
This software is released under the Apache License 2.0.  
The license text is at <http://www.apache.org/licenses/LICENSE-2.0>
