import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.IntRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.swing.*;
import java.util.Arrays;

import static io.jenetics.engine.Codecs.ofVector;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class TransportProblem3by2Test {
    static int answer = 1690;
    @Test
    public void TransportTest(){
        int[] chromosome = Calculate(0,150);
        int x11 = chromosome[0]; //u
        int x12 = chromosome[1]; //v
        int x13 = 120 - x11 - x12;
        int x21 = 70 - x11;
        int x22 = 140 - x12;
        int x23 = x11 + x12 - 30;

        int result = fitness(chromosome);
        System.out.println("Задача 5: \nОжидаемый результат: " + answer + ".\nПолученный результат: " + result+".");
        System.out.println("Полный набор параметров: ["+x11+", "+x12+", "+x13+"]");
        System.out.println("                         ["+x21+", "+x22+", "+x23+"]");
        Assertions.assertEquals(answer, result);
        Assertions.assertEquals(answer, fitness(chromosome));
    }

    public static int fitness(int[] params){
        int u = params[0];
        int v = params[1];
        int sum = 5*u-3*v+2050;
        if ((120-u-v < 0) || (70-u < 0) || (140-v < 0) || (u+v-30 < 0)) sum = 3000;
        return sum;
    }

    public static int[] Calculate(int min, int max){
        final Engine<IntegerGene, Integer> engine = Engine
                .builder(TransportProblem3by2Test::fitness,
                        ofVector(IntRange.of(min,max),2))
                .populationSize(30000)
                .selector(new TournamentSelector<>())
                .maximalPhenotypeAge(11)
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new Mutator<>(0.1),
                        new SinglePointCrossover<>(0.7)
                )
                .build();
        final EvolutionStatistics<Integer,?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype <IntegerGene, Integer> best = engine.stream()
                .limit(bySteadyFitness(100))
                .peek( statistics)
                .collect(toBestPhenotype());
        return best.genotype().chromosome().stream().mapToInt(IntegerGene::allele).toArray();
    }
}
