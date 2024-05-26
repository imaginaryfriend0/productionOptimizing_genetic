import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.IntRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.jenetics.engine.Codecs.ofVector;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class ProductionProblemTest {
    static int answer = 13000;
    @Test
    public void ProductionTest(){
        int[] chromosome = Calculate(0,1000);
        int result = fitness(chromosome);

        System.out.println("Задача 2: \nОжидаемый результат: " + answer + ".\nПолученный результат: " + result+".");
        Assertions.assertEquals(answer, result);
    }

    public static int fitness(int[] params){
        int x = params[0];
        int y = params[1];
        int sum = 80*x+100*y;
        if((20*x+40*y > 4000) || (4*x+6*y > 900) || (4*x+4*y > 600) || (30*x+50*y > 6000)) sum = 0;
        return sum;
    }

    public static int[] Calculate(int min, int max){
        final Engine<IntegerGene, Integer> engine = Engine
                .builder( ProductionProblemTest::fitness,
                        ofVector(IntRange.of(min,max),2))
                .populationSize(25000)
                .selector(new TournamentSelector<>())
                .optimize(Optimize.MAXIMUM)
                .alterers(
                        new Mutator<>(0.1),
                        new MultiPointCrossover<>(0.7)
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
