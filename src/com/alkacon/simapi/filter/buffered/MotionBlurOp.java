/*
** Copyright 2005 Huxtable.com. All rights reserved.
*/

package com.alkacon.simapi.filter.buffered;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorModel;

public class MotionBlurOp implements BufferedImageOp {
    private float distance;
    private float angle;
    private float rotation;
    private float zoom;

    public MotionBlurOp() {
    }
    
    public MotionBlurOp( float distance, float angle, float rotation, float zoom ) {
        this.distance = distance;
        this.angle = angle;
        this.rotation = rotation;
        this.zoom = zoom;
    }
    
    public void setAngle( float angle ) {
        this.angle = angle;
    }

    public float getAngle() {
        return angle;
    }
    
    public void setDistance( float distance ) {
        this.distance = distance;
    }

    public float getDistance() {
        return distance;
    }
    
    public void setRotation( float rotation ) {
        this.rotation = rotation;
    }

    public float getRotation() {
        return rotation;
    }
    
    public void setZoom( float zoom ) {
        this.zoom = zoom;
    }

    public float getZoom() {
        return zoom;
    }
    
    public BufferedImage createCompatibleDestImage(BufferedImage src, ColorModel dstCM) {
        if ( dstCM == null )
            dstCM = src.getColorModel();
        return new BufferedImage(dstCM, dstCM.createCompatibleWritableRaster(src.getWidth(), src.getHeight()), dstCM.isAlphaPremultiplied(), null);
    }
    
    private int log2( int n ) {
        int m = 1;
        int log2n = 0;

        while (m < n) {
            m *= 2;
            log2n++;
        }
        return log2n;
    }

    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        if ( dst == null )
            dst = createCompatibleDestImage( src, null );
        BufferedImage tsrc = src;
        float cx = (float)src.getWidth() / 2;
        float cy = (float)src.getHeight() / 2;
        float imageRadius = (float)Math.sqrt( cx*cx + cy*cy );
        float translateX = (float)(distance * Math.cos( angle ));
        float translateY = (float)(distance * -Math.sin( angle ));
        float scale = zoom;
        float rotate = rotation;
        float maxDistance = distance + Math.abs(rotation*imageRadius) + zoom*imageRadius;
        int steps = log2((int)maxDistance);

        translateX /= maxDistance;
        translateY /= maxDistance;
        scale /= maxDistance;
        rotate /= maxDistance;
        
        if ( steps == 0 ) {
            Graphics2D g = dst.createGraphics();
            g.drawRenderedImage( src, null );
            g.dispose();
            return dst;
        }
        
        BufferedImage tmp = createCompatibleDestImage( src, null );
        for ( int i = 0; i < steps; i++ ) {
            Graphics2D g = tmp.createGraphics();
            g.drawImage( tsrc, null, null );
            g.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
            g.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR );
            g.setComposite( AlphaComposite.getInstance( AlphaComposite.SRC_OVER, 0.5f ) );

            g.translate( cx+translateX, cy+translateY );
            g.scale( 1.0001+scale, 1.0001+scale );  // The .0001 works round a bug on Windows where drawImage throws an ArrayIndexOutofBoundException
            if ( rotation != 0 )
                g.rotate( rotate );
            g.translate( -cx, -cy );

            g.drawImage( dst, null, null );
            g.dispose();
            BufferedImage ti = dst;
            dst = tmp;
            tmp = ti;
            tsrc = dst;

            translateX *= 2;
            translateY *= 2;
            scale *= 2;
            rotate *= 2;
        }
        return dst;
    }
    
    public Rectangle2D getBounds2D( BufferedImage src ) {
        return new Rectangle(0, 0, src.getWidth(), src.getHeight());
    }
    
    public Point2D getPoint2D( Point2D srcPt, Point2D dstPt ) {
        if ( dstPt == null )
            dstPt = new Point2D.Double();
        dstPt.setLocation( srcPt.getX(), srcPt.getY() );
        return dstPt;
    }

    public RenderingHints getRenderingHints() {
        return null;
    }

    public String toString() {
        return "Blur/Motion Blur...";
    }
}
