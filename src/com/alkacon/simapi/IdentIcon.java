
package com.alkacon.simapi;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Renders an identity icon calculated from the given initialization String.<p>
 *
 * Default size of the icon is 32x32 pixel.
 * The icons are generated from a grid of 4x4 patches.<p>
 *
 * This is intended to be used for generating individual icons for uses bases on their
 * unique user name / OU combination.
 *
 * Based on the original Identicon implementation of Don Park,
 * see <a href="https://github.com/donpark/identicon">https://github.com/donpark/identicon</a>.<p>
 *
 * Original copyright notice:
 * <pre>
 * Copyright (c) 2007-2012 Don Park &lt;donpark@docuverse.com&gt;
 * </pre>
 */
public class IdentIcon {

    /** Constant to identify a transparent background fill color. */
    public static final Color COLOR_TRANSPARENT = new Color(255, 255, 255, 0);

    /** Size of the individual patch when drawing. */
    private static final float DEFAULT_PATCH_SIZE = 16.0f;

    /** Salt String for generation better hashes. */
    public static final String IDENTICON_SALT = "(§!$/%.&#?@-_)";

    /**
     * Grid size of the patches is 5x5.
     *
     * Each patch is a polygon created from a list of vertices on a 5 by 5 grid.
     * Vertices are numbered from 0 to 24, starting from top-left corner of the
     * grid, moving left to right and top to bottom.
     */
    private static final int PATCH_GRIDS = 5;

    /** Flag to indicate if a specific patch is inverted by default. */
    private static final byte PATCH_INVERTED = 1;

    /** Flag to indicate a move to operation while drawing a patch. */
    private static final int PATCH_MOVETO = -1;

    /** Patch types that can be used in the center. */
    private static final int PATCH_TYPES_CENTER[] = {0, 4, 8, 15, 1, 5, 7, 21, 23, 24, 25, 31, 6, 12, 11, 30};

    /** Patch shape definition. */
    private static final byte[] patch00 = {0, 4, 24, 20};

    /** Patch shape definition. */
    private static final byte[] patch01 = {0, 4, 20};

    /** Patch shape definition. */
    private static final byte[] patch02 = {2, 24, 20};

    /** Patch shape definition. */
    private static final byte[] patch03 = {0, 2, 20, 22};

    /** Patch shape definition. */
    private static final byte[] patch04 = {2, 14, 22, 10};

    /** Patch shape definition. */
    private static final byte[] patch05 = {0, 14, 24, 22};

    /** Patch shape definition. */
    private static final byte[] patch06 = {2, 24, 22, 13, 11, 22, 20};

    /** Patch shape definition. */
    private static final byte[] patch07 = {0, 14, 22};

    /** Patch shape definition. */
    private static final byte[] patch08 = {6, 8, 18, 16};

    /** Patch shape definition. */
    private static final byte[] patch09 = {4, 20, 10, 12, 2};

    /** Patch shape definition. */
    private static final byte[] patch10 = {0, 2, 12, 10};

    /** Patch shape definition. */
    private static final byte[] patch11 = {10, 14, 22};

    /** Patch shape definition. */
    private static final byte[] patch12 = {20, 12, 24};

    /** Patch shape definition. */
    private static final byte[] patch13 = {10, 2, 12};

    /** Patch shape definition. */
    private static final byte[] patch14 = {0, 2, 10};

    /** Patch shape definition. */
    private static final byte[] patch15 = {0, 4, 10};

    /** Patch shape definition. */
    private static final byte[] patch16 = {20, 24, 10};

    /** Patch shape definition. */
    private static final byte[] patch17 = {0, 20, 3};

    /** Patch shape definition. */
    private static final byte[] patch18 = {1, 4, 24};

    /** Patch shape definition. */
    private static final byte[] patch19 = {0, 1, 14, 21, 20};

    /** Patch shape definition. */
    private static final byte[] patch20 = {10, 0, 2, 22, 24, 14};

    /** Patch shape definition. */
    private static final byte[] patch21 = {5, 9, 22};

    /** Patch shape definition. */
    private static final byte[] patch22 = {10, 2, 22, 14};

    /** Patch shape definition. */
    private static final byte[] patch23 = {0, 20, 4, 24};

    /** Patch shape definition. */
    private static final byte[] patch24 = {0, 7, 4, 13, 24, 17, 20, 11};

    /** Patch shape definition. */
    private static final byte[] patch25 = {0, 3, 24};

    /** Patch shape definition. */
    private static final byte[] patch26 = {0, 15, 24};

    /** Patch shape definition. */
    private static final byte[] patch27 = {0, 2, 19};

    /** Patch shape definition. */
    private static final byte[] patch28 = {0, 10, 23};

    /** Patch shape definition. */
    private static final byte[] patch29 = {0, 2, 18, 10};

    /** Patch shape definition. */
    private static final byte[] patch30 = {0, 8, 16};

    /** Flags for patches */
    private static final byte PATCH_FLAGS[] = {
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        PATCH_INVERTED,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0};

    /** All available patch types in an array */
    private static final byte[] PATCH_TYPES[] = {
        patch00,
        patch01,
        patch02,
        patch03,
        patch04,
        patch05,
        patch06,
        patch07,
        patch08,
        patch09,
        patch10,
        patch11,
        patch12,
        patch13,
        patch14,
        patch00, // inverted square, equals empty space
        patch15,
        patch16,
        patch17,
        patch18,
        patch19,
        patch20,
        patch21,
        patch22,
        patch23,
        patch24,
        patch25,
        patch26,
        patch27,
        patch28,
        patch29,
        patch30};

    /** Background color of the patches. */
    private Color m_backgroundColor;

    /** Digester to use for generating hashes. */
    private MessageDigest m_digest;

    /** Reserved color for patches, if patch color is to close to this color the opposite color is used. */
    private Color m_reservedColor;

    /** Offset for patch drawing. */
    private float m_patchOffset;

    /** The calculated patch shapes / polygons. */
    private GeneralPath[] m_patchShapes;

    /** Size of the individual patch, default is 16. */
    private float m_patchSize;

    /** Target size for the rendered patch, default is 32. */
    private int m_size;

    /**
     * Constructor.
     */
    public IdentIcon() {

        setPatchSize(DEFAULT_PATCH_SIZE);
        setBackgroundColor(COLOR_TRANSPARENT);
        setSize(32);
        setReservedColor(null);

        try {
            m_digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            // should better not happen
            e.printStackTrace();
        }
    }

    /**
     * Returns the background color of the IdentIcon.<p>
     *
     * @return the background color of the IdentIcon
     */
    public Color getBackgroundColor() {

        return m_backgroundColor;
    }

    /**
     * Returns the size in pixels at which each patch will be rendered before
     * they are scaled down to requested IdentIcon size.<p>
     *
     * @return the size in pixels at which each patch will be rendered
     */
    public float getPatchSize() {

        return m_patchSize;
    }

    /**
     * Returns the reserved color for the IdentIcon renderer.<p>
     *
     * The default is <code>null</code> which disables the reserved color feature.<p>
     *
     * The reserved color can be set to make sure a certain color
     * is only used for specific input Strings. A practical example would be
     * "draw all admin users in orange, but no other users". In case the color
     * calculated for the user name would be to close to the reserved color,
     * the opposite color from the spectrum is used instead.
     *
     * @return the reserved color for the IdentIcon renderer
     */
    public Color getReservedColor() {

        return m_reservedColor;
    }

    /**
     * Returns the target image size (height, width) for the rendered patch, default is 32.<p>
     *
     * @return the target image size (height, width) for the rendered patch
     */
    public int getSize() {

        return m_size;
    }

    /**
     * Renders the IdentIcon for the given input String.<p>
     *
     * The protected color in this case is NOT allowed to be used for the generated icon.<p>
     *
     * @param input the input String to render the IdentIcon for
     *
     * @return the IdentIcon for the given input String
     */
    public BufferedImage render(String input) {

        return render(input, false, getSize());
    }

    /**
     * Renders the IdentIcon for the given input String.<p>
     *
     * @param input the input String to render the IdentIcon for
     * @param allowProtected controls if the reserves color set by {@link #getReservedColor()} can be used or not
     *
     * @return the IdentIcon for the given input String
     */
    public BufferedImage render(String input, boolean allowProtected) {

        return render(input, allowProtected, getSize());
    }

    /**
     * Renders the IdentIcon for the given input String with the given size.<p>
     *
     * Good values for the size parameter should set in dependency with the patch size set with
     * {@link #getPatchSize()}. The largest size should be the patch size multiplied by four.<p>
     *
     * @param input the input String to render the IdentIcon for
     * @param allowProtected controls if the reserves color set by {@link #getReservedColor()} can be used or not
     * @param size the target size of the output image
     *
     * @return the IdentIcon for the given input String
     */
    public BufferedImage render(String input, boolean allowProtected, int size) {

        byte[] hash = hash(input);
        hash[6] = allowProtected ? (byte)1 : (byte)0;

        return renderIcon(hash, size);
    }

    /**
     * Renders the IdentIcon for the given input String.<p>
     *
     * Good values for the size parameter should set in dependency with the patch size set with
     * {@link #getPatchSize()}. The largest size should be the patch size multiplied by four.<p>
     *
     * The protected color in this case is NOT allowed to be used for the generated icon.<p>
     *
     * @param input the input String to render the IdentIcon for
     * @param size the target size of the output image
     *
     * @return the IdentIcon for the given input String
     */
    public BufferedImage render(String input, int size) {

        return render(input, false, size);
    }

    /**
     * Renders the IdentIcon for the given input String in the given color.<p>
     *
     * The protected color in this case is NOT allowed to be used for the generated icon,
     * as this method exists mostly for test purposes.<p>
     *
     * @param input the input String to render the IdentIcon for
     * @param red the red color component
     * @param green the green color component
     * @param blue the blue color component
     *
     * @return the IdentIcon for the given input String
     */
    public BufferedImage render(String input, int red, int green, int blue) {

        byte[] hash = hash(input);
        hash[2] = (byte)blue;
        hash[3] = (byte)green;
        hash[4] = (byte)red;

        return renderIcon(hash, getSize());
    }

    /**
     * The background color to be used for the IdentIcon.<p>
     *
     * @param backgroundColor the background color to set
     */
    public void setBackgroundColor(Color backgroundColor) {

        this.m_backgroundColor = backgroundColor;
    }

    /**
     * Set the size in pixels at which each patch will be rendered before they
     * are scaled down to requested identicon size.<p>
     *
     * Default size is 16 pixels which means, for 16-block identicon,
     * a 64x64 image will be rendered and scaled down.
     *
     * @param size patch size in pixels
     */
    public void setPatchSize(float size) {

        this.m_patchSize = size;
        this.m_patchOffset = m_patchSize / 2.0f; // used to center patch shape at
        float patchScale = m_patchSize / 4.0f;
        // origin.
        this.m_patchShapes = new GeneralPath[PATCH_TYPES.length];
        for (int i = 0; i < PATCH_TYPES.length; i++) {
            GeneralPath patch = new GeneralPath(Path2D.WIND_NON_ZERO);
            boolean moveTo = true;
            byte[] patchVertices = PATCH_TYPES[i];
            for (int j = 0; j < patchVertices.length; j++) {
                int v = patchVertices[j];
                if (v == PATCH_MOVETO) {
                    moveTo = true;
                }
                float vx = ((v % PATCH_GRIDS) * patchScale) - m_patchOffset;
                float vy = (((float)Math.floor(((float)v) / PATCH_GRIDS)) * patchScale) - m_patchOffset;
                if (!moveTo) {
                    patch.lineTo(vx, vy);
                } else {
                    moveTo = false;
                    patch.moveTo(vx, vy);
                }
            }
            patch.closePath();
            this.m_patchShapes[i] = patch;
        }
    }

    /**
     * Returns the reserved color for the IdentIcon renderer.<p>
     *
     * The default is <code>null</code> which disables the reserved color feature.<p>
     *
     * The reserved color can be set to make sure a certain color
     * is only used for specific input Strings. A practical example would be
     * "draw all admin users in orange, but no other users". In case the color
     * calculated for the user name would be to close to the reserved color,
     * the opposite color from the spectrum is used instead.
     *
     * @param reservedColor the reserved color for the IdentIcon renderer to set
     */
    public void setReservedColor(Color reservedColor) {

        m_reservedColor = reservedColor;
    }

    /**
     * Sets the target image size (height, width) for the rendered patch, default is 32.<p>
     *
     * @param size the target image size to set
     */
    public void setSize(int size) {

        m_size = size;
    }

    /**
     * @param g the graphic to draw on
     * @param x x position
     * @param y y position
     * @param size size of the tile
     * @param patch patch type to draw
     * @param turn turn of the patch
     * @param invert invert color or not
     * @param fillColor fill color
     * @param strokeColor stroke color
     */
    protected void drawPatch(
        Graphics2D g,
        float x,
        float y,
        float size,
        int patch,
        int turn,
        boolean invert,
        Color fillColor,
        Color strokeColor) {

        assert patch >= 0;
        assert turn >= 0;
        patch %= PATCH_TYPES.length;
        turn %= 4;
        if ((PATCH_FLAGS[patch] & PATCH_INVERTED) != 0) {
            invert = !invert;
        }

        Shape shape = m_patchShapes[patch];
        double scale = ((double)size) / ((double)m_patchSize);
        float offset = size / 2.0f;

        // paint background
        g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC));
        g.setColor(invert ? fillColor : m_backgroundColor);
        g.fill(new Rectangle2D.Float(x, y, size, size));

        AffineTransform savet = g.getTransform();
        g.translate(x + offset, y + offset);
        g.scale(scale, scale);
        g.rotate(Math.toRadians(turn * 90));

        // if stroke color was specified, apply stroke
        // stroke color should be specified if fore color is too close to the
        // back color.
        if (strokeColor != null) {
            g.setColor(strokeColor);
            g.draw(shape);
        }

        // render rotated patch using fore color (back color if inverted)
        g.setColor(invert ? m_backgroundColor : fillColor);
        g.fill(shape);

        g.setTransform(savet);
    }

    /**
     * Method to draw a single shape, useful for visual testing the shape output.<p>
     *
     * @param shape the shape to draw
     *
     * @return a buffered image of the shape
     */
    protected BufferedImage drawShape(int shape) {

        int size = 60;
        Color fill = new Color(0xb3, 0x1b, 0x34);
        Color stroke = null;

        BufferedImage targetImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = targetImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setBackground(getBackgroundColor());
        g.clearRect(0, 0, size, size);

        drawPatch(g, 0, 0, size, shape, 0, false, fill, stroke);

        g.dispose();

        return targetImage;
    }

    /**
     * Returns the distance between two colors.
     *
     * @param c1 the first color
     * @param c2 the second color
     *
     * @return the distance between the two colors
     */
    protected float getColorDistance(Color c1, Color c2) {

        float dx = c1.getRed() - c2.getRed();
        float dy = c1.getGreen() - c2.getGreen();
        float dz = c1.getBlue() - c2.getBlue();
        return (float)Math.sqrt((dx * dx) + (dy * dy) + (dz * dz));
    }

    /**
     * Returns the complementary color for the given color.<p>
     *
     * @param color the base to calculate the complementary color for
     *
     * @return  the complementary color
     */
    protected Color getComplementaryColor(Color color) {

        return new Color(color.getRGB() ^ 0x00FFFFFF);
    }

    /**
     * Generates an MD5 hash array from the given input String.<p>
     *
     * The {@link #IDENTICON_SALT} is added to the String for better results.<p>
     *
     * @param input the input to generate the hash for
     *
     * @return an MD5 hash array from the given input String
     */
    protected byte[] hash(String input) {

        byte[] hash = null;
        try {
            hash = m_digest.digest((IDENTICON_SALT + input + IDENTICON_SALT).getBytes("UTF-8"));
            hash[6] = 0; // used for allowing reserved color later, default is 0 = false
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(System.err);
        }

        return hash;
    }

    /**
     * Renders the IdentIcon based on the input hash, using the internal parameters.<p>
     *
     * @param hash the hash to render
     * @param size the size of the image to generate
     *
     * @return the IdentIcon based on the input hash
     */
    protected BufferedImage renderIcon(byte[] hash, int size) {

        // -------------------------------------------------
        // PREPARE
        //

        int hash0 = 0xff & hash[0];
        int hash1 = 0xff & hash[1];
        int hash2 = 0xff & hash[5];

        int blue = 0xff & hash[2];
        int green = 0xff & hash[3];
        int red = 0xff & hash[4];

        boolean allowProtected = hash[6] > 0 ? true : false;

        // int middleType = hash0 & 0x1f;
        int middleType = PATCH_TYPES_CENTER[hash0 & 0xf];
        boolean middleInvert = ((hash0 >> 5) & 0x1) != 0;
        int middleTurn = 0; // ((hash0 >> 6) & 0x3);

        int cornerType = hash1 & 0x1f;
        boolean cornerInvert = ((hash1 >> 5) & 0x1) != 0;
        int cornerTurn = ((hash1 >> 6) & 0x3);

        int sideType = hash2 & 0x01f;
        boolean sideInvert = ((hash2 >> 5) & 0x1) != 0;
        int sideTurn = ((hash2 >> 6) & 0x3);

        // color components are used at top of the range for color difference
        Color fill = new Color(red, green, blue);

        Color middleColor = fill;
        if (m_reservedColor != null) {
            if (!allowProtected) {
                float distance = getColorDistance(fill, m_reservedColor);
                if (distance < 96.0f) {
                    fill = getComplementaryColor(fill);
                    middleColor = fill;
                }
            } else {
                middleColor = m_reservedColor;
                fill = middleColor.brighter();
                sideInvert = false;
                cornerInvert = false;
            }
        }

        // outline shapes with a noticeable color (complementary will do) if
        // shape color and background color are too similar (measured by color
        // distance).
        Color stroke = null;
        if (getColorDistance(fill, getBackgroundColor()) < 32.0f) {
            stroke = getComplementaryColor(fill);
        }

        // -------------------------------------------------
        // RENDER
        //

        BufferedImage targetImage = new BufferedImage(size, size, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = targetImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g.setBackground(getBackgroundColor());
        g.clearRect(0, 0, size, size);

        float s = size / 4.0f;
        float bs1 = s;
        float bs2 = s * 2.0f;
        float bs3 = s * 3.0f;

        // middle patches
        drawPatch(g, bs1, bs1, s, middleType, middleTurn++, middleInvert, middleColor, stroke);
        drawPatch(g, bs2, bs1, s, middleType, middleTurn++, middleInvert, middleColor, stroke);
        drawPatch(g, bs2, bs2, s, middleType, middleTurn++, middleInvert, middleColor, stroke);
        drawPatch(g, bs1, bs2, s, middleType, middleTurn++, middleInvert, middleColor, stroke);

        // side patches, starting from top and moving clock-wise
        Color sideColor = sideInvert ? fill.darker() : fill;
        sideInvert = false;
        drawPatch(g, bs1, 0, s, sideType, sideTurn++, sideInvert, sideColor, stroke);
        drawPatch(g, bs2, 0, s, sideType, sideTurn, sideInvert, sideColor, stroke);
        drawPatch(g, bs3, bs1, s, sideType, sideTurn++, sideInvert, sideColor, stroke);
        drawPatch(g, bs3, bs2, s, sideType, sideTurn, sideInvert, sideColor, stroke);

        drawPatch(g, bs2, bs3, s, sideType, sideTurn++, sideInvert, sideColor, stroke);
        drawPatch(g, bs1, bs3, s, sideType, sideTurn, sideInvert, sideColor, stroke);
        drawPatch(g, 0, bs2, s, sideType, sideTurn++, sideInvert, sideColor, stroke);
        drawPatch(g, 0, bs1, s, sideType, sideTurn, sideInvert, sideColor, stroke);

        // corner patches, starting from top left and moving clock-wise
        Color cornerColor = cornerInvert ? fill.brighter() : fill;
        cornerInvert = false;
        drawPatch(g, 0, 0, s, cornerType, cornerTurn++, cornerInvert, cornerColor, stroke);
        drawPatch(g, bs3, 0, s, cornerType, cornerTurn++, cornerInvert, cornerColor, stroke);
        drawPatch(g, bs3, bs3, bs1, cornerType, cornerTurn++, cornerInvert, cornerColor, stroke);
        drawPatch(g, 0, bs3, s, cornerType, cornerTurn++, cornerInvert, cornerColor, stroke);

        g.dispose();

        return targetImage;
    }
}
