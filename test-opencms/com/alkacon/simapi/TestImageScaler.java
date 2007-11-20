/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/test-opencms/com/alkacon/simapi/TestImageScaler.java,v $
 * Date   : $Date: 2007/11/20 15:59:13 $
 * Version: $Revision: 1.1 $
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

import org.opencms.loader.CmsImageScaler;

import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Test class for the imaging operations.<p>
 */
public class TestImageScaler extends VisualTestCase {

    /**
     * Default JUnit constructor.<p>
     * 
     * @param params JUnit parameters
     */
    public TestImageScaler(String params) {

        super(params);
    }

    /**
     * Tests an issue where certain JPEG images have are reduced to a "black image" when scaling.<p>
     * 
     *  @throws Exception if the test fails
     */
    public void testBlackImageIssue() throws Exception {

        File input = new File(getClass().getResource("jugendliche.jpg").getPath());
        byte[] imgBytes = readFile(input);
        BufferedImage img1 = Simapi.read(imgBytes);

        CmsImageScaler scaler = new CmsImageScaler();
        scaler.parseParameters("w:200,h:200,t:1");
        byte[] scaled = scaler.scaleImage(imgBytes, "jugendliche.jpg");
        BufferedImage img2 = Simapi.read(scaled);

        checkImage(new BufferedImage[] {img1, img2}, "Is the 'black image' issue solved?");
    }

    /**
     * Tests an issue with a "missing line" when scaling certain pixel sizes.<p>
     * 
     * Because of inconsistent use of rounding, some images did contain a black or "missing" line
     * at the bottom when scaling to certain target sizes. 
     * 
     *  @throws Exception if the test fails
     */
    public void testMissingLineIssue() throws Exception {

        File input = new File(getClass().getResource("verm.gif").getPath());
        byte[] imgBytes = readFile(input);
        BufferedImage img1 = Simapi.read(imgBytes);

        CmsImageScaler scaler = new CmsImageScaler();
        scaler.parseParameters("w:100,h:100,t:3");
        byte[] scaled = scaler.scaleImage(imgBytes, "verm.gif");
        BufferedImage img2 = Simapi.read(scaled);

        checkImage(new BufferedImage[] {img1, img2}, "Is the 'missing line' issue solved?");
        assertEquals(100, img2.getWidth());
        assertEquals(98, img2.getHeight()); // aspect ratio kept intact
    }

    /**
     * Tests resizing a transparent image.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testScaleTransparentIssue() throws Exception {

        File input = new File(getClass().getResource("logo_alkacon_160_t.png").getPath());
        byte[] imgBytes = readFile(input);
        BufferedImage img1 = Simapi.read(imgBytes);

        CmsImageScaler scaler = new CmsImageScaler();
        scaler.parseParameters("w:160,h:90,t:4,q:85");
        byte[] scaled = scaler.scaleImage(imgBytes, "logo_alkacon_160_t.png");
        BufferedImage img2 = Simapi.read(scaled);

        checkImage(
            new BufferedImage[] {img1, img2},
            "Has it been scaled to 160x52 pixel and saved as PNG with transparent background ok?");
    }

    /**
     * Stops the test.<p>
     * 
     * Uncomment in case only a few selected tests should be performed.<p>
     */
    public void testStop() {

        System.exit(0);
    }
}