/*
 * File   : $Source: /alkacon/cvs/AlkaconSimapi/src/com/alkacon/simapi/test/Attic/TestSimapi.java,v $
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

package com.alkacon.simapi.test;

import com.alkacon.simapi.Simapi;
import com.alkacon.simapi.SimapiFactory;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Iterator;

import javax.imageio.ImageIO;

import junit.framework.Test;
import junit.framework.TestSuite;

//import com.drew.imaging.jpeg.JpegMetadataReader;
//import com.drew.metadata.Directory;
//import com.drew.metadata.Metadata;
//import com.drew.metadata.Tag;

/**
 * Test class for the imaging operations.<p>
 */
public class TestSimapi extends VisualTestCase {

    /**
     * Test suite for this test class.<p>
     * 
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestSimapi.class.getName());

        suite.addTest(new TestSimapi("testWriteJpegQuality"));
        suite.addTest(new TestSimapi("testResizeScaleFillSmall"));
        suite.addTest(new TestSimapi("testResizeScaleFill"));
        suite.addTest(new TestSimapi("testCrop"));
        suite.addTest(new TestSimapi("testResizeCrop"));
        suite.addTest(new TestSimapi("testWriteGif"));
        suite.addTest(new TestSimapi("testWriteJpegAndPng"));
        suite.addTest(new TestSimapi("testResizeScale"));
        suite.addTest(new TestSimapi("testRead"));

        return suite;
    }

    /**
     * Default JUnit constructor.<p>
     * 
     * @param params JUnit parameters
     */
    public TestSimapi(String params) {

        super(params);
    }


    /**
     * Tests writing an image as JPEG with different quality settings.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testWriteJpegQuality() throws Exception {
        
        Simapi imaging = SimapiFactory.getInstace();

        BufferedImage read;
        File input;
        File destination;
                
        input = new File(getClass().getResource("DSCN0754.JPG").getPath());

//        // Java standard metadata extraction - no Exif info is read from file
//        ImageInputStream stream = ImageIO.createImageInputStream(input);
//        Iterator iter = ImageIO.getImageReaders(stream);
//        ImageReader reader = (ImageReader)iter.next();                
//        ImageReadParam param = reader.getDefaultReadParam();
//        reader.setInput(stream, true, true);        
//        IIOMetadata meta = reader.getImageMetadata(0);
//        if (meta != null) {
//            String formats[] = meta.getMetadataFormatNames();
//            System.out.println("format count: " + formats.length);
//            for (int i=0; i<formats.length; i++) {
//                System.out.println("format " + i + ": " + formats[i]);
//                Node metaData = meta.getAsTree(formats[i]);
//                TransformerFactory tFactory = TransformerFactory.newInstance();
//                Transformer transformer = tFactory.newTransformer();
//                DOMSource source = new DOMSource(metaData);
//                StreamResult sr = new StreamResult(System.out);
//                transformer.transform(source, sr);
//            }            
//        } else {
//            System.out.println("meta is null");
//        }        
//        BufferedImage img0 = reader.read(0, param);
//        stream.close();
//        reader.dispose();
//      
        
//        Metadata metadata = JpegMetadataReader.readMetadata(input);
//        // iterate through metadata directories
//        Iterator directories = metadata.getDirectoryIterator();
//        while (directories.hasNext()) {
//            Directory directory = (Directory)directories.next();
//            // iterate through tags and print to System.out
//            Iterator tags = directory.getTagIterator();
//            while (tags.hasNext()) {
//                Tag tag = (Tag)tags.next();
//                // use Tag.toString()
//                System.out.println(tag);
//            }
//        }
        
        /* TODO: Writing of Exif information
         * 
         * Libary for Exif extraction AND writing (but only with Java 1.5):
         * http://mediachest.sourceforge.net/mediautil/
         */

        read = imaging.read(input);
        read = imaging.resize(read, 800, 600, false);
        checkImage(new BufferedImage[] {read}, "Has it been read?");
        
        input = new File(getClass().getResource("screen1.png").getPath());
        BufferedImage img1 = imaging.read(input);
        
        destination = new File(input.getParentFile(), "saved_q_low.jpg");
        imaging.write(img1, destination, "JPEG", 0.3f);
        read = imaging.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a _low_ quality version?");
        
        destination = new File(input.getParentFile(), "saved_q_high.jpg");
        imaging.write(img1, destination, "JPEG", 0.8f);
        read = imaging.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a _high_ quality version?");
    }
    
    /**
     * Tests resizing/scaling and filling a small image.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testResizeScaleFillSmall() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        File input;
        BufferedImage img1;
        BufferedImage result1;
        BufferedImage result2;
        BufferedImage result3;

        input = new File(getClass().getResource("opencms.gif").getPath());
        img1 = imaging.read(input);

        result1 = imaging.resize(img1, 300, 300, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
        result2 = imaging.resize(img1, 300, 300, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
        result3 = imaging.resize(img1, 300, 300, Color.GREEN, Simapi.POS_STRAIGHT_UP);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed down, right, up");

        input = new File(getClass().getResource("opencms.gif").getPath());
        img1 = imaging.read(input);

        result1 = imaging.resize(img1, 300, 300, Color.MAGENTA, Simapi.POS_CENTER);
        result2 = imaging.resize(img1, 300, 300, Color.YELLOW, Simapi.POS_DOWN_LEFT);
        result3 = imaging.resize(img1, 300, 300, Color.GREEN, Simapi.POS_UP_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed center, down left, up right");
    }

    /**
     * Tests resizing/scaling and filling an image.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testResizeScaleFill() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        File input;
        BufferedImage img1;
        BufferedImage result1;
        BufferedImage result2;
        BufferedImage result3;

        input = new File(getClass().getResource("screen1.png").getPath());
        img1 = imaging.read(input);

        result1 = imaging.resize(img1, 250, 250, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
        result2 = imaging.resize(img1, 350, 250, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
        result3 = imaging.resize(img1, 250, 250, Color.GREEN, Simapi.POS_STRAIGHT_UP);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed down, right, up");

        input = new File(getClass().getResource("opencms.gif").getPath());
        img1 = imaging.read(input);

        result1 = imaging.resize(img1, 150, 150, Color.MAGENTA, Simapi.POS_STRAIGHT_DOWN);
        result2 = imaging.resize(img1, 250, 50, Color.YELLOW, Simapi.POS_STRAIGHT_RIGHT);
        result3 = imaging.resize(img1, 150, 150, Color.GREEN, Simapi.POS_STRAIGHT_UP);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be scaled and placed down, right, up");
    }

    /**
     * Tests cropping and resizing an image.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testResizeCrop() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        File input = new File(getClass().getResource("opencms.gif").getPath());
        BufferedImage img1 = imaging.read(input);
        BufferedImage result1 = imaging.resize(img1, 200, 100, Simapi.POS_CENTER);
        BufferedImage result2 = imaging.resize(img1, 200, 100, Simapi.POS_STRAIGHT_LEFT);
        BufferedImage result3 = imaging.resize(img1, 200, 100, Simapi.POS_STRAIGHT_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be resized and cropped at center, left, right");

        input = new File(getClass().getResource("screen1.png").getPath());
        img1 = imaging.read(input);
        result1 = imaging.resize(img1, 400, 200, Simapi.POS_CENTER);
        result2 = imaging.resize(img1, 400, 200, Simapi.POS_STRAIGHT_UP);
        result3 = imaging.resize(img1, 400, 200, Simapi.POS_STRAIGHT_DOWN);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be resized and cropped at center, up, down");
    }

    /**
     * Tests cropping an image.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testCrop() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        File input;
        BufferedImage img1;
        BufferedImage result1;
        BufferedImage result2;
        BufferedImage result3;

        input = new File(getClass().getResource("screen1.png").getPath());
        img1 = imaging.read(input);

        result1 = imaging.crop(img1, 250, 250, Simapi.POS_STRAIGHT_LEFT);
        result2 = imaging.crop(img1, 250, 250, Simapi.POS_CENTER);
        result3 = imaging.crop(img1, 250, 250, Simapi.POS_STRAIGHT_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be cropped MIDDLE at left, center right");

        result1 = imaging.crop(img1, 250, 250, Simapi.POS_UP_LEFT);
        result2 = imaging.crop(img1, 250, 250, Simapi.POS_STRAIGHT_UP);
        result3 = imaging.crop(img1, 250, 250, Simapi.POS_UP_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be cropped UP at left, center, right");

        result1 = imaging.crop(img1, 250, 250, Simapi.POS_DOWN_LEFT);
        result2 = imaging.crop(img1, 250, 250, Simapi.POS_STRAIGHT_DOWN);
        result3 = imaging.crop(img1, 250, 250, Simapi.POS_DOWN_RIGHT);
        checkImage(
            new BufferedImage[] {result1, result2, result3},
            "Images should be cropped DOWN at left, center, right");
    }

    /**
     * Tests writing an image as GIF.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testWriteGif() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        ImageIO.scanForPlugins();
        Iterator gifWriters = ImageIO.getImageWritersByFormatName("GIF");
        if (!gifWriters.hasNext()) {
            fail("No Java ImageIO writers for the GIF format are available.");
        }

        BufferedImage result;
        BufferedImage read;
        File input;
        File destination;

        input = new File(getClass().getResource("opencms.gif").getPath());
        BufferedImage img1 = imaging.read(input);
        result = imaging.resize(img1, 300, 150, true, true);

        destination = new File(input.getParentFile(), "saved3.gif");
        System.out.println(destination.getAbsolutePath());
        imaging.write(result, destination, "GIF");
        read = imaging.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as GIF in a scaled version?");
    }

    /**
     * Tests writing an image as JPEG and PNG.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testWriteJpegAndPng() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        BufferedImage result;
        BufferedImage read;
        File input;
        File destination;

        input = new File(getClass().getResource("opencms.gif").getPath());
        BufferedImage img1 = imaging.read(input);
        result = imaging.resize(img1, 300, 150, true, true);
        destination = new File(input.getParentFile(), "saved1.jpg");
        System.out.println(destination.getAbsolutePath());
        imaging.write(result, destination, "JPEG");
        read = imaging.read(destination);
        checkImage(new BufferedImage[] {img1, read}, "Has it been written to disk as JPEG in a scaled version?");
        
        input = new File(getClass().getResource("screen1.png").getPath());
        BufferedImage img2 = imaging.read(input);
        result = imaging.scale(img2, 0.8f);
        destination = new File(input.getParentFile(), "saved2.png");
        System.out.println(destination.getAbsolutePath());
        imaging.write(result, destination, "PNG");
        read = imaging.read(destination);
        checkImage(new BufferedImage[] {img2, read}, "Has it been written to disk as PNG in a scaled version?");
    }
    
    /**
     * Tests resizing and scaling an image.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testResizeScale() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        BufferedImage result;

        BufferedImage img1 = imaging.read(getClass().getResource("screen1.png"));
        result = imaging.scale(img1, 0.75f);
        checkImage(new BufferedImage[] {img1, result}, "Has it been scaled to 75%?");

        BufferedImage img2 = imaging.read(getClass().getResource("opencms_text.jpg"));
        result = imaging.resize(img2, 100, 50);
        checkImage(new BufferedImage[] {img2, result}, "Has it been resized to 100x50 pixel?");

        BufferedImage img3 = imaging.read(getClass().getResource("alkacon.png"));
        result = imaging.resize(img3, 400, 300, true);

        checkImage(new BufferedImage[] {img3, result}, "Has it been resized to 400x300 pixel with aspect intact?");
    }

    /**
     * Tests reading an image.<p>
     * 
     * @throws Exception if the test fails
     */
    public void testRead() throws Exception {

        Simapi imaging = SimapiFactory.getInstace();

        BufferedImage img1 = imaging.read(getClass().getResource("alkacon.png"));
        BufferedImage img2 = imaging.read(getClass().getResource("opencms_text.jpg"));
        BufferedImage img3 = imaging.read(getClass().getResource("opencms.gif"));

        checkImage(new BufferedImage[] {img1, img2, img3}, "Do you see 3 images?");
    }
}