
Release Notes
=============

### Jumi 0.5.390 (2013-08-07)

- Fixed `JumiBootstrap.enableDebugMode()` closing the launcher's `System.err` after the daemon process exits

### Jumi 0.5.376 (2013-07-30)

- Fixed a deadlock when the test classpath contains no directories

### Jumi 0.5.365 (2013-07-16)

- Added a progress bar to TextUI

### Jumi 0.4.350 (2013-07-10)

- Moved `TestBench` out of jumi-core's test-jar, into the main jumi-core.jar, so that declaring a dependency to it would be easier ([Issue #5](https://github.com/orfjackal/jumi/issues/5))

### Jumi 0.4.345 (2013-07-09)

- Bulletproofed `SuiteNotifier` and `TestNotifier` to detect incorrect notifier API usage
- `TestNotifier.fireTestFinished()` may now be called from a different thread than where the test was started. Fixes an incompatibility with the [specs2](http://specs2.org/) testing framework
- Added references from jumi-api's Javadocs to tests that prove the documentation's claims

### Jumi 0.4.317 (2013-02-10)

- Made `JumiBootstrap` more configurable

### Jumi 0.4.313 (2013-02-09)

This release makes it possible to run your existing JUnit tests using the Jumi test runner. All testing frameworks that run on JUnit should also run on Jumi, though incompatibilities are possible for testing frameworks that use JUnit's APIs incorrectly, because Jumi is stricter than JUnit about incorrect API usage.

JUnit tests benefit from test class level parallelism through Jumi's JUnit compatibility layer. For test method level parallelism, JUnit and other testing frameworks will need to implement their own Jumi driver. Visit [our wiki](https://github.com/orfjackal/jumi/wiki) for tips on making your test suites capable of being run in parallel (e.g. avoiding various forms of global mutable state).

- JUnit backward compatibility: Jumi now runs JUnit 3, JUnit 4 and `@RunWith` annotated test classes. Requires you to have JUnit on your test classpath
- Can configure the number of threads to use for running tests. Defaults to the number of CPU cores
- Reports internal errors to the user
- Sets the context class loader for test threads ([Issue #2](https://github.com/orfjackal/jumi/issues/2)), as required by many frameworks
- Fixed a deadlock when calling `Throwable.printStackTrace()` and printing to `System.err` concurrently ([Issue #3](https://github.com/orfjackal/jumi/issues/3))
- Prevents `Throwable.printStackTrace()` from being interleaved with printing to `System.out` from parallel threads of the same test run (this is in addition to the stdout/stderr isolation of parallel test runs that we've had since ever). Note that we can handle only the common case; `Throwable.printStackTrace(System.out)` can still interleave with printing to `System.err` as usual
- Fixed a crash if a test prints something (from a background thread) after it's finished ([Issue #1](https://github.com/orfjackal/jumi/issues/1)). The current text UI hides the late printed output, but in the future we'll create a GUI that will show all of the output
- Will not try to run abstract test classes ([Issue #4](https://github.com/orfjackal/jumi/issues/4))
- Will not try to run classes that are not identified as a test class, even though the class name matches the test file pattern. This the same as how the Maven Surefire Plugin and most IDEs work

### Jumi 0.3.257 (2013-01-07)

This release makes it possible to use Java 7's glob patterns to configure that which test classes should be run. The default pattern finds all top-level classes whose name ends with "Test". For now only `.class` test files are supported, but in the future Jumi may also recognize tests in arbitrary files.

After upgrading to this release, it is recommended to find and remove all `.jumi` directories on your hard drive (typically in a project's working copy). From this release onwards only one such directory will be created in `~/.jumi`

- Automatic discovery of test classes based on file name patterns
- Jumi writes its temporary files now to `~/.jumi` instead of under the current working directory
- Doesn't anymore require all thrown exception classes to be in the launcher's classpath
- The working directory is now configurable

### Jumi 0.2.241 (2012-12-25)

- `JumiBootstrap` now throws an `AssertionError` or calls `System.exit(1)` if the suite contained test failures

### Jumi 0.2.235 (2012-12-24)

This release is ready for early adopters to start using Jumi. It adds support for running multiple test classes (though they must be listed manually; automatic test discovery will be added in the following release).

- Runs multiple test classes in a suite
- Added `fi.jumi.launcher.JumiBootstrap` for running tests without support from IDEs and build tools
- TextUI can optionally hide passing tests
- `@RunVia` can now be inherited from a test's super class
- Better error message if `@RunVia` is missing
- Added a default constructor to `fi.jumi.core.output.OutputCapturer` in order to avoid the need of a `NullOutputStream` in tests
- Removed from jumi-api's Javadocs mentions of planned features which do not yet exist

Jumi Actors is from this release onwards its own project with its own release cycle.

### Jumi 0.1.196 (2012-09-19)

First release of the Jumi test runner. This release is targeted at the developers of testing frameworks and other tools, for them to start implementing support for Jumi. This release includes the following features:

- Launches a new JVM process for running the tests
- Runs tests in parallel
- Expressive test notification model
  - Nested tests
  - More than one failure per test
  - New tests can be discovered as test execution progresses; no need to know all tests up-front
- Reports what tests print to `System.out` and `System.err` more accurately than all other test runners
- Text user interface for showing the results, including exact events of when nested tests start and finish
- An example [SimpleUnit](https://github.com/orfjackal/jumi/tree/master/simpleunit) testing framework to show how to write a testing framework driver for Jumi

Some features critical for regular users are still missing and will be implemented in following releases. Some of the most obvious limitations are:

- No IDE and build tool integration yet; launching a suite requires configuring the suite's classpath manually, in a main method
- Runs only one test class per suite; cannot yet discover test classes automatically
- No JUnit test runner backward compatibility yet
- The text user interface shows all test runs; you cannot hide passing tests
- The number of worker threads is hard-coded to 4
- The working directory is hard-coded to be the same as what the launching process has
- Creates a `.jumi` directory in the working directory; eventually it will probably be placed in the user's home directory (it will contain settings and temporary files)
- The exception classes of all test failures must be also in the classpath of the launching process and must be serializable
- Does not report uncaught exceptions outside tests. To debug some categories of testing framework bugs, you must explicitly configure the launcher to print the daemon's raw standard output

Additionally this release includes the following changes to the Jumi Actors library:

- Improved logging of events with string parameters; special characters are now escaped
- Made configurable the language level for the Java compiler used by the Jumi Actors Maven plugin. Enables the use of event interfaces which depend on Java 7+ language features

### Jumi 0.1.64 (2012-07-10)

- Javadocs for the public APIs of Jumi Actors
- Fixed a concurrency bug in `WorkerCounter`

### Jumi 0.1.46 (2012-07-07)

- Initial release of Jumi Actors
