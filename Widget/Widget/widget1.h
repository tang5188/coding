#pragma once

#include <QtWidgets/QMainWindow>
#include "ui_widget1.h"

class Widget1 : public QMainWindow
{
	Q_OBJECT

public:
	Widget1(QWidget *parent = Q_NULLPTR);

private:
	Ui::Widget1Class ui;
private slots:
	void on_checkBox_clicked();
	void on_checkBox_2_clicked();
	void on_pushButton_clicked();
	void on_btnClear_clicked();
};
