package cn.ac.iscas.config.mqtt;

import org.springframework.integration.annotation.MessagingGateway;
import org.springframework.integration.mqtt.support.MqttHeaders;
import org.springframework.messaging.handler.annotation.Header;

/**
 * @author WTF
 * @date 2022/8/8 16:39
 */
@MessagingGateway(defaultRequestChannel = "mqttOutboundChannel")
public interface MqttGateway {

    void sendToMqtt(String data);

    void sendToMqtt(byte[] data);

    void sendToMqtt(@Header(MqttHeaders.TOPIC) String topic, byte[] data);

}
