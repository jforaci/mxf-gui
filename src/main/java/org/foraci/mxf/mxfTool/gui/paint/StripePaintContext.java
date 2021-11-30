package org.foraci.mxf.mxfTool.gui.paint;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

/**
 * A stripey paint context
 */
public class StripePaintContext implements PaintContext {
    private static final int[] white = createColor(Color.WHITE);
    private final int[] stripeColor;
    private final int stripeWidth;

    public StripePaintContext(Color color, int stripeWidth) {
        this.stripeWidth = stripeWidth;
        stripeColor = createColor(color);
    }

    public static int[] createColor(Color c) {
        int[] cc = new int[] { c.getRed(), c.getGreen(), c.getBlue(), 0xFF };
        return cc;
    }

    public void dispose() {}

    public ColorModel getColorModel() {
        return ColorModel.getRGBdefault();
    }

    // getRaster makes use of the enclosing CheckPaint instance
    public Raster getRaster(int xOffset, int yOffset, int w, int h) {
        WritableRaster raster =
                getColorModel().createCompatibleWritableRaster(w, h);

        // Row major traversal, x coordinate changes fastest.
        for (int j = 0; j < h; ++j) {
            for (int i = 0; i < w; ++i) {
                int x = i + xOffset;
//                int y = j + yOffset;
                int s = (x / stripeWidth) % 2;
                if (0 == s) {
                    raster.setPixel(i, j, stripeColor);
                } else {
                    raster.setPixel(i, j, white);
                }
            }
        }
        return raster;
    }
}
