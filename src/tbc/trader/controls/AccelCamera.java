package tbc.trader.controls;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;
import tbc.data.spatial.Point3D;
import tbc.trader.Player;

import static android.content.Context.SENSOR_SERVICE;
import static android.hardware.SensorManager.SENSOR_ACCELEROMETER;
import static android.hardware.SensorManager.SENSOR_DELAY_GAME;
import static android.hardware.SensorManager.DATA_X;
import static android.hardware.SensorManager.DATA_Y;

/**
 * An AccelCamera provides camera offset values based on the goings-on of 
 * accelerometer sensor readings. The idea being that the user can physically 
 * pan the camera.
 * 
 * @author Karl Ward
 */
public class AccelCamera implements Player.CameraControl, SensorEventListener {
	
	// Time stamps in this class are taken from sensor times, not game scene
	// times.
	
	/** Scene units per sensor unit per second. */
	private static final float RATE_PER_SECOND = 5.0f;
	
	/** Scene units per sensor unit per nano second. */
	private static final float RATE_PER_NANO = RATE_PER_SECOND / 1000000000;
	
	private Point3D     accumulatedOffset = new Point3D(0.0f, 0.0f, 0.0f);
	private SensorEvent lastAccelEvent;
	private boolean     hasChanged = false;
	private boolean     supported = false;

	public AccelCamera(Context context) {
		SensorManager sm = (SensorManager) context.getSystemService(SENSOR_SERVICE);
		supported = sm.registerListener(this, 
				                        sm.getDefaultSensor(SENSOR_ACCELEROMETER), 
				                        SENSOR_DELAY_GAME);
	}
	
	@Override
	public Point3D getCameraOffset() {
		synchronized (this) {
			hasChanged = false;
			return accumulatedOffset.copy();
		}
	}

	@Override
	public boolean hasCameraOffsetChanged() {
		synchronized (this) {
			return supported && hasChanged;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (event.sensor.getType() == SENSOR_ACCELEROMETER) {
			synchronized (this) {
				if (lastAccelEvent != null) {
					/* Determine rate of change of the X and Y axis. */
					long elapsedNanos = event.timestamp - lastAccelEvent.timestamp;
					float xDelta = event.values[DATA_X] - lastAccelEvent.values[DATA_X];
					float yDelta = event.values[DATA_Y] - lastAccelEvent.values[DATA_Y];

					accumulatedOffset.x += (xDelta * elapsedNanos) * RATE_PER_NANO;
					accumulatedOffset.y += (yDelta * elapsedNanos) * RATE_PER_NANO;

					hasChanged = true;

					Log.i("AccelCamera", xDelta + ", " + yDelta);
				}

				lastAccelEvent = event;
			}
		}
	}


}
