package org.example;

import io.jenetics.*;
import io.jenetics.engine.*;
import io.jenetics.util.DoubleRange;
import io.jenetics.util.IntRange;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;
import java.util.stream.IntStream;

import static io.jenetics.engine.EvolutionResult.toBestPhenotype;
import static io.jenetics.engine.Limits.bySteadyFitness;

class EvolutionAlgorithm {
    private int[] bestChromosome;
    private double bestNetProfit;

    private float[] cost;
    private double credPercent;
    private int[] min;
    private int[] max;
    private int k;
    private float budget;
    private int[] productionLimit;
    private float[] resourcesCost;
    private double[][] resources;
    public IntRange[] intRanges;
    public DoubleRange doubleRange;
    private int chromLength;
    private boolean isChecked;


    private void fillRestrictions(double[][] rest) {
        // Начинаем с 0, поскольку шапка и первый столбец уже исключены
        for (int j = 0; j < rest[0].length; j++) {
            max[j] = (int) rest[0][j];
            cost[j] = (float) rest[1][j];
            productionLimit[j] = (int) rest[2][j];
            min[j] = (int) rest[3][j];
        }
    }

    private void fillResources(double[][] res) {
        int rows = res.length;
        int cols = res[0].length;

        // Копируем значения последнего столбца массива res в массив resourcesCost
        for (int i = 0; i < rows; i++) {
            resourcesCost[i] = (float) res[i][cols - 1];
        }

        // Копируем остальные значения из res в массив resources
        for (int i = 0; i < rows; i++) {
            // Исключаем последний столбец
            System.arraycopy(res[i], 0, resources[i], 0, cols - 1);
        }
    }


    public void Start(double[][] restr, double[][] res, float qval, float zval, int kval, float pzval, boolean isChecked) {
        this.isChecked = isChecked;
        int length = restr[0].length;
        max = new int[length];
        min = new int[length];
        productionLimit = new int[length];
        cost = new float[length];
        resources = new double[res.length][res[0].length - 1];
        resourcesCost = new float[res.length];
        chromLength = length;
        k = kval;
        budget = qval;
        credPercent = pzval;
        fillRestrictions(restr);
        fillResources(res);

        if (!isChecked) {
            for (int i = 0; i < length; i++) {
                min[i] *= k;
                max[i] *= k;
                productionLimit[i] *= k;
            }
        }

        isEnoughFunds(qval,zval);

        intRanges = new IntRange[length];
        for (int i = 0; i < length; i++) {
            intRanges[i] = IntRange.of(min[i], max[i]);
        }
        doubleRange = DoubleRange.of(0, zval);
        System.out.println(Arrays.toString(intRanges));
        System.out.println((doubleRange));

        double[] bestChromosomeFitness = RunEvolution();
        //Конвертируем хромосому в целочисленную
        bestChromosome = convertBestChromosomeToInt(bestChromosomeFitness,intRanges);

        //Аналогичная конвертация, но теперь кредитного гена
        double bestCreditUsed = (round(extractLastDoubleGene(bestChromosomeFitness, doubleRange), 2));

        double bestSumCost = resCost(bestChromosome);
        System.out.println("best cost = "+ bestSumCost);

        double bestSumProfit = totSum(bestChromosome);
        System.out.println("best sum = "+ bestSumProfit);

        System.out.println(bestNetProfit == (round((bestSumProfit - bestSumCost),2)));
        double totalCreditPaymentValue = totalCreditPayment(bestCreditUsed, pzval, k * 12);


        if (bestNetProfit != 0) {
            StringBuilder message = new StringBuilder();
            message.append("Оптимальный план производства: ").append(Arrays.toString(bestChromosome)).append(".\n");
            message.append("Были указаны бюджет в размере ").append(budget).append(" у.д.е и кредитный потенциал в размере ").append(zval).append(" у.д.е.\n");
            message.append("Затраты производства по рассчитанному плану составят ").append(round(bestSumCost, 2)).append(" у.д.е.\n");
            message.append("Для реализации рассчитанного плана потребуется взять в кредит ").append(bestCreditUsed).append(" у.д.е.\n");
            message.append("Кредитные затраты составят ").append(round(totalCreditPaymentValue, 2)).append(" у.д.е.\n");
            message.append("Сумма затрат по рассчитанному плану составит ").append(round((bestSumCost + totalCreditPaymentValue), 2)).append(" у.д.е.\n");

            message.append("Выручка с продаж составит ").append(round(bestSumProfit, 2)).append(" у.д.е.\n");

            message.append("Прибыль составит ").append(bestNetProfit).append(" у.д.е.\n");

            showResults(message);
        } else {JOptionPane.showMessageDialog(null,"Оптимальное решение под заданные параметры не найдено");}
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    public static int[] convertBestChromosomeToInt(double[] bestChromosome, IntRange[] ranges) {
        return IntStream.range(0, ranges.length)
                .map(i -> (int) (bestChromosome[i] * ranges[i].size()) + ranges[i].min())
                .toArray();
    }

    public static double extractLastDoubleGene(double[] bestChromosome, DoubleRange range) {
        double value = bestChromosome[bestChromosome.length - 1];
        return value * (range.max() - range.min()) + range.min();
    }


    public double[] RunEvolution() {
        final Engine<DoubleGene, Double> engine = Engine
                .builder(this::fitness, codec(intRanges, doubleRange))
                .populationSize(100000)
                .selector(new TournamentSelector<>())
                .optimize(Optimize.MAXIMUM)
                .alterers(
                        new Mutator<>(0.05),
                        new SinglePointCrossover<>(0.65)
                )
                .build();
        final EvolutionStatistics<Double, ?>
                statistics = EvolutionStatistics.ofNumber();

        final Phenotype<DoubleGene, Double> best = engine.stream()
                .limit(bySteadyFitness(30))
                .peek(statistics)
                .collect(toBestPhenotype());
        bestNetProfit = round(best.fitness(),2);
        return best.genotype().chromosome().stream().mapToDouble(DoubleGene::allele).toArray();
    }

    public record Ranges(int[] ivalues, double dvalue) {
    }

    static Codec<Ranges, DoubleGene> codec(IntRange[] iranges, DoubleRange drange) {
        return Codec.of(
                Genotype.of(DoubleChromosome.of(DoubleRange.of(0, 1), iranges.length + 1)),
                gt -> {
                    final var ch = gt.chromosome();
                    return new Ranges(
                            IntStream.range(0, iranges.length)
                                    .map(i -> (int) (ch.get(i).doubleValue() * iranges[i].size()) + iranges[i].min())
                                    .toArray(),
                            ch.get(iranges.length).doubleValue() * (drange.max() - drange.min()) + drange.min()
                    );
                }
        );
    }

    public double fitness(Ranges value) {
        double totalProfit = 0;
        double totalCost = 0;
        int[] x = value.ivalues();
        double y = value.dvalue(); // Взятый кредит

        // Вычисление общей прибыли
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < resources.length; j++) {
                if (!isChecked) {
                    totalCost += resources[j][i] * x[i] * resourcesCost[j]*k;
                }
                else totalCost += resources[j][i] * x[i] * resourcesCost[j];
            }
            totalProfit += cost[i] * x[i];
            if (!isChecked){
                if (x[i] < min[i] || x[i] > max[i] || x[i] > productionLimit[i] * k) return 0;
            }
            else {
                if (x[i] < min[i] || x[i] > max[i] || x[i] > productionLimit[i]) return 0;
            }
        }

        double totalCreditPaymentValue = totalCreditPayment(y, credPercent, k * 12);
        totalCost += totalCreditPaymentValue;

        // Проверка на превышение бюджета и кредитного потенциала
        if (totalCost > (budget + y)) return 0;
        return totalProfit - totalCost;
    }

    public boolean isEnoughFunds(double q, double z) {
        // Вычисление затрат на ресурсы
        double minPlanCost = 0;
        for (int i = 0; i < chromLength; i++) {
            for (int j = 0; j < resources.length; j++) {
                if (!isChecked){
                    minPlanCost += (resources[j][i] * min[i] * resourcesCost[j]*k);
                }
                else minPlanCost += (resources[j][i] * min[i] * resourcesCost[j]);
            }
        }
        // Отладочное сообщение о минимальной сумме для начала производства
        System.out.println("Минимальная сумма для начала производства: " + minPlanCost);
        // Проверяем, достаточно ли средств для минимального плана
        if (minPlanCost <= (q + z)) {
            return true;
        } else {
            // Выводим сообщение в окне Swing о недостатке средств
            JOptionPane.showMessageDialog(null, "Недостаточно средств для минимального производственного плана."+
                    "\n"+"Для указанных параметров сумма выполнения минимального плана равна "+round(minPlanCost,2)+" у.д.е", "Ошибка", JOptionPane.ERROR_MESSAGE);
            // В случае недостатка средств выбрасываем исключение
            throw new IllegalStateException("Insufficient funds for the minimum production plan.");
            // либо возвращаем false, если хотим просто завершить выполнение метода
            // return false;
        }
    }

    public double resCost(int[] x){
        double totalCost = 0;
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < resources.length; j++) {
                if (!isChecked) {
                     totalCost += resources[j][i] * x[i] * resourcesCost[j]*k;
                }
                else totalCost += resources[j][i] * x[i] * resourcesCost[j];
            }
        }
        return totalCost;
    }

    public double totSum(int[] x){
        double sum = 0;
        for (int i = 0; i < x.length; i++) {
            sum += cost[i] * x[i];
        }
        return sum;
    }

    private double totalCreditPayment(double principal, double annualRate, int months) {
        if (annualRate == 0) {
            return principal; // Процентная ставка 0%, возвращаем только тело кредита
        }
        double monthlyRate = annualRate / 100 / 12;
        double annuityCoefficient = (monthlyRate * Math.pow(1 + monthlyRate, months)) / (Math.pow(1 + monthlyRate, months) - 1);
        double monthlyPayment = principal * annuityCoefficient;
        return monthlyPayment * months;
    }
/*    private static double creditPayment(double z, double p, int k) {
        if (p == 0) {
            return 0; // Процентная ставка 0%
        }
        p = p / 100;
        return ((z * p) / 365) * 30.4d * k;
    }*/



    private JTable createTable(int[] production, int k) {
        // Создание модели таблицы
        DefaultTableModel model = new DefaultTableModel();
        model.addColumn("Месяц");

        // Добавление столбцов для каждого товара
        for (int i = 0; i < production.length; i++) {
            model.addColumn("Товар " + (i + 1));
        }

        // Расчет количества каждого товара в месяц и добавление в таблицу
        int[][] monthlyProduction = new int[k][production.length];
        for (int i = 0; i < production.length; i++) {
            int remaining =  production[i];
            for (int month = 0; month < k; month++) {
                int amount = (int) Math.ceil((double) remaining / (k - month));
                monthlyProduction[month][i] = amount;
                remaining -= amount;
            }
        }

        // Добавление данных в таблицу
        for (int month = 0; month < k; month++) {
            Object[] row = new Object[production.length + 1];
            row[0] = "Месяц " + (month + 1);
            for (int i = 0; i < production.length; i++) {
                row[i + 1] = monthlyProduction[month][i];
            }
            model.addRow(row);
        }

        // Создание таблицы
        JTable table = new JTable(model);

        // Установка предпочтительного размера таблицы
        table.setPreferredScrollableViewportSize(table.getPreferredSize());

        return table;
    }

    private void showResults(StringBuilder message) {
        if (bestChromosome == null) {
            System.err.println("Ошибка: Лучшая хромосома не была определена.");
            return;
        }

        JTable table = createTable(bestChromosome, k); // Ваши данные
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(700, 400));

        // Создание панели для текста
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String text = "<html>" + message.toString().replace("\n", "<br>") + "</html>";
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createTitledBorder("Итоги расчёта:"));
        textPanel.setBorder(BorderFactory.createEtchedBorder(0));
        textPanel.add(label);

        // Создание панели для таблицы
        JPanel tablePanel = new JPanel(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(Color.decode("#7BB4AD"));
        scrollPane.setBorder(BorderFactory.createTitledBorder("Распределение выпуска продукции на " + k + " месяцев:"));

        // Установка предпочтительных размеров для JScrollPane
        scrollPane.setPreferredSize(new Dimension(680, 200));
        tablePanel.setBorder(BorderFactory.createEtchedBorder(0));
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // Добавление панелей на основную панель
        mainPanel.add(textPanel);
        mainPanel.add(tablePanel);
        mainPanel.setBackground(Color.decode("#7BB4AD"));

        // Создание всплывающего окна с основной панелью
        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(mainPanel);
        optionPane.setBackground(Color.decode("#7BB4AD"));
        JDialog dialog = optionPane.createDialog("Итоги расчёта");
        dialog.getRootPane().setBackground(Color.decode("#7BB4AD"));
        dialog.pack();
        dialog.setVisible(true);
    }
}
