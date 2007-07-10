package com.alkacon.simapi.filter;

import java.awt.*;
import java.awt.image.*;

public class TileImageFilter extends ImageFilter implements java.io.Serializable {

	static final long serialVersionUID = 4926390225069192478L;
	
	public static final int FLIP_NONE = 0;
	public static final int FLIP_H = 1;
	public static final int FLIP_V = 2;
	public static final int FLIP_HV = 3;
	public static final int FLIP_180 = 4;

	private int width;
	private int height;
	private int tileWidth;
	private int tileHeight;

	private int edge = 0;

	private int cols;
	private int rows;

	public TileImageFilter() {
		this(32, 32);
	}

	public TileImageFilter(int width, int height) {
		this.width = width;
		this.height = height;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getWidth() {
		return width;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public int getHeight() {
		return height;
	}

	public void setDimensions(int width, int height) {
		consumer.setDimensions(this.width, this.height);

		tileWidth = width-edge;
		tileHeight = height-edge;

		cols = (this.width + tileWidth - 1) / tileWidth;
		rows = (this.height + tileHeight - 1) / tileHeight;
	}

    public void setHints(int hints) {
	    hints &= ~TOPDOWNLEFTRIGHT;
	    hints &= ~COMPLETESCANLINES;
	    hints |= RANDOMPIXELORDER;
		consumer.setHints(hints);
	}

	public void setPixels(int sx, int sy, int w, int h, ColorModel model, byte pixels[], int offset, int scan) {
		for (int y = 0; y < rows; y++) {
			int clippedHeight = Math.min(h, height-sy);
			if (clippedHeight > 0) {
				int tx = sx;
				for (int x = 0; x < cols; x++) {
					int clippedWidth = Math.min(w, width-sx);
					if (clippedWidth > 0)
						consumer.setPixels(tx, sy, clippedWidth, clippedHeight, model, pixels, offset, scan);
					tx += tileWidth;
				}
			}
			sy += tileHeight;
		}
	}

	public void setPixels(int sx, int sy, int w, int h, ColorModel model, int pixels[], int offset, int scan) {
		if (edge > 0)
			pixels = blendPixels(sx, sy, w, h, pixels, offset, scan);
		for (int y = 0; y < rows; y++) {
			int clippedHeight = Math.min(h, height-sy);
			if (clippedHeight > 0) {
				int tx = sx;
				for (int x = 0; x < cols; x++) {
					int clippedWidth = Math.min(w, width-sx);
					if (clippedWidth > 0) {
						if (symmetryMatrix != null)
							consumer.setPixels(tx, sy, clippedWidth, clippedHeight, model, flipPixels(sx, sy, w, h, pixels, offset, scan, symmetryMatrix[x % symmetryCols][y % symmetryRows]), offset, scan);
						else
							consumer.setPixels(tx, sy, clippedWidth, clippedHeight, model, pixels, offset, scan);
					}
					tx += tileWidth;
				}
			}
			sy += tileHeight;
		}
	}

	public int[] blendPixels(int x, int y, int w, int h, int[] pixels, int off, int stride) {
		int[] newPixels = new int[w * h];
		int edge = 8;
		int i = 0;

		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				if (row < edge || col < edge) {
					int row2 = row < edge ? h-edge-1+row : row;
					int col2 = col < edge ? w-edge-1+col : col;
					if (row < edge && col < edge) {
						float frow = (float)row / (float)edge;
						float fcol = (float)col / (float)edge;
						int i2 = row2*w + col2;
						int i3 = row*w + col2;
						int i4 = row2*w + col;
						int left = ImageMath.mixColors(frow, pixels[i4], pixels[i]);
						int right = ImageMath.mixColors(frow, pixels[i2], pixels[i3]);
						newPixels[i] = ImageMath.mixColors(fcol, right, left);
					} else {
						float f = (float)Math.min(row, col) / (float)edge;
						int i2 = row2*w + col2;
						newPixels[i] = ImageMath.mixColors(f, pixels[i2], pixels[i]);
					}
				} else
					newPixels[i] = pixels[i];
				i++;
			}
		}
		return newPixels;
	}

	private int[][] symmetryMatrix = null;
/*
	private int[][] symmetryMatrix = {
		{ FLIP_NONE, FLIP_H },
		{ FLIP_V, FLIP_HV }
	};
*/
	private int symmetryRows = 2, symmetryCols = 2;

	public void setSymmetryMatrix(int[][] symmetryMatrix) {
		this.symmetryMatrix = symmetryMatrix;
		symmetryRows = symmetryMatrix.length;
		symmetryCols = symmetryMatrix[0].length;
	}

	public int[][] getSymmetryMatrix() {
		return symmetryMatrix;
	}

	public int[] flipPixels(int x, int y, int w, int h, int[] pixels, int off, int scansize, int operation) {
		int newX = x;
		int newY = y;
		int newW = w;
		int newH = h;
		switch (operation) {
		case FLIP_H:
			newX = width - (x + w);
			break;
		case FLIP_V:
			newY = height - (y + h);
			break;
		case FLIP_HV:
			newW = h;
			newH = w;
			newX = y;
			newY = x;
			break;
		case FLIP_180:
			newX = width - (x + w);
			newY = height - (y + h);
			break;
		}
		int[] newPixels = new int[newW * newH];
		for (int row = 0; row < h; row++) {
			for (int col = 0; col < w; col++) {
				int index = row * scansize + off + col;
				int newRow = row;
				int newCol = col;
				switch (operation) {
				case FLIP_H:
					newCol = w - col - 1;
					break;
				case FLIP_V:
					newRow = h - row - 1;
					break;
				case FLIP_HV:
					newRow = col;
					newCol = row;
					break;
				case FLIP_180:
					newRow = h - row - 1;
					newCol = w - col - 1;
					break;
				}
				int newIndex = newRow * newW + newCol;
				newPixels[newIndex] = pixels[index];
			}
		}
		return newPixels;
	}

	public String toString() {
		return "Tile";
	}
}
