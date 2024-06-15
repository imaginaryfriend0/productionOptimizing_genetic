import io.jenetics.*;
import io.jenetics.engine.Codecs;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.DoubleRange;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

public class RastriginFunctionTest {
    private static final double A = 10;
    private static final double R = 5.12;
    private static final int N = 5;
    static int answer = 0;
    @Test
    public void RastriginTest(){
        double[] chromosome = Calculate(-R,R,N);
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
                .populationSize(100000)
                .optimize(Optimize.MINIMUM)
                .alterers(
                        new Mutator<>(0.03),
                        new MeanAlterer<>(0.6)
                )
                .build();
        final EvolutionStatistics<Double,?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype <DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(10))
                .peek( statistics)
                .collect(toBestPhenotype());
        return best.genotype().chromosome().stream().mapToDouble(DoubleGene::allele).toArray();
    }
}
