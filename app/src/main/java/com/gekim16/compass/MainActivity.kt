package com.gekim16.compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SensorEventListener {
    private val accelerometerReading = FloatArray(3)
    private val magnetometerReading = FloatArray(3)
    private val orientationAngles = FloatArray(3)

    private val sensorManager by lazy { getSystemService(Context.SENSOR_SERVICE) as SensorManager }
    private val accelerometer by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) }
    private val magneticField by lazy { sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onResume() {  // onResume() 이 호출되면 센서 등록
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
        sensorManager.registerListener(this, magneticField, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {  // 메모리 누수를 막기위해서 화면을 벗어나면 센서 등록 해제
        super.onPause()
        sensorManager.unregisterListener(this, accelerometer)
        sensorManager.unregisterListener(this, magneticField)
    }

    //센서 정확도가 바뀌었을 때
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {

    }

    //센서 값이 바뀌었을 때
    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerometerReading, 0, event.values.size)
        }
        else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magnetometerReading, 0, event.values.size)
        }

        if (accelerometerReading.isNotEmpty() && magnetometerReading.isNotEmpty()) {
            val rotationMatrix = FloatArray(9)

            // Orientation 을 계산하는 메소드는 Android 2.2(API 레벨 8) 이상부터 지원 중단, getRotationMatrix() 와 getOrientation() 로 구할 수 있음
            SensorManager.getRotationMatrix(rotationMatrix, null, accelerometerReading, magnetometerReading)

            // orientationAngles 배열은 [Azimuth(z축 회전 각도), Pitch(x축 회전 각도), Roll(y축 회전 각도)] 로 구성되어 있다.
            // -180 ~ 180 인 범위를 0 ~ 360 으로 변경시키고 라디안을 각도로 바꿔줌
            val azimuthDegrees = -(Math.toDegrees(
                SensorManager.getOrientation(rotationMatrix, orientationAngles)[0].toDouble()) + 360).toInt() % 360.toFloat()

            // 이미지 회전
            compass.rotation = azimuthDegrees

            textView.text = String.format(getString(R.string.angle_string), (-compass.rotation.toInt()).toString())
        }
    }
}
