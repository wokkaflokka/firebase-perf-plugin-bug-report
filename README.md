## Firebase Perf Plugin Bug Reproducer

This repo serves to demonstrate a bug in Firebase Perf Plugin v1.2.+ that leads to a high volume of error output in the logs.

This manifested as a bug in the CI/CD pipeline for larger builds, when we started observing build logs between 1 and 15 GB
in size. This escalated disk contention and ultimately led to a minor CI/CD outage until we remediated our builds by reverting
to an older version of the plugin.

### Details/STR

Four samples are provided:

1. `bug` -- this is a minimal reproduction of the bug and contains only the firebase performance dependency + kotlin and android api's.
To reproduce the output, move to the root of the project and run:


```
    $ ./gradlew clean bug:bundleRelease
```

You can optionally output the results to a file, which can then be measured. Such a procedure might look like:

```
    $ ./gradlew clean bug:bundleRelease &> build.output.bug.log
    $ du -h build.output.log 
      616K	  build.output.bug.log
```

Note, for this project with no dependencies but Firebase Perf Plugin, this log is 616K. We will see later that it scales
up as more dependencies are added.

2. `nobug` -- this is even more minimal than the `bug` module, removing the firebase perf dependencies.

```
    $ ./gradlew clean nobug:bundleRelease
```

You can optionally output the results to a file, which can then be measured. Such a procedure might look like:

```
    $ ./gradlew clean nobug:bundleRelease &> build.output.nobug.log
    $ du -h build.output.nobug.log 
      4.0K	build.output.nobug.log
```

3. `moredeps` -- this is identical to the `bug` module, but adds some random dependencies to demonstrate that the
build log size will scale with the corresponding project size.

```
    $ ./gradlew clean moredeps:bundleRelease
```

You can optionally output the results to a file, which can then be measured. Such a procedure might look like:

```
    $ ./gradlew clean moredeps:bundleRelease &> build.output.moredeps.log
    $ du -h build.output.moredeps.log 
      34M	build.output.moredeps.log
```

By adding a few dependencies, build log is now well over 34x the size it was based on the minimal reproduction steps.

2. `nobug-1.1.5` -- this is identical to the `bug` module, but uses `v1.1.5` of the perf plugin. This version works fine.

```
    $ ./gradlew clean nobug-1.1.5:bundleRelease
```

You can optionally output the results to a file, which can then be measured. Such a procedure might look like:

```
    $ ./gradlew clean nobug-1.1.5:bundleRelease &> build.output.nobug.log
    $ du -h build.output.nobug.log 
      4.0K	build.output.nobug.log
```
