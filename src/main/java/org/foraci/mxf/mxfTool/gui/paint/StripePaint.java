package org.foraci.mxf.mxfTool.gui.paint;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.ColorModel;

/**
 * A stripey paint
 */
public class StripePaint implements Paint {
    private final Color stripeColor;
    private final int stripeWidth;

    public StripePaint(Color stripeColor, int stripeWidth) {
        this.stripeColor = stripeColor;
        this.stripeWidth = stripeWidth;
    }

    public PaintContext createContext(ColorModel cm,
        Rectangle deviceBounds,
        Rectangle2D userBounds,
        AffineTransform xform,
        RenderingHints hints) {
        return new StripePaintContext(stripeColor, stripeWidth);
    }

    public int getTransparency() {
      return OPAQUE;
    }
}
