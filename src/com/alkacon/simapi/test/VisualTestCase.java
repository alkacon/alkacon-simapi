/*
 * The Apache Software License, Version 1.1
 * 
 * Copyright (c) 2000-2002 The Apache Software Foundation. All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 * 
 * 3. The end-user documentation included with the redistribution, if any, must
 * include the following acknowlegement: "This product includes software
 * developed by the Apache Software Foundation (http://www.apache.org/)."
 * Alternately, this acknowlegement may appear in the software itself, if and
 * wherever such third-party acknowlegements normally appear.
 * 
 * 4. The names "The Jakarta Project", "Ant", and "Apache Software Foundation"
 * must not be used to endorse or promote products derived from this software
 * without prior written permission. For written permission, please contact
 * apache@apache.org.
 * 
 * 5. Products derived from this software may not be called "Apache" nor may
 * "Apache" appear in their names without prior written permission of the Apache
 * Group.
 * 
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE APACHE
 * SOFTWARE FOUNDATION OR ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * ====================================================================
 * 
 * This software consists of voluntary contributions made by many individuals on
 * behalf of the Apache Software Foundation. For more information on the Apache
 * Software Foundation, please see <http://www.apache.org/>.
 */

package com.alkacon.simapi.test;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;

import junit.framework.TestCase;

/**
 * Visual test case allows to display an image after a test case has run, 
 * in order to manually confirm the image transformation works.<p>
 * 
 * @author Abey  
 */
public class VisualTestCase extends TestCase {

    /**
     * A simple panel to show an image.<p>
     */
    class ImagePanel extends JScrollPane {

        /** Required serial version UID. */
        private static final long serialVersionUID = 7240900865709777915L;

        /**
         * Public constructor.<p> 
         * 
         * @param img the image to thos
         * @param width the width of the panel
         * @param height the height of the panel
         */
        ImagePanel(Image img, int width, int height) {

            ImageIcon icon = new ImageIcon(img);
            this.setViewportView(new JLabel(icon));
            this.setPreferredSize(new Dimension(width, height));
        }
    }

    /**
     * Implements the "button press" action for the panel.<p>
     */
    class PerformAction implements Runnable {

        private ActionEvent event;

        private final Thread m_curr;

        private final JFrame m_jf;

        /**
         * Performs an action on the panel.<p>
         * 
         * @param jf the frame where the action occured
         * @param curr the current thread
         */
        PerformAction(JFrame jf, Thread curr) {

            super();
            m_jf = jf;
            m_curr = curr;
        }

        /**
         * Returns true if the user clicked "Yes", or false if the user clicked "No".<p>
         * 
         * @return true if the user clicked "Yes", or false if the user clicked "No"
         */
        public boolean isOk() {

            return !event.getActionCommand().equals("No");
        }

        public void run() {

            m_jf.dispose();
            m_curr.interrupt();
        }

        /**
         * Notifies the panel an event occured.<p>
         *  
         * @param evt the event that occured
         */
        public void setEvent(ActionEvent evt) {

            this.event = evt;
        }
    }

    /** Message to show on in the confirmation window. */
    String m_message;

    /**
     * Public constructor.<p>
     * 
     * @param params JUnit parameters
     */
    public VisualTestCase(String params) {

        super(params);
    }

    /**
     * Reads a file from the RFS and returns the file content.<p> 
     * 
     * @param file the file to read 
     * @return the read file content
     * 
     * @throws IOException in case of file access errors
     */
    public static byte[] readFile(File file) throws IOException {

        // create input and output stream
        FileInputStream in = new FileInputStream(file);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        // read the file content
        int c;
        while ((c = in.read()) != -1) {
            out.write(c);
        }

        in.close();
        out.close();

        return out.toByteArray();
    }

    /**
     * Displays all images in the given array and waits until the user has
     * pressed one of the confirmation buttons.<p>
     * 
     * @param img the images to show
     * @param message the message to display
     */
    public void checkImage(final BufferedImage img[], final String message) {

        final Thread curr = Thread.currentThread();
        m_message = message;
        final JFrame jf = new JFrame(message);
        jf.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        jf.getContentPane().add(new JLabel(message, SwingConstants.CENTER), BorderLayout.NORTH);
        final JPanel imgPanel = new JPanel();
        ImagePanel sp = null;

        for (int i = 0; i < img.length; i++) {
            if (img[i] == null)
                continue;
            int w = img[i].getWidth() + 50;
            int h = img[i].getHeight() + 50;
            sp = new ImagePanel(img[i], w, h);
            imgPanel.add(sp);
        }

        final JScrollPane js = new JScrollPane(imgPanel);
        jf.getContentPane().add(js, BorderLayout.CENTER);

        final PerformAction pa = new PerformAction(jf, curr);
        final Thread listenerThread = new Thread(pa);
        try {
            listenerThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        final class ConfirmListener implements ActionListener {

            public void actionPerformed(ActionEvent e) {

                pa.setEvent(e);
                listenerThread.start();
            }
        }

        final ActionListener cl = new ConfirmListener();
        JButton yes = new JButton("Yes");
        JButton no = new JButton("No");
        yes.addActionListener(cl);
        no.addActionListener(cl);

        final JPanel optionPanel = new JPanel();
        optionPanel.add(yes);
        optionPanel.add(no);
        jf.getContentPane().add(optionPanel, BorderLayout.SOUTH);

        jf.pack();
        jf.show();

        // wait until the user confirms the test
        boolean wait = true;
        while (wait) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // continue the thread
                wait = false;
            }
        }
        if (!pa.isOk()) {
            fail("Failed : " + m_message);
        }
    }
}