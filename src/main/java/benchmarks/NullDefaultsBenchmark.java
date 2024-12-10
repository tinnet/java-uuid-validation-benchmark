package benchmarks;

import org.openjdk.jmh.annotations.*;
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
public class NullDefaultsBenchmark {
    private record Car(String make, String model, int horsePower) {
        static Car best() {
            return new Car("Volvo", "XC70", 308);
        }
    }

    @Param({"OBJECT", "NULL"})
    private String testCase;

    private Object objOrNull;

    private final Car preAllocated = Car.best();

    @Setup
    public void prepare() {
        objOrNull = switch (testCase) {
            case "OBJECT" -> new Car("Polestar", "2", 400);
            case "NULL" -> null;
            default -> throw new IllegalArgumentException("Invalid test case");
        };
    }


    @Benchmark
    public Object baselineAllocation() {
        return Car.best();
    }

    @Benchmark
    public Object ternaryNullCheck() {
        return objOrNull == null ? preAllocated : objOrNull;
    }

    @Benchmark
    public Object ternaryNullCheckWithAllocation() {
        return objOrNull == null ? Car.best() : objOrNull;
    }

    @Benchmark
    public Object requireNonNullElse() {
        return Objects.requireNonNullElse(objOrNull, preAllocated);
    }

    @Benchmark
    public Object requireNonNullElseWithAllocation() {
        return Objects.requireNonNullElse(objOrNull, Car.best());
    }

    @Benchmark
    public Object optionalOrElse() {
        return Optional.ofNullable(objOrNull).orElse(preAllocated);
    }

    @Benchmark
    public Object optionalOrElseWithAllocation() {
        return Optional.ofNullable(objOrNull).orElse(Car.best());
    }

    public static void main(String[] args) throws RunnerException {
        var className = NullDefaultsBenchmark.class.getSimpleName();
        var opt = new OptionsBuilder()
                .include(".*" + className + ".*")
                // check it out on https://jmh.morethan.io/
                .result(className + "-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
