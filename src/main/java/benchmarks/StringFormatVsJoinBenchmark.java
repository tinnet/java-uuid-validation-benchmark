package benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput) // Measures operations per second
@OutputTimeUnit(TimeUnit.MILLISECONDS) // Results in milliseconds
@State(Scope.Thread) // One instance per thread
public class StringFormatVsJoinBenchmark {

    private String corporation = "Corp";
    private String department = "Dept";
    private int employeeNumber = 12345;

    @Benchmark
    public String testStringFormat() {
        return String.format("%s-%s-%d", corporation, department, employeeNumber);
    }

    @Benchmark
    public String testStringJoin() {
        return String.join("-", corporation, department, String.valueOf(employeeNumber));
    }

    @Benchmark
    public String testStringBuilder() {
        StringBuilder sb = new StringBuilder();
        sb.append(corporation).append(department).append("Dept").append("-").append(employeeNumber);
        return sb.toString();
    }

    @Benchmark
    public String testStringPlus() {
        return corporation + "-" + department + "-" + employeeNumber;
    }

    @Benchmark
    public String testStringJoinStream() {
        return Stream.of(corporation, department, String.valueOf(employeeNumber)).collect(Collectors.joining("-"));
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(".*" + StringFormatVsJoinBenchmark.class.getSimpleName() + ".*")
                .warmupIterations(2)
                .measurementIterations(3)
                .threads(2)
                .forks(1)
                .build();

        new Runner(opt).run();
    }
}
