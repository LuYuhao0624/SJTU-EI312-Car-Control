package com.car;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

class Dot {
	public static final int NORMAL = 0;
	public static final int SELECTED = 1;
	public float x, y;
	public int status = 0;
	public int logical_row = 0;
	public int logical_column = 0;
	public Dot(float x, float y) {
		this.x = x;
		this.y = y;
	}
}

class ControlSignalPair {
	public int signal;
	public float duration;
	public int degree;
	public ControlSignalPair(int signal, float duration, int degree) {
		this.signal = signal;
		this.duration = duration;
		this.degree = degree;
	}
}

public class TraceDrawer extends View {
	private static final int num_rows = 3;
	private static final int num_columns = 3;
	private Dot[][] dots = new Dot[num_rows][num_columns];
	private Bitmap dot_normal, dot_selected;
	private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private ArrayList<Dot> selected_dots = new ArrayList<>();
	private ArrayList<ControlSignalPair> control_sequence = new ArrayList<>();
	public boolean occupied = false;
	// current move position
	private float x, y;
	private int dots_radius;
	// indicate whether the dots are initialized
	private boolean initialized = false;
	// indicate whether current touch position is on a dot
	private boolean on_dot = false;
	// indicate whether the first dot is selected
	private boolean first_selected = false;
	private static final float UNIT_DISTANCE = 1.0f;

	public Bluetooth bluetooth;
	public int azimuthClient = 0;
	public int azimuthController = 0;

	public TraceDrawer(Context context, AttributeSet attrs) {
		super(context, attrs);
		decodeDotImage();
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (!initialized)
			initDots();
		drawDots(canvas);
		drawLines(canvas);
	}

	private void initDots() {
		int screen_width = getWidth();
		int screen_height = getHeight();
		int interval = screen_width / num_rows;
		dots_radius = dot_normal.getWidth() / 2;
		for (int i = 0; i < num_rows; i++) {
			for (int j = 0; j < num_columns; j++) {
				dots[i][j] = new Dot(screen_width / 2 + (num_columns / 2 - j) * interval, screen_height / 2 + (num_rows / 2 - i) * interval);
			}
		}
		for (int i = 0; i < num_rows; i++) {
			dots[i][0].logical_row = 0;
			dots[i][1].logical_row = 1;
			dots[i][2].logical_row = 2;
		}
		for (int i = 0; i < num_columns; i++) {
			dots[0][i].logical_column = 2;
			dots[1][i].logical_column = 1;
			dots[2][i].logical_column = 0;
		}
		initialized = true;
	}

	private void drawDots(Canvas canvas) {
		for (int i = 0; i < num_rows; i++) {
			for (int j = 0; j < num_rows; j++) {
				Dot dot = dots[i][j];
				if (dot.status == Dot.NORMAL) {
					canvas.drawBitmap(dot_normal, dot.x - dots_radius, dot.y - dots_radius, paint);
				}
				else {
					canvas.drawBitmap(dot_selected, dot.x - dots_radius, dot.y - dots_radius, paint);
				}
			}
		}
	}

	private void drawLines(Canvas canvas) {
		Paint new_paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		new_paint.setColor(Color.GREEN);
		new_paint.setStrokeWidth(15.0f);
		if (selected_dots.size() > 0) {
			Dot start_dot = selected_dots.get(0);
			for (int i = 0; i < selected_dots.size(); i++) {
				Dot end_dot = selected_dots.get(i);
				canvas.drawLine(start_dot.x, start_dot.y, end_dot.x, end_dot.y, new_paint);
				start_dot = end_dot;
			}
			if (!on_dot) {
				canvas.drawLine(start_dot.x, start_dot.y, x, y, new_paint);
			}
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		x = event.getX();
		y = event.getY();
		on_dot = true;
		Dot dot = null;
		switch (event.getAction()) {
			case MotionEvent.ACTION_DOWN: // indicate start of draw
				resetDots();
				dot = moveOnDot(x, y);
				if (dot != null) {
					first_selected = true;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				if (first_selected) {
					dot = moveOnDot(x, y);
					if (dot == null) {
						on_dot = false;
					}
				}
				break;
			case MotionEvent.ACTION_UP: // indicate end of draw
				first_selected = false;
				decodeSelectedSequence();
				new ControlThread().start();
				break;
		}
		if (dot != null && dot.status == Dot.NORMAL) {
			dot.status = Dot.SELECTED;
			selected_dots.add(dot);
		}
		postInvalidate();
		return true;
	}

	private void decodeSelectedSequence() {
		if (selected_dots.size() <= 1)
			return;
		int[] previous_vector = {-1, 0};
		Dot start_dot;
		Dot end_dot;
		for (int i = 0; i < selected_dots.size() - 1; i++) {
			start_dot = selected_dots.get(i);
			end_dot = selected_dots.get(i + 1);
			int[] vector = {end_dot.logical_row - start_dot.logical_row, end_dot.logical_column - start_dot.logical_column};
			addControl(previous_vector, vector);
			previous_vector = vector;
		}
	}

	private ControlSignalPair getTurnControl(int[] previous_vector, int[] vector) {
		// the direction are the same
		if ((previous_vector[0] == vector[0] && previous_vector[1] == vector[1]) ||
				(previous_vector[0] == 2 * vector[0] && previous_vector[1] == 2 * vector[1]) ||
				(2 * previous_vector[0] == vector[0] && 2 * previous_vector[1] == vector[1]))
			return new ControlSignalPair(0, -1, 0);
			// the direction are the opposite
		else if ((previous_vector[0] + vector[0] == 0 && previous_vector[1] + vector[1] == 0) ||
				(previous_vector[0] + 2 * vector[0] == 0 && previous_vector[1] + 2 * vector[1] == 0) ||
				(2 * previous_vector[0] + vector[0] == 0 && 2 * previous_vector[1] + vector[1] == 0))
			return new ControlSignalPair(2, -1, 180);
		// vec x pre_vec, indicate turn left or right
		int outer_product = vector[0] * previous_vector[1] - vector[1] * previous_vector[0];
		// indicate the turing degree
		int inner_product = vector[0] * previous_vector[0] + vector[1] * previous_vector[1];
		double cos_theta = inner_product / (norm(vector) * norm(previous_vector));
		int degree = (int) Math.toDegrees(Math.acos(cos_theta));
		int direction = (outer_product > 0) ? 3 : 2;
		return new ControlSignalPair(direction, -1, degree);
	}

	private void addControl(int[] previous_vector, int[] vector) {
		ControlSignalPair turn_control = getTurnControl(previous_vector, vector);
		control_sequence.add(turn_control);
		// forward control
		control_sequence.add(new ControlSignalPair(1, norm(vector) * UNIT_DISTANCE, -1));
	}

	private float norm(int[] vector) {
		return (float)Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1]);
	}

	private void resetDots() {
		for (Dot dot:selected_dots) {
			dot.status = Dot.NORMAL;
		}
		selected_dots.clear();
		control_sequence.clear();
	}

	private Dot moveOnDot(float x, float y) {
		for (int i = 0; i < num_rows; i++) {
			for (int j = 0; j < num_columns; j++) {
				if ((x - dots[i][j].x) * (x - dots[i][j].x) + (y - dots[i][j].y) * (y - dots[i][j].y) <= dots_radius * dots_radius)
					return dots[i][j];
			}
		}
		return null;
	}

	private void decodeDotImage(){
		dot_normal = BitmapFactory.decodeResource(getResources(), R.drawable.point_normal);
		dot_selected = BitmapFactory.decodeResource(getResources(), R.drawable.point_pressed);
	}

	class ControlThread extends Thread{
		@Override
		public void run(){
			int targetAzimuth = azimuthClient;

			occupied = true;

			for (ControlSignalPair ctrl:control_sequence) {
				if (ctrl.degree < 0){
					//前进信号
					bluetooth.send(1);
					try{
						sleep(Math.round(ctrl.duration*1000));
					} catch(InterruptedException e){
						Log.i("swallow.sync", "Sleep interrupted.");
					}
				}else {
					//转弯信号
					if(ctrl.signal == 2)
						targetAzimuth -= ctrl.degree;
					else if(ctrl.signal == 3)
						targetAzimuth += ctrl.degree;
					else
						continue;

					while(true){
						int delta = (azimuthClient-targetAzimuth) % 360;
						if(Math.abs(delta) <= 10)
							break;

						bluetooth.send(delta > 0 ? 2 : 3);
						try{
							sleep(10);
						} catch(InterruptedException e){
							Log.i("swallow.sync", "Sleep interrupted.");
						}
					}
				}
			}
			bluetooth.send(0);
			try{
				sleep(1000);
			}catch(InterruptedException e){
				Log.i("swallow.sync", "Sleep interrupted.");
			}
			occupied = false;
		}
	}
}
