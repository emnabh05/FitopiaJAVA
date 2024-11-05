/********************************************************************************
** Form generated from reading UI file 'employeee.ui'
**
** Created by: Qt User Interface Compiler version 6.6.3
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_EMPLOYEEE_H
#define UI_EMPLOYEEE_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QComboBox>
#include <QtWidgets/QFrame>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QMainWindow>
#include <QtWidgets/QMenuBar>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QScrollArea>
#include <QtWidgets/QStackedWidget>
#include <QtWidgets/QStatusBar>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_employeee
{
public:
    QWidget *centralwidget;
    QLabel *label;
    QLabel *label_2;
    QFrame *frame_2;
    QLabel *label_3;
    QLabel *label_4;
    QLabel *label_7;
    QStackedWidget *stackedWidget;
    QWidget *page_1;
    QFrame *frame_12;
    QLineEdit *lineEdit_Address;
    QLabel *label_38;
    QLineEdit *lineEdit_Tel;
    QLineEdit *lineEdit_Email;
    QLineEdit *lineEdit_Codep;
    QPushButton *pushButton_5;
    QFrame *frame_11;
    QLineEdit *lineEdit_CIN;
    QLabel *label_39;
    QLineEdit *lineEdit_Name;
    QLineEdit *lineEdit_Surname;
    QLineEdit *lineEdit_role_e;
    QLineEdit *lineEdit_rfid;
    QWidget *page_2;
    QFrame *frame_17;
    QScrollArea *scrollArea;
    QWidget *scrollAreaWidgetContents;
    QComboBox *comboBox;
    QLineEdit *lineEdit_recherche;
    QWidget *page_3;
    QFrame *frame_13;
    QLineEdit *lineEdit_CIN_4;
    QLabel *label_42;
    QLineEdit *lineEdit_Name_4;
    QLineEdit *lineEdit_Surname_4;
    QLineEdit *lineEdit_role_e_4;
    QFrame *frame_14;
    QLineEdit *lineEdit_Address_2;
    QLabel *label_43;
    QLineEdit *lineEdit_Tel_2;
    QLineEdit *lineEdit_Email_2;
    QLineEdit *lineEdit_Codep_2;
    QPushButton *pushButton_6;
    QFrame *frame_54;
    QPushButton *pushButton_1;
    QPushButton *pushButton_2;
    QFrame *frame;
    QVBoxLayout *verticalLayout_2;
    QFrame *frame_3;
    QLabel *label_5;
    QLabel *label_6;
    QFrame *frame_5;
    QVBoxLayout *verticalLayout;
    QFrame *frame_6;
    QHBoxLayout *horizontalLayout_4;
    QLabel *label_10;
    QLabel *label_11;
    QFrame *frame_7;
    QHBoxLayout *horizontalLayout_5;
    QLabel *label_12;
    QLabel *label_13;
    QFrame *frame_8;
    QHBoxLayout *horizontalLayout_6;
    QLabel *label_14;
    QLabel *label_15;
    QFrame *frame_9;
    QHBoxLayout *horizontalLayout_7;
    QLabel *label_16;
    QLabel *label_17;
    QFrame *frame_10;
    QHBoxLayout *horizontalLayout_8;
    QLabel *label_18;
    QLabel *label_19;
    QPushButton *pushButton;
    QMenuBar *menubar;
    QStatusBar *statusbar;

    void setupUi(QMainWindow *employeee)
    {
        if (employeee->objectName().isEmpty())
            employeee->setObjectName("employeee");
        employeee->resize(1251, 755);
        centralwidget = new QWidget(employeee);
        centralwidget->setObjectName("centralwidget");
        label = new QLabel(centralwidget);
        label->setObjectName("label");
        label->setGeometry(QRect(-10, 10, 1251, 201));
        label->setStyleSheet(QString::fromUtf8("background-color: rgb(94, 114, 228);"));
        label_2 = new QLabel(centralwidget);
        label_2->setObjectName("label_2");
        label_2->setGeometry(QRect(-20, 120, 1271, 581));
        label_2->setStyleSheet(QString::fromUtf8("background-color: rgb(245, 246, 247);"));
        frame_2 = new QFrame(centralwidget);
        frame_2->setObjectName("frame_2");
        frame_2->setGeometry(QRect(310, 50, 171, 41));
        frame_2->setFrameShape(QFrame::StyledPanel);
        frame_2->setFrameShadow(QFrame::Raised);
        label_3 = new QLabel(frame_2);
        label_3->setObjectName("label_3");
        label_3->setGeometry(QRect(10, 10, 51, 21));
        QFont font;
        font.setFamilies({QString::fromUtf8("Segoe MDL2 Assets")});
        font.setPointSize(10);
        font.setBold(true);
        label_3->setFont(font);
        label_3->setStyleSheet(QString::fromUtf8("color: rgb(158, 175, 240);"));
        label_4 = new QLabel(frame_2);
        label_4->setObjectName("label_4");
        label_4->setGeometry(QRect(80, 10, 81, 21));
        QFont font1;
        font1.setFamilies({QString::fromUtf8("Segoe MDL2 Assets")});
        font1.setPointSize(10);
        label_4->setFont(font1);
        label_4->setStyleSheet(QString::fromUtf8("color: rgb(255, 255, 255);"));
        label_7 = new QLabel(frame_2);
        label_7->setObjectName("label_7");
        label_7->setGeometry(QRect(67, 10, 21, 21));
        QFont font2;
        font2.setPointSize(15);
        label_7->setFont(font2);
        label_7->setStyleSheet(QString::fromUtf8("color: rgb(255, 255, 255);"));
        stackedWidget = new QStackedWidget(centralwidget);
        stackedWidget->setObjectName("stackedWidget");
        stackedWidget->setGeometry(QRect(300, 110, 941, 531));
        stackedWidget->setStyleSheet(QString::fromUtf8(""));
        page_1 = new QWidget();
        page_1->setObjectName("page_1");
        frame_12 = new QFrame(page_1);
        frame_12->setObjectName("frame_12");
        frame_12->setGeometry(QRect(450, 20, 421, 501));
        frame_12->setStyleSheet(QString::fromUtf8("background-color: rgb(73, 53,\n"
"                                                        212);\n"
"                                                        border-top-right-radius: 15px;\n"
"                                                        border-bottom-right-radius: 15px;\n"
"                                                 "));
        frame_12->setFrameShape(QFrame::StyledPanel);
        frame_12->setFrameShadow(QFrame::Raised);
        lineEdit_Address = new QLineEdit(frame_12);
        lineEdit_Address->setObjectName("lineEdit_Address");
        lineEdit_Address->setGeometry(QRect(30, 130, 381, 27));
        QFont font3;
        font3.setFamilies({QString::fromUtf8("Segoe MDL2 Assets")});
        font3.setPointSize(10);
        font3.setBold(false);
        lineEdit_Address->setFont(font3);
        lineEdit_Address->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        label_38 = new QLabel(frame_12);
        label_38->setObjectName("label_38");
        label_38->setGeometry(QRect(140, 10, 161, 51));
        QFont font4;
        font4.setFamilies({QString::fromUtf8("Nirmala UI")});
        font4.setPointSize(15);
        font4.setBold(true);
        label_38->setFont(font4);
        label_38->setStyleSheet(QString::fromUtf8("color: rgb(245, 246,\n"
"                                                               247)"));
        lineEdit_Tel = new QLineEdit(frame_12);
        lineEdit_Tel->setObjectName("lineEdit_Tel");
        lineEdit_Tel->setGeometry(QRect(30, 236, 381, 27));
        lineEdit_Tel->setFont(font3);
        lineEdit_Tel->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        lineEdit_Email = new QLineEdit(frame_12);
        lineEdit_Email->setObjectName("lineEdit_Email");
        lineEdit_Email->setGeometry(QRect(30, 290, 381, 27));
        lineEdit_Email->setFont(font3);
        lineEdit_Email->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        lineEdit_Codep = new QLineEdit(frame_12);
        lineEdit_Codep->setObjectName("lineEdit_Codep");
        lineEdit_Codep->setGeometry(QRect(30, 179, 131, 27));
        lineEdit_Codep->setFont(font3);
        lineEdit_Codep->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        pushButton_5 = new QPushButton(frame_12);
        pushButton_5->setObjectName("pushButton_5");
        pushButton_5->setGeometry(QRect(40, 410, 221, 51));
        QFont font5;
        font5.setPointSize(13);
        font5.setBold(true);
        pushButton_5->setFont(font5);
        pushButton_5->setStyleSheet(QString::fromUtf8("QPushButton#pushButton_5{\n"
"                                                               background-color:\n"
"                                                               qlineargradient(spread:pad, x1:0,\n"
"                                                               y1:0, x2:1, y2:1, stop:0 rgba(0, 144,\n"
"                                                               255, 255), stop:1 rgba(207, 0, 255,\n"
"                                                               194));\n"
"                                                               color:rgba(255, 255, 255, 210);\n"
"                                                               border-radius:5px;\n"
"                                                               }\n"
"                                                               QPushButton#pushButton_5:hover{\n"
"                                                               background-color:qlineargradient(spread:pad,\n"
"                                                              "
                        " x1:0, y1:0, x2:1, y2:1, stop:0\n"
"                                                               rgba(0, 0, 255, 255), stop:0.873684\n"
"                                                               rgba(255, 0, 255, 227))\n"
"                                                               }\n"
"                                                               QPushButton#pushButton_5:pressed{\n"
"                                                               padding-left:5px;\n"
"                                                               padding-top:5px;\n"
"                                                               background-color:rgb(170, 0, 255);\n"
"                                                               }"));
        frame_11 = new QFrame(page_1);
        frame_11->setObjectName("frame_11");
        frame_11->setGeometry(QRect(40, 20, 421, 501));
        frame_11->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255,\n"
"                                                        255);\n"
"                                                        border-top-left-radius: 15px;\n"
"                                                        border-bottom-left-radius: 15px;"));
        frame_11->setFrameShape(QFrame::StyledPanel);
        frame_11->setFrameShadow(QFrame::Raised);
        lineEdit_CIN = new QLineEdit(frame_11);
        lineEdit_CIN->setObjectName("lineEdit_CIN");
        lineEdit_CIN->setGeometry(QRect(22, 130, 371, 27));
        lineEdit_CIN->setFont(font3);
        lineEdit_CIN->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        label_39 = new QLabel(frame_11);
        label_39->setObjectName("label_39");
        label_39->setGeometry(QRect(100, 10, 241, 51));
        label_39->setFont(font4);
        lineEdit_Name = new QLineEdit(frame_11);
        lineEdit_Name->setObjectName("lineEdit_Name");
        lineEdit_Name->setGeometry(QRect(19, 179, 184, 27));
        lineEdit_Name->setFont(font3);
        lineEdit_Name->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        lineEdit_Surname = new QLineEdit(frame_11);
        lineEdit_Surname->setObjectName("lineEdit_Surname");
        lineEdit_Surname->setGeometry(QRect(209, 179, 183, 27));
        lineEdit_Surname->setFont(font3);
        lineEdit_Surname->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        lineEdit_role_e = new QLineEdit(frame_11);
        lineEdit_role_e->setObjectName("lineEdit_role_e");
        lineEdit_role_e->setGeometry(QRect(20, 236, 371, 27));
        lineEdit_role_e->setFont(font3);
        lineEdit_role_e->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        lineEdit_rfid = new QLineEdit(frame_11);
        lineEdit_rfid->setObjectName("lineEdit_rfid");
        lineEdit_rfid->setGeometry(QRect(20, 300, 371, 27));
        lineEdit_rfid->setFont(font3);
        lineEdit_rfid->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        stackedWidget->addWidget(page_1);
        page_2 = new QWidget();
        page_2->setObjectName("page_2");
        frame_17 = new QFrame(page_2);
        frame_17->setObjectName("frame_17");
        frame_17->setGeometry(QRect(20, 50, 901, 481));
        frame_17->setFrameShape(QFrame::StyledPanel);
        frame_17->setFrameShadow(QFrame::Raised);
        scrollArea = new QScrollArea(frame_17);
        scrollArea->setObjectName("scrollArea");
        scrollArea->setGeometry(QRect(0, 0, 901, 531));
        scrollArea->setStyleSheet(QString::fromUtf8("border:none;\n"
"                                                               background-color: transparent;"));
        scrollArea->setWidgetResizable(true);
        scrollAreaWidgetContents = new QWidget();
        scrollAreaWidgetContents->setObjectName("scrollAreaWidgetContents");
        scrollAreaWidgetContents->setGeometry(QRect(0, 0, 901, 531));
        scrollArea->setWidget(scrollAreaWidgetContents);
        comboBox = new QComboBox(page_2);
        comboBox->addItem(QString());
        comboBox->addItem(QString());
        comboBox->addItem(QString());
        comboBox->addItem(QString());
        comboBox->addItem(QString());
        comboBox->setObjectName("comboBox");
        comboBox->setGeometry(QRect(830, 10, 73, 22));
        lineEdit_recherche = new QLineEdit(page_2);
        lineEdit_recherche->setObjectName("lineEdit_recherche");
        lineEdit_recherche->setGeometry(QRect(640, 10, 113, 22));
        stackedWidget->addWidget(page_2);
        page_3 = new QWidget();
        page_3->setObjectName("page_3");
        frame_13 = new QFrame(page_3);
        frame_13->setObjectName("frame_13");
        frame_13->setGeometry(QRect(60, 40, 421, 501));
        frame_13->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255,\n"
"                                                        255);\n"
"                                                        border-top-left-radius: 15px;\n"
"                                                        border-bottom-left-radius: 15px;"));
        frame_13->setFrameShape(QFrame::StyledPanel);
        frame_13->setFrameShadow(QFrame::Raised);
        lineEdit_CIN_4 = new QLineEdit(frame_13);
        lineEdit_CIN_4->setObjectName("lineEdit_CIN_4");
        lineEdit_CIN_4->setGeometry(QRect(22, 130, 371, 27));
        lineEdit_CIN_4->setFont(font3);
        lineEdit_CIN_4->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        label_42 = new QLabel(frame_13);
        label_42->setObjectName("label_42");
        label_42->setGeometry(QRect(100, 10, 241, 51));
        label_42->setFont(font4);
        lineEdit_Name_4 = new QLineEdit(frame_13);
        lineEdit_Name_4->setObjectName("lineEdit_Name_4");
        lineEdit_Name_4->setGeometry(QRect(19, 179, 184, 27));
        lineEdit_Name_4->setFont(font3);
        lineEdit_Name_4->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        lineEdit_Surname_4 = new QLineEdit(frame_13);
        lineEdit_Surname_4->setObjectName("lineEdit_Surname_4");
        lineEdit_Surname_4->setGeometry(QRect(209, 179, 183, 27));
        lineEdit_Surname_4->setFont(font3);
        lineEdit_Surname_4->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        lineEdit_role_e_4 = new QLineEdit(frame_13);
        lineEdit_role_e_4->setObjectName("lineEdit_role_e_4");
        lineEdit_role_e_4->setGeometry(QRect(20, 236, 371, 27));
        lineEdit_role_e_4->setFont(font3);
        lineEdit_role_e_4->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid\n"
"                                                               rgba(46,82,101,200);\n"
"                                                               color:rgba(0,0,0,240);\n"
"                                                               padding-bottom:7px;"));
        frame_14 = new QFrame(page_3);
        frame_14->setObjectName("frame_14");
        frame_14->setGeometry(QRect(470, 40, 421, 501));
        frame_14->setStyleSheet(QString::fromUtf8("background-color: rgb(73, 53,\n"
"                                                        212);\n"
"                                                        border-top-right-radius: 15px;\n"
"                                                        border-bottom-right-radius: 15px;\n"
"                                                 "));
        frame_14->setFrameShape(QFrame::StyledPanel);
        frame_14->setFrameShadow(QFrame::Raised);
        lineEdit_Address_2 = new QLineEdit(frame_14);
        lineEdit_Address_2->setObjectName("lineEdit_Address_2");
        lineEdit_Address_2->setGeometry(QRect(30, 130, 381, 27));
        lineEdit_Address_2->setFont(font3);
        lineEdit_Address_2->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        label_43 = new QLabel(frame_14);
        label_43->setObjectName("label_43");
        label_43->setGeometry(QRect(140, 10, 161, 51));
        label_43->setFont(font4);
        label_43->setStyleSheet(QString::fromUtf8("color: rgb(245, 246,\n"
"                                                               247)"));
        lineEdit_Tel_2 = new QLineEdit(frame_14);
        lineEdit_Tel_2->setObjectName("lineEdit_Tel_2");
        lineEdit_Tel_2->setGeometry(QRect(30, 236, 381, 27));
        lineEdit_Tel_2->setFont(font3);
        lineEdit_Tel_2->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        lineEdit_Email_2 = new QLineEdit(frame_14);
        lineEdit_Email_2->setObjectName("lineEdit_Email_2");
        lineEdit_Email_2->setGeometry(QRect(30, 290, 381, 27));
        lineEdit_Email_2->setFont(font3);
        lineEdit_Email_2->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        lineEdit_Codep_2 = new QLineEdit(frame_14);
        lineEdit_Codep_2->setObjectName("lineEdit_Codep_2");
        lineEdit_Codep_2->setGeometry(QRect(30, 179, 131, 27));
        lineEdit_Codep_2->setFont(font3);
        lineEdit_Codep_2->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"                                                               border:none;\n"
"                                                               border-bottom:2px solid rgb(245, 246,\n"
"                                                               247);\n"
"                                                               color:white;\n"
"                                                               padding-bottom:7px;"));
        pushButton_6 = new QPushButton(frame_14);
        pushButton_6->setObjectName("pushButton_6");
        pushButton_6->setGeometry(QRect(40, 410, 221, 51));
        pushButton_6->setFont(font5);
        pushButton_6->setStyleSheet(QString::fromUtf8("QPushButton#pushButton_6{\n"
"                                                               background-color:\n"
"                                                               qlineargradient(spread:pad, x1:0,\n"
"                                                               y1:0, x2:1, y2:1, stop:0 rgba(0, 144,\n"
"                                                               255, 255), stop:1 rgba(207, 0, 255,\n"
"                                                               194));\n"
"                                                               color:rgba(255, 255, 255, 210);\n"
"                                                               border-radius:5px;\n"
"                                                               }\n"
"                                                               QPushButton#pushButton_6:hover{\n"
"                                                               background-color:qlineargradient(spread:pad,\n"
"                                                              "
                        " x1:0, y1:0, x2:1, y2:1, stop:0\n"
"                                                               rgba(0, 0, 255, 255), stop:0.873684\n"
"                                                               rgba(255, 0, 255, 227))\n"
"                                                               }\n"
"                                                               QPushButton#pushButton_6:pressed{\n"
"                                                               padding-left:5px;\n"
"                                                               padding-top:5px;\n"
"                                                               background-color:rgb(170, 0, 255);\n"
"                                                               }"));
        stackedWidget->addWidget(page_3);
        frame_54 = new QFrame(centralwidget);
        frame_54->setObjectName("frame_54");
        frame_54->setGeometry(QRect(620, 30, 269, 60));
        frame_54->setFrameShape(QFrame::StyledPanel);
        frame_54->setFrameShadow(QFrame::Raised);
        pushButton_1 = new QPushButton(frame_54);
        pushButton_1->setObjectName("pushButton_1");
        pushButton_1->setGeometry(QRect(9, 17, 121, 31));
        pushButton_1->setStyleSheet(QString::fromUtf8("QPushButton#pushButton_1{\n"
"                                                 background-color: rgb(0, 0, 127);\n"
"                                                 color: rgb(255, 255, 255);\n"
"                                                 border-top-left-radius:10px;\n"
"                                                 border-bottom-left-radius:10px;\n"
"                                                 }\n"
"\n"
"                                                 QPushButton#pushButton_1:hover{\n"
"                                                 background-color:qlineargradient(spread:pad, x1:0,\n"
"                                                 y1:0, x2:1, y2:1, stop:0 rgba(0, 0, 255, 255),\n"
"                                                 stop:0.873684 rgba(255, 0, 255, 227));\n"
"                                                 }\n"
"\n"
"                                                 QPushButton#pushButton_1:pressed{\n"
"                                                 background-color:rgb(170"
                        ", 0, 255);\n"
"                                                 }"));
        pushButton_2 = new QPushButton(frame_54);
        pushButton_2->setObjectName("pushButton_2");
        pushButton_2->setGeometry(QRect(130, 17, 121, 31));
        pushButton_2->setStyleSheet(QString::fromUtf8("QPushButton#pushButton_2{\n"
"                                                 background-color: rgb(85, 170, 255);\n"
"                                                 color: rgb(255, 255, 255);\n"
"                                                 border-top-right-radius:10px;\n"
"                                                 border-bottom-right-radius:10px;\n"
"                                                 }\n"
"\n"
"                                                 QPushButton#pushButton_2:hover{\n"
"                                                 background-color:qlineargradient(spread:pad, x1:0,\n"
"                                                 y1:0, x2:1, y2:1, stop:0 rgba(0, 0, 255, 255),\n"
"                                                 stop:0.873684 rgba(255, 0, 255, 227));\n"
"                                                 }\n"
"\n"
"                                                 QPushButton#pushButton_2:pressed{\n"
"                                                 background-color:rg"
                        "b(170, 0, 255);\n"
"                                                 }"));
        frame = new QFrame(centralwidget);
        frame->setObjectName("frame");
        frame->setGeometry(QRect(20, 30, 281, 611));
        frame->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"                                          border-radius:25px;"));
        frame->setFrameShape(QFrame::StyledPanel);
        frame->setFrameShadow(QFrame::Raised);
        verticalLayout_2 = new QVBoxLayout(frame);
        verticalLayout_2->setObjectName("verticalLayout_2");
        frame_3 = new QFrame(frame);
        frame_3->setObjectName("frame_3");
        frame_3->setFrameShape(QFrame::StyledPanel);
        frame_3->setFrameShadow(QFrame::Raised);
        label_5 = new QLabel(frame_3);
        label_5->setObjectName("label_5");
        label_5->setGeometry(QRect(0, 10, 130, 130));
        label_5->setMinimumSize(QSize(130, 130));
        label_5->setMaximumSize(QSize(130, 130));
        label_5->setPixmap(QPixmap(QString::fromUtf8("\n"
"                                                                      :/img/images/logoblue.png")));
        label_5->setScaledContents(true);
        label_6 = new QLabel(frame_3);
        label_6->setObjectName("label_6");
        label_6->setGeometry(QRect(110, 50, 131, 51));
        label_6->setFont(font4);
        label_6->setStyleSheet(QString::fromUtf8("color: rgb(102,\n"
"                                                                      115, 231);"));

        verticalLayout_2->addWidget(frame_3);

        frame_5 = new QFrame(frame);
        frame_5->setObjectName("frame_5");
        frame_5->setFrameShape(QFrame::StyledPanel);
        frame_5->setFrameShadow(QFrame::Raised);
        verticalLayout = new QVBoxLayout(frame_5);
        verticalLayout->setObjectName("verticalLayout");
        frame_6 = new QFrame(frame_5);
        frame_6->setObjectName("frame_6");
        frame_6->setFrameShape(QFrame::NoFrame);
        frame_6->setFrameShadow(QFrame::Raised);
        horizontalLayout_4 = new QHBoxLayout(frame_6);
        horizontalLayout_4->setObjectName("horizontalLayout_4");
        label_10 = new QLabel(frame_6);
        label_10->setObjectName("label_10");
        label_10->setMinimumSize(QSize(40, 40));
        label_10->setMaximumSize(QSize(40, 40));
        label_10->setPixmap(QPixmap(QString::fromUtf8("\n"
"                                                                                                         :/img/images/employee.png")));
        label_10->setScaledContents(false);

        horizontalLayout_4->addWidget(label_10);

        label_11 = new QLabel(frame_6);
        label_11->setObjectName("label_11");
        QFont font6;
        font6.setFamilies({QString::fromUtf8("\n"
"                                                                                                                Segoe\n"
"                                                                                                                UI\n"
"                                                                                                                Emoji")});
        font6.setPointSize(10);
        label_11->setFont(font6);

        horizontalLayout_4->addWidget(label_11);


        verticalLayout->addWidget(frame_6);

        frame_7 = new QFrame(frame_5);
        frame_7->setObjectName("frame_7");
        frame_7->setFrameShape(QFrame::NoFrame);
        frame_7->setFrameShadow(QFrame::Raised);
        horizontalLayout_5 = new QHBoxLayout(frame_7);
        horizontalLayout_5->setObjectName("horizontalLayout_5");
        label_12 = new QLabel(frame_7);
        label_12->setObjectName("label_12");
        label_12->setMinimumSize(QSize(40, 40));
        label_12->setMaximumSize(QSize(40, 40));
        label_12->setPixmap(QPixmap(QString::fromUtf8("\n"
"                                                                                                         :/img/images/license-plate.png")));
        label_12->setScaledContents(false);

        horizontalLayout_5->addWidget(label_12);

        label_13 = new QLabel(frame_7);
        label_13->setObjectName("label_13");
        label_13->setFont(font6);

        horizontalLayout_5->addWidget(label_13);


        verticalLayout->addWidget(frame_7);

        frame_8 = new QFrame(frame_5);
        frame_8->setObjectName("frame_8");
        frame_8->setFrameShape(QFrame::NoFrame);
        frame_8->setFrameShadow(QFrame::Raised);
        horizontalLayout_6 = new QHBoxLayout(frame_8);
        horizontalLayout_6->setObjectName("horizontalLayout_6");
        label_14 = new QLabel(frame_8);
        label_14->setObjectName("label_14");
        label_14->setMinimumSize(QSize(40, 40));
        label_14->setMaximumSize(QSize(40, 40));
        label_14->setPixmap(QPixmap(QString::fromUtf8("\n"
"                                                                                                         :/img/images/license-plate.png")));
        label_14->setScaledContents(false);

        horizontalLayout_6->addWidget(label_14);

        label_15 = new QLabel(frame_8);
        label_15->setObjectName("label_15");
        label_15->setFont(font6);

        horizontalLayout_6->addWidget(label_15);


        verticalLayout->addWidget(frame_8);

        frame_9 = new QFrame(frame_5);
        frame_9->setObjectName("frame_9");
        frame_9->setFrameShape(QFrame::NoFrame);
        frame_9->setFrameShadow(QFrame::Raised);
        horizontalLayout_7 = new QHBoxLayout(frame_9);
        horizontalLayout_7->setObjectName("horizontalLayout_7");
        label_16 = new QLabel(frame_9);
        label_16->setObjectName("label_16");
        label_16->setMinimumSize(QSize(40, 40));
        label_16->setMaximumSize(QSize(40, 40));
        label_16->setPixmap(QPixmap(QString::fromUtf8("\n"
"                                                                                                         :/img/images/license-plate.png")));
        label_16->setScaledContents(false);

        horizontalLayout_7->addWidget(label_16);

        label_17 = new QLabel(frame_9);
        label_17->setObjectName("label_17");
        label_17->setFont(font6);

        horizontalLayout_7->addWidget(label_17);


        verticalLayout->addWidget(frame_9);

        frame_10 = new QFrame(frame_5);
        frame_10->setObjectName("frame_10");
        frame_10->setFrameShape(QFrame::NoFrame);
        frame_10->setFrameShadow(QFrame::Raised);
        horizontalLayout_8 = new QHBoxLayout(frame_10);
        horizontalLayout_8->setObjectName("horizontalLayout_8");
        label_18 = new QLabel(frame_10);
        label_18->setObjectName("label_18");
        label_18->setMinimumSize(QSize(40, 40));
        label_18->setMaximumSize(QSize(40, 40));
        label_18->setPixmap(QPixmap(QString::fromUtf8("\n"
"                                                                                                         :/img/images/license-plate.png")));
        label_18->setScaledContents(false);

        horizontalLayout_8->addWidget(label_18);

        label_19 = new QLabel(frame_10);
        label_19->setObjectName("label_19");
        label_19->setFont(font6);

        horizontalLayout_8->addWidget(label_19);


        verticalLayout->addWidget(frame_10);


        verticalLayout_2->addWidget(frame_5);

        pushButton = new QPushButton(centralwidget);
        pushButton->setObjectName("pushButton");
        pushButton->setGeometry(QRect(1060, 50, 93, 28));
        employeee->setCentralWidget(centralwidget);
        label_2->raise();
        label->raise();
        frame_2->raise();
        stackedWidget->raise();
        frame_54->raise();
        frame->raise();
        pushButton->raise();
        menubar = new QMenuBar(employeee);
        menubar->setObjectName("menubar");
        menubar->setGeometry(QRect(0, 0, 1251, 26));
        employeee->setMenuBar(menubar);
        statusbar = new QStatusBar(employeee);
        statusbar->setObjectName("statusbar");
        employeee->setStatusBar(statusbar);

        retranslateUi(employeee);

        stackedWidget->setCurrentIndex(0);


        QMetaObject::connectSlotsByName(employeee);
    } // setupUi

    void retranslateUi(QMainWindow *employeee)
    {
        employeee->setWindowTitle(QCoreApplication::translate("employeee", "employeee", nullptr));
        label->setText(QString());
        label_2->setText(QString());
        label_3->setText(QCoreApplication::translate("employeee", "Pages", nullptr));
        label_4->setText(QCoreApplication::translate("employeee", " Employee", nullptr));
        label_7->setText(QCoreApplication::translate("employeee", "/", nullptr));
        lineEdit_Address->setPlaceholderText(QCoreApplication::translate("employeee", "Address", nullptr));
        label_38->setText(QCoreApplication::translate("employeee", "Contact Info", nullptr));
        lineEdit_Tel->setPlaceholderText(QCoreApplication::translate("employeee", "Telephone", nullptr));
        lineEdit_Email->setPlaceholderText(QCoreApplication::translate("employeee", "Email", nullptr));
        lineEdit_Codep->setPlaceholderText(QCoreApplication::translate("employeee", "Code Postal", nullptr));
        pushButton_5->setText(QCoreApplication::translate("employeee", "Add Employee", nullptr));
        lineEdit_CIN->setText(QString());
        lineEdit_CIN->setPlaceholderText(QCoreApplication::translate("employeee", "CIN", nullptr));
        label_39->setText(QCoreApplication::translate("employeee", "General Iformation", nullptr));
        lineEdit_Name->setPlaceholderText(QCoreApplication::translate("employeee", "Name", nullptr));
        lineEdit_Surname->setPlaceholderText(QCoreApplication::translate("employeee", "Surname", nullptr));
        lineEdit_role_e->setPlaceholderText(QCoreApplication::translate("employeee", "role_e", nullptr));
        lineEdit_rfid->setPlaceholderText(QCoreApplication::translate("employeee", "RFID card", nullptr));
        comboBox->setItemText(0, QCoreApplication::translate("employeee", "role_e", nullptr));
        comboBox->setItemText(1, QCoreApplication::translate("employeee", "email", nullptr));
        comboBox->setItemText(2, QCoreApplication::translate("employeee", "adress", nullptr));
        comboBox->setItemText(3, QCoreApplication::translate("employeee", "surname", nullptr));
        comboBox->setItemText(4, QCoreApplication::translate("employeee", "nom", nullptr));

        lineEdit_CIN_4->setText(QString());
        lineEdit_CIN_4->setPlaceholderText(QCoreApplication::translate("employeee", "CIN", nullptr));
        label_42->setText(QCoreApplication::translate("employeee", "General Iformation", nullptr));
        lineEdit_Name_4->setPlaceholderText(QCoreApplication::translate("employeee", "Name", nullptr));
        lineEdit_Surname_4->setPlaceholderText(QCoreApplication::translate("employeee", "Surname", nullptr));
        lineEdit_role_e_4->setPlaceholderText(QCoreApplication::translate("employeee", "role_e", nullptr));
        lineEdit_Address_2->setPlaceholderText(QCoreApplication::translate("employeee", "Address", nullptr));
        label_43->setText(QCoreApplication::translate("employeee", "Contact Info", nullptr));
        lineEdit_Tel_2->setPlaceholderText(QCoreApplication::translate("employeee", "Telephone", nullptr));
        lineEdit_Email_2->setPlaceholderText(QCoreApplication::translate("employeee", "Email", nullptr));
        lineEdit_Codep_2->setPlaceholderText(QCoreApplication::translate("employeee", "Code Postal", nullptr));
        pushButton_6->setText(QCoreApplication::translate("employeee", "Update Employee", nullptr));
        pushButton_1->setText(QCoreApplication::translate("employeee", "Add Employee", nullptr));
        pushButton_2->setText(QCoreApplication::translate("employeee", "Employee list", nullptr));
        label_5->setText(QString());
        label_6->setText(QCoreApplication::translate("employeee", "SafeClaim", nullptr));
        label_10->setText(QString());
        label_11->setText(QCoreApplication::translate("employeee", "\n"
"                                                                                                         Employees", nullptr));
        label_12->setText(QString());
        label_13->setText(QCoreApplication::translate("employeee", "\n"
"                                                                                                         Vehicles", nullptr));
        label_14->setText(QString());
        label_15->setText(QCoreApplication::translate("employeee", "\n"
"                                                                                                         Placeholder", nullptr));
        label_16->setText(QString());
        label_17->setText(QCoreApplication::translate("employeee", "\n"
"                                                                                                         Placeholder", nullptr));
        label_18->setText(QString());
        label_19->setText(QCoreApplication::translate("employeee", "\n"
"                                                                                                         Placeholder", nullptr));
        pushButton->setText(QCoreApplication::translate("employeee", "PDF", nullptr));
    } // retranslateUi

};

namespace Ui {
    class employeee: public Ui_employeee {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_EMPLOYEEE_H
