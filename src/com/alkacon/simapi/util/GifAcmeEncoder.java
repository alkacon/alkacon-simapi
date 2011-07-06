/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/util/GifAcmeEncoder.java,v $
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

package com.alkacon.simapi.util;

import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.io.DataOutput;
import java.io.IOException;

/**  
 * GifEncoder - writes out an image as a GIF.
 *
 * Transparency handling and variable bit size courtesy of Jack Palevich.
 *
 * Copyright (C) 1996 by Jef Poskanzer <jef@acme.com>.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Visit the ACME Labs Java page for up-to-date versions of this and other
 * fine Java utilities: http://www.acme.com/java/
 * 
 * Adapted from ppmtogif, which is based on GIFENCOD by David
 * Rowley <mgardi@watdscu.waterloo.edu>. Lempel-Zim compression
 * based on "compress".
 */
public class GifAcmeEncoder {

    private static final int BITS = 12;
    private static final int EOF = -1;
    private static final int HSIZE = 5003; // 80% occupancy
    private int a_count;
    private byte[] accum = new byte[256];
    private boolean clear_flg = false;
    private int ClearCode;
    private int[] codetab = new int[HSIZE];
    private int cur_accum = 0;
    private int cur_bits = 0;
    private int EOFCode;
    private int free_ent = 0; // first unused entry
    private int g_init_bits;

    // Adapted from ppmtogif, which is based on GIFENCOD by David
    // Rowley <mgardi@watdscu.waterloo.edu>.  Lempel-Zim compression
    // based on "compress".

    private int hsize = HSIZE; // for dynamic table sizing
    private int[] htab = new int[HSIZE];

    private boolean interlace = false;

    private int masks[] = {
        0x0000,
        0x0001,
        0x0003,
        0x0007,
        0x000F,
        0x001F,
        0x003F,
        0x007F,
        0x00FF,
        0x01FF,
        0x03FF,
        0x07FF,
        0x0FFF,
        0x1FFF,
        0x3FFF,
        0x7FFF,
        0xFFFF};
    private int maxbits = BITS; // user settable max # bits/code
    private int maxcode; // maximum code, given n_bits

    // General DEFINEs

    private int maxmaxcode = 1 << BITS; // should NEVER generate this code

    // GIF Image compression - modified 'compress'
    //
    // Based on: compress.c - File compression ala IEEE Computer, June 1984.
    //
    // By Authors:  Spencer W. Thomas      (decvax!harpo!utah-cs!utah-gr!thomas)
    //              Jim McKie              (decvax!mcvax!jim)
    //              Steve Davies           (decvax!vax135!petsd!peora!srd)
    //              Ken Turkowski          (decvax!decwrl!turtlevax!ken)
    //              James A. Woods         (decvax!ihnp4!ames!jaw)
    //              Joe Orost              (decvax!vax135!petsd!joe)

    private int n_bits; // number of bits/code
    private int numPixels;
    private int pixelIndex;
    private int[] pixels;

    private byte[] r, g, b; // the color look-up table

    private int transparentPixel = -1; // hpm
    private int width, height;

    /** 
     * Constructs a new GifEncoder using an 8-bit BufferedImage Image.<p>
     * 
     * The image color model needs to be of type {@link IndexColorModel}.<p>
     * 
     * @param img the image to encode as GIF
     */
    public GifAcmeEncoder(BufferedImage img) {

        width = img.getWidth(null);
        height = img.getHeight(null);
        
        if (! (img.getColorModel() instanceof IndexColorModel)) {
            throw new IllegalArgumentException("GIF Encoder: Image must be 8-bit");
        }
        
        pixels = img.getRaster().getPixels(0, 0, width, height, (int[])null);
        IndexColorModel icm = (IndexColorModel)img.getColorModel();
        transparentPixel = icm.getTransparentPixel();        
        int mapSize = icm.getMapSize();
        r = new byte[mapSize];
        g = new byte[mapSize];
        b = new byte[mapSize];
        icm.getReds(r);
        icm.getGreens(g);
        icm.getBlues(b);
        interlace = false;
        pixelIndex = 0;
        numPixels = width * height;
    }

    private static void writeString(DataOutput out, String str) throws IOException {

        out.write(str.getBytes());
    }

    // Algorithm:  use open addressing double hashing (no chaining) on the
    // prefix code / next character combination.  We do a variant of Knuth's
    // algorithm D (vol. 3, sec. 6.4) along with G. Knott's relatively-prime
    // secondary probe.  Here, the modular division first probe is gives way
    // to a faster exclusive-or manipulation.  Also do block compression with
    // an adaptive reset, whereby the code table is cleared when the compression
    // ratio decreases, but after the table fills.  The variable-length output
    // codes are re-sized at this point, and a special CLEAR code is generated
    // for the decompressor.  Late addition:  construct the table according to
    // file size for noticeable speed improvement on small files.  Please direct
    // questions about this implementation to ames!jaw.

    /** 
     * Saves the image as a GIF file to the given DataOutput.<p>
     * 
     * @param out the output stream to save the image at
     * 
     * @throws IOException in case the encoding fails
     */
    public void write(DataOutput out) throws IOException {

        // Figure out how many bits to use.
        int numColors = r.length;
        int BitsPerPixel;
        if (numColors <= 2)
            BitsPerPixel = 1;
        else if (numColors <= 4)
            BitsPerPixel = 2;
        else if (numColors <= 16)
            BitsPerPixel = 4;
        else BitsPerPixel = 8;

        int ColorMapSize = 1 << BitsPerPixel;
        byte[] reds = new byte[ColorMapSize];
        byte[] grns = new byte[ColorMapSize];
        byte[] blus = new byte[ColorMapSize];
        for (int i = 0; i < numColors; i++) {
            reds[i] = r[i];
            grns[i] = g[i];
            blus[i] = b[i];
        }

        GIFEncode(out, width, height, interlace, (byte)0, transparentPixel, BitsPerPixel, reds, grns, blus);
    }

    // Set up the 'byte output' routine
    private void char_init() {

        a_count = 0;
    }

    // output
    //
    // Output the given code.
    // Inputs:
    //      code:   A n_bits-bit integer.  If == -1, then EOF.  This assumes
    //              that n_bits =< wordsize - 1.
    // Outputs:
    //      Outputs code to the file.
    // Assumptions:
    //      Chars are 8 bits long.
    // Algorithm:
    //      Maintain a BITS character long buffer (so that 8 codes will
    // fit in it exactly).  Use the VAX insv instruction to insert each
    // code in turn.  When the buffer fills up empty it and start over.

    // Add a character to the end of the current packet, and if it is 254
    // characters, flush the packet to disk.
    private void char_out(byte c, DataOutput outs) throws IOException {

        accum[a_count++] = c;
        if (a_count >= 254)
            flush_char(outs);
    }

    // table clear for block compress
    private void cl_block(DataOutput outs) throws IOException {

        cl_hash(hsize);
        free_ent = ClearCode + 2;
        clear_flg = true;

        output(ClearCode, outs);
    }

    // reset code table
    private void cl_hash(int hxsize) {

        for (int i = 0; i < hxsize; ++i)
            htab[i] = -1;
    }

    private void compress(int init_bits, DataOutput outs) throws IOException {

        int fcode;
        int i /* = 0 */;
        int c;
        int ent;
        int disp;
        int hsize_reg;
        int hshift;

        // Set up the globals:  g_init_bits - initial number of bits
        g_init_bits = init_bits;

        // Set up the necessary values
        clear_flg = false;
        n_bits = g_init_bits;
        maxcode = MAXCODE(n_bits);

        ClearCode = 1 << (init_bits - 1);
        EOFCode = ClearCode + 1;
        free_ent = ClearCode + 2;

        char_init();

        ent = GIFNextPixel();

        hshift = 0;
        for (fcode = hsize; fcode < 65536; fcode *= 2)
            ++hshift;
        hshift = 8 - hshift; // set hash code range bound

        hsize_reg = hsize;
        cl_hash(hsize_reg); // clear hash table

        output(ClearCode, outs);

        outer_loop: while ((c = GIFNextPixel()) != EOF) {
            fcode = (c << maxbits) + ent;
            i = (c << hshift) ^ ent; // xor hashing

            if (htab[i] == fcode) {
                ent = codetab[i];
                continue;
            } else if (htab[i] >= 0) // non-empty slot
            {
                disp = hsize_reg - i; // secondary hash (after G. Knott)
                if (i == 0)
                    disp = 1;
                do {
                    if ((i -= disp) < 0)
                        i += hsize_reg;

                    if (htab[i] == fcode) {
                        ent = codetab[i];
                        continue outer_loop;
                    }
                } while (htab[i] >= 0);
            }
            output(ent, outs);
            ent = c;
            if (free_ent < maxmaxcode) {
                codetab[i] = free_ent++; // code -> hashtable
                htab[i] = fcode;
            } else cl_block(outs);
        }
        // Put out the final code.
        output(ent, outs);
        output(EOFCode, outs);
    }

    // Clear out the hash table

    // Flush the packet to disk, and reset the accumulator
    private void flush_char(DataOutput outs) throws IOException {

        if (a_count > 0) {
            outs.write(a_count);
            outs.write(accum, 0, a_count);
            a_count = 0;
        }
    }

    private void GIFEncode(
        DataOutput outs,
        int Width,
        int Height,
        boolean Interlace,
        byte Background,
        int Transparent,
        int BitsPerPixel,
        byte[] Red,
        byte[] Green,
        byte[] Blue) throws IOException {

        byte B;
        int LeftOfs, TopOfs;
        int ColorMapSize;
        int InitCodeSize;
        int i;

        ColorMapSize = 1 << BitsPerPixel;
        LeftOfs = TopOfs = 0;

        // The initial code size
        if (BitsPerPixel <= 1)
            InitCodeSize = 2;
        else InitCodeSize = BitsPerPixel;

        // Write the Magic header
        writeString(outs, "GIF89a");

        // Write out the screen width and height
        Putword(Width, outs);
        Putword(Height, outs);

        // Indicate that there is a global colour map
        B = (byte)0x80; // Yes, there is a color map
        // OR in the resolution
        B |= (byte)((8 - 1) << 4);
        // Not sorted
        // OR in the Bits per Pixel
        B |= (byte)((BitsPerPixel - 1));

        // Write it out
        Putbyte(B, outs);

        // Write out the Background colour
        Putbyte(Background, outs);

        // Pixel aspect ratio - 1:1.
        //Putbyte( (byte) 49, outs );
        // Java's GIF reader currently has a bug, if the aspect ratio byte is
        // not zero it throws an ImageFormatException.  It doesn't know that
        // 49 means a 1:1 aspect ratio.  Well, whatever, zero works with all
        // the other decoders I've tried so it probably doesn't hurt.
        Putbyte((byte)0, outs);

        // Write out the Global Colour Map
        for (i = 0; i < ColorMapSize; ++i) {
            Putbyte(Red[i], outs);
            Putbyte(Green[i], outs);
            Putbyte(Blue[i], outs);
        }

        // Write out extension for transparent colour index, if necessary.
        if (Transparent != -1) {
            Putbyte((byte)'!', outs);
            Putbyte((byte)0xf9, outs);
            Putbyte((byte)4, outs);
            Putbyte((byte)1, outs);
            Putbyte((byte)0, outs);
            Putbyte((byte)0, outs);
            Putbyte((byte)Transparent, outs);
            Putbyte((byte)0, outs);
        }

        // Write an Image separator
        Putbyte((byte)',', outs);

        // Write the Image header
        Putword(LeftOfs, outs);
        Putword(TopOfs, outs);
        Putword(Width, outs);
        Putword(Height, outs);

        // Write out whether or not the image is interlaced
        if (Interlace)
            Putbyte((byte)0x40, outs);
        else Putbyte((byte)0x00, outs);

        // Write out the initial code size
        Putbyte((byte)InitCodeSize, outs);

        // Go and actually compress the data
        compress(InitCodeSize + 1, outs);

        // Write out a Zero-length packet (to end the series)
        Putbyte((byte)0, outs);

        // Write the GIF file terminator
        Putbyte((byte)';', outs);
    }

    // GIF Specific routines

    // Return the next pixel from the image
    private int GIFNextPixel() {

        if (pixelIndex == numPixels)
            return EOF;
        else return pixels[pixelIndex++];
    }

    private final int MAXCODE(int nx_bits) {

        return (1 << nx_bits) - 1;
    }

    private void output(int code, DataOutput outs) throws IOException {

        cur_accum &= masks[cur_bits];

        if (cur_bits > 0)
            cur_accum |= (code << cur_bits);
        else cur_accum = code;

        cur_bits += n_bits;

        while (cur_bits >= 8) {
            char_out((byte)(cur_accum & 0xff), outs);
            cur_accum >>= 8;
            cur_bits -= 8;
        }

        // If the next entry is going to be too big for the code size,
        // then increase it, if possible.
        if (free_ent > maxcode || clear_flg) {
            if (clear_flg) {
                maxcode = MAXCODE(n_bits = g_init_bits);
                clear_flg = false;
            } else {
                ++n_bits;
                if (n_bits == maxbits)
                    maxcode = maxmaxcode;
                else maxcode = MAXCODE(n_bits);
            }
        }

        if (code == EOFCode) {
            // At EOF, write the rest of the buffer.
            while (cur_bits > 0) {
                char_out((byte)(cur_accum & 0xff), outs);
                cur_accum >>= 8;
                cur_bits -= 8;
            }

            flush_char(outs);
        }
    }

    // Write out a byte to the GIF file
    private void Putbyte(byte bo, DataOutput outs) throws IOException {

        outs.write(bo);
    }

    // Write out a word to the GIF file
    private void Putword(int w, DataOutput outs) throws IOException {

        Putbyte((byte)(w & 0xff), outs);
        Putbyte((byte)((w >> 8) & 0xff), outs);
    }
}