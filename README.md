# NonTransitiveRClassAndCompilationAvoidance

An experimental project that demonstrate that in case a new resource
(e.g. a new string in `strings.xml`) were added to a module,
all dependent modules has to be recompiled again.

## Project setup

* 6 modules (`app`, `lib1`, `lib2`, `lib3`, `lib4`, `libstrings`)
* Each module depends on `libstrings`
* `libstrings` contains string resources (`strings.xml`)
* `android.nonTransitiveRClass` is set to `true` in `gradle.properties`
* Each module has at least a Kotlin class that access the resourced from `libstrings`

## Test case

1. Run `compileDebugKotlin` **two times**:

```bash
./gradlew compileDebugKotlin --console plain
```

> The first time will just "prepare" the project.
> When running the second time, a lot of noise got removed from the output
> and we can observe the problem better.

After the second run, you notice that the `compileDebugKotlin` class is UP-TO-DATE (expected).

2. **Add** or **remove** a string
   from [`libstrings/src/main/res/values/strings.xml`](libstrings/src/main/res/values/strings.xml)

3. Run `compileDebugKotlin **again**:

```bash
./gradlew compileDebugKotlin --console plain
```

Notice that each `compileDebugKotlin` task (of every module) are executed.
No task is UP-TO-DATE anymore.

```
[...]
> Task :lib1:compileDebugKotlin
[...]
> Task :lib2:compileDebugKotlin
[...]
> Task :lib3:compileDebugKotlin
[...]
> Task :lib4:compileDebugKotlin
[...]
> Task :app:compileDebugKotlin
[...]
```

4. Bonus point for running step 3 with `--debug`

```bash
./gradlew :lib1:compileDebugKotlin --console plain --debug
```

If you run it with `--debug` you can observe the files that are going to be recompiled.
In this case the output contains like this:

```
[...]
:lib1:compileDebugKotlin Kotlin compiler args: -Xallow-no-source-files -classpath [masked]/lib1/build/intermediates/compile_r_class_jar/debug/R.jar:[masked]/kotlin-stdlib-1.9.22.jar:[masked]/annotations-13.0.jar:[masked]/libstrings/build/intermediates/compile_library_classes_jar/debug/classes.jar:[masked]/android.jar:[masked]/build-tools/34.0.0/core-lambda-stubs.jar -d [masked]/lib1/build/tmp/kotlin-classes/debug -jvm-target 1.8 -module-name lib1_debug -no-jdk -no-reflect -no-stdlib -verbose [masked]/lib1/src/main/kotlin/guru/stefma/nontransitiverclassandcompilationavoidance/lib1/NoResClass.kt [masked]/lib1/src/main/kotlin/guru/stefma/nontransitiverclassandcompilationavoidance/lib1/subpackage/AnotherNoResClass.kt [masked]/lib1/src/main/kotlin/guru/stefma/nontransitiverclassandcompilationavoidance/lib1/PrivateResClass.kt [masked]/lib1/src/main/kotlin/guru/stefma/nontransitiverclassandcompilationavoidance/lib1/ResClass.kt
[...]
```

So it **seems** that even classes got recompiled that doesn't use or import the R file at all.
For example
the [`NoResClass`](lib1/src/main/kotlin/guru/stefma/nontransitiverclassandcompilationavoidance/lib1/NoResClass.kt)

## Outcome of this experiment

Enabling `NonTransitiveRClass` **doesn't improve build time** when you have *such an project structure*.
It will "only" make sure that the R class will be smaller.
But since the **whole** module will be recompiled anyways, it "doesn't matter" if its recompile
the R class too (with more or less fields in it) or not. 
**However** it helps in situations where you have at least three levels of dependencies. 
For example `app` -> `lib1` -> `libstrings` (but `app` doesn't include `libstrings`).
In this case (with `NonTransitiveRClass` enabled), `app` will not be recompiled.

## Some resources

* [Android documentation about non-transitive R classes](https://developer.android.com/build/optimize-your-build#use-non-transitive-r-classes)
* [Gradle blog post about compilation avoidance](https://blog.gradle.org/compilation-avoidance)
* [Another Gradle blog post about compilation avoidance](https://blog.gradle.org/our-approach-to-faster-compilation)
* [Kotlin blog about incremental compilation](https://blog.jetbrains.com/kotlin/2022/07/a-new-approach-to-incremental-compilation-in-kotlin/)
* [Kotlin documentation about incremental compilation](https://kotlinlang.org/docs/gradle-compilation-and-caches.html#incremental-compilation)