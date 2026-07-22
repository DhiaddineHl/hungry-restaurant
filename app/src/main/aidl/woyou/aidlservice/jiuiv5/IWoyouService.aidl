package woyou.aidlservice.jiuiv5;

import woyou.aidlservice.jiuiv5.ICallback;
import android.graphics.Bitmap;

/**
 * SUNMI Inner Printer service interface (subset of the official AIDL SDK).
 * Bind with action "woyou.aidlservice.jiuiv5.IWoyouService" and package
 * "woyou.aidlservice.jiuiv5". Present on Sunmi devices with a built-in printer.
 */
interface IWoyouService {

    /** Reset the printer to its initial state (clears style, buffers). */
    void printerInit(in ICallback callback);

    /** Run a printer self-check page. */
    void printerSelfChecking(in ICallback callback);

    /** Printer board serial number. */
    String getPrinterSerialNo();

    /** Printer firmware version. */
    String getPrinterVersion();

    /** 0 = left, 1 = center, 2 = right. */
    void setAlignment(int alignment, in ICallback callback);

    /** Set global font name/typeface. */
    void setFontName(String typeface, in ICallback callback);

    /** Set global font size (points). */
    void setFontSize(float fontSize, in ICallback callback);

    /** Print text using the current style. Append "\n" to feed a line. */
    void printText(String text, in ICallback callback);

    /** Print text overriding typeface and size for this call only. */
    void printTextWithFont(String text, String typeface, float fontsize, in ICallback callback);

    /** Print text verbatim (vector fonts, no auto width scaling). */
    void printOriginalText(String text, in ICallback callback);

    /**
     * Print a row of columns.
     * @param colsTextArr  text for each column
     * @param colsWidthArr relative weight of each column
     * @param colsAlign    0 left / 1 center / 2 right per column
     */
    void printColumnsText(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);

    /** Like printColumnsText but width is measured in characters. */
    void printColumnsString(in String[] colsTextArr, in int[] colsWidthArr, in int[] colsAlign, in ICallback callback);

    /** Print a bitmap (e.g. a logo). Max width 384px on 58mm heads. */
    void printBitmap(in Bitmap bitmap, in ICallback callback);

    /** Print a 1D barcode. */
    void printBarCode(String data, int symbology, int height, int width, int textposition, in ICallback callback);

    /** Print a QR code. modulesize 1..16, errorlevel 0..3. */
    void printQRCode(String data, int modulesize, int errorlevel, in ICallback callback);

    /** Feed n blank lines. */
    void lineWrap(int n, in ICallback callback);

    /** Cut the paper (only on devices with a cutter). */
    void cutPaper(in ICallback callback);

    /** Open a transaction buffer; commands are queued until commit. */
    void enterPrinterBuffer(boolean clean);

    /** Commit the buffered commands. */
    void commitPrinterBuffer();

    /** Exit the transaction buffer, optionally committing. */
    void exitPrinterBuffer(boolean commit);

    /** Send raw ESC/POS bytes. */
    void sendRAWData(in byte[] data, in ICallback callback);

    /** Length of paper printed since power-on (mm). */
    int getPrintedLength();
}
