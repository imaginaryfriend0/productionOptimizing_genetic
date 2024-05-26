import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.IntRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static io.jenetics.engine.Codecs.ofVector;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class TransportProblem2by2Test {
    static int answer = 34;
    @Test
    public void TransportTest(){
        int[] chromosome = Calculate(0,150);
        int x11 = chromosome[0]; //t
        int x12 = 20 - x11;
        int x21 = 16 - x11;
        int x22 = x11 - 6;

        int result = fitness(chromosome);
        System.out.println("Задача 4: \nОжидаемый результат: " + answer + ".\nПолученный результат: " + result+".");
        System.out.println("Полный набор параметров: ["+x11+", "+x12+"]");
        System.out.println("                         ["+x21+", "+x22+"]");
        Assertions.assertEquals(answer, result);
        Assertions.assertEquals(answer, fitness(chromosome));
    }

    public static int fitness(int[] params){
        int t = params[0];
        int sum = 66-2*t;
        if ((t>16)||t<6) sum = 1000;
        return sum;
    }

    public static int[] Calculate(int min, int max){
        final Engine<IntegerGene, Integer> engine = Engine
                .builder(TransportProblem2by2Test::fitness,
                        ofVector(IntRange.of(min,max),1))
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
