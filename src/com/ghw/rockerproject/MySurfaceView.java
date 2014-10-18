package com.ghw.rockerproject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;

import android.app.Service;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;

public class MySurfaceView extends SurfaceView implements Callback, Runnable {
	private SensorManager sm;
	/*
	 * <加速度传感器>
	 */
	
	private Sensor sensor_acce;
	private SensorEventListener mySensorListener_acce;
	private double x = 0, y = 0, z = 0;
	public static float TRAPDOOR = (float) 2.5;
	public static float LOW = (float) 1.2;
	boolean direct_flag[];
	/*
	 * <角度传感器>
	 */
	private SensorEventListener mySensorListener_gyro;
	private Sensor sensor_gyro;
	public float[] angle;  //陀螺仪获取角度
	public float ANGLE_TRAPDOOR = 20;
	public float ANGLE_LOW = 15;
	
	public int ACCE_MODE_SPEED = 2;
	public int mode;    //模式  mode=0时为传统的双摇杆控制模式，mode=1时为手势控制模式
	
	public int ctrl_signal_status = 0;   //控制信号的网络状态
	public int video_signal_status;    //视频传输信号的网络状态
	
	public float RectX;   //手势控制中按钮的y坐标
	public final String BROADCAST_IP = "224.0.0.1";
	public final int SEND_PERIOD = 20; //ms
	public final String CLIENT_IP = "192.168.1.108";
			
	//public String CLIENT_IP = "127.0.0.1";
	public String command = "";
	public MulticastSocket  ms;
	public long timepre=0;
	public long timeaft=0;
	public final int MULTI_LISTEN_PORT = 9999;
	public final int CTRL_DEST_PORT = 9998;
	public final int CTRL_SRC_PORT = 9998;
	private SurfaceHolder sfh;
	private float smallCenterX, smallCenterY, smallCenterR;
	private float BigCenterX, BigCenterY;
	private float BigCenterR;
	private float smallCenterX2, smallCenterY2, smallCenterR2;
	private float BigCenterX2, BigCenterY2;
	private float BigCenterR2;
	float percent = 1;
	public static String hostip; // 本机IP
	public Bitmap background;
	public Bitmap old_background;
	public double offsetX=0.0,offsetY=0.0,offsetX2=0.0,offsetY2=0.0;
	private ServerSocket ss;
	private InputStream ins;
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
	private Paint paint, paint_black, paint_white, paint_line, paint_red;
	public DatagramSocket send_ctrl_socket;
	public MySurfaceView(Context context) {
		super(context);
		sfh = this.getHolder();
		sfh.addCallback(this);
		sfh.setFormat(PixelFormat.TRANSPARENT);
		setZOrderOnTop(true);
		
		direct_flag = new boolean [6];
		for (int i=0; i<6; i++)
			direct_flag[i] = false;
		//通过服务得到传感器管理对象 
		sm = (SensorManager) MainActivity.ma.getSystemService(Service.SENSOR_SERVICE);
		sensor_acce = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		sensor_gyro = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
		mySensorListener_acce = new SensorEventListener() {
			@Override
			//传感器获取值发生改变时在响应此函数
			public void onSensorChanged(SensorEvent event) {//备注1 
				//传感器获取值发生改变，在此处理 
				x = event.values[0]; //手机横向翻滚
				//x>0 说明当前手机左翻 x<0右翻     
				y = event.values[1]; //手机纵向翻滚
				//y>0 说明当前手机下翻 y<0上翻
				z = (float) (event.values[2] - 9.8); //屏幕的朝向
				//z>0 手机屏幕朝上 z<0 手机屏幕朝下
				
				if (direct_flag[0] == false && (x>TRAPDOOR)) {
					direct_flag[0] = true;
					Log.d("flag", "0true");
				} else if (direct_flag[0]==true && x<LOW&&x>-LOW) {
					direct_flag[0] = false;
					Log.d("flag", "0false");
				}
				
				if (direct_flag[1] == false && (x<-TRAPDOOR)) {
					direct_flag[1] = true;
					Log.d("flag", "1true");
				} else if (direct_flag[1]==true && x<LOW&&x>-LOW) {
					direct_flag[1] = false;
					Log.d("flag", "1false");
				}
				
				if (direct_flag[2] == false && (y>TRAPDOOR)) {
					direct_flag[2] = true;
					Log.d("flag", "2true");
				} else if (direct_flag[2]==true && y<LOW&&y>-LOW) {
					direct_flag[2] = false;
					Log.d("flag", "2false");
				}
				
				if (direct_flag[3] == false && (y<-TRAPDOOR)) {
					direct_flag[3] = true;
					Log.d("flag", "3true");
				} else if (direct_flag[3]==true && y<LOW&&y>-LOW) {
					direct_flag[3] = false;
					Log.d("flag", "3false");
				}
				
				if (direct_flag[4] == false && (angle[2]>ANGLE_TRAPDOOR)) {
					direct_flag[4] = true;
					Log.d("flag", "4true");
				} else if (direct_flag[4]==true && angle[2]<ANGLE_LOW&&angle[2]>-ANGLE_LOW) {
					direct_flag[4] = false;
					Log.d("flag", "4false");
				}
				if (direct_flag[5] == false && (angle[2]<-ANGLE_TRAPDOOR)) {
					direct_flag[5] = true;
					Log.d("flag", "5true");
				} else if (direct_flag[5]==true && angle[2]<ANGLE_LOW&&angle[2]>-ANGLE_LOW) {
					direct_flag[5] = false;
					Log.d("flag", "5false");
				}	
					
				
			}

			@Override
			//传感器的精度发生改变时响应此函数
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub

			}
		};
		mySensorListener_gyro = new SensorEventListener() {
			@Override
			
			public void onSensorChanged(SensorEvent event)
			{
			   //if (timestamp != 0) {
			    //   final float dT = (event.timestamp - timestamp) * NS2S;
			       angle[0] += event.values[0];// * dT;
			       angle[1] += event.values[1];// * dT;
			       angle[2] += event.values[2];// * dT;
			       Log.d("angle", "0: "+angle[0] + " 1: " +angle[1] + " 2: " + angle[2]);
			   //}
			   //timestamp = event.timestamp;
			}

			@Override
			public void onAccuracyChanged(Sensor sensor, int accuracy) {
				// TODO Auto-generated method stub

			}
		};
		
		
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
		paint_red = new Paint();
		paint_red.setColor(Color.RED);
		paint_red.setAntiAlias(true);
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
		try {
			send_ctrl_socket = new DatagramSocket(CTRL_SRC_PORT);
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		angle = new float [3];
		for (int i=0; i<3; i++) {
			angle[i] = 0;
		}
		screenW = this.getWidth();
		screenH = this.getHeight();
		percent = (float) (screenH / 720);
		RectX = screenW*7/8 - percent * 120;    //初始化按钮
		//System.out.println("screenH:" + screenH + "   screenW:" + screenW);
		// map = Bitmap.createScaledBitmap(map, (int)screenW, (int)screenH,
		// true);
		smallCenterR = smallCenterR2 = (float) (60 * 1.3 * percent);
		BigCenterR = BigCenterR2 = (float) (120 * 1.3 * percent);
		smallCenterY = screenH * 5 / 8 + BigCenterR;
		smallCenterX = screenW / 7;
		BigCenterY = screenH * 5 / 8;
		BigCenterX = screenW / 7;
		smallCenterY2 = screenH * 5 / 8;
		smallCenterX2 = screenW * 6 / 7;
		BigCenterY2 = screenH * 5 / 8;
		BigCenterX2 = screenW * 6 / 7;
		
		
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
		dstbtn1 = Bitmap.createBitmap(btn1, 0, 0, width3, height3, matrix3,	true);
		// dstbtn2 = Bitmap.createBitmap(btn2,0,0,
		// width3,height3,matrix3,true);
		dstbtn3 = Bitmap.createBitmap(btn3, 0, 0, width3, height3, matrix3, true);
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

		try {
			ms = new MulticastSocket(MULTI_LISTEN_PORT);
			InetAddress serverAddress = InetAddress.getByName(BROADCAST_IP);
			ms.joinGroup(serverAddress);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		th = new Thread(this);
		th.start();
		
		DisplayThread myDisThread = new DisplayThread();
		myDisThread.start();

		
		
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
								
				if (background == null && old_background != null) {
					Log.e("!!","background is null");
					Matrix matrix = new Matrix();
					int width = old_background.getWidth();// 获取资源位图的宽
					int height = old_background.getHeight();// 获取资源位图的高
//					Log.e("!!",width + " " + height + " " + screenW + " " +screenH );
					matrix.postScale((float) (screenW / width),
							(float) (screenH / height));
					
					Bitmap dst_old_background= Bitmap.createBitmap(old_background, 0, 0, width,
							height, matrix, true);
					
					canvas.drawBitmap(dst_old_background, 0, 0, null);
				}
				else if(background != null){
					Log.e("!!","background is not null");
					Matrix matrix = new Matrix();
					int width = background.getWidth();// 获取资源位图的宽
					int height = background.getHeight();// 获取资源位图的高
//					Log.e("!!",width + " " + height + " " + screenW + " " +screenH );
					matrix.postScale((float) (screenW / width),
							(float) (screenH / height));
					
					Bitmap dst_background= Bitmap.createBitmap(background, 0, 0, width,
							height, matrix, true);
					
					canvas.drawBitmap(dst_background, 0, 0, null);
				}
				

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
				

				if (btn1_choosed == true) {
					canvas.drawBitmap(dstbtn1_pressed, screenW / 8 - percent
							* 120, 60 * percent, null);
				}
				else {
					canvas.drawBitmap(dstbtn1, screenW / 8 - percent * 120,
							60 * percent, null);
				}

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
//				canvas.drawText("   电量:100%         距离:100m  摄像头ip:" + CLIENT_IP, 0,
//						40 * percent, paint_white);
				//canvas.drawText("设置", screenW - 100, 40 * percent, paint_white);
				float offsetX =  (float) ((smallCenterX-screenW/7)*1.5*256/percent/234);
				float offsetY =  (float) ((screenH*5/8-smallCenterY)*1.5*256/percent/234);
				float offsetX2 = (float) ((smallCenterX2-screenW*6/7)*1.5*256/percent/234);
				float offsetY2 = (float) ((screenH*5/8-smallCenterY2)*1.5*256/percent/234);
				canvas.drawText("Rockerproject_v2.2 "+hostip+" OffsetX: " + offsetX + "  OffsetY: " + offsetY +
						"  OffsetX2: " + offsetX2 +"  OffsetY2: " + offsetY2, 0, 40*percent , paint_white);
				Log.d("percent","offset:" + percent);
				byte[] channel = new byte[6];
				channel[0] = (byte)((offsetX2/2)+128);
				channel[1] = (byte)((offsetY2/2)+128);
				channel[2] = (byte)((offsetY/2)+128);
				channel[3] = (byte)((offsetX/2)+128);
				channel[4] = (byte)128;
				channel[5] = (byte)1;
								
				Thread ctrl_thread_chanel1 = new UDPCommandSendThread(send_ctrl_socket,channel);
				ctrl_thread_chanel1.start();
	            timeaft=System.currentTimeMillis();
	            long deltasend=timeaft-timepre;
	            timepre = timeaft;
	            canvas.drawText("周期: "+(float)deltasend/1000 + "s", 0, 600 , paint_white);
	            
				canvas.drawText("  通道1: "+channel[0], 0, 80*percent , paint_white);
				canvas.drawText("  通道2: "+channel[1], 0, 120*percent , paint_white);
				canvas.drawText("  通道3: "+channel[2], 0, 160*percent , paint_white);
				canvas.drawText("  通道4: "+channel[3], 0, 200*percent , paint_white);
				canvas.drawText("  通道5: "+channel[4], 0, 240*percent , paint_white);
				canvas.drawText("  通道6: "+channel[5], 0, 280*percent , paint_white);
				canvas.drawText("angle: "+
						new java.text.DecimalFormat("0.00").format(angle[0])+"  " + 
						new java.text.DecimalFormat("0.00").format(angle[1])+ "  " + 
						new java.text.DecimalFormat("0.00").format(angle[2]), 0, 500, paint_white);
				if (mode == 1) {
					paint_black.setAlpha(0x77);
					canvas.drawRect(0, 0, screenW , screenH, paint_black);
					canvas.drawRect(screenW * 1 / 8 + percent * 120, screenH * 7 / 8 - percent * 120, screenW * 7 / 8 - percent * 120, screenH * 1 / 8 + percent * 120, paint);
					//canvas.drawText("[debug]  RectX: " + RectX + "  hightest: " + (screenW * 7 / 8), 0, 500, paint_red);
					//绘制mode1中的油门（血条状）
					for (float i = RectX; i<screenW * 7 / 8- percent * 130; i+= percent *20)
						canvas.drawRect(i, screenH * 7 / 8 - percent * 125, i+percent * 15, screenH * 1 / 8 + percent * 125, paint_red);
					float tempX = smallCenterX;
					float tempX2 = smallCenterX2;
					float tempY = smallCenterY;
					float tempY2 = smallCenterY2;
					
					if (direct_flag[0] == true) {
						canvas.drawText("flag[0]==true", 0, 290, paint_white);
						tempX2-=ACCE_MODE_SPEED;
					} else if (direct_flag[1] == true) {
						canvas.drawText("flag[1]==true", 0, 320, paint_white);
						tempX2+=ACCE_MODE_SPEED;
					} else
						tempX2 = BigCenterX2;
					if (direct_flag[2] == true) {
						canvas.drawText("flag[2]==true", 0, 350, paint_white);
						tempY2+=ACCE_MODE_SPEED;
					} else if (direct_flag[3] == true){
						canvas.drawText("flag[3]==true", 0, 380, paint_white);
						tempY2-=ACCE_MODE_SPEED;
					} else
						tempY2 = BigCenterY2;
					if (direct_flag[4] == true) {
						canvas.drawText("flag[4]==true", 0, 350, paint_white);
						tempX-=ACCE_MODE_SPEED;
					} else if (direct_flag[5] == true){
						canvas.drawText("flag[5]==true", 0, 380, paint_white);
						tempX+=ACCE_MODE_SPEED;
					} else
						tempX = BigCenterX;

				
					if (Math.sqrt(Math.pow((BigCenterX - (int) tempX), 2)
							+ Math.pow((BigCenterY - (int) tempY), 2)) <= BigCenterR) {
						// System.out.println("In left circle!");
						smallCenterX = tempX;
						smallCenterY = tempY;
					} else if (Math.sqrt(Math.pow((BigCenterX - (int) tempX), 2)
							+ Math.pow((BigCenterY - (int) tempY), 2)) <= BigCenterR + 60
							* percent) {
						// System.out.println("On left circle!");
						setSmallCircleXY(BigCenterX, BigCenterY, BigCenterR,
								getRad(BigCenterX, BigCenterY, tempX, tempY));
					} 
					
					if (Math.sqrt(Math.pow((BigCenterX2 - (int) tempX2), 2)
							+ Math.pow((BigCenterY2 - (int) tempY2), 2)) <= BigCenterR2) {
						smallCenterX2 = tempX2;
						smallCenterY2 = tempY2;
					} else if (Math.sqrt(Math.pow((BigCenterX2 - (int) tempX2), 2)
							+ Math.pow((BigCenterY2 - (int) tempY2), 2)) <= BigCenterR2 + 60
							* percent) {
						setSmallCircleXY2(BigCenterX2, BigCenterY2, BigCenterR2,
								getRad(BigCenterX2, BigCenterY2, tempX2, tempY2));
					}
						
				
					Log.d("xy", "tempx2: "+tempX2 + " tempy2:" + tempY2+"\nx2: "+smallCenterX2+"y2: " +smallCenterY2);
					paint_black.setAlpha(0xFF);
					
				}
				
				if (ctrl_signal_status == 0 )
					canvas.drawCircle(screenW - 25*percent, 27*percent, 10*percent, paint_red);
				else 
					canvas.drawCircle(screenW - 25*percent, 27*percent, 10*percent, paint_white);
			}
		} catch (Exception e) {

		} finally {
			if (canvas != null)
				sfh.unlockCanvasAndPost(canvas);
		}
	}
	public static int binaryToAlgorism(String binary) {		
        int max = binary.length();
        int result = 0;
        for (int i = max; i > 0; i--) {
            char c = binary.charAt(i - 1);
            int algorism = c - '0';
            result += Math.pow(2, max - i) * algorism;
        }
        
        //Log.d("udp", "result:"+result);
        return result;
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
		if (mode == 0) {
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
			} 
	
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
		} else if (mode == 1) {
			 if (
					 (x < screenW * 7 / 8 - percent * 120) && (x > screenW * 1 / 8 + percent * 120)
					 && (y < screenH * 7 / 8 - percent * 120) && (y > screenH * 1 / 8 + percent * 120)
			    ) {
					// System.out.println("Btn3 pressed!");
					return 13;
				} 
			return 14;
		}
		return 0;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		float x0 = event.getX(0);
		float y0 = event.getY(0);
//		System.out.println("x=" + x0 / percent + " y=" + y0 / percent);

		int pointerCount = event.getPointerCount();
		int action = event.getAction();
		if (pointerCount == 1) {
			int choose = judgeTouchArea(x0, y0);
			switch (action) {
			case MotionEvent.ACTION_UP:
				if (mode == 0){
					smallCenterX = BigCenterX;
					// smallCenterY = BigCenterY;
					smallCenterX2 = BigCenterX2;
					smallCenterY2 = BigCenterY2;
				}
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
					if (btn3_choosed == false) {
						mode = 1;   //切换为手势控制模式
						sm.registerListener(mySensorListener_acce, sensor_acce, SensorManager.SENSOR_DELAY_GAME);
						sm.registerListener(mySensorListener_gyro, sensor_gyro, SensorManager.SENSOR_DELAY_GAME);
						btn3_choosed = true;
					}
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
				case 13:
					if (mode == 1) {
						RectX  = x0;
						smallCenterY = BigCenterY+BigCenterR+BigCenterR * 2 *(RectX-screenW * 7 / 8 + percent * 120)/(screenW * 3 / 4 - percent * 240);
					}
					break;
				case 14:
					mode = 0;  //切换为传统双摇杆模式
					sm.unregisterListener(mySensorListener_acce);
					sm.unregisterListener(mySensorListener_gyro);
					for (int i=0; i<3; i++)
						angle[i] = 0;
					btn3_choosed = false;
					break; 
				}
				break;
			case MotionEvent.ACTION_MOVE:
				// System.out.println("ACTION_MOVE pointerCount=" +
				// pointerCount);
				//System.out.println("choose:" + choose);
				switch (choose) {

				case 9:// in left circle
					if (mode == 0) {
						smallCenterX = x0;
						smallCenterY = y0;
					}
					break;
				case 10:
					if (mode == 0) {
						setSmallCircleXY(BigCenterX, BigCenterY, BigCenterR,
								getRad(BigCenterX, BigCenterY, x0, y0));
					}
					break;
				case 11:
					if (mode == 0) { 
						smallCenterX2 = x0;
						smallCenterY2 = y0;
					}
					break;
				case 12:
					if (mode == 0) {
						setSmallCircleXY2(BigCenterX2, BigCenterY2, BigCenterR2,
								getRad(BigCenterX2, BigCenterY2, x0, y0));
					}
					break;
				case 13:
					if (mode == 1) {
						RectX  = x0;
					//	把mode1的油门量按比例转化为mode0的油门量
						smallCenterY = BigCenterY+BigCenterR+BigCenterR * 2 *(RectX-screenW * 7 / 8 + percent * 120)/(screenW * 3 / 4 - percent * 240);
					}
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
			//System.out.println("choose1:" + choose1 + " choose2:" + choose2);
			//System.out.println("x0:" + x0 + " y0:" + y0 + " x1:" + x1 + " y1:"
				//	+ y1);
			switch (action) {
			case MotionEvent.ACTION_MOVE:
				// System.out.println("ACTION_MOVE pointerCount=" +
				// pointerCount);
				switch (choose1) {

				case 9:// in left circle
					if (mode == 0) {
						smallCenterX = x0;
						smallCenterY = y0;
					}
					break;
				case 10:
					if (mode == 0) {
						setSmallCircleXY(BigCenterX, BigCenterY, BigCenterR,
								getRad(BigCenterX, BigCenterY, x0, y0));
					}
					break;
				case 11:
					if (mode == 0) {
						smallCenterX2 = x0;
						smallCenterY2 = y0;
					}
					break;
				case 12:
					if (mode == 0) {
						setSmallCircleXY2(BigCenterX2, BigCenterY2, BigCenterR2,
								getRad(BigCenterX2, BigCenterY2, x0, y0));
					}
					break;
				}
				switch (choose2) {

				case 9:// in left circle
					if (mode == 0) {
						smallCenterX = x1;
						smallCenterY = y1;
					}
					break;
				case 10:
					if (mode == 0) {
						setSmallCircleXY(BigCenterX, BigCenterY, BigCenterR,
								getRad(BigCenterX, BigCenterY, x1, y1));
					}
					break;
				case 11:
					if (mode == 0) {
						smallCenterX2 = x1;
						smallCenterY2 = y1;
					}
					break;
				case 12:
					if (mode == 0) {
						setSmallCircleXY2(BigCenterX2, BigCenterY2, BigCenterR2,
							getRad(BigCenterX2, BigCenterY2, x1, y1));
					}
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


//	public class DisplayThread extends Thread {
//		//public DatagramSocket ds;
//		
//		public int buffSize = 32768;
//		
//		private InputStream ins;
//		public Bitmap mybmp;
//
//		public void run() {
//			while (true) {
//				byte message[] = new byte[buffSize];
//				DatagramPacket datagramPacket = new DatagramPacket(message,
//						buffSize);
//				try {
//					ms.receive(datagramPacket);
//					CLIENT_IP = datagramPacket.getAddress().getHostAddress();
//					Log.d("dispthread", "接受到数据");
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//
//				this.ins = new ByteArrayInputStream(datagramPacket.getData());
//				Log.d("dispthread", "ins: " + ins);
//				if (ins!=null) {
//					background=BitmapFactory.decodeStream(ins);
//				}
//			}
//		
//		}
//	}
	class DisplayThread extends Thread {
		private InputStream ins;
		private int count=0;
		public  int buffSize = 32768;
		public int packetSize = 32768;
		public byte frameBuffer[] = new byte[buffSize];
		public String flag = "";
		@Override
		public void run() {
			while (true) {
				byte byteBuffer[] = new byte[packetSize];
				byte message[] = new byte[packetSize];
			    DatagramPacket datagramPacket = new DatagramPacket(message, packetSize);  
			    try {
					ms.receive(datagramPacket);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} 
			    Log.d("conn", "连接成功!");
			    this.ins = new ByteArrayInputStream(datagramPacket.getData());
			    
			    //Arrays.fill(byteBuffer,(byte)0);
			    try {
					ins.read(byteBuffer);
					ins.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			    
			    int recv_byte = packetSize;
			    for (int i=packetSize-1; i>=0; i--) {                 //计算收到数据的字节数
			        //System.out.println(byteBuffer[i]);
			        if (byteBuffer[i] == 0){
			            //System.out.println("recv_byte--");
			            recv_byte--;
			        }
			        else
			            break;
			    }
			   // System.out.println("接收到 " + recv_byte + " bytes 数据.");

			    /*
			     *   处理视频流
			     */
			    for (int i=0; i<recv_byte; i++) {
			        //System.out.println("in loop ["+i+"]: " + byteBuffer[i]);
			        if (byteBuffer[i] == -1 && byteBuffer[i+1] == -40) {          //ffd8  begin
			        	if (flag == "begin")
			        		flag = "rebegin";
			        	else
			        		flag = "begin";
			        	Log.d("disthread","meet ffd8");
			            count = 0;
			        } else if (byteBuffer[i] == -1 && byteBuffer[i+1] == -39) {   //ffd9  end
			            flag = "end";
			            Log.d("disthread","meet ffd9");
			        }            
			        if (flag == "begin") {
			            frameBuffer[count] = byteBuffer[i];
			            // System.out.println("["+count+"]"+frameBuffer[count] + "   byteBuffer["+i+"]: " + byteBuffer[i]);
			       
			            count++;
			        } else if (flag == "end") {
			            frameBuffer[count] = byteBuffer[i];
//			            System.out.println("["+count+"]"+frameBuffer[count] + "   byteBuffer["+i+"]: " + byteBuffer[i]);
			       
			            frameBuffer[count+1] = byteBuffer[i+1];
//			            System.out.println("["+(count+1)+"]"+frameBuffer[count+1] + "   byteBuffer["+(i+1)+"]: " + byteBuffer[i+1]);
			            Log.d("disthread","frameBuffer: "+frameBuffer);
			           // bitmap = BitmapFactory.decodeByteArray(frameBuffer, 0, buffSize);
			            InputStream picStream = new ByteArrayInputStream(frameBuffer);
			            if (background != null)
			            	old_background = background;
						background=BitmapFactory.decodeStream(picStream);
						if (background == null) Log.d("disthread","background is null");
						Log.d("disthread","complete backgroud");
						frameBuffer = new byte[buffSize];
			            count = 0;
			            flag = "";
			        } else if (flag == "rebegin") {
			        	InputStream picStream = new ByteArrayInputStream(frameBuffer);
			            if (background != null){
			            	old_background = background;
			            }
						background=BitmapFactory.decodeStream(picStream);
						if (background == null) Log.d("disthread","rebegin:background is null");
						else Log.d("disthread","rebegin:background is not null");
						count = 0;
						frameBuffer = new byte[buffSize];
						frameBuffer[count] = byteBuffer[i];
						count++;
						flag = "begin";
			        }
			    }

			    try {
					this.ins.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
					
			}
			
		}

	}

					
					

	
	class UDPCommandSendThread extends Thread{
		DatagramSocket socket;
		byte[] data;
    	public UDPCommandSendThread(DatagramSocket socket, byte[]data) {
			// TODO Auto-generated constructor stub
    		this.socket = socket;
    		this.data = data; 
    		//this.socket = ms;
		}
		@SuppressWarnings("null")
		@Override
		public void run() {
			// TODO Auto-generated method stub
			//super.run();
			try {
				
				/**
				 *  使用广播发送udp控制信息，但是发送周期长达3s
				InetAddress destAddress = InetAddress.getByName(destAddressStr);  
                byte[] sendMSG = "11#msg".getBytes();  
                DatagramPacket dp = new DatagramPacket(sendMSG, sendMSG.length, destAddress  , 9998);  
                ms.send(dp);  
                */
				
				
		           //创建一个InetAddree
				Log.d("CLIENT_IP",CLIENT_IP);
		        InetAddress serverAddress = InetAddress.getByName(CLIENT_IP);
		   

//		        Log.d("udp", "data: " + data.length);
//		        Log.d("udp", "ipname: " + ipname);
//		        Log.d("udp", "dest_port: " + CTRL_DEST_PORT);
		        DatagramPacket packet = new DatagramPacket(data,data.length,serverAddress,CTRL_DEST_PORT);
		        String command = new String(data);
		        Log.d("command", command + " : " + data.length);

		        socket.send(packet);

		        
			} catch (Exception e) {
				// TODO: handle exception	
				e.printStackTrace();
			}
			
		}
	}

}
