import io.jenetics.*;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.DoubleRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.*;

import static java.lang.Math.PI;
import static java.lang.Math.cos;
import java.util.Arrays;

import static io.jenetics.engine.Codecs.ofVector;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class RastriginFunctionTest {
    private static final double A = 10;
    private static final double R = 5.12;
    private static final int N = 2;
    static int answer = 0;
    @Test
    public void RastriginTest(){
        double[] chromosome = Calculate(-R,R,N);
        int[] intChromosome = new int[2];
        intChromosome[0] = (int) chromosome[0];
        intChromosome[1] = (int) chromosome[1];
        double result = fitness(chromosome);

        System.out.println("Задача 3: \nОжидаемый результат: " + answer + ".\nПолученный результат: " + result+".");
        Assertions.assertEquals(answer, result);
    }

    public static double fitness(double[] x){
        double value = A*N;
        for (int i = 0; i < N; i++){
            value += x[i]*x[i] - A*cos(2.0*PI*x[i]);
        }
        return value;
    }

    public static double[] Calculate(double min, double max, int size){
        final Engine<DoubleGene, Double> engine = Engine
                .builder(RastriginFunctionTest::fitness,
                        Codecs.ofVector(DoubleRange.of(min,max),size))
                .populationSize(30000)
                .selector(new TournamentSelector<>())
                .maximalPhenotypeAge(11)
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new Mutator<>(0.03),
                        new MeanAlterer<>(0.6)
                )
                .build();
        final EvolutionStatistics<Double,?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype <DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(100))
                .peek( statistics)
                .collect(toBestPhenotype());
        return best.genotype().chromosome().stream().mapToDouble(DoubleGene::allele).toArray();
    }
}