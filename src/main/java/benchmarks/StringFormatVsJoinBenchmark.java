package benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class StringFormatVsJoinBenchmark {

    private String corporation;
    private String department;
    private int employeeNumber;

    private Random random = new Random();

    @Setup
    public void setup(){
        corporation = "Corp";
        department = "Dept";
        employeeNumber = 12345;
    }

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
        // intelij wants to replace this with a string concat, don't let it!
        StringBuilder sb = new StringBuilder();
        sb.append(corporation).append(department).append("Dept").append("-").append(employeeNumber);
        return sb.toString();
    }

    @Benchmark
    public String testStringPlus() {
        return corporation + "-" + department + "-" + employeeNumber;
    }

    @Benchmark
    public String testStringPlusDynamic() {
        return corporation + "-" + department + "-" + random.nextInt();
    }

    @Benchmark
    public String testStringJoinStream() {
        // intelij wants to replace this with String.join concat, don't let it!
        return Stream.of(corporation, department, String.valueOf(employeeNumber)).collect(Collectors.joining("-"));
    }

    public static void main(String[] args) throws RunnerException {
        var className = StringFormatVsJoinBenchmark.class.getSimpleName();
        var opt = new OptionsBuilder()
                .include(".*" + className + ".*")
                // check it out on https://jmh.morethan.io/
                .result(className + "-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
