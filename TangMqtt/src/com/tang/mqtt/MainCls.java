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
	private JButton[] jButtons = new JButton[] { new JButton("��ʼ"),
			new JButton("�Ͽ�") };

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
		this.setTitle("���Ի���");
		this.setBounds(100, 100, 400, 300);
		this.setVisible(true);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new MainCls();
	}

	// Mqtt���ӿͻ���
	MqttManager manager = null;
	private static Logger logger = Logger.getLogger(MainCls.class);

	@Override
	public void actionPerformed(ActionEvent e) {
		CardLayout cl = (CardLayout) jPanel.getLayout();
		// ȷ��ֻ�����һ�����ӿͻ���
		if (e.getSource() == jButtons[0]) {

			if (manager == null)
				manager = new MqttManager();
			if (manager.IsConnected())
				return;
			logger.info("button��ʼ����..");

			String clientID = "ABCDEFG";
			MqttReceiveListener mqttReceiveListener = new MqttReceiveListener() {

				@Override
				public void MqttReceiveListener(String topic, int qos,
						JsonBase message) {
					// ��ӡ���յ�����Ϣ
					System.out.println("topic:" + topic + ", value:"
							+ new Gson().toJson(message));
				}
			};
			MqttConnectStatusListener mqttConnectStatusListener = new MqttConnectStatusListener() {

				@Override
				public void MqttConnectStatusListener(int status,
						String dateTime, String info) {
					String log = "����״̬:" + status + (status == 1 ? "����" : "�ж�")
							+ ", datetime:" + dateTime + ", info:" + info;
					// Log������ļ�
					if (status == 1)
						logger.info(log);
					else
						logger.error(log);
				}
			};
			// ��������
			manager.mqttStart(clientID, 10, mqttReceiveListener,
					mqttConnectStatusListener);
		} else if (e.getSource() == jButtons[1]) { // �Ͽ�����
			if (manager == null)
				return;
			if (!manager.IsConnected())
				return;
			logger.info("button��ʼ�ر�..");
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
		g.drawString("���Ի���", 100, 10);
	}
}
