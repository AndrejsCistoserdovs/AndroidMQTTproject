package message.publisher;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

/**
 * Created by Andrei on 04/03/16.
 */
public class MessagePublisher {

    public static void main(String[] args) {

        String topic        = "cia";
        String content      = "hello";   // fixed Test message
        int qos             = 2;    // Quality of Service of the message
        String broker       = "tcp://192.168.0.7:1883"; // local broker
        //String broker       = "tcp://iot.eclipse.org:1883"; // hosted broker
        String clientId     = "first";
        MemoryPersistence persistence = new MemoryPersistence();  // local memory for temporary message storage

        try {
            MqttClient Client = new MqttClient(broker, clientId,persistence);  // create a new client
            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);  // enable clean session
            System.out.println("Connecting to broker: " + broker);
            Client.connect(options);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);

            // This loop publishes the specified number of messages
            for(int i=1;i<1001;i++) {
                content = Integer.toString(i);
                //content="hello";
                MqttMessage message = new MqttMessage(content.getBytes());
                message.setQos(qos);    // setting QoS
                Client.publish(topic, message); //publish the message
                System.out.println(i);

               try {
                    Thread.sleep(1000);     // Delay between each message dispatch
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Message published");
            //sampleClient.disconnect();
            System.out.println("Disconnected");
            System.exit(0);
        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
    }
}
