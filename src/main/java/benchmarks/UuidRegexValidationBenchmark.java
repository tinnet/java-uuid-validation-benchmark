package benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class UuidRegexValidationBenchmark {

    // Pattern 1 was encountered in an actual application
    private static final Pattern UUID_PATTERN_1 = Pattern.compile("(:?[a-f0-9]){8,8}-(:?[a-f0-9]){4,4}-(:?[a-f0-9]){4,4}-(:?[a-f0-9]){4,4}-(:?[a-f0-9]){12,12}");

    // Patterns 2 - 4 are tuning steps (expected to get faster with each step)
    private static final Pattern UUID_PATTERN_2 = Pattern.compile("(:?[a-f0-9]){8}-(:?[a-f0-9]){4}-(:?[a-f0-9]){4}-(:?[a-f0-9]){4}-(:?[a-f0-9]){12}");
    private static final Pattern UUID_PATTERN_3 = Pattern.compile("[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}");
    private static final Pattern UUID_PATTERN_4 = Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$");

    // Pattern 5 takes into account that uppercase letters are valid for UUID.fromString()
    private static final Pattern UUID_PATTERN_5 = Pattern.compile("^[a-f0-9]{8}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{4}-[a-f0-9]{12}$", Pattern.CASE_INSENSITIVE);

    // Pattern 6 is taken from http://stackoverflow.com/a/13653180 and is more strict
    private static final Pattern UUID_PATTERN_6 = Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$", Pattern.CASE_INSENSITIVE);

    private static final String UUID_VALID = UUID.randomUUID().toString();
    private static final String UUID_INVALID = UUID.randomUUID().toString().replace("-", "::");
    private static final String UUID_INVALID_LARGE = IntStream.range(1, 100).mapToObj(i -> UUID.randomUUID().toString()).collect(Collectors.joining());

    public enum TestCase {
        VALID,
        INVALID,
        INVALID_LARGE,
    }

    @Param({"VALID", "INVALID", "INVALID_LARGE" })
    private TestCase testCase;

    private String uuidStr;

    @Setup
    public void prepare() {
        uuidStr = switch (testCase) {
            case VALID -> UUID_VALID;
            case INVALID -> UUID_INVALID;
            case INVALID_LARGE -> UUID_INVALID_LARGE;
        };
    }

    @Benchmark
    public Object measureFromString() {
        try {
            return UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            return uuidStr;
        }
    }

    @Benchmark
    public Object measurePattern1() {
        return UUID_PATTERN_1.matcher(uuidStr).matches();
    }

    @Benchmark
    public Object measurePattern2() {
        return UUID_PATTERN_2.matcher(uuidStr).matches();
    }


    @Benchmark
    public Object measurePattern3() {
        return UUID_PATTERN_3.matcher(uuidStr).matches();
    }

    @Benchmark
    public Object measurePattern4() {
        return UUID_PATTERN_4.matcher(uuidStr).matches();
    }

    @Benchmark
    public Object measurePattern5() {
        return UUID_PATTERN_5.matcher(uuidStr).matches();
    }

    @Benchmark
    public Object measurePattern6() {
        return UUID_PATTERN_6.matcher(uuidStr).matches();
    }

    @Benchmark
    public Object measurePattern4WithLengthCheck() {
        return uuidStr.length() == 36 && UUID_PATTERN_4.matcher(uuidStr).matches();
    }

    @Benchmark
    public Object measurePattern4WithNullCheck() {
        return uuidStr != null && UUID_PATTERN_4.matcher(uuidStr).matches();
    }

    public static void main(String[] args) throws RunnerException {
        var className = UuidRegexValidationBenchmark.class.getSimpleName();
        var opt = new OptionsBuilder()
                .include(".*" + className + ".*")
                // check it out on https://jmh.morethan.io/
                .result(className + "-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
