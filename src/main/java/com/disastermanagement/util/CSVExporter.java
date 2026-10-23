package com.disastermanagement.util;

import javax.swing.*;
import javax.swing.table.TableModel;
import java.awt.Component;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Utility to export JTable and report data into CSV files.
 */
public class CSVExporter {

    private CSVExporter() {}

    /**
     * Exports a JTable to a CSV file selected by the user via JFileChooser.
     * @param parentComponent Parent Swing component for dialog display
     * @param table JTable containing data
     * @param defaultFileName Default file name suggestion
     * @return true if export succeeded
     */
    public static boolean exportTableToCSV(Component parentComponent, JTable table, String defaultFileName) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report to CSV");
        fileChooser.setSelectedFile(new File(defaultFileName));

        int userSelection = fileChooser.showSaveDialog(parentComponent);
        if (userSelection != JFileChooser.APPROVE_OPTION) {
            return false;
        }

        File fileToSave = fileChooser.getSelectedFile();
        if (!fileToSave.getName().toLowerCase().endsWith(".csv")) {
            fileToSave = new File(fileToSave.getParentFile(), fileToSave.getName() + ".csv");
        }

        try (PrintWriter writer = new PrintWriter(new FileWriter(fileToSave))) {
            TableModel model = table.getModel();
            int colCount = model.getColumnCount();
            int rowCount = model.getRowCount();

            // Write CSV Header
            for (int col = 0; col < colCount; col++) {
                writer.print(escapeCSV(model.getColumnName(col)));
                if (col < colCount - 1) writer.print(",");
            }
            writer.println();

            // Write CSV Rows
            for (int row = 0; row < rowCount; row++) {
                for (int col = 0; col < colCount; col++) {
                    Object val = model.getValueAt(row, col);
                    writer.print(escapeCSV(val == null ? "" : val.toString()));
                    if (col < colCount - 1) writer.print(",");
                }
                writer.println();
            }

            JOptionPane.showMessageDialog(parentComponent, 
                    "Report successfully exported to:\n" + fileToSave.getAbsolutePath(), 
                    "Export Successful", JOptionPane.INFORMATION_MESSAGE);
            return true;
        } catch (IOException e) {
            JOptionPane.showMessageDialog(parentComponent, 
                    "Failed to export CSV: " + e.getMessage(), 
                    "Export Error", JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    private static String escapeCSV(String value) {
        if (value == null) return "\"\"";
        if (value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
