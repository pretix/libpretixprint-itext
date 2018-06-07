package eu.pretix.libpretixprint.helpers;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.ExceptionConverter;
import com.itextpdf.text.Image;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.codec.CCITTG4Encoder;

import java.util.Map;

public class BarcodeQR {
    private BitMatrix bitMatrix;

    public BarcodeQR(String content, int size, Map<EncodeHintType, Object> hints) {
        try {
            QRCodeWriter qc = new QRCodeWriter();
            bitMatrix = qc.encode(content, BarcodeFormat.QR_CODE, size, size, hints);
        } catch (WriterException ex) {
            throw new ExceptionConverter(ex);
        }
    }

    private byte[] bitMatrixToArray() {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int steps = (width + 7) / 8;
        byte[] b = new byte[steps * height];
        for (int y = 0; y < height; ++y) {
            for (int x = 0; x < width; ++x) {
                if (!bitMatrix.get(x, y)) {
                    int offset = steps * y + x / 8;
                    b[offset] |= (byte) (0x80 >> (x % 8));
                }
            }
        }
        return b;
    }

    public Image getImage() throws DocumentException {
        byte g4[] = CCITTG4Encoder.compress(bitMatrixToArray(), bitMatrix.getWidth(), bitMatrix.getHeight());
        Image img = Image.getInstance(
                bitMatrix.getWidth(),
                bitMatrix.getHeight(),
                false,
                Image.CCITTG4,
                Image.CCITT_BLACKIS1,
                g4,
                null
        );
        Image mask = Image.getInstance(img);
        mask.makeMask();
        img.setImageMask(mask);
        return img;
    }

    public Rectangle getBarcodeSize() {
        return new Rectangle(0, 0, bitMatrix.getWidth(), bitMatrix.getHeight());
    }
}