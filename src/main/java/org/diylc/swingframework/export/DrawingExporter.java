/*
  DIY Layout Creator (DIYLC).
  Copyright (c) 2009-2020 held jointly by the individual authors.

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

import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.xobject.PdfFormXObject;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.orsonpdf.PDFDocument;
import com.orsonpdf.PDFGraphics2D;
import com.orsonpdf.Page;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import javax.imageio.ImageIO;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.diylc.common.Config;
import org.diylc.swingframework.IDrawingProvider;

/**
 * Export layout images.
 *
 * <p>Export destinations:
 * <ul>
 *   <li>PDF
 *   <li>PNG
 *   <li>Printer
 * </ul>
 *
 * @author Branislav Stojkovic
 */
public class DrawingExporter {

  private static final Logger LOG = LogManager.getLogger(DrawingExporter.class);
  private static final double margin = 0; // 1cm
  private static final int PDF_RESOLUTION = 72;
  private static final int PNG_RESOLUTION = 300;
  private static final int SCREEN_RESOLUTION = Toolkit.getDefaultToolkit().getScreenResolution();

  private DrawingExporter() {
    //
  }

  /**
   * Prints the image and scales it down if needed.
   *
   * @param provider
   * @throws PrinterException
   */
  public static void print(final IDrawingProvider provider) throws PrinterException {
    PrinterJob printJob = PrinterJob.getPrinterJob();
    final int pageCount = provider.getPageCount();
    printJob.setPrintable(new Printable() {

        @Override
        public int print(Graphics graphics, PageFormat pageFormat, int pageIndex)
            throws PrinterException {
          if (pageIndex >= pageCount) {
            return (NO_SUCH_PAGE);
          } else {
            Graphics2D g2d = (Graphics2D) graphics;

            Dimension d = provider.getSize();

            double pageRatio = (pageFormat.getWidth() / pageFormat.getHeight());
            double imageRatio = d.getWidth() / d.getHeight();
            double scale;
            if (imageRatio > pageRatio) {
              scale = ((pageFormat.getWidth() - 2 * margin) / d.getWidth());
            } else {
              scale = ((pageFormat.getHeight() - 2 * margin) / d.getHeight());
            }
            if (scale > 1) {
              scale = 1d;
            }

            g2d.translate(
                pageFormat.getImageableX() + margin,
                pageFormat.getImageableY() + margin);

            g2d.setFont(new Font(Config.getString("font.sans-serif"), Font.PLAIN, 6));
            FontMetrics metrics = g2d.getFontMetrics();
            g2d.setColor(Color.gray);

            if (scale < 1) {
              String warningStr = "Note: image has been scaled down to fit the page.";
              g2d.drawString(warningStr, 0, (int) (d.getHeight() * scale + metrics.getHeight()));
            }

            // g2d.scale(scale, scale);

            provider.draw(pageIndex, g2d, scale);

            return (PAGE_EXISTS);
          }
        }
      });
    if (printJob.printDialog()) {
      printJob.print();
    }
  }

  /**
   * Creates a PDF in the same size as the project.
   *
   * @param provider
   * @param file // * @throws DocumentException
   * @throws FileNotFoundException
   */
  public static void exportPdf(IDrawingProvider provider, File file)
      throws FileNotFoundException, IOException {

    Dimension d = provider.getSize();
    // We have to scale everything down because PDF resolution is slightly
    // lower.
    double factor = 1f * PDF_RESOLUTION / SCREEN_RESOLUTION;
    float totalWidth = (float) (factor * (2 * margin + d.getWidth()));
    float totalHeight = (float) (factor * (2 * margin + d.getHeight()));
    /*
    Document document =
        new Document(new com.lowagie.text.Rectangle(totalWidth, totalHeight));
    PdfWriter writer =
        PdfWriter.getInstance(document, new FileOutputStream(file));
    */
    PdfWriter writer = new PdfWriter(file);
    PdfDocument pdfDoc = new PdfDocument(writer);
    // TODO: Use size from totalWidth * totalHeight!
    // PageSize.A4.rotate() is just a placeholder! //ola 20191217
    // Document document = new Document(pdfDoc, PageSize.A4.rotate());
    Document document = new Document(pdfDoc);
    PDFDocument graphicPage = new PDFDocument();
    Rectangle boundingRectangle = new Rectangle((int) totalWidth, (int) totalHeight);
    Page page = graphicPage.createPage(boundingRectangle);
    // document.open();
    /* iText 5 stuff
    DefaultFontMapper mapper = new DefaultFontMapper() {
        @Override
        public BaseFontParameters getBaseFontParameters(String arg0) {
            BaseFontParameters p = super.getBaseFontParameters(arg0);
            if (p != null)
                p.encoding = BaseFont.IDENTITY_H;
            return p;
        }
        };
        // TODO use system fonts
    if (Utils.isWindows()) {
        mapper.insertDirectory(System.getenv("windir") + "\\Fonts");
    } else if (Utils.isMac()) {
        mapper.insertDirectory("$HOME/Library/Fonts");
    } else if (Utils.isUnix()) {
        mapper.insertDirectory("/usr/share/fonts/truetype/");
    }
    */
    // Map map = mapper.getMapper();
    // for (Iterator i = map.keySet().iterator(); i.hasNext();) {
    // String name = (String) i.next();
    // System.out.println(name + ": "
    // + ((DefaultFontMapper.BaseFontParameters) map.get(name)).fontName);
    // }

    for (int i = 0; i < provider.getPageCount(); i++) {
      if (i > 0) {
        // document.newPage();
        page = graphicPage.createPage(boundingRectangle);
      }
      /*
         PdfContentByte contentByte = writer.getDirectContent();
         PdfTemplate template = contentByte.createTemplate(totalWidth,
                                              totalHeight);
         Graphics2D g2d =
      template.createGraphics((float) (factor * d.getWidth()),
                        (float) (factor * d.getHeight()),
                        mapper);
         //                     g2d.scale(factor, factor);
         provider.draw(i, g2d, factor);
         contentByte.addTemplate(template, (float) margin, (float) margin);
         g2d.dispose();
         */
      PDFGraphics2D g2 = page.getGraphics2D();
      provider.draw(i, g2, factor);

      PdfReader r = new PdfReader(new ByteArrayInputStream(graphicPage.getPDFBytes()));
      PdfDocument pdfDoc = new PdfDocument(r);
      PdfFormXObject c = pdfDoc.getFirstPage().copyAsFormXObject(pdfDoc);
      Image im = new Image(c);
      document.add(im);
    }
    document.close();
  }

  /**
   * Renders the project into a PNG file.
   *
   * @param provider
   * @param file
   */
  public static void exportPng(IDrawingProvider provider, File file) {
    try {
      int pageCount = provider.getPageCount();
      Dimension d = provider.getSize();
      double factor = 1f * PNG_RESOLUTION / SCREEN_RESOLUTION;
      for (int i = 0; i < pageCount; i++) {
        BufferedImage image = new BufferedImage(
            (int) (d.getWidth() * factor),
            (int) (d.getHeight() * factor),
            BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = (Graphics2D) image.getGraphics();
        provider.draw(0, g2d, factor);
        // Move down
        g2d.translate(0, (int) (d.getHeight() * factor));
        // TODO: what is the point of translate() when dispose() is immediately after?
        g2d.dispose();
        File outFile =
            i == 0
            ? file
            : new File(file.getAbsolutePath().replaceAll("\\.png", "_" + (i + 1) + ".png"));
        ImageIO.write(image, "PNG", outFile);
      }
    } catch (Exception e) {
      LOG.error("Error exporting PNG", e);
    }
  }
}
