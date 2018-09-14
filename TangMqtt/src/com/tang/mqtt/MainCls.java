package com.tang.mqtt;

import java.awt.CardLayout;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;
import java.util.zip.DataFormatException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class MainCls extends JFrame implements ActionListener {

	private JPanel jPanel = new JPanel();
	private JButton[] jButtons = new JButton[] { new JButton("开始"),
			new JButton("断开") };

	public MainCls() {
		this.setLayout(null);

		for (int i = 0; i < jButtons.length; i++) {
			jButtons[i].setBounds(280, 30 + 40 * i, 100, 30);
			this.add(jButtons[i]);
			jButtons[i].addActionListener(this);
		}
		jPanel.setLayout(new CardLayout());
		jPanel.add(new MyPanel(0));
		jPanel.setBounds(10, 10, 240, 240);
		this.add(jPanel);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		this.setTitle("测试画面");
		this.setBounds(100, 100, 400, 300);
		this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MainCls();
	}

	// Mqtt连接客户端
	MqttManager manager = null;
	private static Logger logger = Logger.getLogger(MainCls.class);

	@Override
	public void actionPerformed(ActionEvent e) {
		CardLayout cl = (CardLayout) jPanel.getLayout();
		// 确保只会产生一个连接客户端
		if (e.getSource() == jButtons[0]) {

			if (manager == null)
				manager = new MqttManager();
			if (manager.IsConnected())
				return;
			logger.info("button开始连接..");

			String clientID = "ABCDEFG";
			MqttReceiveListener mqttReceiveListener = new MqttReceiveListener() {

				@Override
				public void MqttReceiveListener(String topic, int qos,
						JsonBase message) {
					// 打印出收到的消息
					System.out.println("topic:" + topic + ", value:"
							+ new Gson().toJson(message));
				}
			};
			MqttConnectStatusListener mqttConnectStatusListener = new MqttConnectStatusListener() {

				@Override
				public void MqttConnectStatusListener(int status,
						String dateTime, String info) {
					String log = "连接状态:" + status + (status == 1 ? "连接" : "中断")
							+ ", datetime:" + dateTime + ", info:" + info;
					// Log输出到文件
					if (status == 1)
						logger.info(log);
					else
						logger.error(log);
				}
			};
			// 开启连接
			manager.mqttStart(clientID, 10, mqttReceiveListener,
					mqttConnectStatusListener);
		} else if (e.getSource() == jButtons[1]) { // 断开连接
			if (manager == null)
				return;
			if (!manager.IsConnected())
				return;
			logger.info("button开始关闭..");
			manager.Shutdown();
		}
	}
}

class MyPanel extends JPanel {
	int index = 0;

	public MyPanel(int index) {
		this.index = index + 1;
	}

	public void paint(Graphics g) {
		g.clearRect(0, 0, 250, 250);
		g.drawString("测试画面", 100, 10);
	}
}
