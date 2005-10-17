/* 
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/Simapi.java,v $
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

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;

/**
 * Simple Image API (Simapi) that provides convenient access to commonly used imaging operations.<p>
 * 
 * @author Alexander Kandzior
 */
public interface Simapi {

    /** Position indicator: Center. */
    int POS_CENTER = 0;

    /** Position indicator: Down left. */
    int POS_DOWN_LEFT = 1;

    /** Position indicator: Down right. */
    int POS_DOWN_RIGHT = 2;

    /** Position indicator: Straight down. */
    int POS_STRAIGHT_DOWN = 3;

    /** Position indicator: Straight left. */
    int POS_STRAIGHT_LEFT = 4;

    /** Position indicator: Straight right. */
    int POS_STRAIGHT_RIGHT = 5;

    /** Position indicator: Straight up. */
    int POS_STRAIGHT_UP = 6;

    /** Position indicator: Up left. */
    int POS_UP_LEFT = 7;

    /** Position indicator: Up right. */
    int POS_UP_RIGHT = 8;

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
     * @param blowup if false, smaller images will not be enlarged to the target dimensions
     * 
     * @return the transformed image
     */
    public BufferedImage resize(BufferedImage image, int width, int height, boolean bestfit, boolean blowup);

    /**
     * Crops an image according to the width and height specified.<p>
     * 
     * Use the constants <code>{@link #POS_CENTER}</code> etc. to indicate the crop position.<p>
     * 
     * @param image the image to crop
     * @param width the width of the target image
     * @param height the height of the target image
     * @param cropPosition the position to crop the image at
     * 
     * @return the transformed image
     */
    BufferedImage crop(BufferedImage image, int width, int height, int cropPosition);

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
    byte[] getBytes(BufferedImage image, String type) throws IOException;

    /**
     * Returns the image type from the given file name based on the file suffix (extension)
     * and the available image writers.<p>
     * 
     * For example, for the file name "opencms.gif" the type is GIF, for 
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
    String getImageType(String filename);

    /**
     * Loads an image from a byte array
     * 
     * @param source the byte array to read the image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded 
     */
    BufferedImage read(byte[] source) throws IOException;

    /**
     *  Loads an image from a local file.<p>
     * 
     * @param source the file to read the input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded 
     */
    BufferedImage read(File source) throws IOException;

    /**
     * Loads an image from an InputStream.<p>
     * 
     * @param source the input stream to read the input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded 
     * 
     */
    BufferedImage read(InputStream source) throws IOException;

    /**
     * Loads an image from a file whose path is supplied as a String
     * 
     * @param source the path to read to input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded 
     */
    BufferedImage read(String source) throws IOException;

    /**
     *  Loads an image from a  URL.<p>
     * 
     * @param source the URL to read the input image from
     * 
     * @return the loaded image
     * 
     * @throws IOException in case the image could not be loaded      
     */
    BufferedImage read(URL source) throws IOException;

    /**
     * Reduces the colors in the given image to the given maximum number.<p>
     *  
     * @param image the image to reduce the colors from
     * @param maxColors the maximum number of allowed colors in the output image
     * @param alphaToBitmask indicates if alpha information should be converted
     * 
     * @return the transformed image
     * 
     * @throws IOException in case image conversion fails
     */
    BufferedImage reduceColors(BufferedImage image, int maxColors, boolean alphaToBitmask) throws IOException;

    /**
     * Resizes an image according to the width and height specified.<p>
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * 
     * @return the transformed image
     */
    BufferedImage resize(BufferedImage image, int width, int height);

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
    BufferedImage resize(BufferedImage image, int width, int height, boolean bestfit);

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
    BufferedImage resize(BufferedImage image, int width, int height, Color backgroundColor, int position);

    /**
     * Resizes an image according to the width and height specified,
     * cropping the image along the sides in case the required height and with can not be reached without 
     * changing the apsect ratio of the image.<p>
     * 
     * Use the constants <code>{@link #POS_CENTER}</code> etc. to indicate the crop position.<p>
     * 
     * @param image the image to resize
     * @param width the width of the target image
     * @param height the height of the target image
     * @param position the position to place the cropped image at
     * 
     * @return the transformed image
     */
    BufferedImage resize(BufferedImage image, int width, int height, int position);

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
    BufferedImage scale(BufferedImage image, float scale);

    /**
     * Scale the image with different ratios along the width and height.<p>
     * 
     * @param image the image to scale
     * @param widthScale the scale factor for the width
     * @param heightScale the sacle factor for the height
     * 
     * @return the transformed image
     */
    BufferedImage scale(BufferedImage image, float widthScale, float heightScale);

    /**
     * Writes an image to a local file.<p>
     * 
     * @param image the image to write
     * @param destination the destination file
     * @param type the type of the image to write
     * 
     * @throws IOException in case the image could not be written
     */
    void write(BufferedImage image, File destination, String type) throws IOException;

    /**
     * Writes an image to a local file, using the the given quality.<p>
     * 
     * The <code>quality</code> is used only if the image <code>type</code> supports different qualities.
     * For example, this it is used when writing JPEG images.
     * A quality of 0.1 is very poor, 0.75 is ok, 1.0 is maximum.<p> 
     * 
     * @param image the image to write
     * @param destination the destination file
     * @param type the type of the image to write
     * @param quality the quality to use
     * 
     * @throws IOException in case the image could not be written
     */
    void write(BufferedImage image, File destination, String type, float quality) throws IOException;

    /**
     * Writes an image to an output stream.<p>
     * 
     * @param image the image to write
     * @param destination the output stream to write the image to
     * @param type the type of the image to write
     * 
     * @throws IOException in case the image could not be written
     */
    void write(BufferedImage image, OutputStream destination, String type) throws IOException;

    /**
     * Writes an image to an output stream, using the the given quality.<p>
     * 
     * The <code>quality</code> is used only if the image <code>type</code> supports different qualities.
     * For example, this it is used when writing JPEG images.
     * A quality of 0.1 is very poor, 0.75 is ok, 1.0 is maximum.<p> 
     * 
     * @param image the image to write
     * @param destination the destination output stream
     * @param type the type of the image to write
     * @param quality the quality to use
     * 
     * @throws IOException in case the image could not be written
     */
    void write(BufferedImage image, OutputStream destination, String type, float quality) throws IOException;

    /**
     * Writes an image to a local file.<p>
     * 
     * @param image the image to write
     * @param destination the destination file name
     * @param type the type of the image to write
     * 
     * @throws IOException in case the image could not be written
     */
    void write(BufferedImage image, String destination, String type) throws IOException;
}