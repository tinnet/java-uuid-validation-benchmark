package benchmarks;

import com.google.common.base.Strings;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class StringUtils {
    @Param({"STRING", "BLANK", "EMPTY", "NULL"})
    private String testCase;

    private String stringOrNull;

    @Setup
    public void prepare() {
        stringOrNull = switch (testCase) {
            case "STRING" -> UUID.randomUUID().toString();
            case "BLANK" -> " ".repeat(128);
            case "EMPTY" -> "";
            case "NULL" -> null;
            default -> throw new IllegalArgumentException("Invalid test case");
        };
    }


    @Benchmark
    public boolean isEmpty_String() {
        return stringOrNull != null && stringOrNull.isEmpty();
    }

    @Benchmark
    public boolean isEmpty_Apache() {
        return org.apache.commons.lang3.StringUtils.isEmpty(stringOrNull);
    }

    @Benchmark
    public boolean isEmpty_Guava() {
        return Strings.isNullOrEmpty(stringOrNull);
    }

    @Benchmark
    public boolean isBlank_String() {
        return stringOrNull != null && stringOrNull.isBlank();
    }

    @Benchmark
    public boolean isBlank_Apache() {
        return org.apache.commons.lang3.StringUtils.isBlank(stringOrNull);
    }

    @Benchmark
    public boolean isBlank_Guava() {
        // guava does not have a dedicated isblank
        return Strings.nullToEmpty(stringOrNull).isBlank();
    }

    public static void main(String[] args) throws RunnerException {
        var className = StringUtils.class.getSimpleName();
        var opt = new OptionsBuilder()
                .include(".*" + className + ".*")
                // check it out on https://jmh.morethan.io/
                .result(className + "-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
