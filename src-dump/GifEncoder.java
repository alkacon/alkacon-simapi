/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src-dump/GifEncoder.java,v $
 * Date   : $Date: 2005/11/15 14:04:02 $
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

/*
 * (C) 2004 - Geotechnical Software Services
 * 
 * This code is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public 
 * License as published by the Free Software Foundation; either 
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This code is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with this program; if not, write to the Free 
 * Software Foundation, Inc., 59 Temple Place - Suite 330, Boston, 
 * MA  02111-1307, USA.
 */

package com.alkacon.simapi.util;

import java.awt.image.BufferedImage;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Class for converting images to GIF files.
 *
 * <p>
 * Contribution:
 * <ul>
 *   <li>Sverre H. Huseby (gifsave.c on which this is based)</li>
 *   <li>Adam Doppelt (Initial Java port)</li>
 *   <li>Greg Faron (Initial java port)</li>
 * </ul>
 * 
 * @author Jacob Dreyer
 */
public class GifEncoder {

    /**
     * Utility class for GIF compression.<p>
     */
    private class BitFile {

        private byte[] buffer_;
        private DataOutput stream_ = null;
        private int streamIndex_, bitsLeft_;

        /**
         * Constructor.<p>
         * 
         * @param stream the stream to use
         */
        BitFile(DataOutput stream) {

            stream_ = stream;
            buffer_ = new byte[256];
            streamIndex_ = 0;
            bitsLeft_ = 8;
        }

        /**
         * Flushes the stream.<p>
         *  
         * @throws IOException in case of IO errors
         */
        void flush() throws IOException {

            int nBytes = streamIndex_ + ((bitsLeft_ == 8) ? 0 : 1);

            if (nBytes > 0) {
                stream_.write(nBytes);
                stream_.write(buffer_, 0, nBytes);

                buffer_[0] = 0;
                streamIndex_ = 0;
                bitsLeft_ = 8;
            }
        }

        /**
         * Writes the given bits.<p>
         * 
         * @param bits the bits to write
         * @param nBits the nbits to write
         * 
         * @throws IOException in case of IO errors
         */
        void writeBits(int bits, int nBits) throws IOException {

            int nBitsWritten = 0;
            int nBytes = 255;

            do {
                if ((streamIndex_ == 254 && bitsLeft_ == 0) || streamIndex_ > 254) {
                    stream_.write(nBytes);
                    stream_.write(buffer_, 0, nBytes);

                    buffer_[0] = 0;
                    streamIndex_ = 0;
                    bitsLeft_ = 8;
                }

                if (nBits <= bitsLeft_) {
                    buffer_[streamIndex_] |= (bits & ((1 << nBits) - 1)) << (8 - bitsLeft_);

                    nBitsWritten += nBits;
                    bitsLeft_ -= nBits;
                    nBits = 0;
                }

                else {
                    buffer_[streamIndex_] |= (bits & ((1 << bitsLeft_) - 1)) << (8 - bitsLeft_);

                    nBitsWritten += bitsLeft_;
                    bits >>= bitsLeft_;
                    nBits -= bitsLeft_;
                    buffer_[++streamIndex_] = 0;
                    bitsLeft_ = 8;
                }

            } while (nBits != 0);
        }
    }

    /**
     * Used to compress the image by looking for repeating
     * elements.
     */
    private class LzwStringTable {

        private final static short HASH_FREE = (short)0xFFFF;
        private final static short HASHSIZE = 9973;
        private final static short HASHSTEP = 2039;
        private final static int MAXBITS = 12;
        private final static int MAXSTR = (1 << MAXBITS);
        private final static short NEXT_FIRST = (short)0xFFFF;
        private final static int RES_CODES = 2;

        private short nStrings_;
        private byte strChr_[];
        private short strHsh_[];
        private short strNxt_[];

        /**
         * Inits the LZW compression.<p>
         */
        LzwStringTable() {

            strChr_ = new byte[MAXSTR];
            strNxt_ = new short[MAXSTR];
            strHsh_ = new short[HASHSIZE];
        }

        /**
         * Adds the char string.<p>
         * 
         * @param index the index
         * @param b the byte
         * 
         * @return the added int
         */
        int addCharString(short index, byte b) {

            int hshidx;
            if (nStrings_ >= MAXSTR)
                return 0xFFFF;

            hshidx = hash(index, b);
            while (strHsh_[hshidx] != HASH_FREE)
                hshidx = (hshidx + HASHSTEP) % HASHSIZE;

            strHsh_[hshidx] = nStrings_;
            strChr_[nStrings_] = b;
            strNxt_[nStrings_] = (index != HASH_FREE) ? index : NEXT_FIRST;

            return nStrings_++;
        }

        /**
         * Clears the table.<p>
         * 
         * @param codesize the code size
         */
        void clearTable(int codesize) {

            nStrings_ = 0;

            for (int q = 0; q < HASHSIZE; q++)
                strHsh_[q] = HASH_FREE;

            int w = (1 << codesize) + RES_CODES;
            for (int q = 0; q < w; q++)
                this.addCharString((short)0xFFFF, (byte)q);
        }

        /**
         * Finds the char string.<p>
         * 
         * @param index the index
         * @param b the byte
         * 
         * @return the found char string
         */
        short findCharString(short index, byte b) {

            int hshidx, nxtidx;

            if (index == HASH_FREE)
                return b;

            hshidx = hash(index, b);
            while ((nxtidx = strHsh_[hshidx]) != HASH_FREE) {
                if (strNxt_[nxtidx] == index && strChr_[nxtidx] == b)
                    return (short)nxtidx;
                hshidx = (hshidx + HASHSTEP) % HASHSIZE;
            }

            return (short)0xFFFF;
        }

        /**
         * Calculates the hash code.<p>
         * 
         * @param index the index
         * @param lastbyte the last byte
         * 
         * @return the calculated hash code
         */
        int hash(short index, byte lastbyte) {

            return (((short)(lastbyte << 8) ^ index) & 0xFFFF) % HASHSIZE;
        }
    }

    private byte[] colors_ = null;
    private short imageWidth_, imageHeight_;

    private int nColors_;

    private byte[] pixels_ = null;

    /**
     * Constructing a GIF encoder.
     *
     * @param image  The image to encode. The image must be
     *               completely loaded.
     * @throws IOException  If memory is exhausted
     */
    public GifEncoder(BufferedImage image)
    throws IOException {

        imageWidth_ = (short)image.getWidth();
        imageHeight_ = (short)image.getHeight();

        int values[] = image.getRGB(0, 0, image.getWidth(), image.getHeight(), null, 0, image.getWidth());

        byte[][] r = new byte[imageWidth_][imageHeight_];
        byte[][] g = new byte[imageWidth_][imageHeight_];
        byte[][] b = new byte[imageWidth_][imageHeight_];

        int index = 0;

        for (int y = 0; y < imageHeight_; y++) {
            for (int x = 0; x < imageWidth_; x++, index++) {
                r[x][y] = (byte)((values[index] >> 16) & 0xFF);
                g[x][y] = (byte)((values[index] >> 8) & 0xFF);
                b[x][y] = (byte)((values[index] >> 0) & 0xFF);
            }
        }

        toIndexColor(r, g, b);
    }

    /**
     * Create a GIF encoder. r[i][j] refers to the pixel at
     * column i, row j.
     *
     * @param r  Red intensity values.
     * @param g  Green intensity values.
     * @param b  Blue intensity values.
     * @throws IOException  If memory is exhausted or image contains
     *                       more than 256 colors.
     */
    public GifEncoder(byte[][] r, byte[][] g, byte[][] b)
    throws IOException {

        imageWidth_ = (short)(r.length);
        imageHeight_ = (short)(r[0].length);

        toIndexColor(r, g, b);
    }

    /**
     * Writes the image out to a stream in GIF format.
     * This will be a single GIF87a image, non-interlaced, with no
     * background color.
     *
     * @param  stream       The stream to which to output.
     * @throws IOException  Thrown if a write operation fails.
     */
    public void write(DataOutput stream) throws IOException {

        writeString(stream, "GIF87a");
        writeScreenDescriptor(stream);

        stream.write(colors_, 0, colors_.length);

        writeImageDescriptor(stream, imageWidth_, imageHeight_, ',');

        byte codeSize = bitsNeeded(nColors_);
        if (codeSize == 1) {
            codeSize++;
        }

        stream.write(codeSize);

        writeLzwCompressed(stream, codeSize, pixels_);
        stream.write(0);

        writeImageDescriptor(stream, (short)0, (short)0, ';');
    }

    /**
     * Writes the GIF to the given output stream.<p> 
     * 
     * @param stream the stream to write to
     * 
     * @throws IOException in case of write errors
     */
    public void write(OutputStream stream) throws IOException {

        write((DataOutput)new DataOutputStream(stream));
    }

    private byte bitsNeeded(int n) {

        if (n-- == 0)
            return 0;

        byte nBitsNeeded = 1;
        while ((n >>= 1) != 0)
            nBitsNeeded++;

        return nBitsNeeded;
    }

    /**
     * Converts rgb desrcription of image to colour
     * number description used by GIF.
     *
     * @param r  Red array of pixels.
     * @param g  Green array of pixels.
     * @param b  Blue array of pixels.
     * @throws   IOException
     *           Thrown if too many different colours in image.
     */
    private void toIndexColor(byte[][] r, byte[][] g, byte[][] b) throws IOException {

        pixels_ = new byte[imageWidth_ * imageHeight_];
        colors_ = new byte[256 * 3];
        int colornum = 0;

        for (int x = 0; x < imageWidth_; x++) {
            for (int y = 0; y < imageHeight_; y++) {
                int search;
                for (search = 0; search < colornum; search++) {
                    if (colors_[search * 3 + 0] == r[x][y]
                        && colors_[search * 3 + 1] == g[x][y]
                        && colors_[search * 3 + 2] == b[x][y]) {
                        break;
                    }
                }

                if (search > 255)
                    throw new IOException("Too many colors.");

                // Row major order y=row x=col
                pixels_[y * imageWidth_ + x] = (byte)search;

                if (search == colornum) {
                    colors_[search * 3 + 0] = r[x][y]; // [col][row]
                    colors_[search * 3 + 1] = g[x][y];
                    colors_[search * 3 + 2] = b[x][y];
                    colornum++;
                }
            }
        }

        nColors_ = 1 << bitsNeeded(colornum);
        byte copy[] = new byte[nColors_ * 3];
        System.arraycopy(colors_, 0, copy, 0, nColors_ * 3);

        colors_ = copy;
    }

    private void writeImageDescriptor(DataOutput stream, short width, short height, char separator) throws IOException {

        stream.write(separator);

        short leftPosition = 0;
        short topPosition = 0;

        writeWord(stream, leftPosition);
        writeWord(stream, topPosition);
        writeWord(stream, width);
        writeWord(stream, height);

        byte flag = 0;

        // Local color table size
        byte localColorTableSize = 0;
        flag |= (localColorTableSize & 7);

        // Reserved
        byte reserved = 0;
        flag |= (reserved & 3) << 3;

        // Sort flag
        byte sortFlag = 0;
        flag |= (sortFlag & 1) << 5;

        // Interlace flag
        byte interlaceFlag = 0;
        flag |= (interlaceFlag & 1) << 6;

        // Local color table flag
        byte localColorTableFlag = 0;
        flag |= (localColorTableFlag & 1) << 7;

        stream.write(flag);
    }

    private void writeLzwCompressed(DataOutput stream, int codeSize, byte toCompress[]) throws IOException {

        byte c;
        short index;
        int clearcode, endofinfo, numbits, limit;
        short prefix = (short)0xFFFF;

        BitFile bitFile = new BitFile(stream);
        LzwStringTable strings = new LzwStringTable();

        clearcode = 1 << codeSize;
        endofinfo = clearcode + 1;

        numbits = codeSize + 1;
        limit = (1 << numbits) - 1;

        strings.clearTable(codeSize);
        bitFile.writeBits(clearcode, numbits);

        for (int loop = 0; loop < toCompress.length; loop++) {
            c = toCompress[loop];
            if ((index = strings.findCharString(prefix, c)) != -1)
                prefix = index;
            else {
                bitFile.writeBits(prefix, numbits);
                if (strings.addCharString(prefix, c) > limit) {
                    if (++numbits > 12) {
                        bitFile.writeBits(clearcode, numbits - 1);
                        strings.clearTable(codeSize);
                        numbits = codeSize + 1;
                    }

                    limit = (1 << numbits) - 1;
                }

                prefix = (short)(c & 0xFF);
            }
        }

        if (prefix != -1)
            bitFile.writeBits(prefix, numbits);

        bitFile.writeBits(endofinfo, numbits);
        bitFile.flush();
    }

    private void writeScreenDescriptor(DataOutput stream) throws IOException {

        writeWord(stream, imageWidth_);
        writeWord(stream, imageHeight_);

        byte flag = 0;

        // Global color table size
        byte globalColorTableSize = (byte)(bitsNeeded(nColors_) - 1);
        flag |= globalColorTableSize & 7;

        // Global color table flag
        byte globalColorTableFlag = 1;
        flag |= (globalColorTableFlag & 1) << 7;

        // Sort flag
        byte sortFlag = 0;
        flag |= (sortFlag & 1) << 3;

        // Color resolution
        byte colorResolution = 7;
        flag |= (colorResolution & 7) << 4;

        byte backgroundColorIndex = 0;
        byte pixelAspectRatio = 0;

        stream.write(flag);
        stream.write(backgroundColorIndex);
        stream.write(pixelAspectRatio);
    }

    private void writeString(DataOutput stream, String string) throws IOException {

        for (int i = 0; i < string.length(); i++)
            stream.write((byte)(string.charAt(i)));
    }

    private void writeWord(DataOutput stream, short w) throws IOException {

        stream.write(w & 0xFF);
        stream.write((w >> 8) & 0xFF);
    }
}
