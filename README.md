# Benchmarks

Benchmarks based on [Oracle Java Microbenchmark Harness](http://openjdk.java.net/projects/code-tools/jmh/), inspired by [metrics-benchmark](https://github.com/dropwizard/metrics/blob/master/metrics-benchmarks).

# Running

### compile the jar

    mvn clean package

## run the benchmarks

Run all benchmarks in the .jar in 4 threads, 10 iterations each, 5 warmup iterations each and fork (restart) the jvm 2 times:

    java -jar target/benchmarks.jar -t 4  -f 2 -i 10 -wi 5


Run only the benchmarks whose name matches "*Pregen*" (case sensitive):

    java -jar target/benchmarks.jar "Pregen" -t 4  -f 2 -i 10 -wi 5

List all benchmark that match the pattern:

    java -jar target/benchmarks.jar "Pregen" -l

Show all command line options:

    java -jar target/benchmarks.jar -h

# Results

Sample results from a MacBook

    > system_profiler
    [...]
      Model Name: MacBook Pro
      Model Identifier: MacBookPro11,2
      Processor Name: Intel Core i7
      Processor Speed: 2.5 GHz
      Number of Processors: 1
      Total Number of Cores: 4
      L2 Cache (per Core): 256 KB
      L3 Cache: 6 MB
      Memory: 16 GB
      [...]

With an Java8 VM

    > java -version
    java version "1.8.0_25"
    Java(TM) SE Runtime Environment (build 1.8.0_25-b17)
    Java HotSpot(TM) 64-Bit Server VM (build 25.25-b02, mixed mode)

## sample results 1 - ops/s bound by UUID generation

This first - overly simple - benchmarks generate & check UUIDs on each call, performance is actually bound by the UUID generation call.

    > java -jar target/benchmarks.jar "CPUBound" -t 4 -f 2 -i 10 -wi 5
    [...]
    # Run complete. Total time: 00:06:05

    Benchmark                                                                          Mode  Samples       Score       Error  Units
    b.RegexValidationCPUBoundBenchmark.baseLine                                       thrpt       20  352298.704 ± 11717.708  ops/s
    b.RegexValidationCPUBoundBenchmark.baseLineInvalid                                thrpt       20  252595.339 ± 14906.622  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern1                                thrpt       20  226109.948 ± 12648.803  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern2                                thrpt       20  226839.684 ± 14141.188  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern3                                thrpt       20  279217.057 ± 10224.225  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern4                                thrpt       20  280204.781 ±  8563.777  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern4WithLengthCheckInvalidLength    thrpt       20  275216.042 ±  6913.262  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern4WithLengthCheckValidLength      thrpt       20  270861.328 ± 14193.556  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern5                                thrpt       20  273651.118 ± 10785.304  ops/s
    b.RegexValidationCPUBoundBenchmark.measurePattern6                                thrpt       20  282376.512 ±  4649.281  ops/s


**=>** difference between the baseline and the benchmarks is to small, these results are not useful

## sample results 2 - with pregenerated UUIDs

This benchmark pregenerates some (thousands) of UUIDs that are randomly pulled from an ArrayList and checked.

    > java -jar target/benchmarks.jar "Pregen" -t 4 -f 2 -i 10 -wi 5
    [...]
    # Run complete. Total time: 00:05:30

    Benchmark                                                                             Mode  Samples         Score        Error  Units
    b.RegexValidationPregenerateBenchmark.baseLine                                       thrpt       20  10396376.451 ± 332112.273  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern1                                thrpt       20   2374787.839 ±  98314.276  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern2                                thrpt       20   2394137.294 ±  66162.498  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern3                                thrpt       20   5659104.820 ± 254798.908  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern4                                thrpt       20   5735105.874 ± 421684.361  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern4WithLengthCheckInvalidLength    thrpt       20  10997490.853 ± 269508.205  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern4WithLengthCheckValidLength      thrpt       20   6193859.808 ± 162732.081  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern5                                thrpt       20   5453760.979 ± 145312.710  ops/s
    b.RegexValidationPregenerateBenchmark.measurePattern6                                thrpt       20   5868671.104 ± 177649.887  ops/s

* running the benchmarks for only a short time yields a big margin of error
* the optimizations of the regular expression behave pretty much as expected
* all patterns "beyond" Pattern 3 seem to be equally performant
* adding a length check is not overly expensive and brings huge gains if most of the checked UUIDStrings are expected to be *invalid*
* allowing case insensitivity makes the regular expression more correct and has a tolerable performance impact

**=>** recommended regular expression is *Pattern 5* which takes case insensitivity into account. *Pattern 6* might be to strict for legacy software that is using some of the UUIDs bits as flags \*cough\*
