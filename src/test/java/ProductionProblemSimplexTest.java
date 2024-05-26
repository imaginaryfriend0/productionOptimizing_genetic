import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.IntRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.jenetics.engine.Codecs.ofVector;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class ProductionProblemSimplexTest {
    static int answer = 840;
    @Test
    public void ProductionSimplexTest(){
        int[] chromosome = Calculate(0,500);
        int result = fitness(chromosome);

        System.out.println("Задача 1: \nОжидаемый результат: " + answer + ".\nПолученный результат: " + result+".");
        Assertions.assertEquals(answer, result);
    }

    public static int fitness(int[] params){
        int x1 = params[0];
        int x2 = params[1];
        int x3 = params[2];
        int sum = x1 + 3*x2 + 3*x3;
        if ((5*x1+10*x2+6*x3 > 2000) || (4*x1+5*x2+8*x3 > 2000)) sum = 0;
        return sum;
    }

    public static int[] Calculate(int min, int max){
        final Engine<IntegerGene, Integer> engine = Engine
                .builder(ProductionProblemSimplexTest::fitness,
                        ofVector(IntRange.of(min,max),3))
                .populationSize(30000)
                .selector(new TournamentSelector<>())
                .optimize(Optimize.MAXIMUM)
                .alterers(
                        new Mutator<>(0.03),
                        new SinglePointCrossover<>(0.6)
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
