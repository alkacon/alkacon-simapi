/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/Attic/SimapiImpl.java,v $
 * Date   : $Date: 2005/10/17 07:35:30 $
 * Version: $Revision: 1.1 $
 *
 * This library is part of OpenCms -
 * the Open Source Content Mananagement System
 *
 * Copyright (C) 2002 - 2005 Alkacon Software (http://www.alkacon.com)
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
 * For further information about Alkacon Software, please see the
 * company website: http://www.alkacon.com
 *
 * For further information about OpenCms, please see the
 * project website: http://www.opencms.org
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package com.alkacon.simapi;

import com.alkacon.simapi.util.GifImageWriterSpi;
import com.alkacon.simapi.util.Quantize;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;

import javax.imageio.IIOException;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.stream.ImageOutputStream;

/**
 * Implements the {@link com.alkacon.simapi.Simapi} interface<p>
 * 
 * @author Alexander Kandzior
 */
public class SimapiImpl implements Simapi {

    /**
     * Register the GIF encoder.<p>
     */
    static {

        // register the GIF encoder with the Java ImageIO registry
        IIORegistry.getDefaultInstance().registerServiceProvider(new GifImageWriterSpi());
    }

    /**
     * @see com.alkacon.simapi.Simapi#crop(java.awt.image.BufferedImage, int, int, int)
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
            case POS_DOWN_LEFT:
                x = 0;
                y = imageHeight - height;
                break;
            case POS_DOWN_RIGHT:
                x = imageWidth - width;
                y = imageHeight - height;
                break;
            case POS_STRAIGHT_DOWN:
                x = (imageWidth - width) / 2;
                y = imageHeight - height;
                break;
            case POS_STRAIGHT_LEFT:
                x = 0;
                y = (imageHeight - height) / 2;
                break;
            case POS_STRAIGHT_RIGHT:
                x = imageWidth - width;
                y = (imageHeight - height) / 2;
                break;
            case POS_STRAIGHT_UP:
                x = (imageWidth - width) / 2;
                y = 0;
                break;
            case POS_UP_LEFT:
                x = 0;
                y = 0;
                break;
            case POS_UP_RIGHT:
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
     * @see com.alkacon.simapi.Simapi#getBytes(java.awt.image.BufferedImage, java.lang.String)
     */
    public byte[] getBytes(BufferedImage image, String type) throws IOException {

        ByteArrayOutputStream out = new ByteArrayOutputStream(4096);
        ImageIO.write(image, type, out);
        return out.toByteArray();
    }

    /**
     * @see com.alkacon.simapi.Simapi#getImageType(java.lang.String)
     */
    public String getImageType(String filename) {

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
        if (type.equalsIgnoreCase("jpg") || type.equalsIgnoreCase("jpeg")) {
            type = "JPEG";
        } else if (type.equalsIgnoreCase("tif") || type.equalsIgnoreCase("tiff")) {
            type = "TIFF";
        } else if (type.equalsIgnoreCase("pbm")) {
            type = "PNM";
        } else if (type.equalsIgnoreCase("pgm")) {
            type = "PNM";
        } else if (type.equalsIgnoreCase("ppm")) {
            type = "PNM";
        } else if (type.equalsIgnoreCase("png")) {
            type = "PNG";
        } else if (type.equalsIgnoreCase("bmp")) {
            type = "BMP";
        } else if (type.equalsIgnoreCase("gif")) {
            type = "GIF";
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
     * @see com.alkacon.simapi.Simapi#read(byte[])
     */
    public BufferedImage read(byte[] source) throws IOException {

        ByteArrayInputStream in = new ByteArrayInputStream(source);
        return read(in);
    }

    /**
     * @see com.alkacon.simapi.Simapi#read(java.io.File)
     */
    public BufferedImage read(File source) throws IOException {

        return ImageIO.read(source);
    }

    /**
     * @see com.alkacon.simapi.Simapi#read(java.io.InputStream)
     */
    public BufferedImage read(InputStream source) throws IOException {

        return ImageIO.read(source);
    }

    /**
     * @see com.alkacon.simapi.Simapi#read(java.lang.String)
     */
    public BufferedImage read(String source) throws IOException {

        return read(new File(source));
    }

    /**
     * @see com.alkacon.simapi.Simapi#read(java.net.URL)
     */
    public BufferedImage read(URL source) throws IOException {

        return ImageIO.read(source);
    }

    /**
     * @see com.alkacon.simapi.Simapi#reduceColors(java.awt.image.BufferedImage, int, boolean)
     */
    public BufferedImage reduceColors(BufferedImage image, int maxColors, boolean alphaToBitmask) {

        return Quantize.process(image, maxColors, alphaToBitmask);
    }

    /**
     * @see com.alkacon.simapi.Simapi#resize(java.awt.image.BufferedImage, int, int)
     */
    public BufferedImage resize(BufferedImage image, int width, int height) {

        return resize(image, width, height, false);
    }

    /**
     * @see com.alkacon.simapi.Simapi#resize(java.awt.image.BufferedImage, int, int, boolean)
     */
    public BufferedImage resize(BufferedImage image, int width, int height, boolean bestfit) {

        return resize(image, width, height, bestfit, false);
    }

    /**
     * @see com.alkacon.simapi.Simapi#resize(java.awt.image.BufferedImage, int, int, boolean, boolean)
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

        if (bestfit) {
            // keep image aspect ratio, find best scale for the result image
            if (widthScale <= heightScale) {
                heightScale = widthScale;
            } else {
                widthScale = heightScale;
            }
        }
        return scale(image, widthScale, heightScale);
    }

    /**
     * @see com.alkacon.simapi.Simapi#resize(java.awt.image.BufferedImage, int, int, java.awt.Color, int)
     */
    public BufferedImage resize(BufferedImage image, int width, int height, Color backgroundColor, int position) {

        // resize the image to fit into the required dimension
        image = resize(image, width, height, true, false);
        // check if the image fits after rescale
        int imageWidth = image.getWidth();
        int imageHeight = image.getHeight();
        if ((imageWidth == width) && (imageHeight == height)) {
            // no resize required
            return image;
        }

        // create the background image        
        BufferedImage background = createImage(image.getColorModel(), width, height);
        Graphics2D g = background.createGraphics();
        g.setPaintMode();
        g.setColor(backgroundColor);
        g.fillRect(0, 0, width, height);
        g.drawLine(0, 0, width, height);

        int x;
        int y;
        switch (position) {
            case POS_DOWN_LEFT:
                x = 0;
                y = height - imageHeight;
                break;
            case POS_DOWN_RIGHT:
                x = width - imageWidth;
                y = height - imageHeight;
                break;
            case POS_STRAIGHT_DOWN:
                x = (width - imageWidth) / 2;
                y = height - imageHeight;
                break;
            case POS_STRAIGHT_LEFT:
                x = 0;
                y = (height - imageHeight) / 2;
                break;
            case POS_STRAIGHT_RIGHT:
                x = width - imageWidth;
                y = (height - imageHeight) / 2;
                break;
            case POS_STRAIGHT_UP:
                x = (width - imageWidth) / 2;
                y = 0;
                break;
            case POS_UP_LEFT:
                x = 0;
                y = 0;
                break;
            case POS_UP_RIGHT:
                x = width - imageWidth;
                y = 0;
                break;
            default:
                // crop center
                x = (width - imageWidth) / 2;
                y = (height - imageHeight) / 2;
        }

        // draw the overly image
        g.drawImage(image, x, y, null);

        return background;
    }

    /**
     * @see com.alkacon.simapi.Simapi#resize(java.awt.image.BufferedImage, int, int, int)
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
     * @see com.alkacon.simapi.Simapi#scale(java.awt.image.BufferedImage, float)
     */
    public BufferedImage scale(BufferedImage image, float scale) {

        return scale(image, scale, scale);
    }

    /**
     * @see com.alkacon.simapi.Simapi#scale(java.awt.image.BufferedImage, float, float)
     */
    public BufferedImage scale(BufferedImage image, float widthScale, float heightScale) {

        // transformation rendering hints
        HashMap hints = new HashMap();
        hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
        hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_NORMALIZE);

        // create the image scaling transformation
        AffineTransform at = AffineTransform.getScaleInstance(widthScale, heightScale);
        AffineTransformOp ato = new AffineTransformOp(at, new RenderingHints(hints));

        // create a buffer for the result image
        BufferedImage result = createImage(
            image.getColorModel(),
            (int)(image.getWidth() * widthScale),
            (int)(image.getHeight() * heightScale));
        Graphics2D g2d = result.createGraphics();

        // apply the transformation
        g2d.drawImage(image, ato, 0, 0);

        // return the result
        return result;
    }

    /**
     * @see com.alkacon.simapi.Simapi#write(java.awt.image.BufferedImage, java.io.File, java.lang.String)
     */
    public void write(BufferedImage image, File destination, String type) throws IOException {

        ImageIO.write(image, type, destination);
    }

    /**
     * @see com.alkacon.simapi.Simapi#write(java.awt.image.BufferedImage, java.io.File, java.lang.String, float)
     */
    public void write(BufferedImage image, File destination, String type, float quality) throws IOException {

        write(image, (Object)destination, type, quality);
    }

    /**
     * @see com.alkacon.simapi.Simapi#write(java.awt.image.BufferedImage, java.io.OutputStream, java.lang.String)
     */
    public void write(BufferedImage image, OutputStream destination, String type) throws IOException {

        ImageIO.write(image, type, destination);
    }

    /**
     * @see com.alkacon.simapi.Simapi#write(java.awt.image.BufferedImage, java.io.OutputStream, java.lang.String, float)
     */
    public void write(BufferedImage image, OutputStream destination, String type, float quality) throws IOException {

        write(image, (Object)destination, type, quality);
    }

    /**
     * @see com.alkacon.simapi.Simapi#write(java.awt.image.BufferedImage, java.lang.String, java.lang.String)
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

        BufferedImage buff;
        if (colorModel.getTransparency() == Transparency.OPAQUE) {
            buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        } else {
            buff = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        }
        return buff;
    }

    /**
     * Duplicates a given image, for further modification.<p>
     * 
     * @param image the image to duplicate
     * 
     * @return the duplicated image
     */
    protected BufferedImage duplicateImage(BufferedImage image) {

        BufferedImage result = createImage(image.getColorModel(), image.getWidth(), image.getHeight());
        Graphics2D g = result.createGraphics();
        g.drawImage(image, 0, 0, null);

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
     * @param quality the quality to use
     * 
     * @throws IOException in case the image could not be written
     */
    protected void write(RenderedImage im, Object output, String formatName, float quality) throws IOException {

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

        // obtain the writer for the image
        ImageWriter writer;
        Iterator iter = ImageIO.getImageWritersByFormatName(formatName);
        if (iter.hasNext()) {
            writer = (ImageWriter)iter.next();
        } else {
            writer = null;
        }

        if (writer == null) {
            throw new IllegalArgumentException("no writers found for format '" + formatName + "'");
        }

        // get default image writer parameter
        ImageWriteParam param = writer.getDefaultWriteParam();
        // set quality parameters
        param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        param.setCompressionQuality(quality);

        // now write the image
        writer.setOutput(stream);
        writer.write(null, new IIOImage(im, null, null), param);
        stream.flush();
        writer.dispose();

        stream.close();
    }
}