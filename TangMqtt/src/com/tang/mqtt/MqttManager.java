package com.tang.mqtt;

import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import com.tang.mqtt.info.JsonTime;
import com.tang.mqtt.info.WillInfo;

public class MqttManager {
	private static final String host = "tcp://47.98.238.36:61613";
	private static final String userName = "admin";
	private static final String password = "password";
	public static final String appId = "xls001";
	public static final String myTopicSend = appId + "/server/message";

	private MqttClient client;
	private MqttConnectOptions options;

	Logger logger = Logger.getLogger(MqttManager.class);

	public MqttManager() {
	}

	private static String clientId = null;

	private static String getClientId() {
		if (null == clientId) {
			synchronized (MqttManager.class) {
				if (null == clientId) {
					clientId = "Undefined";
				}
			}
		}
		return clientId;
	}

	public boolean IsConnected() {
		if (this.client == null)
			return false;
		return this.client.isConnected();
	}

	private void initOptions() {
		options = new MqttConnectOptions();
		options.setCleanSession(true);
		options.setUserName(userName);
		options.setPassword(password.toCharArray());
		options.setConnectionTimeout(30);
		options.setKeepAliveInterval(60);
		WillInfo willinfo = new WillInfo();
		willinfo.box_id = getClientId();
		willinfo.value = "close";
		options.setWill(myTopicSend, willinfo.toJsonString().getBytes(), 0,
				false); // 遗嘱
	}

	public void setCallback(final MqttReceiveListener handler,
			final MqttConnectStatusListener connectStatus) {
		if (null == client) {
			return;
		}
		client.setCallback(new MqttCallback() {
			// 连接丢失后，一般在这里面进行重连
			@Override
			public void connectionLost(Throwable throwable) {
				if (null != connectStatus) {
					String dateTime = JsonTime.getServiceTime();
					connectStatus.MqttConnectStatusListener(-1,
							null == dateTime ? "null" : dateTime, null);
					logger.error("连接丢失... serverTime:" + dateTime);
				}
			}

			// subscribe后得到的消息会执行到这里面
			@Override
			public void messageArrived(String topicName, MqttMessage message) {
				if (null != handler) {
					String str = new String(message.getPayload());
					JsonBase jsonBase = JsonBase.fromJson(str);
					handler.MqttReceiveListener(topicName, message.getQos(),
							jsonBase);
				}
			}

			// publish后会执行到这里
			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
			}
		});
	}

	private ScheduledExecutorService scheduler;

	private boolean crateClient() {
		if (null == client) {
			try {
				String clientId = UUID.randomUUID().toString().replace("-", "");
				client = new MqttClient(host, clientId, new MemoryPersistence());
				return true;
			} catch (MqttException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}

	public void mqttStart(String serial, int period,
			final MqttReceiveListener handler,
			final MqttConnectStatusListener connectStatus) {
		MqttManager.clientId = serial;
		if (!crateClient()) {
			return;
		}

		if (scheduler != null) {
			return;
		}
		scheduler = Executors.newSingleThreadScheduledExecutor();
		scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				connect(handler, connectStatus);
			}
		}, 0, period * 1000, TimeUnit.MILLISECONDS);
	}

	private synchronized void connect(MqttReceiveListener handler,
			MqttConnectStatusListener connectStatus) {
		if (!crateClient()) {
			return;
		}
		// String dateTime2 = new
		// SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
		String dateTime = JsonTime.getServiceTime();
		if (null == dateTime) {
			connectStatus.MqttConnectStatusListener(0, "null", null);
			return;
		}

		if (client.isConnected()) {
			if (null != connectStatus) {
				connectStatus.MqttConnectStatusListener(1, dateTime, null);
			}
			return;
		}

		this.initOptions();
		this.setCallback(handler, connectStatus);
		String[] topic = new String[] { appId + "/" + getClientId() + "/"
				+ "event", };
		int[] qos = new int[] { 0 };
		try {
			logger.info("connect begin...");
			client.connect(options);
			client.subscribe(topic, qos);
			connectStatus.MqttConnectStatusListener(1, dateTime, null);
			logger.info("connect end...");
		} catch (MqttException e) {
			e.printStackTrace();
			if (null != connectStatus) {
				connectStatus.MqttConnectStatusListener(0, dateTime, null);
			}
			logger.error("connect error...");
		}
	}

	public void Publish(String msg) {
		Publish(myTopicSend, 0, msg);
	}

	public synchronized void Publish(String topic, int qos, String msg) {
		if ((null == client) || (!client.isConnected())) {
			return;
		}
		try {
			client.getTopic(topic).publish(msg.getBytes(), qos, false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public synchronized void sendLog(String log) {
		if ((null == client) || (!client.isConnected())) {
			return;
		}
		try {
			log = getClientId() + "    " + log;
			System.out.println(log);
			client.getTopic("lichen/20180524/message").publish(log.getBytes(),
					0, false);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public void Shutdown() {
		try {
			scheduler.shutdown();
			client.disconnect();
			scheduler = null;
		} catch (Exception ex) {
			logger.error("shutdown", ex);
		}
	}
}
