/*
** Copyright 2005 Huxtable.com. All rights reserved.
*/

package com.alkacon.simapi.filter.buffered;

import java.awt.image.BufferedImage;
import java.awt.image.Kernel;

/**
 * A filter which applies Gaussian blur to an image. This is a subclass of ConvolveFilter
 * which simply creates a kernel with a Gaussian distribution for blurring.
 * @author Jerry Huxtable
 */
public class GaussianFilter extends ConvolveFilter {

    static final long serialVersionUID = 5377089073023183684L;

    protected float radius;
    protected Kernel[] kernels;
    
    /**
     * Construct a Gaussian filter
     */
    public GaussianFilter() {
        this(2);
    }

    /**
     * Construct a Gaussian filter
     * @param radius blur radius in pixels
     */
    public GaussianFilter(float radius) {
        setRadius(radius);
    }

    /**
     * Set the radius of the kernel, and hence the amount of blur. The bigger the radius, the longer this filter will take.
     * @param radius the radius of the blur in pixels.
     */
    public void setRadius(float radius) {
        this.radius = radius;
        kernels = separatedKernels(radius);
    }
    
    /**
     * Get the radius of the kernel.
     * @return the radius
     */
    public float getRadius() {
        return radius;
    }

    public BufferedImage filter( BufferedImage src, BufferedImage dst ) {
        int width = src.getWidth();
        int height = src.getHeight();

        if ( dst == null )
            dst = createCompatibleDestImage( src, null );

        int[] inPixels = new int[width*height];
        int[] outPixels = new int[width*height];
        src.getRGB( 0, 0, width, height, inPixels, 0, width );

        convolveH(kernels[0], inPixels, outPixels, width, height, alpha, CLAMP_EDGES);
        convolveV(kernels[1], outPixels, inPixels, width, height, alpha, CLAMP_EDGES);

        dst.setRGB( 0, 0, width, height, inPixels, 0, width );
        return dst;
    }

    /**
     * Make a Gaussian blur kernel. Don't use this: make separated kernels instead.
     */
    public static Kernel makeKernel(float radius) {
        int r = (int)Math.ceil(radius);
        int rows = r*2+1;
        int cols = rows;
        float[] matrix = new float[rows*cols];
        float sigma = radius/3;
        float sigma22 = 2*sigma*sigma;
        float sigmaPi2 = 2*ImageMath.PI*sigma;
        float radius2 = radius*radius;
        float total = 0;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            for (int col = -r; col <= r; col++) {
                float distance = row*row+col*col;
                if (distance > radius2)
                    matrix[index] = 0;
                else
                    matrix[index] = (float)Math.exp(-(distance)/sigma22) / sigmaPi2;
                total += matrix[index];
                index++;
            }
        }
        for (int i = 0; i < rows*cols; i++)
            matrix[i] /= total;
        Kernel kernel = new Kernel(cols, rows, matrix);
        return kernel;
    }
    
    public static Kernel[] separatedKernels(float radius) {
        int r = (int)Math.ceil(radius);
        int rows = r*2+1;
        float[] matrix = new float[rows];
        float sigma = radius/3;
        float sigma22 = 2*sigma*sigma;
        float sigmaPi2 = 2*ImageMath.PI*sigma;
        float sqrtSigmaPi2 = (float)Math.sqrt(sigmaPi2);
        float radius2 = radius*radius;
        float total = 0;
        int index = 0;
        for (int row = -r; row <= r; row++) {
            float distance = row*row;
            if (distance > radius2)
                matrix[index] = 0;
            else
                matrix[index] = (float)Math.exp(-(distance)/sigma22) / sqrtSigmaPi2;
            total += matrix[index];
            index++;
        }
        for (int i = 0; i < rows; i++)
            matrix[i] /= total;

        return new Kernel[] {
            new Kernel(rows, 1, matrix),
            new Kernel(1, rows, matrix),
        };
    }

    public String toString() {
        return "Blur/Gaussian Blur...";
    }
}
