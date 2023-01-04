package com.unipi.msc.javablockchainapi.Benchmarking;

import com.unipi.msc.javablockchainapi.Constants.BenchmarkData;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.OptionsBuilder;

import java.util.concurrent.TimeUnit;
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(value = 1)
@Warmup(iterations = 2, timeUnit = TimeUnit.MILLISECONDS, time = 5000)
@Measurement(iterations = 5, timeUnit = TimeUnit.MILLISECONDS, time = 5000)

public class BenchMark {
    public static void main(String[] args) {
        try   {
            new Runner(new OptionsBuilder()
                .include(BenchMark.class.getSimpleName())
                .forks(1)
                .build()
            ).run();
        } catch (RunnerException e) {
            throw new RuntimeException(e);
        }
    }
    @Benchmark
    public static void mineBlockV1(){
        BenchmarkData.getBlockV1().mineBlock();
    }
    @Benchmark
    public static void mineBlockV2(){
        BenchmarkData.getBlockV2().mineBlock();
    }
    @Benchmark
    public static void mineBlockV3(){
        BenchmarkData.getBlockV3().mineBlock();
    }
}
