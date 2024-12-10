package benchmarks;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class NullDefaults {
    private static class ClassToReturn {
        @Override
        public String toString() {
            return "ClassToReturn{}";
        }
    }

    private record Car(String make, String model, int horsePower) {
    }

    @Param({"STRING", "RECORD", "NULL"})
    private String testCase;

    private Object objOrNull;

    private ClassToReturn toReturn;

    @Setup
    public void prepare() {
        objOrNull = switch (testCase) {
            case "STRING" -> "some string";
            case "RECORD" -> new Car("Volvo", "XC70", 308);
            case "NULL" -> null;
            default -> throw new IllegalArgumentException("Invalid test case");
        };
        toReturn = new ClassToReturn();
    }

    @Benchmark
    public Object ternaryNullCheck() {
        return objOrNull == null ? toReturn : objOrNull;
    }

    @Benchmark
    public Object requireNonNullElse() {
        return Objects.requireNonNullElse(objOrNull, toReturn);
    }

    @Benchmark
    public Object optionalOrElse() {
        return Optional.ofNullable(objOrNull).orElse(toReturn);
    }

    public static void main(String[] args) throws RunnerException {
        var className = NullDefaults.class.getSimpleName();
        var opt = new OptionsBuilder()
                .include(".*" + className + ".*")
                // check it out on https://jmh.morethan.io/
                .result(className + "-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
