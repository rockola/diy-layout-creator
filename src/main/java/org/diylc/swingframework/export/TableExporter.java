/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2010-2020 held jointly by the individual authors.

  This file is part of DIYLC.

  DIYLC is free software: you can redistribute it and/or modify it
  under the terms of the GNU General Public License as published by
  the Free Software Foundation, either version 3 of the License, or
  (at your option) any later version.

  DIYLC is distributed in the hope that it will be useful, but WITHOUT
  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
  or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public
  License for more details.

  You should have received a copy of the GNU General Public License
  along with DIYLC. If not, see <http://www.gnu.org/licenses/>.
*/
package org.diylc.swingframework.export;

import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Method;
import javax.imageio.ImageIO;
import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.diylc.common.Config;

/**
 * Utility class that exports {@link JTable} to:
 *
 * <ul>
 *   <li>CSV
 *   <li>PNG
 *   <li>Excel
 *   <li>HTML
 * </ul>
 *
 * @author Branislav Stojkovic
 */
public class TableExporter {

  private static final Logger LOG = LogManager.getLogger(TableExporter.class);

  private static TableExporter instance;

  public static TableExporter getInstance() {
    if (instance == null) {
      instance = new TableExporter();
    }
    return instance;
  }

  private TableExporter() {}

  public void exportToExcel(JTable table, File file) throws IOException {
    LOG.info("Exporting table to Excel file: " + file.getAbsolutePath());
    LOG.debug("Creating workbook");
    HSSFWorkbook wb = new HSSFWorkbook();
    FileOutputStream fileOut = new FileOutputStream(file);

    LOG.debug("Creating sheet");
    HSSFSheet bomSheet = wb.createSheet("B.O.M.");

    LOG.debug("Writing header row");
    HSSFRow headerRow = bomSheet.createRow(0);
    HSSFCellStyle headerStyle = wb.createCellStyle();
    HSSFFont font = wb.createFont();
    font.setBold(true);
    font.setFontName(Config.getString("font.sans-serif"));
    headerStyle.setFont(font);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    headerStyle.setFillForegroundColor(HSSFColorPredefined.SKY_BLUE.getIndex());

    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
      HSSFCell cell = headerRow.createCell(i);
      // cell.setCellType(HSSFCell.CELL_TYPE_STRING);
      cell.setCellValue(table.getColumnName(i));
      cell.setCellStyle(headerStyle);
    }

    LOG.debug("Writing the data");
    HSSFCellStyle cellStyle = wb.createCellStyle();
    font = wb.createFont();
    font.setFontName(Config.getString("font.sans-serif"));
    cellStyle.setFont(font);
    cellStyle.setBorderBottom(BorderStyle.THIN);
    cellStyle.setBorderTop(BorderStyle.THIN);
    cellStyle.setBorderLeft(BorderStyle.THIN);
    cellStyle.setBorderRight(BorderStyle.THIN);

    for (int j = 0; j < table.getRowCount(); j++) {
      HSSFRow row = bomSheet.createRow(j + 1);
      for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
        HSSFCell cell = row.createCell(i);
        cell.setCellStyle(cellStyle);
        int columnIdx = table.convertRowIndexToModel(i);
        Object value = table.getValueAt(j, i);
        if (value != null) {
          Class<?> clazz = table.getModel().getColumnClass(columnIdx);

          if (short.class.isAssignableFrom(clazz) || Short.class.isAssignableFrom(clazz)) {
            // cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue(((Short) value).intValue());
          } else if (int.class.isAssignableFrom(clazz) || Integer.class.isAssignableFrom(clazz)) {
            // cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue((Integer) value);
          } else if (long.class.isAssignableFrom(clazz) || Long.class.isAssignableFrom(clazz)) {
            // cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue(((Long) value).intValue());
          } else if (float.class.isAssignableFrom(clazz) || Float.class.isAssignableFrom(clazz)) {
            // cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue(((Float) value).doubleValue());
          } else if (double.class.isAssignableFrom(clazz) || Double.class.isAssignableFrom(clazz)) {
            // cell.setCellType(HSSFCell.CELL_TYPE_NUMERIC);
            cell.setCellValue((Double) value);
          } else {
            // cell.setCellType(HSSFCell.CELL_TYPE_STRING);
            cell.setCellValue(value.toString());
          }
        }
      }
    }
    LOG.debug("Auto-fitting columns");
    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
      bomSheet.autoSizeColumn(i);
    }
    LOG.debug("Writing to the file");
    wb.write(fileOut);
    fileOut.close();
    LOG.debug("Done");
  }

  public void exportToHTML(JTable table, File file) throws IOException {
    LOG.info("Exporting table to HTML file: " + file.getAbsolutePath());
    FileWriter fstream = new FileWriter(file);
    BufferedWriter out = new BufferedWriter(fstream);
    out.write("<html><body><font face=\"sans-serif\"><table cellspacing=\"0\" border=\"1\">\n");

    LOG.debug("Writing header row");
    out.write("  <tr>\n");
    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
      out.write("    <th align=\"left\" bgcolor=\"#00CCFF\">" + table.getColumnName(i) + "</th>\n");
    }
    out.write("  </tr>\n");

    LOG.debug("Writing the data");
    for (int j = 0; j < table.getRowCount(); j++) {
      out.write("  <tr>\n");
      for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
        int rowIdx = table.convertRowIndexToModel(j);
        int columnIdx = table.convertRowIndexToModel(i);
        Object value = table.getValueAt(j, i);
        TableCellRenderer renderer = table.getCellRenderer(j, i);
        if (renderer != null) {
          Component rendererComponent =
              renderer.getTableCellRendererComponent(table, value, false, false, rowIdx, columnIdx);
          try {
            Method method = rendererComponent.getClass().getMethod("getText");
            out.write("    <td>" + method.invoke(rendererComponent) + "</td>\n");
          } catch (Exception e) {
            out.write("    <td>&nbsp;</td>\n");
          }
        } else {
          out.write("    <td>&nbsp;</td>\n");
        }
      }
      out.write("  </tr>\n");
    }
    out.write("</table></font></body></html>");
    out.close();
  }

  public void exportToCSV(JTable table, File file) throws IOException {
    LOG.info("Exporting table to CSV file: " + file.getAbsolutePath());
    FileWriter fstream = new FileWriter(file);
    BufferedWriter out = new BufferedWriter(fstream);

    LOG.debug("Writing header row");
    for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
      if (i > 0) {
        out.write(",");
      }
      out.write(table.getColumnName(i));
    }
    out.write("\n");

    LOG.debug("Writing the data");
    for (int j = 0; j < table.getRowCount(); j++) {
      for (int i = 0; i < table.getColumnModel().getColumnCount(); i++) {
        if (i > 0) {
          out.write(",");
        }
        int rowIdx = table.convertRowIndexToModel(j);
        int columnIdx = table.convertRowIndexToModel(i);
        Object value = table.getValueAt(j, i);
        TableCellRenderer renderer = table.getCellRenderer(j, i);
        if (renderer != null) {
          Component rendererComponent =
              renderer.getTableCellRendererComponent(table, value, false, false, rowIdx, columnIdx);
          try {
            Method method = rendererComponent.getClass().getMethod("getText");
            out.write(method.invoke(rendererComponent).toString());
          } catch (Exception e) {
          }
        }
      }
      out.write("\n");
    }
    out.close();
  }

  public void exportToPNG(JTable table, File file) throws IOException {
    LOG.info("Exporting table to PNG file: " + file.getAbsolutePath());
    table.clearSelection();
    LOG.debug("Creating image");
    BufferedImage image =
        new BufferedImage(
            table.getWidth(),
            table.getHeight() + table.getTableHeader().getHeight(),
            BufferedImage.TYPE_INT_RGB);
    Graphics2D g2d = image.createGraphics();
    LOG.debug("Painting the header");
    table.getTableHeader().paint(g2d);
    LOG.debug("Painting the table");
    g2d.translate(0, table.getTableHeader().getHeight());
    table.paint(g2d);
    LOG.debug("Writing image to file");
    ImageIO.write(image, "png", file);
    g2d.dispose();
  }
}
