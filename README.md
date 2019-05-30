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

## Error Format

The underlying error being produced by the plugin looks like:

```
Can't instrument ~/sandbox/bugreports/firebase-perf-plugin/moredeps/build/intermediates/javac/release/compileReleaseJavaWithJavac/classes/com/squareup/picasso/R.class : null
java.lang.IllegalArgumentException
        at org.objectweb.asm.ClassVisitor.<init>(ClassVisitor.java:79)
        at com.google.firebase.perf.plugin.instrumentation.InstrumentationVisitor.<init>(InstrumentationVisitor.java:55)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrument(Instrument.java:170)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrumentClassFile(Instrument.java:84)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrumentClassesInDir(Instrument.java:56)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrumentClassesInDir(Instrument.java:53)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrumentClassesInDir(Instrument.java:53)
        at com.google.firebase.perf.plugin.instrumentation.Instrument.instrumentClassesInDir(Instrument.java:53)
        at com.google.firebase.perf.plugin.FirebasePerfTransform.performTransformationFor(FirebasePerfTransform.java:448)
        at com.google.firebase.perf.plugin.FirebasePerfTransform.transformDirectoryInputs(FirebasePerfTransform.java:364)
        at com.google.firebase.perf.plugin.FirebasePerfTransform.transform(FirebasePerfTransform.java:291)
        at com.android.build.gradle.internal.pipeline.TransformTask$2.call(TransformTask.java:239)
        at com.android.build.gradle.internal.pipeline.TransformTask$2.call(TransformTask.java:235)
        at com.android.builder.profile.ThreadRecorder.record(ThreadRecorder.java:102)
        at com.android.build.gradle.internal.pipeline.TransformTask.transform(TransformTask.java:230)
        at sun.reflect.GeneratedMethodAccessor194.invoke(Unknown Source)
        at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
        at java.lang.reflect.Method.invoke(Method.java:498)
        at org.gradle.internal.reflect.JavaMethod.invoke(JavaMethod.java:103)
        at org.gradle.api.internal.project.taskfactory.IncrementalTaskInputsTaskAction.doExecute(IncrementalTaskInputsTaskAction.java:46)
        at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:41)
        at org.gradle.api.internal.project.taskfactory.AbstractIncrementalTaskAction.execute(AbstractIncrementalTaskAction.java:25)
        at org.gradle.api.internal.project.taskfactory.StandardTaskAction.execute(StandardTaskAction.java:28)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$5.run(ExecuteActionsTaskExecuter.java:404)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:402)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor$RunnableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:394)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor.run(DefaultBuildOperationExecutor.java:92)
        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.run(DelegatingBuildOperationExecutor.java:31)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeAction(ExecuteActionsTaskExecuter.java:393)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.executeActions(ExecuteActionsTaskExecuter.java:376)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.access$200(ExecuteActionsTaskExecuter.java:80)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter$TaskExecution.execute(ExecuteActionsTaskExecuter.java:213)
        at org.gradle.internal.execution.steps.ExecuteStep.lambda$execute$0(ExecuteStep.java:32)
        at java.util.Optional.map(Optional.java:215)
        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:32)
        at org.gradle.internal.execution.steps.ExecuteStep.execute(ExecuteStep.java:26)
        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:58)
        at org.gradle.internal.execution.steps.CleanupOutputsStep.execute(CleanupOutputsStep.java:35)
        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:48)
        at org.gradle.internal.execution.steps.ResolveInputChangesStep.execute(ResolveInputChangesStep.java:33)
        at org.gradle.internal.execution.steps.CancelExecutionStep.execute(CancelExecutionStep.java:39)
        at org.gradle.internal.execution.steps.TimeoutStep.executeWithoutTimeout(TimeoutStep.java:73)
        at org.gradle.internal.execution.steps.TimeoutStep.execute(TimeoutStep.java:54)
        at org.gradle.internal.execution.steps.CatchExceptionStep.execute(CatchExceptionStep.java:35)
        at org.gradle.internal.execution.steps.CreateOutputsStep.execute(CreateOutputsStep.java:51)
        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:45)
        at org.gradle.internal.execution.steps.SnapshotOutputsStep.execute(SnapshotOutputsStep.java:31)
        at org.gradle.internal.execution.steps.CacheStep.executeWithoutCache(CacheStep.java:201)
        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:70)
        at org.gradle.internal.execution.steps.CacheStep.execute(CacheStep.java:45)
        at org.gradle.internal.execution.steps.BroadcastChangingOutputsStep.execute(BroadcastChangingOutputsStep.java:49)
        at org.gradle.internal.execution.steps.StoreSnapshotsStep.execute(StoreSnapshotsStep.java:43)
        at org.gradle.internal.execution.steps.StoreSnapshotsStep.execute(StoreSnapshotsStep.java:32)
        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:38)
        at org.gradle.internal.execution.steps.RecordOutputsStep.execute(RecordOutputsStep.java:24)
        at org.gradle.internal.execution.steps.SkipUpToDateStep.executeBecause(SkipUpToDateStep.java:96)
        at org.gradle.internal.execution.steps.SkipUpToDateStep.lambda$execute$0(SkipUpToDateStep.java:89)
        at java.util.Optional.map(Optional.java:215)
        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:54)
        at org.gradle.internal.execution.steps.SkipUpToDateStep.execute(SkipUpToDateStep.java:38)
        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:77)
        at org.gradle.internal.execution.steps.ResolveChangesStep.execute(ResolveChangesStep.java:37)
        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:36)
        at org.gradle.internal.execution.steps.legacy.MarkSnapshottingInputsFinishedStep.execute(MarkSnapshottingInputsFinishedStep.java:26)
        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:90)
        at org.gradle.internal.execution.steps.ResolveCachingStateStep.execute(ResolveCachingStateStep.java:48)
        at org.gradle.internal.execution.impl.DefaultWorkExecutor.execute(DefaultWorkExecutor.java:33)
        at org.gradle.api.internal.tasks.execution.ExecuteActionsTaskExecuter.execute(ExecuteActionsTaskExecuter.java:120)
        at org.gradle.api.internal.tasks.execution.ResolveBeforeExecutionStateTaskExecuter.execute(ResolveBeforeExecutionStateTaskExecuter.java:75)
        at org.gradle.api.internal.tasks.execution.ValidatingTaskExecuter.execute(ValidatingTaskExecuter.java:62)
        at org.gradle.api.internal.tasks.execution.SkipEmptySourceFilesTaskExecuter.execute(SkipEmptySourceFilesTaskExecuter.java:108)
        at org.gradle.api.internal.tasks.execution.ResolveBeforeExecutionOutputsTaskExecuter.execute(ResolveBeforeExecutionOutputsTaskExecuter.java:67)
        at org.gradle.api.internal.tasks.execution.ResolveAfterPreviousExecutionStateTaskExecuter.execute(ResolveAfterPreviousExecutionStateTaskExecuter.java:46)
        at org.gradle.api.internal.tasks.execution.CleanupStaleOutputsExecuter.execute(CleanupStaleOutputsExecuter.java:94)
        at org.gradle.api.internal.tasks.execution.FinalizePropertiesTaskExecuter.execute(FinalizePropertiesTaskExecuter.java:46)
        at org.gradle.api.internal.tasks.execution.ResolveTaskExecutionModeExecuter.execute(ResolveTaskExecutionModeExecuter.java:95)
        at org.gradle.api.internal.tasks.execution.SkipTaskWithNoActionsExecuter.execute(SkipTaskWithNoActionsExecuter.java:57)
        at org.gradle.api.internal.tasks.execution.SkipOnlyIfTaskExecuter.execute(SkipOnlyIfTaskExecuter.java:56)
        at org.gradle.api.internal.tasks.execution.CatchExceptionTaskExecuter.execute(CatchExceptionTaskExecuter.java:36)
        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.executeTask(EventFiringTaskExecuter.java:73)
        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:52)
        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter$1.call(EventFiringTaskExecuter.java:49)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:416)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor$CallableBuildOperationWorker.execute(DefaultBuildOperationExecutor.java:406)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor$1.execute(DefaultBuildOperationExecutor.java:165)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:250)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor.execute(DefaultBuildOperationExecutor.java:158)
        at org.gradle.internal.operations.DefaultBuildOperationExecutor.call(DefaultBuildOperationExecutor.java:102)
        at org.gradle.internal.operations.DelegatingBuildOperationExecutor.call(DelegatingBuildOperationExecutor.java:36)
        at org.gradle.api.internal.tasks.execution.EventFiringTaskExecuter.execute(EventFiringTaskExecuter.java:49)
        at org.gradle.execution.plan.LocalTaskNodeExecutor.execute(LocalTaskNodeExecutor.java:43)
        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:355)
        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$InvokeNodeExecutorsAction.execute(DefaultTaskExecutionGraph.java:343)
        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:336)
        at org.gradle.execution.taskgraph.DefaultTaskExecutionGraph$BuildOperationAwareExecutionAction.execute(DefaultTaskExecutionGraph.java:322)
        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker$1.execute(DefaultPlanExecutor.java:134)
        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker$1.execute(DefaultPlanExecutor.java:129)
        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.execute(DefaultPlanExecutor.java:202)
        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.executeNextNode(DefaultPlanExecutor.java:193)
        at org.gradle.execution.plan.DefaultPlanExecutor$ExecutorWorker.run(DefaultPlanExecutor.java:129)
        at org.gradle.internal.concurrent.ExecutorPolicy$CatchAndRecordFailures.onExecute(ExecutorPolicy.java:63)
        at org.gradle.internal.concurrent.ManagedExecutorImpl$1.run(ManagedExecutorImpl.java:46)
        at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1142)
        at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:617)
        at org.gradle.internal.concurrent.ThreadFactoryImpl$ManagedThreadRunnable.run(ThreadFactoryImpl.java:55)
        at java.lang.Thread.run(Thread.java:748)

```
