package com.ghw.rockerproject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements Callback, Runnable {
	public int portnum = 6000;
	private SurfaceHolder sfh;
	private float smallCenterX, smallCenterY, smallCenterR = 60;
	private float BigCenterX, BigCenterY, BigCenterR = 120;
	private float smallCenterX2, smallCenterY2, smallCenterR2 = 60;
	private float BigCenterX2, BigCenterY2, BigCenterR2 = 120;
	float percent = 1;
	public static String hostip; // 本机IP
	public Bitmap background;
	private ServerSocket ss;
	private InputStream ins;
	private int angle;
	private boolean btn1_choosed = false, btn2_choosed = false,
			btn3_choosed = false, btn4_choosed = false;
	private Thread th;

	private boolean flag;
	private Canvas canvas;
	public float screenW, screenH;
	private float mapX = 0, mapY = 0;
	private Bitmap map, bigCircleLeft, bigCircleRight, smallCircleLeft,
			smallCircleRight, dstBigCircleLeft, dstBigCircleRight,
			dstSmallCircleLeft, dstSmallCircleRight;
	private Bitmap up, down, btn1, btn1_pressed, btn2, btn2_pressed, btn3,
			btn3_pressed, btn4, btn4_pressed, enlarg, shrink, video, picture;
	private Bitmap dstup, dstdown, dstbtn1, dstbtn1_pressed, dstbtn2,
			dstbtn2_pressed, dstbtn3, dstbtn3_pressed, dstbtn4,
			dstbtn4_pressed, dstenlarg, dstshrink, dstvideo, dstpicture;
	private Paint paint, paint_black, paint_white, paint_line;

	public MySurfaceView(Context context) {
		super(context);
		sfh = this.getHolder();
		sfh.addCallback(this);
		sfh.setFormat(PixelFormat.TRANSPARENT);
		setZOrderOnTop(true);
		paint = new Paint();
		paint.setColor(Color.parseColor("#20B2AA"));
		paint.setAntiAlias(true);
		paint_black = new Paint();
		paint_black.setColor(Color.BLACK);
		paint_black.setAntiAlias(true);
		paint_white = new Paint();
		paint_white.setColor(Color.WHITE);
		paint_white.setAntiAlias(true);
		paint_white.setTextSize(30);
		paint_line = new Paint();
		paint_line.setColor(Color.parseColor("#d14242"));
		paint_line.setAntiAlias(true);
		paint_line.setStrokeWidth((float) 5.0);
		paint_line.setTextSize(30);
		background = null;
		// map =
		// BitmapFactory.decodeResource(this.getResources(),R.drawable.test);
		bigCircleLeft = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.bigcircle);
		bigCircleRight = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.bigcircle);
		smallCircleLeft = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.bluebtn);
		smallCircleRight = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.bluebtn);
		btn1 = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.btn1);
		btn1_pressed = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.btn1_pressed);
		// btn2 =
		// BitmapFactory.decodeResource(this.getResources(),R.drawable.btn2);
		// btn2_pressed =
		// BitmapFactory.decodeResource(this.getResources(),R.drawable.btn2_pressed);
		btn3 = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.btn3);
		btn3_pressed = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.btn3_pressed);
		// btn4 =
		// BitmapFactory.decodeResource(this.getResources(),R.drawable.btn4);
		// btn4_pressed =
		// BitmapFactory.decodeResource(this.getResources(),R.drawable.btn4_pressed);
		// enlarg =
		// BitmapFactory.decodeResource(this.getResources(),R.drawable.enlarg);
		// shrink =
		// BitmapFactory.decodeResource(this.getResources(),R.drawable.shrink);
		up = BitmapFactory.decodeResource(this.getResources(), R.drawable.up);
		down = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.down);
		video = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.video);
		picture = BitmapFactory.decodeResource(this.getResources(),
				R.drawable.picture);
		setFocusable(true);

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		screenW = this.getWidth();
		screenH = this.getHeight();
		percent = (float) (screenH / 720);
		System.out.println("screenH:" + screenH + "   screenW:" + screenW);
		// map = Bitmap.createScaledBitmap(map, (int)screenW, (int)screenH,
		// true);
		smallCenterY = screenH * 5 / 8;
		smallCenterX = screenW / 7;
		BigCenterY = screenH * 5 / 8;
		BigCenterX = screenW / 7;
		smallCenterY2 = screenH * 3 / 5;
		smallCenterX2 = screenW * 6 / 7;
		BigCenterY2 = screenH * 5 / 8;
		BigCenterX2 = screenW * 6 / 7;
		smallCenterR = smallCenterR2 = (float) (60 * 1.3 * percent);
		BigCenterR = BigCenterR2 = (float) (120 * 1.3 * percent);

		flag = true;
		Matrix matrix1 = new Matrix();
		Matrix matrix2 = new Matrix();
		Matrix matrix3 = new Matrix();
		Matrix matrix5 = new Matrix();

		int width1 = bigCircleLeft.getWidth();// 获取资源位图的宽
		int height1 = bigCircleLeft.getHeight();// 获取资源位图的高
		matrix1.postScale((float) (percent * 1.3 * 240 / width1),
				(float) (percent * 1.3 * 240 / width1));
		dstBigCircleLeft = Bitmap.createBitmap(bigCircleLeft, 0, 0, width1,
				height1, matrix1, true);
		dstBigCircleRight = Bitmap.createBitmap(bigCircleRight, 0, 0, width1,
				height1, matrix1, true);
		int width2 = smallCircleLeft.getWidth();// 获取资源位图的宽
		int height2 = smallCircleLeft.getHeight();// 获取资源位图的高
		matrix2.postScale((float) (percent * 120 * 1.3 / width2),
				(float) (percent * 1.3 * 120 / width2));
		dstSmallCircleLeft = Bitmap.createBitmap(smallCircleLeft, 0, 0, width2,
				height2, matrix2, true);
		dstSmallCircleRight = Bitmap.createBitmap(smallCircleRight, 0, 0,
				width2, height2, matrix2, true);
		int width3 = btn1.getWidth();
		int height3 = btn1.getHeight();
		matrix3.postScale((float) percent * 150 / height3, (float) percent
				* 150 / height3);
		dstbtn1 = Bitmap.createBitmap(btn1, 0, 0, width3, height3, matrix3,
				true);
		// dstbtn2 = Bitmap.createBitmap(btn2,0,0,
		// width3,height3,matrix3,true);
		dstbtn3 = Bitmap.createBitmap(btn3, 0, 0, width3, height3, matrix3,
				true);
		// dstbtn4 = Bitmap.createBitmap(btn4,0,0,
		// width3,height3,matrix3,true);

		dstbtn1_pressed = Bitmap.createBitmap(btn1_pressed, 0, 0, width3,
				height3, matrix3, true);
		// dstbtn2_pressed = Bitmap.createBitmap(btn2_pressed,0,0,
		// width3,height3,matrix3,true);
		dstbtn3_pressed = Bitmap.createBitmap(btn3_pressed, 0, 0, width3,
				height3, matrix3, true);
		// dstbtn4_pressed = Bitmap.createBitmap(btn4_pressed,0,0,
		// width3,height3,matrix3,true);

		// int width4 = enlarg.getWidth();
		// int height4 = enlarg.getHeight();
		// matrix4.postScale((float)percent*50/width4,(float)percent*50/width4);
		// dstenlarg = Bitmap.createBitmap(enlarg,0,0,
		// width4,height4,matrix4,true);
		// int width6 = shrink.getWidth();
		// int height6 = shrink.getHeight();
		// dstshrink =
		// Bitmap.createBitmap(shrink,0,0,width6,height6,matrix4,true);
		int width5 = up.getWidth();
		int height5 = up.getHeight();
		matrix5.postScale((float) percent * 50 / width5, (float) percent * 50
				/ width5);
		dstup = Bitmap.createBitmap(up, 0, 0, width5, height5, matrix5, true);
		dstdown = Bitmap.createBitmap(down, 0, 0, width5, height5, matrix5,
				true);

		th = new Thread(this);
		th.start();
	}

	private String getLocalIPAddress() throws SocketException {
		for (Enumeration<NetworkInterface> en = NetworkInterface
				.getNetworkInterfaces(); en.hasMoreElements();) {
			NetworkInterface intf = en.nextElement();
			for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr
					.hasMoreElements();) {
				InetAddress inetAddress = enumIpAddr.nextElement();
				if (!inetAddress.isLoopbackAddress()
						&& (inetAddress instanceof Inet4Address)) {
					return inetAddress.getHostAddress().toString();
				}
			}
		}
		return "null";
	}

	public void myDraw() {
		try {
			canvas = sfh.lockCanvas();
			if (canvas != null) {

				canvas.drawColor(Color.parseColor("#EEE8AA"));
								
				if (background == null)
					;//Log.e("!!","background is null");
				else {
					//Log.e("!!","background is not null");
					canvas.drawBitmap(background, 0, 0, null);
					
					Matrix matrix = new Matrix();

					int width = background.getWidth();// 获取资源位图的宽
					int height = background.getHeight();// 获取资源位图的高
					Log.e("!!",width + " " + height + " " + screenW + " " +screenH );
					matrix.postScale((float) (screenW / width),
							(float) (screenH / height));
					Bitmap dst_background= Bitmap.createBitmap(background, 0, 0, width,
							height, matrix, true);
					
					canvas.drawBitmap(dst_background, 0, 0, null);
				}
				
				DisplayThread myDisThread = new DisplayThread();
				myDisThread.start();
			

				/* 在调试信息中输出本机IP和MAC */
				if (hostip == null) {
					try {
						hostip = getLocalIPAddress();
					} catch (SocketException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} // 获取本机IP

				}

				canvas.drawBitmap(dstBigCircleLeft, BigCenterX - BigCenterR,
						BigCenterY - BigCenterR, null);
				canvas.drawBitmap(dstBigCircleRight, BigCenterX2 - BigCenterR2,
						BigCenterY2 - BigCenterR2, null);
				paint.setAlpha(0x77);
				canvas.drawBitmap(dstSmallCircleLeft, smallCenterX
						- smallCenterR, smallCenterY - smallCenterR, null);
				canvas.drawBitmap(dstSmallCircleRight, smallCenterX2
						- smallCenterR2, smallCenterY2 - smallCenterR2, null);
				paint.setAlpha(0x77);

				if (btn1_choosed == true)
					canvas.drawBitmap(dstbtn1_pressed, screenW / 8 - percent
							* 120, 60 * percent, null);
				else
					canvas.drawBitmap(dstbtn1, screenW / 8 - percent * 120,
							60 * percent, null);

				if (btn3_choosed == true)
					canvas.drawBitmap(dstbtn3_pressed, screenW * 7 / 8
							- percent * 120, 60 * percent, null);
				else
					canvas.drawBitmap(dstbtn3, screenW * 7 / 8 - percent * 120,
							60 * percent, null);

				canvas.drawBitmap(dstup, screenW / 2 - 25 * percent,
						60 * percent, null);
				canvas.drawBitmap(dstdown, screenW / 2 - 25 * percent,
						620 * percent, null);

				canvas.drawRect(0, 0, screenW, 50 * percent, paint_black);
				paint_white.setTextSize(30 * percent);
				canvas.drawText("   电量:100%         距离:100m" + hostip, 0,
						40 * percent, paint_white);
				canvas.drawText("设置", screenW - 100, 40 * percent, paint_white);

			}
		} catch (Exception e) {

		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}

	public void setSmallCircleXY(float centerX, float centerY, float R,
			double rad) {
		smallCenterX = (float) (R * Math.cos(rad)) + centerX;
		smallCenterY = (float) (R * Math.sin(rad)) + centerY;
	}

	public void setSmallCircleXY2(float centerX, float centerY, float R,
			double rad) {
		smallCenterX2 = (float) (R * Math.cos(rad)) + centerX;
		smallCenterY2 = (float) (R * Math.sin(rad)) + centerY;
	}

	private void logic() {

	}

	public double getRad(float px1, float py1, float px2, float py2) {
		float x = px2 - px1;
		float y = py1 - py2;
		float Hypotenuse = (float) Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
		float cosAngle = x / Hypotenuse;
		float rad = (float) Math.acos(cosAngle);
		if (py2 < py1) {
			rad = -rad;
		}
		return rad;
	}

	public int judgeTouchArea(float x, float y) {
		if ((y > 60 * percent) && (y < 210 * percent)) {
			if ((x > screenW / 8 - percent * 120)
					&& (x < screenW / 8 + percent * 120)) {
				// System.out.println("Btn1 pressed!");
				return 1;
			} else if ((x > screenW * 7 / 8 - percent * 120)
					&& (x < screenW * 7 / 8 + percent * 120)) {
				// System.out.println("Btn3 pressed!");
				return 3;
			}
		} /*
		 * else if((y>510*percent)&&(y<660*percent)) { if
		 * ((x>screenW/8-percent*120)&&(x<screenW/8+percent*120)){
		 * //System.out.println("Btn2 pressed!"); return 2; } else if
		 * ((x>screenW*7/8-percent*120)&&(x<screenW*7/8+percent*120)) {
		 * //System.out.println("Btn4 pressed!"); return 4; } }
		 */

		if ((x > screenW / 2 - percent * 50)
				&& (x < screenW / 2 + percent * 50)) {
			if ((y > 0) && (y < 180 * percent)) {
				// System.out.println("Btn up pressed!");
				return 5;
			} else if ((y > 590 * percent) && (y < screenH)) {
				// System.out.println("Btn down pressed!");
				return 6;
			}

		}/*
		 * else if ((y>screenH/2-percent*50)&&(y<screenH/2+percent*50)) { if
		 * ((x>screenW/8+percent*180)&&(x<screenW/8+percent*250)) {
		 * //System.out.println("Btn enlarg pressed!"); return 7; } else if
		 * ((x>screenW*7/8-percent*240)&&(x<screenW*7/8-percent*180)) {
		 * //System.out.println("Btn shrink pressed!"); return 8; }
		 * 
		 * }
		 */

		if (Math.sqrt(Math.pow((BigCenterX - (int) x), 2)
				+ Math.pow((BigCenterY - (int) y), 2)) <= BigCenterR) {
			// System.out.println("In left circle!");
			return 9;
		} else if (Math.sqrt(Math.pow((BigCenterX - (int) x), 2)
				+ Math.pow((BigCenterY - (int) y), 2)) <= BigCenterR + 60
				* percent) {
			// System.out.println("On left circle!");
			return 10;
		} else if (Math.sqrt(Math.pow((BigCenterX2 - (int) x), 2)
				+ Math.pow((BigCenterY2 - (int) y), 2)) <= BigCenterR2) {
			// System.out.println("In right circle!");
			return 11;
		} else if (Math.sqrt(Math.pow((BigCenterX2 - (int) x), 2)
				+ Math.pow((BigCenterY2 - (int) y), 2)) <= BigCenterR2 + 60
				* percent) {
			// System.out.println("On right circle!");
			return 12;
		}

		return 0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x0 = event.getX(0);
		float y0 = event.getY(0);
		System.out.println("x=" + x0 / percent + " y=" + y0 / percent);

		int pointerCount = event.getPointerCount();
		int action = event.getAction();
		if (pointerCount == 1) {
			int choose = judgeTouchArea(x0, y0);
			switch (action) {
			case MotionEvent.ACTION_UP:
				smallCenterX = BigCenterX;
				// smallCenterY = BigCenterY;
				smallCenterX2 = BigCenterX2;
				smallCenterY2 = BigCenterY2;
				// System.out.println("ACTION_DOWN pointerCount=" +
				// pointerCount);
				break;
			case MotionEvent.ACTION_DOWN:
				// System.out.println("ACTION_UP pointerCount=" + pointerCount);
				switch (choose) {
				case 1:
					if (btn1_choosed == false)
						btn1_choosed = true;
					else
						btn1_choosed = false;
					break;
				case 2:
					if (btn2_choosed == false)
						btn2_choosed = true;
					else
						btn2_choosed = false;
					break;
				case 3:
					if (btn3_choosed == false)
						btn3_choosed = true;
					else
						btn3_choosed = false;
					break;
				case 4:
					if (btn4_choosed == false)
						btn4_choosed = true;
					else
						btn4_choosed = false;
					break;
				case 5:

					break;
				case 6:

					break;
				case 7:

					break;
				case 8:

					break;
				}
				break;
			case MotionEvent.ACTION_MOVE:
				// System.out.println("ACTION_MOVE pointerCount=" +
				// pointerCount);
				System.out.println("choose:" + choose);
				switch (choose) {

				case 9:// in left circle
					smallCenterX = x0;
					smallCenterY = y0;
					break;
				case 10:
					setSmallCircleXY(BigCenterX, BigCenterY, BigCenterR,
							getRad(BigCenterX, BigCenterY, x0, y0));
					break;
				case 11:
					smallCenterX2 = x0;
					smallCenterY2 = y0;
					break;
				case 12:
					setSmallCircleXY2(BigCenterX2, BigCenterY2, BigCenterR2,
							getRad(BigCenterX2, BigCenterY2, x0, y0));
					break;
				}
				break;
			}

		}

		if (pointerCount == 2) {
			float x1 = event.getX(1);
			float y1 = event.getY(1);
			int choose1 = judgeTouchArea(x0, y0);
			int choose2 = judgeTouchArea(x1, y1);
			System.out.println("choose1:" + choose1 + " choose2:" + choose2);
			System.out.println("x0:" + x0 + " y0:" + y0 + " x1:" + x1 + " y1:"
					+ y1);
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				// System.out.println("ACTION_MOVE pointerCount=" +
				// pointerCount);
				switch (choose1) {

				case 9:// in left circle
					smallCenterX = x0;
					smallCenterY = y0;
					break;
				case 10:
					setSmallCircleXY(BigCenterX, BigCenterY, BigCenterR,
							getRad(BigCenterX, BigCenterY, x0, y0));
					break;
				case 11:
					smallCenterX2 = x0;
					smallCenterY2 = y0;
					break;
				case 12:
					setSmallCircleXY2(BigCenterX2, BigCenterY2, BigCenterR2,
							getRad(BigCenterX2, BigCenterY2, x0, y0));
					break;
				}
				switch (choose2) {

				case 9:// in left circle
					smallCenterX = x1;
					smallCenterY = y1;
					break;
				case 10:
					setSmallCircleXY(BigCenterX, BigCenterY, BigCenterR,
							getRad(BigCenterX, BigCenterY, x1, y1));
					break;
				case 11:
					smallCenterX2 = x1;
					smallCenterY2 = y1;
					break;
				case 12:
					setSmallCircleXY2(BigCenterX2, BigCenterY2, BigCenterR2,
							getRad(BigCenterX2, BigCenterY2, x1, y1));
					break;
				}
				break;
			case MotionEvent.ACTION_POINTER_1_UP:
				// System.out.println("ACTION_POINTER_1_UP pointerCount=" +
				// pointerCount);
				if (x0 < screenW / 2) {
					smallCenterX = BigCenterX;
					// smallCenterY = BigCenterY;
				} else if (x0 > screenW / 2) {
					smallCenterX2 = BigCenterX2;
					smallCenterY2 = BigCenterY2;
				}
				break;
			case MotionEvent.ACTION_POINTER_2_DOWN:

				switch (choose2) {
				case 1:
					if (btn1_choosed == false)
						btn1_choosed = true;
					else
						btn1_choosed = false;
					break;
				case 2:
					if (btn2_choosed == false)
						btn2_choosed = true;
					else
						btn2_choosed = false;
					break;
				case 3:
					if (btn3_choosed == false)
						btn3_choosed = true;
					else
						btn3_choosed = false;
					break;
				case 4:
					if (btn4_choosed == false)
						btn4_choosed = true;
					else
						btn4_choosed = false;
					break;
				case 5:

					break;
				case 6:

					break;
				case 7:

					break;
				case 8:

					break;
				}
				// System.out.println("ACTION_POINTER_2_DOWN pointerCount=" +
				// pointerCount);
				break;
			case MotionEvent.ACTION_POINTER_2_UP:
				// System.out.println("ACTION_POINTER_2_UP pointerCount=" +
				// pointerCount);
				if (x1 < screenW / 2) {
					smallCenterX = BigCenterX;
					// smallCenterY = BigCenterY;
				} else if (x1 > screenW / 2) {
					smallCenterX2 = BigCenterX2;
					smallCenterY2 = BigCenterY2;
				}
				break;
			}
		}

		return true;
	}

	@Override
	public void run() {
		while (flag) {
			long start = System.currentTimeMillis();
			myDraw();
			logic();
			long end = System.currentTimeMillis();
			try {
				if (end - start < 50) {
					Thread.sleep(50 - (end - start));
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		flag = false;
	}


	public class DisplayThread extends Thread {
		public DatagramSocket ds;
		public int buffSize = 32768;
		
		private InputStream ins;
		public Bitmap mybmp;


		public void run() {
			try {
				ds = new DatagramSocket(portnum);

				while (true) {
					byte message[] = new byte[buffSize];
					DatagramPacket datagramPacket = new DatagramPacket(message,
							buffSize);
					try {
						this.ds.receive(datagramPacket);
						Log.e("dispthread", "接受到数据");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	
					this.ins = new ByteArrayInputStream(datagramPacket.getData());
					Log.e("dispthread", "ins: " + ins);
					
					background=BitmapFactory.decodeStream(ins);
					
					if (background != null){
						Log.e("dispthread", "background not null!");		
					} else {
						Log.e("dispthread", "background null!");
					}
				}
			} catch (SocketException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

}
