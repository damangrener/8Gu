package cn.ac.iscas.config.mqtt;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.stereotype.Component;

/**
 * @author WTF
 * @date 2022/8/17 14:19
 */
@Slf4j
@Component
public class MqttPushCallback implements MqttCallback {

    @Override
    public void connectionLost(Throwable throwable) {
        log.error("connectionLost:",throwable);
    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        log.info("messageArrived");
      log.info("message topic=>,{},mqttMessage={}",s,mqttMessage.toString());

    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
        IMqttAsyncClient client = iMqttDeliveryToken.getClient();
        System.out.println(client.getClientId()+"发布消息成功！");
    }
}
