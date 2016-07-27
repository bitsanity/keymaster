package a.keymaster.cryptils;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

public class QR {
    public static Bitmap encode( String str, int width, int height ) throws Exception {

        QRCodeWriter writer = new QRCodeWriter();
        BitMatrix matrix = writer.encode( str , BarcodeFormat.QR_CODE, width, height );
        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        for (int xx = 0; xx < width; xx++) {
            for (int yy = 0; yy < height; yy++) {
                bmp.setPixel( xx, yy, matrix.get(xx, yy) ? Color.BLACK : Color.WHITE );
            }
        }

        return bmp;
    }
}
