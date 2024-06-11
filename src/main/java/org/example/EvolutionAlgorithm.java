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
    private static int[] bestChromosome;
    private static double bestSumCost;
    private static double bestSumProfit;
    private static double bestNetProfit;
    private static double bestCreditUsed;

    private static float[] cost;
    private static double credPercent;
    private static int[] min;
    private static int[] max;
    private static int k;
    private static float budget;
    private static int[] productionLimit;
    private static float[] resourcesCost;
    private static double[][] resources;
    public static IntRange[] intRanges;
    public static DoubleRange doubleRange;
    private static StringBuilder debugMessages = new StringBuilder();


    private static void fillRestrictions(double[][] rest) {
        // Начинаем с 0, поскольку шапка и первый столбец уже исключены
        for (int j = 0; j < rest[0].length; j++) {
            max[j] = (int) rest[0][j];
            cost[j] = (float) rest[1][j];
            productionLimit[j] = (int) rest[2][j];
            min[j] = (int) rest[3][j];
        }
    }

    private static void fillResources(double[][] res) {
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


    static void Start(double[][] restr, double[][] res, float qval, float zval, int kval, float pzval) {
        int length = restr[0].length;
        max = new int[length];
        min = new int[length];
        productionLimit = new int[length];
        cost = new float[length];
        resources = new double[res.length][res[0].length - 1];
        resourcesCost = new float[res.length];

        k = kval;
        budget = qval;
        credPercent = pzval;
        fillRestrictions(restr);
        fillResources(res);

        intRanges = new IntRange[length];
        for (int i = 0; i < length; i++) {
            intRanges[i] = IntRange.of(min[i], max[i]);
        }
        doubleRange = DoubleRange.of(0, zval);
        System.out.println(Arrays.toString(intRanges));
        System.out.println((doubleRange));

        double[] bestChromosomeFitness = RunEvolution();
        System.out.println(Arrays.toString(bestChromosomeFitness));

        StringBuilder message = new StringBuilder();

        message.append("Лучшая хромосома - ").append(Arrays.toString(bestChromosome)).append(".\n");
        message.append("Затраты производства по рассчитанному плану (включая кредит) составят ").append(rnd((float)bestSumCost)).append(" у.д.е.\n");
        message.append("Были указаны бюджет в размере ").append(budget).append(" у.д.е и кредитные возможности в размере ").append(zval).append(" у.д.е.\n");

        message.append("Выручка с продаж составит ").append(rnd((float)bestSumProfit)).append(" у.д.е.\n");

        message.append("Для реализации рассчитанного плана потребуется взять в кредит ").append(rnd((float)bestCreditUsed)).append(" у.д.е.\n");
        message.append("Прибыль составит ").append(rnd((float)bestNetProfit)).append(" у.д.е.\n");

        // Новый расчет процентов по кредиту и общей суммы долга с процентами
        double totalCreditPaymentValue = totalCreditPayment(bestCreditUsed, pzval, k * 12);
        double monthlyCreditPayment = totalCreditPaymentValue / (k * 12);
        double totalCreditWithInterest = bestCreditUsed + (totalCreditPaymentValue - bestCreditUsed); // Сумма кредита плюс проценты

        showResults(message);
        System.out.println(debugMessages);
    }

    public static double[] RunEvolution() {
        final Engine<DoubleGene, Double> engine = Engine
                .builder(EvolutionAlgorithm::fitness, codec(intRanges, doubleRange))
                .populationSize(100000)
                .selector(new MonteCarloSelector<>())
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


    private static float rnd(float number) {
        number = Math.round(number * 100.0f) / 100.0f;
        return number;
    }
    private static double totalCreditPayment(double principal, double annualRate, int months) {
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


    public static double fitness(Ranges value) {
        double totalProfit = 0;
        double totalCost = 0;
        int[] x = value.ivalues;
        double y = value.dvalue; // Взятый кредит
        StringBuilder debugInfo = new StringBuilder();

        // Вычисление общей прибыли
        for (int i = 0; i < x.length; i++) {
            totalProfit += cost[i] * x[i];
        }

        // Вычисление затрат на ресурсы
        for (int i = 0; i < x.length; i++) {
            for (int j = 0; j < resources.length; j++) {
                totalCost += resources[j][i] * x[i] * resourcesCost[j];
            }
        }

        // Проверка ограничений
        for (int i = 0; i < x.length; i++) {
            if (x[i] < min[i] || x[i] > max[i] || x[i] > productionLimit[i] * k) {
                debugInfo.append("Restriction violated for chromosome: ")
                        .append(Arrays.toString(x))
                        .append(", credit: ")
                        .append(y)
                        .append("\n");
                return 0;
            }
        }

        // Проверка на превышение бюджета и кредитного потенциала
        if (totalCost > (budget + y)) {
            debugInfo.append("Budget exceeded for chromosome: ")
                    .append(Arrays.toString(x))
                    .append(", credit: ")
                    .append(y)
                    .append(", total cost: ")
                    .append(totalCost)
                    .append(", budget: ")
                    .append(budget)
                    .append("\n");
            return 0;
        }

        // Учет кредита
        double totalCreditPaymentValue = totalCreditPayment(y, credPercent, k * 12);
        totalCost += totalCreditPaymentValue; // Добавляем полную стоимость кредита

        // Вычисление чистой прибыли
        double netProfit = totalProfit - totalCost;

        // Отладочный вывод
        debugInfo.append("Chromosome: ")
                .append(Arrays.toString(x))
                .append(", Credit: ")
                .append(y)
                .append(", Net Profit: ")
                .append(netProfit)
                .append("\n");

        // Сохранение данных, если текущая хромосома лучше предыдущей лучшей
        if (netProfit > bestNetProfit) {
            bestNetProfit = netProfit;
            bestChromosome = value.ivalues(); // Сохраняем содержимое хромосомы
            bestCreditUsed = value.dvalue(); // Сохраняем сумму взятого кредита
            bestSumCost = totalCost; // Сохраняем затраты на ресурсы
            bestSumProfit = totalProfit; // Сохраняем полученную выручку
        }

        return netProfit;
    }
    private static JTable createTable(int[] production, int k) {
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

    private static void showResults(StringBuilder message) {
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
