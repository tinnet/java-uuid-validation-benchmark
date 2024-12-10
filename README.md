# Benchmarks

Benchmarks based on [Oracle Java Microbenchmark Harness](http://openjdk.java.net/projects/code-tools/jmh/), inspired by [metrics-benchmark](https://github.com/dropwizard/metrics/blob/master/metrics-benchmarks).

# Running

### compile the jar

    mise run build

## run the benchmarks

Run only the benchmarks whose name matches "*StringForm*" (case sensitive):

    java -jar target/benchmarks.jar "StringForm"

List all benchmark that match the pattern:

    java -jar target/benchmarks.jar "StringForm" -l

Show all command line options:

    java -jar target/benchmarks.jar -h

Sane defaults for the various JMH params have been set for quick local runs, you can override them via:

    java -jar target/benchmarks.jar -t 4  -f 2 -i 10 -wi 5
