/*
 * Copyright (c) Alkacon Software GmbH (http://www.alkacon.com)
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

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

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
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestImageScaler.class.getName());

        suite.addTest(new TestImageScaler("testBlackImageIssue"));
        suite.addTest(new TestImageScaler("testMissingLineIssue"));
        suite.addTest(new TestImageScaler("testImageCroppingFromScaler"));
        suite.addTest(new TestImageScaler("testScaleTransparentIssue"));

        TestSetup wrapper = new TestSetup(suite);
        return wrapper;
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
     * Tests image cropping using an OpenCms image scaler instance.<p>
     * 
     *  @throws Exception if the test fails
     */
    public void testImageCroppingFromScaler() throws Exception {

        File input = new File(getClass().getResource("screen_1024.png").getPath());
        byte[] imgBytes = readFile(input);

        CmsImageScaler scaler = new CmsImageScaler();
        byte[] scaled;
        BufferedImage result;

        scaler.parseParameters("cx:10,cy:10,cw:400,ch:400");
        scaled = scaler.scaleImage(imgBytes, "/dummy.png");
        result = Simapi.read(scaled);
        checkImage(new BufferedImage[] {result}, "Has it been cropped using the scaler?");

        // this use case is not supported since the scaler does not work with negative x,y positions
        //        scaler.parseParameters("cx:0,cy:-100,cw:1250,ch:400");
        //        scaled = scaler.scaleImage(imgBytes, "/dummy.png");
        //        result = Simapi.read(scaled);
        //        checkImage(new BufferedImage[] {result}, "Has it been cropped (oversized) using the scaler?");

        scaler.parseParameters("cx:0,cy:0,cw:160,ch:120,w:800,h:600");
        scaled = scaler.scaleImage(imgBytes, "/dummy.png");
        result = Simapi.read(scaled);
        checkImage(new BufferedImage[] {result}, "Has it been cropped and resized (enlarged) using the scaler?");

        scaler.parseParameters("cx:0,cy:0,cw:400,ch:300,w:160,h:120");
        scaled = scaler.scaleImage(imgBytes, "/dummy.png");
        result = Simapi.read(scaled);
        checkImage(new BufferedImage[] {result}, "Has it been cropped and resized (reduced) using the scaler?");

        input = new File(getClass().getResource("logo_alkacon_150_t.gif").getPath());
        imgBytes = readFile(input);

        scaler.parseParameters("cx:0,cy:0,cw:100,ch:100,w:200,h:200,c:transparent");
        scaled = scaler.scaleImage(imgBytes, "/dummy.png");
        result = Simapi.read(scaled);
        checkImage(new BufferedImage[] {result}, "Has it been cropped with transparent color intact using the scaler?");

        scaler.parseParameters("cx:0,cy:0,cw:100,ch:100,w:200,h:200,c:#ff0000");
        scaled = scaler.scaleImage(imgBytes, "/dummy.png");
        result = Simapi.read(scaled);
        checkImage(new BufferedImage[] {result}, "Has it been cropped with red bg color using the scaler?");

        scaler.parseParameters("h:500,w:1000,cx:21,cy:15,ch:23,cw:1050,c:#00ff00");
        scaled = scaler.scaleImage(imgBytes, "/dummy.png");
        result = Simapi.read(scaled);
        checkImage(new BufferedImage[] {result}, "Has it been cropped with red bg color using the scaler?");
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
}