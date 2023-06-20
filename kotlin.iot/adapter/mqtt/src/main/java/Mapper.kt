package ru.zhuravlev.yuri.adapter.mqtt

import com.ditchoom.buffer.JvmBuffer
import com.ditchoom.buffer.ReadBuffer
import ru.zhuravlev.yuri.core.model.ConfigurationTemperature
import ru.zhuravlev.yuri.core.model.PassiveInfraredSensor
import ru.zhuravlev.yuri.core.model.Temperature
import ru.zhuravlev.yuri.core.model.WaterLevel
import java.nio.ByteBuffer

class Mapper {
    inline fun <reified T> map(payload: ReadBuffer?): T? {
        try {
            if (payload != null)
                when (T::class) {
                    PassiveInfraredSensor::class -> {
                        val pir = payload.readUtf8Line().toString().toInt()
                        return PassiveInfraredSensor(pir == 1) as T
                    }

                    Temperature::class -> {
                        val temperature = payload.readUtf8Line().toString().toInt()
                        return Temperature(temperature) as T
                    }

                    WaterLevel::class -> {
                        val waterLevel = payload.readUtf8Line().toString().toInt()
                        return WaterLevel(waterLevel) as T
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    fun write(configurationTemperature: ConfigurationTemperature): ReadBuffer {
        val stringConfig = configurationTemperature.temperatures.joinToString(prefix = "[", postfix = "]") { temperature -> temperature.value.toString() }
        return JvmBuffer(ByteBuffer.wrap(stringConfig.toByteArray()))
    }
}