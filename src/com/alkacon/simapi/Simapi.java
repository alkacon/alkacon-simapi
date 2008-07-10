/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/Simapi.java,v $
 * Date   : $Date: 2008/07/10 11:29:05 $
 * Version: $Revision: 1.15 $
 *
 * Copyright (c) 2007 Alkacon Software GmbH (http://www.alkacon.com)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * For further information about Alkacon Software GmbH, please see the
 * company website: http://www.alkacon.com
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.alkacon.simapi;

import com.alkacon.simapi.filter.WholeImageFilter;
import com.alkacon.simapi.filter.buffered.BoxBlurFilter;
import com.alkacon.simapi.filter.buffered.GaussianFilter;
import com.alkacon.simapi.util.GifImageWriterSpi;
import com.alkacon.simapi.util.Quantize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.PixelGrabber;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

/**
 * <b>SIM</b>ple <b>IM</b>age <b>API</b> (SIMAPI) that provides convenient access to commonly used imaging operations.<p>
 * 
 * @author Alexander Kandzior
 */
public class Simapi {

    /** Constant to identify a transparent background fill color. */
    public static final Color COLOR_TRANSPARENT = new Color(0, 0, 0, 255);

    /** Position indicator: Center (default). */
    public static final int POS_CENTER = 0;

    /** Position indicator: Down left. */
    public static final int POS_DOWN_LEFT = 1;

    /** Position indicator: Down right. */
    public static final int POS_DOWN_RIGHT = 2;

    /** Position indicator: Straight down. */
    public static final int POS_STRAIGHT_DOWN = 3;

    /** Position indicator: Straight left. */
    public static final int POS_STRAIGHT_LEFT = 4;

    /** Position indicator: Straight right. */
    public static final int POS_STRAIGHT_RIGHT = 5;

    /** Position indicator: Straight up. */
    public static final int POS_STRAIGHT_UP = 6;

    /** Position indicator: Up left. */
    public static final int POS_UP_LEFT = 7;

    /** Position indicator: Up right. */
    public static final int POS_UP_RIGHT = 8;

    /** Indicates to use the <code>MEDIUM</code> render settings. */
    public static final int RENDER_MEDIUM = 1;

    /** Indicates to use the <code>QUALITY</code> render settings (default). */
    public static final int RENDER_QUALITY = 0;

    /** Indicates to use the <code>SPEED</code> render settings. */
    public static final int RENDER_SPEED = 2;

    /** Constant to identify the <code>BMP</code> image type. */
    public static final String TYPE_BMP = "BMP";

    /** Constant to identify the <code>GIF</code> image type. */
    public static final String TYPE_GIF = "GIF";

    /** Constant to identify the <code>JPEG</code> image type. */
    public static final String TYPE_JPEG = "JPEG";

    /** Constant to identify the <code>PNG</code> image type. */
    public static final String TYPE_PNG = "PNG";

    /** Constant to identify the <code>PNM</code> image type. */
    public static final String TYPE_PNM = "PNM";

    /** Constant to identify the <code>TIFF</code> image type. */
    public static final String TYPE_TIFF = "TIFF";

    /** Rendering settings for the image generation / scaling / saving. */
    private RenderSettings m_renderSettings;

    /**
     * Creates a new simapi instance using the default render settings ({@link #RENDER_QUALITY}).<p>     *
     */
    public Simapi() {

        this(new RenderSettings(RENDER_QUALITY));
    }

    /**
     * Creates a new simapi instance with the specified render settings.<p>
     * 
     * @param renderSettings the render settings to use
     */
    public Simapi(RenderSettings renderSettings) {

        m_renderSettings = renderSettings;
    }

    /**
     * Register the GIF encoder.<p>
     */
    static {
        // register the Alkacon GIF encoder with the Java ImageIO registry
        ImageWriterSpi alkaconGifSpi = new GifImageWriterSpi();
        IIORegistry.getDefaultInstance().registerServiceProvider(alkaconGifSpi);
        Iterator i = null;
        try {
            i = IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, true);
        } catch (Throwable t) {
            t.printStackTrace(System.err);
        }
        if (i != null) {
            while (i.hasNext()) {
                ImageWriterSpi spi = (ImageWriterSpi)i.next();
                if (spi.getClass() != GifImageWriterSpi.class) {
                    String[] formats = spi.getFormatNames();
                    for (int j = 0; j < formats.length; j++) {
                        String format = formats[j];
                        if ("gif".equals(format.toLowerCase())) {
                            // the SPI can write GIFs, change order so that Alkacons SPI comes first
                            IIORegistry.getDefaultInstance().setOrdering(ImageWriterSpi.class, alkaconGifSpi, spi);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the image type from the given file name based on the file suffix (extension)
     * and the available image writers.<p>
     * 
     * For example, for the file name "alkacon.gif" the type is GIF, for 
     * "alkacon.jpeg" is is "JPEG" etc.<p> 
     * 
     * In case the input filename has no suffix, or there is no known image writer for the format defined
     * by the suffix, <code>null</code> is returned.<p>
     * 
     * Any non-null result can be used if an image type input value is required.<p>
     * 
     * @param filename the file name to get the type for
     *  
     * @return the image type from the given file name based on the suffix and the available image writers, 
     *      or null if no image writer is available for the format 
     */
    public static String getImageType(String filename) {

        if (filename == null) {
            return null;
        }

        int pos = filename.lastIndexOf('.');
        String type;
        if (pos < 0) {
            type = filename;
        } else {
            if (pos < filename.length()) {
                pos++;
            }
            type = filename.substring(pos);
        }
        type = type.trim().toUpperCase();

        if (type.equals(Simapi.TYPE_JPEG) || type.equals("JPG")) {
            type = Simapi.TYPE_JPEG;
        } else if (type.equals(Simapi.TYPE_GIF)) {
            type = Simapi.TYPE_GIF;
        } else if (type.equals(Simapi.TYPE_PNG)) {
            type = Simapi.TYPE_PNG;
        } else if (type.equals(Simapi.TYPE_TIFF) || type.equals("TIF")) {
            type = Simapi.TYPE_TIFF;
        } else if (type.equals(Simapi.TYPE_BMP)) {
            type = Simapi.TYPE_BMP;
        } else if (type.equals(Simapi.TYPE_PNM) || type.equals("PBM") || type.equals("PGM") || type.equals("PPM")) {
            type = Simapi.TYPE_PNM;
        }

        // check if a writer for the image name can be found
        Iterator iter = ImageIO.getImageWritersByFormatName(type);
        if (iter.hasNext()) {
            // type can be resolved
            return type;
        }

        // type is unknown
        return null;
    }

    /**
     * Loads an image from a byte array
     * 
     * @param source the byte array to read the image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded 
     */
    public static BufferedImage read(byte[] source) throws IOException {

        return read(new ByteArrayInputStream(source));
    }

    /**
     * Loads an image from a local file.<p>
     * 
     * @param source the file to read the input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded 
     */
    public static BufferedImage read(File source) throws IOException {

        return ensureImageIsSystemType(ImageIO.read(source), true);
    }

    /**
     * Loads an image from an InputStream.<p>
     * 
     * @param source the input stream to read the input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded
     */
    public static BufferedImage read(InputStream source) throws IOException {

        return ensureImageIsSystemType(ImageIO.read(source), true);
    }

    /**
     * Loads an image from a local file whose path is supplied as a String
     * 
     * @param source the path to the local file to read the input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded 
     */
    public static BufferedImage read(String source) throws IOException {

        return read(new File(source));
    }

    /**
     * Loads an image from a URL.<p>
     * 
     * @param source the URL to read the input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded      
     */
    public static BufferedImage read(URL source) throws IOException {

        return ensureImageIsSystemType(ImageIO.read(source), true);
    }

    /**
     * Returns an image that is ensured the be of either {@link BufferedImage#TYPE_INT_RGB} or 
     * {@link BufferedImage#TYPE_INT_ARGB}.<p> 
     * 
     * Ensuring the image is of one of the possible return types is done before applying an image filter transformation
     * since if the image is of a different (not native) type, the transformation can take very long
     * and consume a lot of resources.<p>
     * 
     * @param image the original image
     * @param allowTransparent if <code>true</code>, transparent (alpha layer) pixels is allowed
     * @return an image that is ensured the be of a system type
     */
    protected static BufferedImage ensureImageIsSystemType(BufferedImage image, boolean allowTransparent) {

        switch (image.getType()) {
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_RGB:
                // image already uses a system compatible color model, no need for transformation
                return image;
            default:
                // image must be transformed to system color
        }

        BufferedImage result;
        if (allowTransparent && (image.getColorModel().getTransparency() != Transparency.OPAQUE)) {
            // use RGB color model with alpha
            result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        } else {
            // use RGB color model without alpha (no transparency)
            result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_RGB);
        }

        // copy the pixels from the source image ti the result image
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);
        g.dispose();

        // flush original - doesn't actually do anything but looks right to me anyway 
        image.flush();
        image = null;

        return result;
    }

    /**
     * Applies the given filter to the image.<p>
     * 
     * @param image the image to apply the filter to
     * @param filter the filter to apply
     * 
     * @return the image with the filter applied
     */
    public BufferedImage applyFilter(BufferedImage image, ImageFilter filter) {

        // apply filter using default AWT toolkit
        Image img = Toolkit.getDefaultToolkit().createImage(new FilteredImageSource(image.getSource(), filter));

        // use a pixel grabber to retrieve the image's color model;
        // grabbing a single pixel is usually sufficient
        PixelGrabber pg = new PixelGrabber(img, 0, 0, 1, 1, false);
        try {
            pg.grabPixels();
        } catch (InterruptedException e) {
            // ignore
        }

        // recast the AWT image into a BufferedImage (using alpha RGB)
        BufferedImage result = new BufferedImage(
            img.getWidth(null),
            img.getHeight(null),
            pg.getColorModel().hasAlpha() ? BufferedImage.TYPE_INT_ARGB : BufferedImage.TYPE_INT_RGB);

        // draw the generated image to the result canvas and return
        Graphics2D g = result.createGraphics();
        g.drawImage(img, 0, 0, null);
        g.dispose();
        return result;
    }

    /**
     * Calculates the image size for an image after all filters returned by {@link RenderSettings#getImageFilters()}  
     * have been applied.<p>
     *  
     * @param width the image width
     * @param height the image height
     *  
     * @return the image size after all filters have been applied 
     */
    public Rectangle applyFilterDimensions(int width, int height) {

        Rectangle base = new Rectangle(width, height);
        Iterator i = m_renderSettings.getImageFilters().iterator();
        while (i.hasNext()) {
            ImageFilter filter = (ImageFilter)i.next();
            if (filter instanceof WholeImageFilter) {
                WholeImageFilter wholeFilter = (WholeImageFilter)filter;
                base = wholeFilter.getTransformedSpace(base);
            }
        }
        return base;
    }

    /**
     * Applies all filters returned by {@link RenderSettings#getImageFilters()} to the given image.<p>  
     * 
     * @param image the image to apply the filters to
     * 
     * @return the image with the filters applied
     */
    public BufferedImage applyFilters(BufferedImage image) {

        // make sure the image is of a compatible system type
        image = ensureImageIsSystemType(image, true);

        threadSetNice();

        Iterator i = m_renderSettings.getImageFilters().iterator();
        while (i.hasNext()) {
            ImageFilter filter = (ImageFilter)i.next();
            image = applyFilter(image, filter);
        }

        threadSetNormal();

        return image;
    }

    /**
     * Crops an image according to the width and height specified.<p>
     * 
     * Use the constants <code>{@link Simapi#POS_CENTER}</code> etc. to indicate the crop position.<p>
     * 
     * @param image the image to crop
     * @param width the width of the target image
     * @param height the height of the target image
     * @param cropPosition the position to crop the image at
     * 
     * @return the transformed image
     */
    public BufferedImage crop(BufferedImage image, int width, int height, int cropPosition) {

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if ((imageWidth == width) && (imageHeight == height)) {
            // no resize required
            return image;
        }

        int x;
        int y;
        switch (cropPosition) {
            case Simapi.POS_DOWN_LEFT:
                x = 0;
                y = imageHeight - height;
                break;
            case Simapi.POS_DOWN_RIGHT:
                x = imageWidth - width;
                y = imageHeight - height;
                break;
            case Simapi.POS_STRAIGHT_DOWN:
                x = (imageWidth - width) / 2;
                y = imageHeight - height;
                break;
            case Simapi.POS_STRAIGHT_LEFT:
                x = 0;
                y = (imageHeight - height) / 2;
                break;
            case Simapi.POS_STRAIGHT_RIGHT:
                x = imageWidth - width;
                y = (imageHeight - height) / 2;
                break;
            case Simapi.POS_STRAIGHT_UP:
                x = (imageWidth - width) / 2;
                y = 0;
                break;
            case Simapi.POS_UP_LEFT:
                x = 0;
                y = 0;
                break;
            case Simapi.POS_UP_RIGHT:
                x = imageWidth - width;
                y = 0;
                break;
            default:
                // crop center
                x = (imageWidth - width) / 2;
                y = (imageHeight - height) / 2;
        }

        // return the result
        return image.getSubimage(x, y, width, height);
    }

    /**
     * Crops a part of the given image from the specified <code>x,y</code> point to the given <code>width,height</code>. 
     * 
     * Should the target image rectangle be outside of the source image, the source image is enlarged and 
     * the transparent color is used for the additional background pixels.
     * If the image does not support transparent pixels, the transparent replacement color (whit by default)
     * will be used.<p> 
     * 
     * @param image the image to crop
     * @param x the x position where the crop starts
     * @param y the y position where the crop starts
     * @param width the width of the cropped target image 
     * @param height the height of the cropped target image 
     * 
     * @return a cropped part of the given image from the specified 
     *      <code>x,y</code> point to the given <code>width,height</code>
     *      
     * @see #crop(BufferedImage, int, int, int, int, Color)
     */
    public BufferedImage crop(BufferedImage image, int x, int y, int width, int height) {

        return crop(image, x, y, width, height, COLOR_TRANSPARENT);
    }

    /**
     * Crops a part of the given image from the specified <code>x,y</code> point to the given <code>width,height</code>. 
     * 
     * Should the target image rectangle be outside of the source image, the source image is enlarged and 
     * the given background replace color is used for the additional pixels.<p> 
     * 
     * @param image the image to crop
     * @param x the x position where the crop starts
     * @param y the y position where the crop starts
     * @param width the width of the cropped target image 
     * @param height the height of the cropped target image
     * @param backgroundColor the color to use if the background must be enlarged 
     * 
     * @return a cropped part of the given image from the specified 
     *      <code>x,y</code> point to the given <code>width,height</code>
     *      
     * @see #crop(BufferedImage, int, int, int, int)
     */
    public BufferedImage crop(BufferedImage image, int x, int y, int width, int height, Color backgroundColor) {

        if ((x < 0) || (y < 0) || (x + width >= image.getWidth()) || (y + height >= image.getHeight())) {
            // crop area lies partly outside of image - blow up result image to fit
            int xpos = x;
            int ypos = y;
            int imageWidth = image.getWidth();
            int imageHeight = image.getHeight();
            if (x < 0) {
                xpos = Math.abs(x);
                x = 0;
                imageWidth += xpos;
            }
            if (y < 0) {
                ypos = Math.abs(y);
                y = 0;
                imageHeight += ypos;
            }
            if (imageWidth < width) {
                imageWidth += width;
            }
            if (imageHeight < height) {
                imageHeight += height;
            }
            // draw input image to enlarged canvas
            BufferedImage result = createImage(image.getColorModel(), imageWidth, imageHeight);
            Graphics2D g = result.createGraphics();
            // check the background color
            ColorModel cm = result.getColorModel();
            if (!cm.hasAlpha() && (backgroundColor == COLOR_TRANSPARENT)) {
                // alpha not supported by target color model
                backgroundColor = m_renderSettings.getTransparentReplaceColor();
            }
            if (backgroundColor != COLOR_TRANSPARENT) {
                // don't fill if background is transparent
                g.setPaintMode();
                g.setColor(backgroundColor);
                g.fillRect(0, 0, result.getWidth(), result.getHeight());
            }
            g.drawImage(image, xpos, ypos, null);
            g.dispose();
            // exchange image with result
            image = result;
        }

        // return the result image
        return image.getSubimage(x, y, width, height);
    }

    /**
     * Crops a part of the given image from the specified <code>x,y</code> point to the given <code>width,height</code>,
     * and then resizes this cropped image to the dimensions specified in <code>targetWidth,targetHeight</code>. 
     * 
     * Should the target image rectangle be outside of the source image, the source image is enlarged and 
     * the transparent color is used for the additional background pixels.
     * If the image does not support transparent pixels, the transparent replacement color (whit by default)
     * will be used.<p> 
     * 
     * The aspect ratio of the target image is not kept.<p>
     * 
     * @param image the image to crop
     * @param x the x position where the crop starts
     * @param y the y position where the crop starts
     * @param width the width of the cropped area from the source image
     * @param height the height of the cropped area from the source image
     * @param targetWidth the width of the target image  
     * @param targetHeight the width of the target image 
     * 
     * @return a cropped part of the given image from the specified <code>x,y</code> point 
     *      to the given <code>width,height</code>, resized to the dimensions specified in <code>targetWidth,targetHeight</code>
     *      
     * @see #cropToSize(BufferedImage, int, int, int, int, int, int, Color)
     */
    public BufferedImage cropToSize(
        BufferedImage image,
        int x,
        int y,
        int width,
        int height,
        int targetWidth,
        int targetHeight) {

        return cropToSize(image, x, y, width, height, targetWidth, targetHeight, COLOR_TRANSPARENT);
    }

    /**
     * Crops a part of the given image from the specified <code>x,y</code> point to the given <code>width,height</code>,
     * and then resizes this cropped image to the dimensions specified in <code>targetWidth,targetHeight</code>. 
     * 
     * Should the target image rectangle be outside of the source image, the source image is enlarged and 
     * the given background replace color is used for the additional pixels.<p> 
     * 
     * The aspect ratio of the target image is not kept.<p>
     * 
     * @param image the image to crop
     * @param x the x position where the crop starts
     * @param y the y position where the crop starts
     * @param width the width of the cropped area from the source image
     * @param height the height of the cropped area from the source image
     * @param targetWidth the width of the target image  
     * @param targetHeight the width of the target image 
     * @param backgroundColor the color to use if the background must be enlarged 
     * 
     * @return a cropped part of the given image from the specified <code>x,y</code> point 
     *      to the given <code>width,height</code>, resized to the dimensions specified in <code>targetWidth,targetHeight</code> 
     *      
     * @see #cropToSize(BufferedImage, int, int, int, int, int, int)
     */
    public BufferedImage cropToSize(
        BufferedImage image,
        int x,
        int y,
        int width,
        int height,
        int targetWidth,
        int targetHeight,
        Color backgroundColor) {

        image = crop(image, x, y, width, height, backgroundColor);
        image = resize(image, targetWidth, targetHeight);

        return image;
    }

    /**
     * Returns the byte contents of the given image.<p>
     * 
     * @param image the image to get the byte contents for
     * @param type the type of the image to get the byte contents for
     * 
     * @return the byte contents of the given image
     * 
     * @throws IOException in case the image could not be converted to bytes 
     */
    public byte[] getBytes(BufferedImage image, String type) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        write(image, out, type);
        return out.toByteArray();
    }

    /**
     * Reduces the colors in the given image to the given maximum color number.<p>
     *  
     * @param image the image to reduce the colors from
     * @param maxColors the maximum number of allowed colors in the output image (usually 256)
     * @param alphaToBitmask indicates if alpha information should be converted
     * 
     * @return the transformed image
     */
    public BufferedImage reduceColors(BufferedImage image, int maxColors, boolean alphaToBitmask) {

        return Quantize.process(image, maxColors, alphaToBitmask);
    }

    /**
     * Resizes an image according to the width and height specified.<p>
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * 
     * @return the transformed image
     */
    public BufferedImage resize(BufferedImage image, int width, int height) {

        return resize(image, width, height, false);
    }

    /**
     * Resizes an image according to the width and height specified,
     * keeping the aspect ratio if required.<p>
     * 
     * If set to <code>true</code>, the bestfit option will keep the image within the dimensions specified
     * without losing the aspect ratio.<p>
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * @param bestfit if true, the aspect ratio of the image will be kept
     * 
     * @return the transformed image
     */
    public BufferedImage resize(BufferedImage image, int width, int height, boolean bestfit) {

        return resize(image, width, height, bestfit, true);
    }

    /**
     * Resizes an image according to the width and height specified,
     * keeping the aspect ratio if required.<p>
     * 
     * If set to <code>true</code>, the bestfit option will keep the image within the dimensions specified
     * without losing the aspect ratio.<p>
     * 
     * If set to <code>false</code>, the blowup option will not enlarge an image that is already smaller 
     * then the sepcified target dimensions.<p>
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * @param bestfit if true, the aspect ratio of the image will be kept
     * @param blowup if false, smaller images will not be enlarged to fit in the target dimensions
     * 
     * @return the transformed image
     */
    public BufferedImage resize(BufferedImage image, int width, int height, boolean bestfit, boolean blowup) {

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if (((imageWidth == width) && (imageHeight == height))
            || (!blowup && (imageWidth < width) && (imageHeight < height))) {
            // no resize required
            return image;
        }

        float widthScale = (width / (float)imageWidth);
        float heightScale = (height / (float)imageHeight);

        int targetWidth = width;
        int targetHeight = height;
        if (bestfit) {
            // keep image aspect ratio, find best scale for the result image
            if (widthScale < heightScale) {
                heightScale = widthScale;
                targetHeight = (int)(imageHeight * heightScale);
            } else if (widthScale > heightScale) {
                widthScale = heightScale;
                targetWidth = (int)(imageWidth * widthScale);
            }
        }
        return scale(image, widthScale, heightScale, targetWidth, targetHeight);
    }

    /**
     * Resizes the given image to best fit into the given dimensions (only if it does not already
     * fit in the dimensions), placing the scaled image 
     * at the indicated position on a background with the given color. 
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * @param backgroundColor
     * @param position the position to place the scaled image at
     * 
     * @return the transformed image
     */
    public BufferedImage resize(BufferedImage image, int width, int height, Color backgroundColor, int position) {

        return resize(image, width, height, backgroundColor, position, true);
    }

    /**
     * Resizes the given image to best fit into the given dimensions (only if it does not already
     * fit in the dimensions), placing the scaled image 
     * at the indicated position on a background with the given color. 
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * @param backgroundColor
     * @param position the position to place the scaled image at
     * @param blowup if false, smaller images will not be enlarged to fit in the target dimensions
     * 
     * @return the transformed image
     */
    public BufferedImage resize(
        BufferedImage image,
        int width,
        int height,
        Color backgroundColor,
        int position,
        boolean blowup) {

        // resize the image to fit into the required dimension
        BufferedImage scaled = resize(image, width, height, true, blowup);
        // check if the image fits after rescale
        int scaledWidth = scaled.getWidth();
        int scaledHeight = scaled.getHeight();

        // check the background color
        ColorModel cm = scaled.getColorModel();
        if (!cm.hasAlpha() && (backgroundColor == COLOR_TRANSPARENT)) {
            // alpha not supported by target color model
            backgroundColor = m_renderSettings.getTransparentReplaceColor();
        }

        if ((scaledWidth == width) && (scaledHeight == height)) {
            if (image.getColorModel().hasAlpha() && (backgroundColor != COLOR_TRANSPARENT)) {
                // background color replacement may be required
                position = Simapi.POS_UP_LEFT;
            } else {
                // no resize required and also no background color change
                return scaled;
            }
        }

        threadSetNice();

        // create the background image
        BufferedImage result = createImage(scaled.getColorModel(), width, height);
        Graphics2D g = result.createGraphics();
        if (backgroundColor != COLOR_TRANSPARENT) {
            // don't fill if background is transparent
            g.setPaintMode();
            g.setColor(backgroundColor);
            g.fillRect(0, 0, width, height);
        }

        int x;
        int y;
        switch (position) {
            case Simapi.POS_DOWN_LEFT:
                x = 0;
                y = height - scaledHeight;
                break;
            case Simapi.POS_DOWN_RIGHT:
                x = width - scaledWidth;
                y = height - scaledHeight;
                break;
            case Simapi.POS_STRAIGHT_DOWN:
                x = (width - scaledWidth) / 2;
                y = height - scaledHeight;
                break;
            case Simapi.POS_STRAIGHT_LEFT:
                x = 0;
                y = (height - scaledHeight) / 2;
                break;
            case Simapi.POS_STRAIGHT_RIGHT:
                x = width - scaledWidth;
                y = (height - scaledHeight) / 2;
                break;
            case Simapi.POS_STRAIGHT_UP:
                x = (width - scaledWidth) / 2;
                y = 0;
                break;
            case Simapi.POS_UP_LEFT:
                x = 0;
                y = 0;
                break;
            case Simapi.POS_UP_RIGHT:
                x = width - scaledWidth;
                y = 0;
                break;
            default:
                // crop center
                x = (width - scaledWidth) / 2;
                y = (height - scaledHeight) / 2;
        }

        // draw the scaled image to the conext at the target position
        g.drawImage(scaled, x, y, null);
        g.dispose();
        scaled.flush();
        scaled = null;

        threadSetNormal();
        return result;
    }

    /**
     * Resizes an image according to the width and height specified,
     * cropping the image along the sides in case the required height and with can not be reached without 
     * changing the apsect ratio of the image.<p>
     * 
     * Use the constants <code>{@link Simapi#POS_CENTER}</code> etc. to indicate the crop position.<p>
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * @param position the position to place the cropped image at
     * 
     * @return the transformed image
     */
    public BufferedImage resize(BufferedImage image, int width, int height, int position) {

        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if ((imageWidth == width) && (imageHeight == height)) {
            // no resize required
            return image;
        }

        float widthScale = (width / (float)imageWidth);
        float heightScale = (height / (float)imageHeight);

        // keep image aspect ratio, find best scale for the result image
        if (widthScale >= heightScale) {
            heightScale = widthScale;
        } else {
            widthScale = heightScale;
        }

        BufferedImage scaledImage;
        if ((widthScale != 1.0) && (heightScale != 1.0)) {
            // scale the image to the required size
            scaledImage = scale(image, widthScale, heightScale);
            // reset to new scale
            imageWidth = scaledImage.getWidth();
            imageHeight = scaledImage.getHeight();
        } else {
            // no scale required
            scaledImage = image;
        }

        // return the cropped result
        return crop(scaledImage, width, height, position);
    }

    /**
     * Scales an image according to the given scale factor.<p>
     * 
     * If the scale is 2.0, the result image will be twice as large as the original,
     * if the scale is 0.5, the result image will be half as large as the original.<p>
     * 
     * @param image the image to scale
     * @param scale the scale factor
     * 
     * @return the transformed image
     */
    public BufferedImage scale(BufferedImage image, float scale) {

        return scale(image, scale, scale);
    }

    /**
     * Scale the image with different ratios along the width and height.<p>
     * 
     * @param image the image to scale
     * @param widthScale the scale factor for the width
     * @param heightScale the scale factor for the height
     * 
     * @return the transformed image
     */
    public BufferedImage scale(BufferedImage image, float widthScale, float heightScale) {

        int targetWidth = Math.round(image.getWidth() * widthScale);
        int targetHeight = Math.round(image.getHeight() * heightScale);

        return scale(image, widthScale, heightScale, targetWidth, targetHeight);
    }

    /**
     * Scale the image with different ratios along the width and height to the given target dimensions.<p>
     * 
     * Giving both scale factor and target dimensions is required to avoid rounding errors that 
     * lead to the "missing line" issue.<p>
     * 
     * @param image the image to scale
     * @param widthScale the scale factor for the width
     * @param heightScale the scale factor for the height
     * @param targetWidth the width of the target image
     * @param targetHeight the height of the target image
     * 
     * @return the transformed image
     */
    public BufferedImage scale(
        BufferedImage image,
        float widthScale,
        float heightScale,
        int targetWidth,
        int targetHeight) {

        int width = image.getWidth();
        int height = image.getHeight();

        // ensure the image uses a RGB system color model (otherwise operation may take very long and results may be bad quality)
        image = ensureImageIsSystemType(image, m_renderSettings.getTransparentReplaceColor() != COLOR_TRANSPARENT);

        RenderingHints renderHints = m_renderSettings.getRenderingHints();
        if (renderHints == RenderSettings.HINTS_QUALITY) {
            // default render setting, adjust for thumbnail generation to avoid "slow scaling" issue 
            if (((widthScale < 0.25f) && (heightScale < 0.25f))
                || ((widthScale < 0.5f) && (width * widthScale < 101))
                || ((heightScale < 0.5f) && (height * heightScale < 101))) {

                // thumbnail generation, use speed settings
                renderHints = RenderSettings.HINTS_SPEED;
            }
        }

        threadSetNice();

        double factor = ((image.getWidth() / (widthScale * width)) + (image.getHeight() / (heightScale * height))) / 2.0;
        if (m_renderSettings.isUseBlur()
            && ((width * height) < m_renderSettings.getMaximumBlurSize())
            && ((widthScale < 0.575f) || (heightScale < 0.575f))) {
            // must apply blur or the result will look jagged if scale is smaller then 0.5   
            // (actually close to 0.5 it also looks jagged, so we use 0.575 instead)
            // however, if the image is to big, "out of memory" issues may occur
            int average = (width + height) / 2;
            if ((factor < 5.0) && (average < 900)) {
                // image is quite small and suitable factor - use gaussian blur 
                GaussianFilter gauss = new GaussianFilter();
                double radius = Math.sqrt(2.0 * factor);
                gauss.setRadius((float)radius);
                image = gauss.filter(image, null);
            } else {
                // image is rather large, use much faster box blur
                double root = Math.sqrt(factor);
                int radius;
                if (factor < 2.5) {
                    // this is a rather small scale factor, use Math.floor() or image might get blurry
                    radius = (int)Math.floor(root);
                } else {
                    // scale factor is rather large, use Math.round() for better result
                    radius = (int)Math.round(root);
                }
                BoxBlurFilter blur = new BoxBlurFilter();
                blur.setRadius(radius);
                image = blur.filter(image, null);
            }
        }

        // create the image scaling transformation
        AffineTransform at = AffineTransform.getScaleInstance(widthScale, heightScale);
        AffineTransformOp ato = new AffineTransformOp(at, renderHints);

        // must create the result image manually, otherwise the size of the result image may be 1 pixel off
        BufferedImage result = createImage(image.getColorModel(), targetWidth, targetHeight);
        result = ato.filter(image, result);

        threadSetNormal();
        return result;
    }

    /**
     * Writes an image to a local file.<p>
     * 
     * @param image the image to write
     * @param destination the destination file
     * @param type the type of the image to write
     * 
     * @throws IOException in case the image could not be written
     */
    public void write(BufferedImage image, File destination, String type) throws IOException {

        write(image, (Object)destination, type);
    }

    /**
     * Writes an image to an output stream.<p>
     * 
     * @param image the image to write
     * @param destination the output stream to write the image to
     * @param type the type of the image to write
     * 
     * @throws IOException in case the image could not be written
     */
    public void write(BufferedImage image, OutputStream destination, String type) throws IOException {

        write(image, (Object)destination, type);
    }

    /**
     * Writes an image to a local file.<p>
     * 
     * @param image the image to write
     * @param destination the destination file name
     * @param type the type of the image to write
     * 
     * @throws IOException in case the image could not be written
     */
    public void write(BufferedImage image, String destination, String type) throws IOException {

        write(image, new File(destination), type);
    }

    /**
     * Creates a buffered image that has the given dimensions and uses the given color model.<p>
     * 
     * @param colorModel the color model to use
     * @param width the width of the image to create
     * @param height the height of the image to create
     * 
     * @return a new image with the given dimensions and uses the given color model
     */
    protected BufferedImage createImage(ColorModel colorModel, int width, int height) {

        BufferedImage result;
        if ((colorModel.getTransparency() == Transparency.OPAQUE)
            && (m_renderSettings.getTransparentReplaceColor() != COLOR_TRANSPARENT)) {
            result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        } else {
            result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        return result;
    }

    /**
     * Writes an image to the given output object, using the the given quality.<p>
     * 
     * The <code>quality</code> is used only if the image <code>type</code> supports different qualities.
     * For example, this it is used when writing JPEG images.
     * A quality of 0.1 is very poor, 0.75 is ok, 1.0 is maximum.<p> 
     * 
     * @param im the image to write
     * @param output the destination to write the image to
     * @param formatName the type of the image to write
     * 
     * @throws IOException in case the image could not be written
     */
    protected void write(BufferedImage im, Object output, String formatName) throws IOException {

        if (output == null) {
            throw new IllegalArgumentException("output == null!");
        }
        if (im == null) {
            throw new IllegalArgumentException("image == null!");
        }
        if (formatName == null) {
            throw new IllegalArgumentException("formatName == null!");
        }

        // create the output stream
        ImageOutputStream stream = null;
        try {
            stream = ImageIO.createImageOutputStream(output);
        } catch (IOException e) {
            throw new IIOException("Can't create output stream!", e);
        }

        // make sure we have our exact constants to work with
        formatName = getImageType(formatName);
        if (formatName == null) {
            throw new IllegalArgumentException("no writers found for format '" + formatName + "'");
        }

        // make sure there are no transparent pixels left if not supported by the written image format
        if (im.getColorModel().hasAlpha()
            && ((TYPE_JPEG == formatName) || (TYPE_TIFF == formatName) || (TYPE_BMP == formatName))) {
            // several formats do not support alpha
            BufferedImage result = new BufferedImage(im.getWidth(), im.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g = result.createGraphics();
            g.setPaintMode();
            g.setColor(m_renderSettings.getTransparentReplaceColor());
            g.fillRect(0, 0, result.getWidth(), result.getHeight());
            g.drawImage(im, 0, 0, null);
            g.dispose();
            im = result;
        }

        // obtain the writer for the image
        // this must work since it is already done in the #getImageType(String) call above
        ImageWriter writer = (ImageWriter)ImageIO.getImageWritersByFormatName(formatName).next();

        // get default image writer parameter
        ImageWriteParam param = writer.getDefaultWriteParam();
        if (param.canWriteCompressed()) {
            // set compression parameters if supported by writer
            param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            if ((param.getCompressionTypes() != null) && (param.getCompressionType() == null)) {
                // a compression parameter is required but not provided, use the first one available
                param.setCompressionType(param.getCompressionTypes()[0]);
            }
            param.setCompressionQuality(m_renderSettings.getCompressionQuality());
        }

        // now write the image
        writer.setOutput(stream);
        writer.write(null, new IIOImage(im, null, null), param);
        stream.flush();
        writer.dispose();
        stream.close();
    }

    private void threadSetNice() {

        Thread t = Thread.currentThread();
        if (t.getPriority() > m_renderSettings.getThreadNicePriority()) {
            m_renderSettings.setThreadOldPriority(t.getPriority());
            try {
                t.setPriority(m_renderSettings.getThreadNicePriority());
            } catch (Exception e) {
                // can't set thread priority, continue with current priority
            }
        }
    }

    private void threadSetNormal() {

        Thread t = Thread.currentThread();
        if (t.getPriority() != m_renderSettings.getThreadOldPriority()) {
            try {
                t.setPriority(m_renderSettings.getThreadOldPriority());
            } catch (Exception e) {
                // can't set thread priority, continue with current priority
            }
        }
    }
}