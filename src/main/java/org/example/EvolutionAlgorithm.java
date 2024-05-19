package org.example;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.Arrays;

class EvolutionAlgorithm {
    private static float[] cost;
    private static int[] min;
    private static int[] max;
    private static int k;
    private static float creditPos;
    private static float budget;
    private static int[] productionLimit;
    private static float[] resourcesCost;
    private static double[][] resources;
    private static final Color color = Color.decode("#7BB4AD");
    private static void fillRestrictions(double [][] rest){
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
            resourcesCost[i] = (float) res[i][cols-1];
        }

        // Копируем остальные значения из res в массив resources
        for (int i = 0; i < rows; i++) {
            // Исключаем последний столбец
            System.arraycopy(res[i], 0, resources[i], 0, cols - 1);
        }
    }


    static void Start(double[][] restr, double[][] res, float qval, float zval, int kval, float pzval) {
        float totalIncome = 0;
        int length = restr[0].length;
        max = new int[length];
        min = new int[length];
        productionLimit = new int[length];
        cost = new float[length];
        resources = new double[res.length][res[0].length-1];
        resourcesCost = new float[res.length];
        int [] x = new int[length];

        k = kval;
        budget = qval;
        creditPos = zval;
        fillRestrictions(restr);
        fillResources(res);

        int[] bestChromosome = EvolutionEngine.RunEvolution(min[0], 30, length);
        float bestChromosomeFitness = fitness(bestChromosome);
        StringBuilder message = new StringBuilder();

        message.append("Лучшая хромосома - ").append(Arrays.toString(bestChromosome)).append(".\n");
        message.append("Затраты производства по рассчитанному плану составят ").append(rnd(bestChromosomeFitness)).append(" у.д.е.\n");
        message.append("Были указаны бюджет в размере ").append(budget).append(" у.д.е и кредитные возможности в размере ").append(zval).append(" у.д.е.\n");

        for (int i = 0; i < x.length; i++) {
            totalIncome += bestChromosome[i] * cost[i];
        }
        message.append("Выручка с продаж составит ").append(rnd(totalIncome)).append(" у.д.е.\n");

        float credit;
        if (budget < bestChromosomeFitness) {
            credit = bestChromosomeFitness - budget;
            if(credit >= pzval) credit = zval;

            message.append("Для реализации рассчитанного плана потребуется взять в кредит ").append(rnd((credit))).append(" у.д.е.\n");
            message.append("Прибыль составит ").append(rnd(totalIncome - (creditPayment(credit, pzval) + credit + bestChromosomeFitness))).append(" у.д.е.\n");
            float payment = creditPayment(credit, pzval);
            message.append("Проценты по кредиту составят ").append(rnd(payment)).append(" у.д.е. при проценте ").append(pzval)
                    .append("% сумма долга с процентами составит ").append(credit + rnd(payment)).append("\n");
        } else {
            credit = 0;
            message.append("Прибыль составит ").append(rnd(totalIncome - bestChromosomeFitness)).append(" у.д.е.\n");
            message.append("Кредит брать не придется.\n");
        }
        JTable table = createTable(bestChromosome,k);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setPreferredSize(new Dimension(700,400));

// Создание панели для текста
        JPanel textPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        String text = "<html>" + message.toString().replace("\n", "<br>") + "</html>";
        JLabel label = new JLabel(text);
        label.setBorder(BorderFactory.createTitledBorder("Итоги расчёта:"));
        textPanel.setBorder(BorderFactory.createEtchedBorder(0));
        textPanel.add(label);

// Создание панели для таблицы
        JPanel tablePanel = new JPanel(new GridBagLayout());
        table.setPreferredScrollableViewportSize(table.getPreferredSize());
        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBackground(color);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Распределение выпуска продукции на "+k+" месяцев:"));
        scrollPane.setPreferredSize(new Dimension(500,250));
        tablePanel.setBorder(BorderFactory.createEtchedBorder(0));
        tablePanel.add(scrollPane);

// Добавление панелей на основную панель
        mainPanel.add(textPanel);
        mainPanel.add(tablePanel);
        mainPanel.setBackground(color);

// Создание всплывающего окна с основной панелью
        JOptionPane optionPane = new JOptionPane();
        optionPane.setMessage(mainPanel);
        optionPane.setBackground(color);
        JDialog dialog = optionPane.createDialog("Итоги расчёта");
        dialog.getRootPane().setBackground(color);
        dialog.pack();
        dialog.setVisible(true);

        for (int row = 0; row < table.getRowCount(); row++) {
            for (int col = 0; col < table.getColumnCount(); col++) {
                System.out.print(table.getValueAt(row, col) + " ");
            }
            System.out.println();
        }
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
            int remaining = production[i];
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
        return new JTable(model);
    }


    private static float rnd(float number){
        number = Math.round(number * 100.0f) / 100.0f;
        return number;
    }
    private static float creditPayment (float sum, float percent){
        return ((percent/100)*sum);
    }

    static float fitness(final int[] x){
        float sum = 0;
        for (int i = 0; i < x.length; i++){
            for(int j = 0 ; j < resourcesCost.length; j++){ // 3 ресурса
                sum += (float) (x[i] * resources[j][i] * resourcesCost[j]);
            }
            if (x[i]<min[i]) return 0;
            if (x[i]>max[i]) return 0;
            if (x[i]>productionLimit[i] * k) return 0;
        }
        if (sum > (budget+creditPos)) return 0;
        else return sum;
    }
}
