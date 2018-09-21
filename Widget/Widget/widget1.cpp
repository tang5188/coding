#include "widget1.h"

Widget1::Widget1(QWidget *parent)
	: QMainWindow(parent)
{
	ui.setupUi(this);
	connect(ui.checkBox, SIGNAL(clicked()), this, SLOT(on_checkBox_clicked()));
	connect(ui.checkBox_2, SIGNAL(clicked()), this, SLOT(on_checkBox_2_clicked()));
	connect(ui.pushButton, SIGNAL(clicked()), this, SLOT(on_pushButton_clicked()));
}

//ֻ���л�
void Widget1::on_checkBox_clicked() {
	if (ui.checkBox->isChecked())
	{
		ui.lineEdit->setReadOnly(true);
	}
	else
	{
		ui.lineEdit->setReadOnly(false);
	}
}

//������ʾ�л�
void Widget1::on_checkBox_2_clicked() {
	if (ui.checkBox_2->isChecked()) {
		ui.lineEdit->setEchoMode(QLineEdit::Password);
	}
	else {
		ui.lineEdit->setEchoMode(QLineEdit::Normal);
	}
}

//��ʾlineEidt���ݵ�lineText
void Widget1::on_pushButton_clicked() {
	ui.label->setText(ui.lineEdit->text());
}

//�������
void Widget1::on_btnClear_clicked() {
	ui.lineEdit->setText("");
	ui.label->setText("");
}