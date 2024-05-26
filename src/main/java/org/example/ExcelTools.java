package org.example;
import org.apache.poi.ss.usermodel.*;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ExcelTools {
    static void writeToExcel(JTable table1, JTable table2, Workbook wb, String path) throws IOException {
        Sheet sheet1 = wb.createSheet("Таблица ограничений выпуска"); // Создаем новый лист для первой таблицы
        writeTableToSheet(table1, sheet1); // Записываем первую таблицу

        Sheet sheet2 = wb.createSheet("Таблица ресурсов"); // Создаем новый лист для второй таблицы
        writeTableToSheet(table2, sheet2); // Записываем вторую таблицу

        File file = new File(path+".xlsx");
        if (file.exists()) {
            int response = JOptionPane.showConfirmDialog(null,
                    "Файл уже существует, вы хотите его перезаписать?", "Подтвердите перезапись",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (response == JOptionPane.NO_OPTION) {
                return; // Не перезаписывать файл
            }
        }
        FileOutputStream fileOut = new FileOutputStream(file);
        wb.write(fileOut);//Save the file
    }


    private static void writeTableToSheet(JTable table, Sheet sheet) {
        Row row; //Row created at line 3
        TableModel model = table.getModel(); //Table model

        Row headerRow = sheet.createRow(0); //Create row at line 0
        for (int headings = 0; headings < model.getColumnCount(); headings++) { //For each column
            headerRow.createCell(headings).setCellValue(model.getColumnName(headings));//Write column name
        }

        for (int rows = 0; rows < model.getRowCount(); rows++) { //For each table row
            row = sheet.createRow((rows + 1)); //Create new row at the start of each loop iteration
            for (int cols = 0; cols < table.getColumnCount(); cols++) { //For each table column
                if (table.getValueAt(rows, cols) == null) row.createCell(cols).setCellValue("0");
                else row.createCell(cols).setCellValue(model.getValueAt(rows, cols).toString()); //Write value
            }
        }

        model.getRowCount();
    }
    static List<JTable> readFromExcel(String path) throws IOException {
        Workbook workbook = WorkbookFactory.create(new FileInputStream(path));
        List<JTable> tables = new ArrayList<>();

        for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
            Sheet sheet = workbook.getSheetAt(sheetNum);

            int rows = sheet.getPhysicalNumberOfRows()-1;
            int cols = sheet.getRow(1).getPhysicalNumberOfCells();

            // If the second row is empty, decrement the total number of rows
            if (rows > 1 && sheet.getRow(1) != null && sheet.getRow(1).getPhysicalNumberOfCells() == 0)
                rows--;

            String[][] data = new String[rows][cols]; // Создаем массив с новым размером

            for (int r = 1; r <= rows; r++) { // Изменяем условие цикла на r <= rows
                Row row = sheet.getRow(r);
                if (row != null) {
                    for (int c = 0; c < cols; c++) {
                        Cell cell = row.getCell(c);
                        if (cell != null) {
                            data[r-1][c] = cell.toString(); // Заполняем массив, начиная с первой строки
                        } else {
                            data[r-1][c] = ""; // Handle empty cells
                        }
                    }
                }
            }

            System.out.println(Arrays.deepToString(data)+ " " + Arrays.toString(getColumnNames(sheet)));
            tables.add(new JTable(data, getColumnNames(sheet)));

        }
        workbook.close();
        return tables;
    }
    private static String[] getColumnNames(Sheet sheet) {
        Row row = sheet.getRow(0);
        int cols = row.getPhysicalNumberOfCells();
        String[] columnNames = new String[cols];
        for (int c = 0; c < cols; c++) {
            Cell cell = row.getCell(c);
            if (cell != null) {
                columnNames[c] = cell.toString();
            } else {
                columnNames[c] = ""; // Handle empty cells
            }
        }
        return columnNames;
    }
}

