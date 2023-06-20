package ru.zhuravlev.yuri.adapter.mqtt

import com.ditchoom.buffer.JvmBuffer
import com.ditchoom.buffer.ReadBuffer
import com.ditchoom.mqtt.controlpacket.ControlPacket.Companion.readVariableByteInteger
import ru.zhuravlev.yuri.core.model.ConfigurationTemperature
import ru.zhuravlev.yuri.core.model.PassiveInfraredSensor
import ru.zhuravlev.yuri.core.model.Temperature
import ru.zhuravlev.yuri.core.model.WaterLevel
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer

class Mapper {
    inline fun <reified T> map(payload: ReadBuffer?): T? {
        try {
            when (T::class) {
                PassiveInfraredSensor::class -> {
                    val pir = payload?.readVariableByteInteger()?.toInt() == 1
                    return PassiveInfraredSensor(pir) as T
                }

                Temperature::class -> {
                    payload?.readVariableByteInteger()?.toInt()?.let { temperature ->
                        return Temperature(temperature) as T
                    }
                }

                WaterLevel::class -> {
                    payload?.readVariableByteInteger()?.toInt()?.let { percent ->
                        if (percent in 0..100)
                            return WaterLevel(percent) as T
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun write(configurationTemperature: ConfigurationTemperature): ReadBuffer {
        val stream = ByteArrayOutputStream()
        configurationTemperature.temperatures.forEach {
            stream.write(it.value)
        }
        return JvmBuffer(ByteBuffer.wrap(stream.toByteArray()))
    }
}