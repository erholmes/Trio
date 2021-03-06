package com.barcicki.trio.core;

import com.barcicki.trio.R;

import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;

public class Card {

	public final static int SHAPE_SQUARE = 0, SHAPE_TRIANGLE = 2,
			SHAPE_CIRCLE = 1, COLOR_GREEN = 2, COLOR_BLUE = 0, COLOR_RED = 1,
			FILL_EMPTY = 1, FILL_HALF = 2, FILL_FULL = 0, NUMBER_ONE = 0,
			NUMBER_TWO = 1, NUMBER_THREE = 2;

	private int shape, color, fill, number;

	public Card(int shape, int color, int fill, int number) {
		this.color = color;
		this.shape = shape;
		this.fill = fill;
		this.number = number;

	}

	public int getShape() {
		return shape;
	}

	public void setShape(int shape) {
		this.shape = shape;
	}

	public int getColor() {
		return color;
	}

	public void setColor(int color) {
		this.color = color;
	}

	public int getFill() {
		return fill;
	}

	public void setFill(int fill) {
		this.fill = fill;
	}

	public int getNumber() {
		return number;
	}

	public void setNumber(int number) {
		this.number = number;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(getColor());
		sb.append(getFill());
		sb.append(getShape());
		sb.append(getNumber());
		return sb.toString();
	}

	public boolean isEqual(Card c) {
		return c.toString().equals(toString());
	}

}
