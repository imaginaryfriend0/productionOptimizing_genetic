package org.example;

import io.jenetics.*;
import io.jenetics.engine.Engine;
import io.jenetics.engine.EvolutionStatistics;
import io.jenetics.util.IntRange;

import static io.jenetics.engine.Codecs.ofVector;
import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

class EvolutionEngine {
    static int[] RunEvolution(int minValue, int maxValue, int chromosomeLength){
        final Engine<IntegerGene, Float> engine = Engine
                .builder( EvolutionAlgorithm::fitness,
                        ofVector(IntRange.of(minValue,maxValue),chromosomeLength))
                .populationSize(1000)
                .selector(new TournamentSelector<>())
                .optimize(Optimize.MAXIMUM)
                .alterers(
                        new Mutator<>(0.05),
                        new MultiPointCrossover<>(0.7)
                )
                .build();
        final EvolutionStatistics<Float,?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype <IntegerGene,Float> best = engine.stream()
                .limit(bySteadyFitness(100))
                .peek(statistics)
                .collect(toBestPhenotype());
        return best.genotype().chromosome().stream().mapToInt(IntegerGene::allele).toArray();
    }
}
