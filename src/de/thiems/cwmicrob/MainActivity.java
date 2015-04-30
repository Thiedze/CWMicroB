package de.thiems.cwmicrob;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.view.MotionEventCompat;
import android.view.Display;
import android.view.MotionEvent;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private int ScreenHight = 0;
	private int ScreenWidth = 0;
	private int speedLeftWheel  = 0;
	private int speedRightWheel = 0;
	private TextView infoLeftSpeed;
	private TextView infoRightSpeed;
	private final int resolution = 255;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		infoLeftSpeed  = (TextView) findViewById(R.id.ValueLeft);
		infoRightSpeed = (TextView) findViewById(R.id.ValueRight);
		
		SetScreenSize();
	}

	private void SetScreenSize() {
		try {
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			ScreenWidth = size.x;
			ScreenHight = size.y;
		} catch (Exception e) {
			ShowError(e.toString());
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		try {
			int xPosFirstFinger = -1;
			int yPosFirstFinger = -1;
			int xPosSecondFinger = -1;
			int yPosSecondFinger = -1;
			
			
			if (event.getPointerCount() > 1) {
				xPosFirstFinger  = (int) MotionEventCompat.getX(event, 0);
				yPosFirstFinger  = (int) MotionEventCompat.getY(event, 0);

				xPosSecondFinger = (int) MotionEventCompat.getX(event, 1);
				yPosSecondFinger = (int) MotionEventCompat.getY(event, 1);
				
				CalculatePosition(xPosFirstFinger, yPosFirstFinger, 
							xPosSecondFinger, yPosSecondFinger);
			}
			;

			return true;
		}
		catch (Exception e) {
			ShowError(e.toString());
			return false;
		}
	}

	private void CalculatePosition(int xPosFirstFinger, int yPosFirstFinger, 
			int xPosSecondFinger, int yPosSecondFinger) {
		
		if (xPosFirstFinger > xPosSecondFinger) {
			//First finger right
			//Second finger left
			speedRightWheel = resolution - ConvertPositionToSpeed(yPosFirstFinger);
			speedLeftWheel  = resolution -ConvertPositionToSpeed(yPosSecondFinger);
			
		} else {
			//First finger left
			//Second finger right			
			speedLeftWheel  = resolution -ConvertPositionToSpeed(yPosFirstFinger);
			speedRightWheel = resolution -ConvertPositionToSpeed(yPosSecondFinger);
		}
		
		infoLeftSpeed.setText(speedLeftWheel + "");
		infoRightSpeed.setText(speedRightWheel + "");
		
		new Thread(new ClientThread()).start();
	}
	
	private int ConvertPositionToSpeed (int pos) {
		int ScreenOffset = 200;
		int speed = (((pos - ScreenOffset) * resolution) / (ScreenHight - ScreenOffset));
		
		if (speed > resolution)
			speed = resolution;
		
		if (speed < 0)
			speed = 0;			
		
		return speed;
	}

	private void ShowError(String text) {
		Toast.makeText(getApplicationContext(), text, Toast.LENGTH_SHORT)
				.show();
	}

	class ClientThread implements Runnable {

		private DatagramSocket socket;		
		private final int port = 8888;
		
		private void InitSocket() throws SocketException {
			if (socket == null) {
				socket = new DatagramSocket(null);
				socket.setReuseAddress(true);
				socket.setBroadcast(true);
				socket.bind(new InetSocketAddress(port));
			}
		}
		
		@Override
		public void run() {
			try {
				InitSocket();

				ByteBuffer buffer = ByteBuffer.allocate(8);
				buffer.putInt(speedLeftWheel);
				buffer.putInt(speedRightWheel);
				
				DatagramPacket out = new DatagramPacket(
						buffer.array(),
						buffer.array().length, 
						InetAddress.getByName("255.255.255.255"), 
						port);
				
				socket.send(out);
				socket.close();
				
			} catch (Exception e) {
				return;
			}
		}

	}
}
