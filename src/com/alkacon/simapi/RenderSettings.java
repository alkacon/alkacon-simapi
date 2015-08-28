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
import java.awt.RenderingHints;
import java.awt.image.ImageFilter;
import java.util.ArrayList;
import java.util.List;

/**
 * Provides rendering hints of different quality for the image processing.<p>
 *
 * @author Alexander Kandzior
 */
public class RenderSettings {

    /** Rendering hints for the <code>MEDIUM</code> render settings. */
    protected static final RenderingHints HINTS_MEDIUM = initHints(Simapi.RENDER_MEDIUM);

    /** Rendering hints for the default <code>QUALITY</code> render settings. */
    protected static final RenderingHints HINTS_QUALITY = initHints(Simapi.RENDER_QUALITY);

    /** Rendering hints for the <code>HINTS_QUALITY_BICUBIC</code> render settings. */
    protected static final RenderingHints HINTS_QUALITY_BICUBIC = initHints(Simapi.RENDER_QUALITY_BICUBIC);

    /** Rendering hints for the <code>HINTS_QUALITY_SOFT</code> render settings. */
    protected static final RenderingHints HINTS_QUALITY_SOFT = initHints(Simapi.RENDER_QUALITY_SOFT);

    /** Rendering hints for the <code>SPEED</code> render settings. */
    protected static final RenderingHints HINTS_SPEED = initHints(Simapi.RENDER_SPEED);

    /**
     * Maximum height and width of an image to use blur filtering before scaling down.<p>
     *
     * Since the blur operation is quite expensive in memory, this should not be used
     * for very large images. This sets the threshold.<p>
     */
    protected static final int MAX_BLUR_SIZE = 3000;

    /** The image save quality, used for JPEG images (and other formats that support such a setting). */
    private float m_compressionQuality;

    /** The rendering hints of this settings object. */
    private RenderingHints m_hints;

    /** The internal list of image filters to apply to the image. */
    private List<ImageFilter> m_imageFilters;

    /** Used to control if blur is applied when scaling down an image. */
    private boolean m_isUseBlur;

    /** The maxmimum image size to apply blur-before-scale (to avoid "out of memory" issues). */
    private int m_maximumBlurSize;

    /** Thread priority for image operations. */
    private int m_threadNicePriority;

    /** Stored old thread priority. */
    private int m_threadOldPriority;

    /** The backgound color replacement for the transparent color, used if transparency is not supported by the selected image format. */
    private Color m_transparentReplaceColor;

    /** Indicates the blur factor to use when scaling down. */
    private double m_blurFactor;

    /**
     * Create a new set of render settings, based on the given constant base mode.<p>
     *
     * @param baseMode the base mode of the settings, for example {@link Simapi#RENDER_QUALITY}
     */
    public RenderSettings(int baseMode) {

        this(baseMode, null);
    }

    /**
     * Create a new set of render settings, based on the given constant base mode and the provided
     * special rendering hints.<p>
     *
     * @param baseMode the base mode of the settings, for example {@link Simapi#RENDER_QUALITY}
     * @param hints the special rendering hints to use for image processing operations like scaling etc.
     */
    public RenderSettings(int baseMode, RenderingHints hints) {

        switch (baseMode) {
            case Simapi.RENDER_SPEED:
                m_hints = HINTS_SPEED;
                m_compressionQuality = 0.5f;
                m_isUseBlur = false;
                m_blurFactor = 1.0;
                break;
            case Simapi.RENDER_MEDIUM:
                m_hints = HINTS_MEDIUM;
                m_compressionQuality = 0.75f;
                m_isUseBlur = false;
                m_blurFactor = 1.0;
                break;
            case Simapi.RENDER_QUALITY_BICUBIC:
                m_hints = HINTS_QUALITY_BICUBIC;
                m_compressionQuality = 0.95f;
                m_isUseBlur = false;
                m_blurFactor = 1.0;
                break;
            case Simapi.RENDER_QUALITY_SOFT:
                m_hints = HINTS_QUALITY_SOFT;
                m_compressionQuality = 0.95f;
                m_isUseBlur = true;
                m_blurFactor = 1.5;
                break;
            case Simapi.RENDER_QUALITY:
            default:
                m_hints = HINTS_QUALITY;
                m_compressionQuality = 0.95f;
                m_isUseBlur = true;
                m_blurFactor = 1.0;
                break;
        }
        if (hints != null) {
            // must create a new object to modify, otherwise constant values would be affected
            RenderingHints newHints = new RenderingHints(null);
            newHints.add(m_hints);
            newHints.add(hints);
            m_hints = newHints;
        }
        m_transparentReplaceColor = Color.WHITE;
        m_imageFilters = new ArrayList<ImageFilter>();
        m_maximumBlurSize = (MAX_BLUR_SIZE * MAX_BLUR_SIZE);
        m_threadNicePriority = Thread.MIN_PRIORITY;
    }

    /**
     * Initializes the default values for the rendering hints.<p>
     *
     * @param mode the quality mode to use
     *
     * @return the rendering hints
     */
    private static RenderingHints initHints(int mode) {

        // transformation rendering hints
        RenderingHints hints = new RenderingHints(null);

        switch (mode) {
            case Simapi.RENDER_SPEED:
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
                hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
                hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
                hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DEFAULT);
                hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
                break;
            case Simapi.RENDER_MEDIUM:
            case Simapi.RENDER_QUALITY:
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
                break;
            case Simapi.RENDER_QUALITY_BICUBIC:
            case Simapi.RENDER_QUALITY_SOFT:
                hints.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                hints.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
                hints.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                hints.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
                hints.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_ENABLE);
                hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
                hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_DEFAULT);
                break;
            default: // ignore
        }

        return hints;
    }

    /**
     * Adds a new image filter to the filter processing list.<p>
     *
     * @param filter the image filter to add
     */
    public void addImageFilter(ImageFilter filter) {

        m_imageFilters.add(filter);
    }

    /**
     * Returns the base blur factor to use when scaling down.<p>
     *
     * @return the base blur factor to use when scaling down
     */
    public double getBlurFactor() {

        return m_blurFactor;
    }

    /**
     * Returns the image save compression quality, used for JPEG images (and other formats that support such a setting).<p>
     *
     * This is used only if the image <code>type</code> supports different qualities.
     * For example, this it is used when writing JPEG images.
     * A quality of 0.1 is very poor, 0.75 is ok, 1.0 is maximum.<p>
     *
     * @return the image save quality, used for JPEG images (and other formats that support such a setting)
     */
    public float getCompressionQuality() {

        return m_compressionQuality;
    }

    /**
     * Returns a copy of the list of image filters that should be applied to the processed image.<p>
     *
     * @return a copy of the list of image filters that should be applied to the processed image
     */
    public List<ImageFilter> getImageFilters() {

        return new ArrayList<ImageFilter>(m_imageFilters);
    }

    /**
     * Returns the maximum size of an image that is blurred before applying a downscaling operation.<p>
     *
     * If the image size is to big, "out of memory" errors may occur.
     * The default is <code>3000 x 3000</code> pixel.<p>
     * <p>
     *
     * @return the maximum size of an image that is blurred before apply a downscaling operation
     */
    public int getMaximumBlurSize() {

        return m_maximumBlurSize;
    }

    /**
     * Returns the background color replacement for the transparent color.<p>
     *
     * This is used if transparency is not supported by the selected image format.<p>
     *
     * The default color is {@link Color#WHITE}.<p>
     *
     * @return the background color replacement for the transparent color
     */
    public Color getTransparentReplaceColor() {

        return m_transparentReplaceColor;
    }

    /**
     * Returns <code>true</code> if blur is used when downscaling an image to a thumbnail.<p>
     *
     * This improves the thumbnail quality, but uses a lot more CPU and memory resources.<p>
     *
     * @return <code>true</code> if blur is used when downscaling an image to a thumbnail
     */
    public boolean isUseBlur() {

        return m_isUseBlur;
    }

    /**
     * Sets the image save compression quality, used for JPEG images (and other formats that support such a setting).<p>
     *
     * This is used only if the image <code>type</code> supports different qualities.
     * For example, this it is used when writing JPEG images.
     * A quality of 0.1 is very poor, 0.75 is ok, 1.0 is maximum.<p>
     *
     * @param compressionQuality the compression quality to set (must be between 0 and 1)
     */
    public void setCompressionQuality(float compressionQuality) {

        if ((compressionQuality < 0f) || (compressionQuality > 1f)) {
            throw new IllegalArgumentException("compression quality must be between 0.0f and 1.0f");
        }
        m_compressionQuality = compressionQuality;
    }

    /**
     * Sets the maximum size of an image that is blurred before applying a downscaling operation.<p>
     *
     * @param maximumBlurSize the maximum size of an image to set
     */
    public void setMaximumBlurSize(int maximumBlurSize) {

        m_maximumBlurSize = maximumBlurSize;
    }

    /**
     * Sets the backgound color replacement for the transparent color.<p>
     *
     * This is used if transparency is not supported by the selected image format.<p>
     *
     * @param transparentColor the backgound color replacement for the transparent color to set
     */
    public void setTransparentReplaceColor(Color transparentColor) {

        m_transparentReplaceColor = transparentColor;
    }

    /**
     * Returns the image rendering hints to to be used for image scaling etc.<p>
     *
     * This method is protected to avoid changes to the constant values defined in this class.<p>
     *
     * @return the image rendering hints to to be used for image scaling etc.
     */
    protected RenderingHints getRenderingHints() {

        return m_hints;
    }

    /**
     * Returns the thread priority to use for image operations that require a lot of CPU power.<p>
     *
     * @return  the thread priority to use for image operations that require a lot of CPU power
     */
    protected int getThreadNicePriority() {

        return m_threadNicePriority;
    }

    /**
     * Returns the stored thread priority used.<p>
     *
     * @return the stored thread priority used
     */
    protected int getThreadOldPriority() {

        if (m_threadOldPriority <= 0) {
            m_threadOldPriority = Thread.NORM_PRIORITY;
        }

        return m_threadOldPriority;
    }

    /**
     * Sets the thread priority to use for image operations that require a lot of CPU power.<p>
     *
     * @param threadNicePriority the thread priority to set
     */
    protected void setThreadNicePriority(int threadNicePriority) {

        m_threadNicePriority = threadNicePriority;
    }

    /**
     * Sets the stored thread priority.<p>
     *
     * @param threadOldPriority the thread priority to store
     */
    protected void setThreadOldPriority(int threadOldPriority) {

        m_threadOldPriority = threadOldPriority;
    }

    /**
     * Controls if blur should be used at all.<p>
     *
     * @param useBlur if <code>false</code>, don't use blur
     */
    protected void setUseBlur(boolean useBlur) {

        m_isUseBlur = useBlur;
    }
}