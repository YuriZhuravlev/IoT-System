# IoT System

Проект по созданию полной системы IoT с использованием MQTT-брокера.

**kotlin.iot** – часть реализации на языке Kotlin: базовые модели, логика и сервер Ktor. Для тестирования добавлена эмуляция сообщений от устройств. Используется протокол MQTT для общения с устройствами и SSE для обновления данных у клиента (в прототипе это HTML страница).

**info** – вспомогательная информация


Для запуска сервера:
```
cd kotlin.iot
./gradlew :server:buildFatJar
cd ../
java -jar kotlin.iot/server/build/libs/iotSystem.jar
```

Для запуска эмулятора девайсов:
```
cd kotlin.iot
./gradlew :device:shadowJar
cd ../
java -jar kotlin.iot/device/build/libs/deviceEmulator.jar-1.0-all.jar
```