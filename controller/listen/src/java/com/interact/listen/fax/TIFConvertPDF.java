package com.interact.listen.fax;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import org.icepdf.core.exceptions.PDFException;
import org.icepdf.core.exceptions.PDFSecurityException;
import org.icepdf.core.pobjects.Document;
import org.icepdf.core.pobjects.PDimension;
import org.icepdf.core.pobjects.Page;
import org.icepdf.core.util.GraphicsRenderingHints;

public class TIFConvertPDF extends TIFConvert {
    /**
     * A Point size is an absolute measure where 1 point = 1/72 inch</br>
     * </br>
     * When the PostScript page description language was being designed by Adobe Systems</br>
     * the PostScript point was defined as being exactly 72 points to the inch.</br></br>
     * 1/72 inch = 0.013888888888 inches = 0.352777777777 millimeters</br>
     * </br>
     *
     * This system was notably promoted by John Warnock and Charles Geschke,</br>
     * the inventors of Adobe PostScript, and therefore it is sometimes also</br>
     * called PostScript point.</br></br>
     *
     * @see <a href="http://en.wikipedia.org/wiki/Point_(typography)#Traditional_American_point_system">Traditional American point system</a>
     * </br>
     * @see <a href="http://en.wikipedia.org/wiki/Point_(typography)#Traditional_American_point_system">Current DTP point system</a>
     */
    private static final int PDF_DPI = 72;

    /**
     * Convert PDF to a TIF and save to file
     * @param pdf
     * @param tif
     * @return
     */
    public static void convert(String pdf, String tif)
        throws PDFException, PDFSecurityException, IOException {
        convert(pdf, tif, DEFAULT_DPI, DEFAULT_COLOR, DEFAULT_COMPRESSION, DEFAULT_COMPRESSION_QUALITY);
    }

    /**
     * Convert PDF to a TIF and save to file
     * @param pdf
     * @param tif
     * @param dpi
     * @param color
     * @param compression
     * @param quality
     * @return
     */
    public static void convert(String pdf, String tif, int dpi, int color, int compression, float quality)
        throws PDFException, PDFSecurityException, IOException {
        convert(getImageFromPDF(pdf, dpi, color, compression), tif, dpi, compression, quality);
    }

    /**
     * Convert byte array PDF to a TIF and save to file
     * @param pdf
     * @param tif
     * @param dpi
     * @param color
     * @param compression
     * @param quality
     * @return
     */
    public static void convert(byte[] pdf, String tif, int dpi, int color, int compression, float quality)
        throws PDFException, PDFSecurityException, IOException {
        convert(getImageFromPDF(pdf, dpi, color, compression), tif, dpi, compression, quality);
    }

    /**
     * Convert PDF to a TIF byte array
     * @param pdf
     * @param dpi
     * @param color
     * @param compression
     * @param quality
     * @return
     */
    public static byte[] convert(String pdf, int dpi, int color, int compression, float quality)
        throws PDFException, PDFSecurityException, IOException {
        return convert(getImageFromPDF(pdf, dpi, color, compression), dpi, compression, quality);
    }

    /**
     * Convert byte array PDF to a TIF and return as byte array
     * @param pdf
     * @param dpi
     * @param color
     * @param compression
     * @param quality
     * @return
     */
    public static byte[] convert(byte[] pdf, int dpi, int color, int compression, float quality)
        throws PDFException, PDFSecurityException, IOException {
        return convert(getImageFromPDF(pdf, dpi, color, compression), dpi, compression, quality);
    }

    /**
     * Render a PDF document from byte array into a buffered image
     * @param pdf
     * @param dpi
     * @param color
     * @param compression
     * @return
     */
    private static BufferedImage[] getImageFromPDF(byte[] pdf, int dpi, int color, int compression)
        throws PDFException, PDFSecurityException, IOException {
        Document pdfFile = new Document();
        pdfFile.setByteArray(pdf, 0, pdf.length, System.getProperty("java.io.tmpdir"));
        return getImageFromPDF(pdfFile, dpi, color, compression);
    }

    /**
     * Render a PDF document into a buffered image
     * @param pdf
     * @param dpi
     * @param color
     * @param compression
     * @return
     * @throws IOException
     * @throws PDFSecurityException
     * @throws PDFException
     */
    private static BufferedImage[] getImageFromPDF(String pdf, int dpi, int color, int compression)
        throws PDFException, PDFSecurityException, IOException {
        Document pdfFile = new Document();
        pdfFile.setFile(pdf);
        return getImageFromPDF(pdfFile, dpi, color, compression);
    }

    /**
     * Render a PDF document into a buffered image
     * @param pdf
     * @param dpi
     * @param color
     * @param compression
     * @return
     */
    private static BufferedImage[] getImageFromPDF(Document pdf, int dpi, int color, int compression) {
        float scale;
        float rotation = 0f;
        int width;
        int height;
        int numPgs = pdf.getNumberOfPages();

        BufferedImage[] image = new BufferedImage[numPgs];
        PDimension pd;
        Graphics2D g;

        for (int i = 0; i < numPgs; i++) {
            scale  = (float)dpi / PDF_DPI;
            pd = pdf.getPageDimension(i, rotation, scale);
            width  = (int)pd.getWidth();
            height = (int)pd.getHeight();
            image[i] = getBufferedImage(color, compression, width, height);
            g = image[i].createGraphics();
            pdf.paintPage(i, g, GraphicsRenderingHints.PRINT, Page.BOUNDARY_CROPBOX, rotation, scale);
            g.dispose();
        }

        pdf.dispose();

        return image;
    }
}
