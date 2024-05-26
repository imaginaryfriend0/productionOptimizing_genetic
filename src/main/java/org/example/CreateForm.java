package org.example;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Objects;

import static org.example.ExcelTools.readFromExcel;

class CreateForm {
    private static int nval;
    private static int mval;
    private static int kval;
    private static float qval;
    private static float zval;
    private static float pzval;
    private static JTable table1;
    private static JTable table2;
    private static final Dimension textFieldDimension = new Dimension(230,38);
    private static final Dimension textFieldDimension1 = new Dimension(260,38);
    private static String[] columns;
    private static Object[][] data;
    private static JScrollPane pane1;
    private static JScrollPane pane2;
    private static double[][] rest;
    private static double[][] res;
    private static JPanel panelPane1;
    private static JPanel panelPane2;
    private static final Color color = Color.decode("#7BB4AD");

    static void createView(){
        JFrame frame = new JFrame("Генетический алгоритм");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ImageIcon icon = new ImageIcon("icon.png");
        frame.setIconImage(icon.getImage());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setMaximumSize(new Dimension(Toolkit.getDefaultToolkit().getScreenSize()));
        UIManager.put("Panel.background",color);
        frame.add(mainPanel);

        JMenuBar menuBar = new JMenuBar();

        JMenu fileMenu = new JMenu("Файл");
        JMenuItem openItem = new JMenuItem("Открыть");
        JMenuItem saveItem = new JMenuItem("Сохранить");
        JMenuItem exitItem = new JMenuItem("Выйти");


        JPanel panelN = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField n = new JTextField("Введите количество видов товаров");
        n.setBorder(BorderFactory.createTitledBorder("Количество видов товаров (n)"));
        n.setHorizontalAlignment(SwingConstants.CENTER);
        n.setPreferredSize(textFieldDimension);
        n.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                n.setText("");
            }
        });
        panelN.add(n);
        //mainPanel.add(panelN);

        JPanel panelM = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField m = new JTextField("Введите количество видов ресурсов");
        m.setBorder(BorderFactory.createTitledBorder("Количество видов ресурсов (m)"));
        m.setHorizontalAlignment(SwingConstants.CENTER);
        m.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                m.setText("");
            }
        });
        m.setPreferredSize(textFieldDimension);
        panelM.add(m);
        //mainPanel.add(panelM);

        JPanel panelK = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField k = new JTextField("Введите количество месяцев");
        k.setBorder(BorderFactory.createTitledBorder("Количество месяцев (k)"));
        k.setHorizontalAlignment(SwingConstants.CENTER);
        k.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                k.setText("");
            }
        });
        k.setPreferredSize(textFieldDimension);
        panelK.add(k);
        //mainPanel.add(panelK);

        JPanel panelQ = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField q = new JTextField("Введите количество собственных средств");
        q.setBorder(BorderFactory.createTitledBorder("Количество собственных средств (q)"));
        q.setHorizontalAlignment(SwingConstants.CENTER);
        q.setPreferredSize(textFieldDimension);
        q.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                q.setText("");
            }
        });

        JPanel panelZ = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField z = new JTextField("Введите кредитные возможности");
        z.setBorder(BorderFactory.createTitledBorder("Кредитные возможности (z)"));
        z.setHorizontalAlignment(SwingConstants.CENTER);
        z.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                z.setText("");
            }
        });

        JPanel panelPZ = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JTextField pz = new JTextField("Введите предполагаемый процент кредита");
        pz.setBorder(BorderFactory.createTitledBorder("Предполагаемый процент кредита (pz)"));
        pz.setHorizontalAlignment(SwingConstants.CENTER);
        pz.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                pz.setText("");
            }
        });

        JPanel panelB = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton b = new JButton("Создать таблицу");
        b.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                try {
                    nval = Integer.parseInt(n.getText());
                    mval = Integer.parseInt(m.getText());
                    kval = Integer.parseInt(k.getText());

                    n.setEnabled(false);
                    m.setEnabled(false);
                    k.setEnabled(false);
                    b.setEnabled(false);

                    panelPane1 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    pane1 = createTable1(nval);
                    pane1.setBorder(BorderFactory.createTitledBorder("Таблица ограничений выпуска"));
                    pane1.setPreferredSize(new Dimension(500,107));
                    Dimension p1 = pane1.getPreferredSize();
                    System.out.println(p1);
                    panelPane1.add(pane1);
                    //mainPanel.add(panelPane1);

                    panelPane2 = new JPanel(new FlowLayout(FlowLayout.LEFT));
                    pane2 = createTable2(nval, mval);
                    pane2.setBorder(BorderFactory.createTitledBorder("Таблица ресурсов"));
                    pane2.setPreferredSize(new Dimension(500,200));
                    pane2.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
                    Dimension p2 = pane2.getPreferredSize();
                    System.out.println(p2);
                    panelPane2.add(pane2);
                    //mainPanel.add(panelPane2);


                    JPanel second = new JPanel();
                    second.setBorder(BorderFactory.createEtchedBorder(0));
                    second.add(panelPane1);
                    second.add(panelPane2);
                    mainPanel.add(second);

                    JPanel panelB2 = new JPanel(new FlowLayout(FlowLayout.CENTER));
                    JButton b2 = new JButton("Рассчитать");


                    panelQ.add(q);
                    q.setPreferredSize(textFieldDimension1);
                    mainPanel.add(panelQ);
                    panelZ.add(z);
                    z.setPreferredSize(textFieldDimension1);
                    mainPanel.add(panelZ);
                    panelPZ.add(pz);
                    mainPanel.add(panelPZ);
                    pz.setPreferredSize(textFieldDimension1);
                    panelB2.add(b2);
                    mainPanel.add(panelB2);

                    JPanel third = new JPanel();
                    third.setBorder(BorderFactory.createEtchedBorder(0));
                    third.add(panelQ);
                    third.add(panelZ);
                    third.add(panelPZ);
                    mainPanel.add(third);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    b2.addMouseListener(new MouseAdapter() {
                        @Override
                        public void mouseClicked(MouseEvent e) {
                            try {
                                qval = Integer.parseInt(q.getText());
                                zval = Float.parseFloat(z.getText());
                                pzval = Float.parseFloat(pz.getText());

                                if ((Objects.equals(z.getText(), "")) || (Objects.equals(z.getText(), "Введите размер кредита"))) {
                                    zval = 0;
                                } else {
                                    zval = Float.parseFloat(z.getText());
                                }

                                if ((Objects.equals(pz.getText(), "")) || (Objects.equals(pz.getText(), "Введите размер процентной ставки"))) {
                                    pzval = 0;
                                } else {
                                    pzval = Float.parseFloat(pz.getText());
                                }
                                table1.setEnabled(false);
                                table2.setEnabled(false);
                                b2.setEnabled(false);
                                //rest = restrictions(table1);
                                rest = restrictions((JTable) pane1.getViewport().getView());
                                res = resources((JTable) pane2.getViewport().getView());
                                //res = resources(table2);
                                EvolutionAlgorithm.Start(rest, res, qval, zval, kval, pzval);
                                frame.pack();
                                frame.setLocationRelativeTo(null);
                            } catch (NumberFormatException oe) {
                                JOptionPane.showMessageDialog(frame, "Поля ввода должны содержать целочисленные значения, либо числовые значения с плавающей точкой.", "Ошибка!", JOptionPane.ERROR_MESSAGE);
                            }
                        }
                    });
                } catch (NumberFormatException oe) {
                    JOptionPane.showMessageDialog(frame, "Поля ввода должны содержать целочисленные значения.", "Ошибка!", JOptionPane.ERROR_MESSAGE);
                }

                frame.revalidate();
                frame.repaint();
                frame.setVisible(true);
            }
        });

        openItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Выбранный файл: " + selectedFile.getAbsolutePath());

                try {
                    List<JTable> models = readFromExcel(selectedFile.getAbsolutePath());
                    JTable pn1 = models.get(0);
                    pn1.setPreferredScrollableViewportSize(pn1.getPreferredSize());
                    JTable pn2 = models.get(1);
                    pn2.setPreferredScrollableViewportSize(pn2.getPreferredSize());

                    for (JTable model : models) {
                        for (int row = 0; row < model.getRowCount(); row++) {
                            for (int col = 0; col < model.getColumnCount(); col++) {
                                System.out.print(model.getValueAt(row, col) + " ");
                            }
                            System.out.println();
                        }
                        System.out.println("-----");
                    }

                    pane1.setViewportView(pn1);
                    pane2.setViewportView(pn2);

                    panelPane1.revalidate();
                    panelPane1.repaint();

                    panelPane2.revalidate();
                    panelPane2.repaint();
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        saveItem.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                JTable pn1 = (JTable) pane1.getViewport().getView();
                JTable pn2 = (JTable) pane2.getViewport().getView();
                File selectedFile = fileChooser.getSelectedFile();
                System.out.println("Выбранный файл: " + selectedFile.getAbsolutePath());
                try {
                    Workbook wb = new XSSFWorkbook();
                    ExcelTools.writeToExcel(pn1,pn2,wb,selectedFile.getAbsolutePath());
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });

        exitItem.addActionListener(e -> System.exit(0));

        JPanel first = new JPanel();
        first.setBorder(BorderFactory.createEtchedBorder(0));
        first.add(panelN);
        first.add(panelM);
        first.add(panelK);
        mainPanel.add(first);



        panelB.add(b);
        mainPanel.add(panelB);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.setBackground(color);

        fileMenu.add(openItem);
        fileMenu.add(saveItem);
        fileMenu.add(exitItem);
        menuBar.add(fileMenu);

        frame.setJMenuBar(menuBar);

        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    private static double[][] restrictions (JTable table){
        double[][] restrictions = new double[4][nval];
        for(int i = 0; i < 4; i++){
            for(int j = 1; j <= nval; j++){
                if (table.getValueAt(i,j) == null){
                    restrictions[i][j-1] = 0;
                } else restrictions[i][j-1] = Double.parseDouble(table.getValueAt(i,j).toString());
            }
        }
        return restrictions;
    }

    private static double[][] resources (JTable table){
        double[][] resources = new double[mval][nval+1];
        for(int i = 0; i < mval; i++){
            for(int j = 1; j <= nval+1; j++){
                if (table.getValueAt(i,j) == null){
                    resources[i][j - 1] = 0;
                } else resources[i][j - 1] = Double.parseDouble(table.getValueAt(i,j).toString());
            }
        }
        return resources;
    }

    private static JScrollPane createTable1(int n){
        columns = new String[n+1];
        for (int i = 1; i < columns.length; i++) {
            columns[i] = "Товар №" + i;
        }
        columns[0] = "Сведения о товаре";

        data = new Object[4][n+1];
        data[0][0] = "Прогноз. количество";
        data[1][0] = "Прибыль с единицы";
        data[2][0] = "Ограничения выпуска";
        data[3][0] = "Минимальный план";
        table1 = new JTable(data,columns);
        table1.setGridColor(Color.BLACK);
        DefaultTableModel model = new DefaultTableModel(data,columns) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex != 0;
            }
        };
        table1.getTableHeader().setReorderingAllowed(false);
        table1.setModel(model);
        table1.setPreferredScrollableViewportSize(table1.getPreferredSize());
        JScrollPane pane = new JScrollPane(table1);
        resizeColumnWidth(table1);
        table1.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        pane.setBackground(color);
        pane.revalidate();
        pane.repaint();

        return pane;
    }

    private static JScrollPane createTable2(int n, int m){
        columns = new String[n+2];
        for (int i = 1; i < columns.length-1; i++) {
            columns[i] = "Товар №" + i;
        }
        columns[0] = "Вид ресурса";
        columns[n+1] = "Цена за ед. ресурса";

        data = new Object[m][n+2];

        for (int i = 0; i < m; i++){
            data[i][0] = "Ресурс №"+(i+1);
        }

        table2 = new JTable(data,columns);
        table2.setGridColor(Color.BLACK);
        DefaultTableModel model = new DefaultTableModel(data,columns) {
            @Override
            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return columnIndex != 0;
            }
        };
        table2.getTableHeader().setReorderingAllowed(false);
        table2.setModel(model);
        table2.setPreferredScrollableViewportSize(table2.getPreferredSize());
        JScrollPane pane = new JScrollPane(table2);
        resizeColumnWidth(table2);
        table2.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        pane.setBackground(color);
        pane.revalidate();
        pane.repaint();

        return pane;
    }

    private static void resizeColumnWidth(JTable table) {
        final TableColumnModel columnModel = table.getColumnModel();
        for (int column = 0; column < table.getColumnCount(); column++) {
            int width = 15; // Минимальная ширина столбца

            for (int row = 0; row < table.getRowCount(); row++) {
                TableCellRenderer renderer = table.getCellRenderer(row, column);
                Component comp = table.prepareRenderer(renderer, row, column);
                width = Math.max(comp.getPreferredSize().width +1 , width);
            }

            // Учитываем ширину заголовка столбца
            TableCellRenderer headerRenderer = table.getTableHeader().getDefaultRenderer();
            Component headerComp = headerRenderer.getTableCellRendererComponent(table, table.getColumnModel().getColumn(column).getHeaderValue(), false, false, 0, column);
            width = Math.max(width, headerComp.getPreferredSize().width + 1);

            columnModel.getColumn(column).setPreferredWidth(width);
        }
    }
}