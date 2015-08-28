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

import java.awt.Color;
import java.awt.image.BufferedImage;

import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Test class for the imaging operations.<p>
 */
public class TestIdenticon extends VisualTestCase {

    /**
     * Default JUnit constructor.<p>
     *
     * @param params JUnit parameters
     */
    public TestIdenticon(String params) {

        super(params);
    }

    /**
     * Test suite for this test class.<p>
     *
     * @return the test suite
     */
    public static Test suite() {

        TestSuite suite = new TestSuite();
        suite.setName(TestIdenticon.class.getName());

        suite.addTest(new TestIdenticon("testIdenticonShapes"));
        suite.addTest(new TestIdenticon("test4x4IdenticonReservedColor"));
        suite.addTest(new TestIdenticon("test4x4Identicon"));
        suite.addTest(new TestIdenticon("test4x4IdenticonVariability"));

        TestSetup wrapper = new TestSetup(suite);
        return wrapper;
    }

    /**
     * Tests 4x4 identicon generation.<p>
     *
     *  @throws Exception if the test fails
     */
    public void test4x4Identicon() throws Exception {

        IdentIcon renderer = new IdentIcon();
        BufferedImage icon1 = renderer.render("root/admin");
        BufferedImage icon2 = renderer.render("myUserName");
        BufferedImage icon3 = renderer.render("someOtherNae123");
        BufferedImage icon4 = renderer.render("hans");
        BufferedImage icon5 = renderer.render("root/theNameOftheUSer");
        BufferedImage icon6 = renderer.render("lalalal");
        BufferedImage icon7 = renderer.render("u.man");
        BufferedImage icon8 = renderer.render("schakalaka");
        BufferedImage icon9 = renderer.render("sumse/mann");

        checkImage(
            new BufferedImage[] {icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9},
            "4x4 identicon generation ok?");
    }

    /**
     * Tests 4x4 identicon reserved color match.<p>
     *
     *  @throws Exception if the test fails
     */
    public void test4x4IdenticonReservedColor() throws Exception {

        IdentIcon renderer = new IdentIcon();
        renderer.setReservedColor(new Color(0xff, 0xa8, 0x26));
        BufferedImage icon1 = renderer.render("ia", true);
        BufferedImage icon2 = renderer.render("hb", 0xff, 0xc8, 0x46);
        BufferedImage icon3 = renderer.render("gc", 0xdf, 0xc8, 0x46);
        BufferedImage icon4 = renderer.render("fd", 0xcf, 0xc8, 0x46);
        BufferedImage icon5 = renderer.render("ee", 0xcf, 0xc8, 0x56);
        BufferedImage icon6 = renderer.render("df", 0xbf, 0xd8, 0x66);
        BufferedImage icon7 = renderer.render("cg", 0xc0, 0xd8, 0x66);
        BufferedImage icon8 = renderer.render("bh", 0xff, 0xa8, 0x86);
        BufferedImage icon9 = renderer.render("ai", 0xff, 0x48, 0x26);

        checkImage(
            new BufferedImage[] {icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9},
            "4x4 identicon reserved color check ok?");
    }

    /**
     * Tests 4x4 identicon generation vaiability.<p>
     *
     *  @throws Exception if the test fails
     */
    public void test4x4IdenticonVariability() throws Exception {

        IdentIcon renderer = new IdentIcon();
        renderer.setSize(80);
        renderer.setBackgroundColor(Color.LIGHT_GRAY);
        BufferedImage icon1 = renderer.render("a", 0xb3, 0x1b, 0x34);
        BufferedImage icon2 = renderer.render("b", 0x00, 0x30, 0x82);
        BufferedImage icon3 = renderer.render("c");
        BufferedImage icon4 = renderer.render("d");
        BufferedImage icon5 = renderer.render("e");
        BufferedImage icon6 = renderer.render("f");
        BufferedImage icon7 = renderer.render("g");
        BufferedImage icon8 = renderer.render("h");
        BufferedImage icon9 = renderer.render("i");

        checkImage(
            new BufferedImage[] {icon1, icon2, icon3, icon4, icon5, icon6, icon7, icon8, icon9},
            "4x4 identicon generation variability ok?");
    }

    /**
     * Tests identicon shapes.<p>
     *
     *  @throws Exception if the test fails
     */
    public void testIdenticonShapes() throws Exception {

        int shapes = 32;
        BufferedImage[] results = new BufferedImage[shapes];
        IdentIcon renderer = new IdentIcon();
        for (int i = 0; i < shapes; i++) {
            results[i] = renderer.drawShape(i);
        }

        checkImage(results, "Section shapes ok?");
    }
}