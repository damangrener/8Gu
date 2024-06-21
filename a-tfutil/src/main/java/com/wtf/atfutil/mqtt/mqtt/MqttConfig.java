package cn.ac.iscas.config.mqtt;

import cn.ac.iscas.PmoBackendServer;
import cn.ac.iscas.constant.Constant;
import cn.ac.iscas.constant.MessageDataKindEnum;
import cn.ac.iscas.entity.MqttDataHead;
import cn.ac.iscas.util.struct.JavaStruct;
import cn.ac.iscas.util.struct.StructBase;
import cn.ac.iscas.util.struct.StructException;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.IntegrationComponentScan;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.core.DefaultMqttPahoClientFactory;
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.outbound.MqttPahoMessageHandler;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;
import org.springframework.scheduling.annotation.Async;

import java.nio.ByteOrder;

/**
 * @author WTF
 * @date 2022/8/8 15:25
 */
@Configuration
@IntegrationComponentScan
public class MqttConfig {

    @Value("${spring.mqtt.client.username}")
    private String mqttUsername;
    @Value("${spring.mqtt.client.password}")
    private String mqttPassword;
    @Value("${spring.mqtt.client.serverURIs}")
    private String mqttServerURIs;
    @Value("${spring.mqtt.client.clientId}")
    private String mqttClientId;
    @Value("${spring.mqtt.client.keepAliveInterval}")
    private Integer mqttKeepAliveInterval;
    @Value("${spring.mqtt.client.connectionTimeout}")
    private Integer mqttConnectionTimeout;
    @Value("${spring.mqtt.client.topic}")
    private String mqttTopic;

    @Value("${spring.mqtt.producer.defaultQos}")
    private Integer mqttProducerQos;
    @Value("${spring.mqtt.producer.defaultRetained}")
    private Boolean mqttProducerRetained;
    @Value("${spring.mqtt.producer.defaultTopic}")
    private String mqttProducerTopic;

    @Value("${spring.mqtt.consumer.defaultQos}")
    private Integer mqttConsumerQos;
    @Value("${spring.mqtt.consumer.completionTimeout}")
    private Long mqttConsumerCompletionTimeout;
    @Value("${spring.mqtt.consumer.consumerTopics}")
    private String mqttConsumerTopics;
    @Value("${spring.mqtt.consumer.centerTopic}")
    private String mqttCenterTopic;

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    /**
     * 消息驱动
     *
     * @return
     */
    @Bean
    public MessageProducer inbound() {

        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter(mqttClientId, mqttClientFactory(),
                        mqttConsumerTopics);
        // 设置转换器，接收bytes
        DefaultPahoMessageConverter converter = new DefaultPahoMessageConverter();
        converter.setPayloadAsBytes(true);
        adapter.setConverter(converter);
        adapter.setCompletionTimeout(mqttConsumerCompletionTimeout);
        adapter.setQos(mqttConsumerQos);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return message -> {
            String topic = (String) message.getHeaders().get("mqtt_receivedTopic");
            if (topic.equals(mqttCenterTopic)) {
                byte[] payload = (byte[]) message.getPayload();

                byte[] header = new byte[Constant.MQTT_DATA_HEAD_LENGTH];
                System.arraycopy(payload, 0, header, 0, header.length);

                MqttDataHead head = new MqttDataHead();
                try {
                    JavaStruct.unpack(head, header, ByteOrder.LITTLE_ENDIAN);
//                    head.setSourceByteArray(header);
                } catch (StructException e) {
                    e.printStackTrace();
                }

                byte[] status = new byte[payload.length - header.length];
                if (status.length - Constant.MQTT_DATA_HEAD_LENGTH >= 0) {
                    System.arraycopy(payload, Constant.MQTT_DATA_HEAD_LENGTH, status, 0, status.length);
                }

                StructBase struct = MessageDataKindEnum.cStruct2Java(head.getunDataType(), status, header);
                if(null!=struct){
                    PmoBackendServer.messageQueueMap.get(struct.getClass()).offer(struct);
                }

            }

        };
    }

    @Async
    @Bean
    public MqttPahoClientFactory mqttClientFactory() {
        DefaultMqttPahoClientFactory factory = new DefaultMqttPahoClientFactory();
        MqttConnectOptions options = new MqttConnectOptions();
        options.setServerURIs(mqttServerURIs.split(","));
        options.setUserName(mqttUsername);
        options.setPassword(mqttPassword.toCharArray());
        options.setKeepAliveInterval(mqttKeepAliveInterval);
        options.setConnectionTimeout(mqttConnectionTimeout);
        factory.setConnectionOptions(options);
        return factory;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttOutboundChannel")
    public MessageHandler mqttOutbound() {
        MqttPahoMessageHandler messageHandler =
                new MqttPahoMessageHandler("outboundClient", mqttClientFactory());
        messageHandler.setAsync(true);
        messageHandler.setDefaultTopic(mqttProducerTopic);
        messageHandler.setDefaultQos(mqttProducerQos);
        messageHandler.setDefaultRetained(mqttProducerRetained);
        return messageHandler;
    }

    @Bean
    public MessageChannel mqttOutboundChannel() {
        return new DirectChannel();
    }


}
