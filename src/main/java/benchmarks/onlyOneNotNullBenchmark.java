package benchmarks;

import com.google.common.primitives.Booleans;
import org.apache.commons.lang3.BooleanUtils;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

@BenchmarkMode(Mode.Throughput)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 10, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
public class onlyOneNotNullBenchmark {
    record Car(String suv, String sedan, String estate) {
        static Car XC70() {
            return new Car(null, null, "V60");
        }
    }

    private Car car;

    @Setup
    public void prepare() {
        car = Car.XC70();
    }

    @Benchmark
    public boolean apacheXor() {
        return BooleanUtils.xor(new boolean[]{car.sedan != null, car.suv != null, car.estate != null});
    }

    @Benchmark
    public boolean apacheXorAndObjects() {
        return BooleanUtils.xor(new Boolean[]{Objects.nonNull(car.sedan), Objects.nonNull(car.suv), Objects.nonNull(car.estate)});
    }

    @Benchmark
    public boolean stream() {
        return 1 == Stream.of(car.sedan, car.suv, car.estate)
                .filter(Objects::nonNull)
                .count();
    }

    @Benchmark
    public boolean count() {
        int count = 0;
        if (car.sedan != null) {
            count++;
        }
        if (car.suv != null) {
            count++;
        }
        if (car.estate != null) {
            count++;
        }
        return count == 1;
    }

    @Benchmark
    public boolean onlyOneSet_logicalXor() {
        return (car.sedan != null ^ car.suv != null ^ car.estate != null) &&
                !(car.sedan != null && car.suv != null && car.estate != null);
    }

    @Benchmark
    public boolean guavaCount() {
        return 1 == Booleans.countTrue(car.sedan != null, car.suv != null, car.estate != null);
    }

    @Benchmark
    public boolean guavaCountAndObjects() {
        return 1 == Booleans.countTrue(Objects.nonNull(car.sedan), Objects.nonNull(car.suv), Objects.nonNull(car.estate));
    }

    public static void main(String[] args) throws RunnerException {
        var className = onlyOneNotNullBenchmark.class.getSimpleName();
        var opt = new OptionsBuilder()
                .include(".*" + className + ".*")
                // check it out on https://jmh.morethan.io/
                .result(className + "-results.json")
                .resultFormat(org.openjdk.jmh.results.format.ResultFormatType.JSON)
                .build();

        new Runner(opt).run();
    }
}
