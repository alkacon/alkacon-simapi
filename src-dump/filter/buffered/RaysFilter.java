/*
** Copyright 2005 Huxtable.com. All rights reserved.
*/

package com.alkacon.simapi.filter.buffered;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;

public class RaysFilter extends MotionBlurFilter {

    private float opacity = 1.0f;
    private boolean shadowOnly = false;

    public RaysFilter() {
    }
    
    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        // Make a black mask from the image's alpha channel 
        float[][] extractAlpha = {
            { 0, 0, 0, 0, 255 },
            { 0, 0, 0, 0, 255 },
            { 0, 0, 0, 0, 255 },
            { 0, 0, 0, opacity, 0 }
        };
        BufferedImage shadow = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        new BandCombineOp( extractAlpha, null ).filter( src.getRaster(), shadow.getRaster() );
        shadow = super.filter( shadow, null );

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

        Graphics2D g = dst.createGraphics();
        g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, opacity ) );
        g.drawRenderedImage( shadow, null );
        if ( !shadowOnly ) {
            g.setComposite( AlphaComposite.SrcOver );
            g.drawRenderedImage( src, null );
        }
        g.dispose();

        return dst;
    }
    
    public String toString() {
        return "Stylize/Rays...";
    }
}

