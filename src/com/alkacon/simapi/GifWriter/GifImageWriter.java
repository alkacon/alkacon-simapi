/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/util/GifImageWriter.java,v $
 * Date   : $Date: 2007/11/20 15:59:13 $
 * Version: $Revision: 1.3 $
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

package com.alkacon.simapi.GifWriter;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.awt.image.RenderedImage;
import java.io.IOException;

import javax.imageio.IIOImage;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageOutputStream;

/**
 * Implements the GIF image writer.<p>
 * 
 * @author Alexander Kandzior
 */
public class GifImageWriter extends ImageWriter {

    /**
     * Public constructor.<p>
     * 
     * @param provider the originating provider
     */
    public GifImageWriter(GifImageWriterSpi provider) {

        super(provider);
    }

    /**
     * @see javax.imageio.ImageTranscoder#convertImageMetadata(javax.imageio.metadata.IIOMetadata, javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam)
     */
    public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier imageType, ImageWriteParam param) {

        return null;
    }

    /**
     * @see javax.imageio.ImageTranscoder#convertStreamMetadata(javax.imageio.metadata.IIOMetadata, javax.imageio.ImageWriteParam)
     */
    public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) {

        return null;
    }

    /**
     * @see javax.imageio.ImageWriter#getDefaultImageMetadata(javax.imageio.ImageTypeSpecifier, javax.imageio.ImageWriteParam)
     */
    public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier imageType, ImageWriteParam param) {

        return null;
    }

    /**
     * @see javax.imageio.ImageWriter#getDefaultStreamMetadata(javax.imageio.ImageWriteParam)
     */
    public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) {

        return null;
    }

    /**
     * @see javax.imageio.ImageWriter#write(javax.imageio.metadata.IIOMetadata, javax.imageio.IIOImage, javax.imageio.ImageWriteParam)
     */
    public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) throws IOException {

        if (image == null) {
            throw new IllegalArgumentException("image == null");
        }

        if (image.hasRaster()) {
            throw new UnsupportedOperationException("Cannot write rasters");
        }

        output = getOutput();
        if (output == null) {
            throw new IllegalStateException("output was not set");
        }

        if (param == null) {
            param = getDefaultWriteParam();
        }

        ImageOutputStream ios = (ImageOutputStream)output;
        RenderedImage ri = image.getRenderedImage();

        if (ri instanceof BufferedImage) {

            BufferedImage bi = (BufferedImage)ri;
            BufferedImage indexed;
            if (!(bi.getColorModel() instanceof IndexColorModel)) {
                indexed = Quantize.process(bi, 256, true);
            } else {
                indexed = bi;
            }
            GifAcmeEncoder encoder = new GifAcmeEncoder(indexed);
            encoder.write(ios);

        } else {

            throw new IOException("Image not of type BufferedImage");
        }
    }
}