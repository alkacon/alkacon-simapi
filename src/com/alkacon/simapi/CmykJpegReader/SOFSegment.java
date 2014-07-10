/*
 * Copyright (c) 2013, Harald Kuhr
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *     * Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above copyright
 *       notice, this list of conditions and the following disclaimer in the
 *       documentation and/or other materials provided with the distribution.
 *     * Neither the name "TwelveMonkeys" nor the
 *       names of its contributors may be used to endorse or promote products
 *       derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.alkacon.simapi.CmykJpegReader;

import java.util.Arrays;

/**
* SOFSegment
*
* @author <a href="mailto:harald.kuhr@gmail.com">Harald Kuhr</a>
* @author last modified by $Author: haraldk$
* @version $Id: SOFSegment.java,v 1.0 22.04.13 16:40 haraldk Exp$
*/
final class SOFSegment {
    final int marker;
    final int samplePrecision;
    final int lines;          // height
    final int samplesPerLine; // width
    final SOFComponent[] components;

    SOFSegment(int marker, int samplePrecision, int lines, int samplesPerLine, SOFComponent[] components) {
        this.marker = marker;
        this.samplePrecision = samplePrecision;
        this.lines = lines;
        this.samplesPerLine = samplesPerLine;
        this.components = components;
    }

    final int componentsInFrame() {
        return components.length;
    }

    @Override
    public String toString() {
        return String.format(
                "SOF%d[%04x, precision: %d, lines: %d, samples/line: %d, components: %s]",
                marker & 0xff - 0xc0, marker, samplePrecision, lines, samplesPerLine, Arrays.toString(components)
        );
    }
}
