/********************************************************************************
** Form generated from reading UI file 'pagemayssa.ui'
**
** Created by: Qt User Interface Compiler version 6.6.3
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_PAGEMAYSSA_H
#define UI_PAGEMAYSSA_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QDialog>
#include <QtWidgets/QFrame>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QTabWidget>
#include <QtWidgets/QTableView>
#include <QtWidgets/QTextEdit>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_pagemayssa
{
public:
    QFrame *frame;
    QVBoxLayout *verticalLayout_2;
    QFrame *frame_3;
    QLabel *label_6;
    QLabel *label_5;
    QFrame *frame_5;
    QVBoxLayout *verticalLayout;
    QFrame *frame_6;
    QHBoxLayout *horizontalLayout_4;
    QLabel *label_9;
    QLabel *label_10;
    QFrame *frame_7;
    QHBoxLayout *horizontalLayout_5;
    QLabel *label_7;
    QLabel *label_12;
    QFrame *frame_8;
    QHBoxLayout *horizontalLayout_6;
    QLabel *label_13;
    QLabel *label_14;
    QFrame *frame_9;
    QHBoxLayout *horizontalLayout_7;
    QLabel *label_15;
    QLabel *label_16;
    QFrame *frame_10;
    QHBoxLayout *horizontalLayout_8;
    QLabel *label_17;
    QLabel *label_18;
    QLabel *label_2;
    QFrame *frame_2;
    QHBoxLayout *horizontalLayout;
    QLabel *label_3;
    QLabel *label;
    QTabWidget *tabWidget;
    QWidget *tab;
    QFrame *frame_11;
    QLineEdit *lineEdit_cin;
    QLabel *label_19;
    QLineEdit *lineEdit_nom;
    QLineEdit *lineEdit_prenom;
    QLineEdit *lineEdit_email;
    QLineEdit *lineEdit_tel;
    QPushButton *next;
    QPushButton *ajouterclient_button;
    QPushButton *pushButton_3;
    QWidget *tab_2;
    QTableView *tableView_Vehicule;
    QFrame *frame_4;
    QFrame *frame_12;
    QLineEdit *lineEdit_matricule;
    QLabel *label_26;
    QLineEdit *lineEdit_marque;
    QLineEdit *lineEdit_modele;
    QPushButton *pushButton;
    QPushButton *update_v;
    QWidget *tab_3;
    QTableView *tableView_client;
    QPushButton *DeleteButton;
    QLineEdit *lineEdit_2;
    QPushButton *searchButton;
    QPushButton *trier;
    QPushButton *Export;
    QPushButton *showNotification;
    QPushButton *statistic;
    QTextEdit *statisticsTextEdit;
    QPushButton *historique;
    QWidget *tab_4;
    QTableView *tableView_Vehicule_2;
    QPushButton *supp_v;
    QLineEdit *lineEdit_3;
    QPushButton *recherche;
    QPushButton *triv;
    QPushButton *pushButton_2;
    QPushButton *Historique;
    QLineEdit *operationLineEdit;
    QLineEdit *idLineEdit;

    void setupUi(QDialog *pagemayssa)
    {
        if (pagemayssa->objectName().isEmpty())
            pagemayssa->setObjectName("pagemayssa");
        pagemayssa->resize(1241, 755);
        frame = new QFrame(pagemayssa);
        frame->setObjectName("frame");
        frame->setGeometry(QRect(10, 30, 281, 841));
        frame->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        frame->setFrameShape(QFrame::StyledPanel);
        frame->setFrameShadow(QFrame::Raised);
        verticalLayout_2 = new QVBoxLayout(frame);
        verticalLayout_2->setObjectName("verticalLayout_2");
        frame_3 = new QFrame(frame);
        frame_3->setObjectName("frame_3");
        frame_3->setFrameShape(QFrame::StyledPanel);
        frame_3->setFrameShadow(QFrame::Raised);
        label_6 = new QLabel(frame_3);
        label_6->setObjectName("label_6");
        label_6->setGeometry(QRect(110, 40, 141, 51));
        QFont font;
        font.setPointSize(15);
        font.setBold(true);
        label_6->setFont(font);
        label_6->setStyleSheet(QString::fromUtf8("color: rgb(102, 115, 231);"));
        label_5 = new QLabel(frame_3);
        label_5->setObjectName("label_5");
        label_5->setGeometry(QRect(-20, 10, 130, 130));
        label_5->setMinimumSize(QSize(130, 130));
        label_5->setMaximumSize(QSize(130, 130));
        label_5->setStyleSheet(QString::fromUtf8("image: url(:/images/images/logoblue.png);"));
        label_5->setPixmap(QPixmap(QString::fromUtf8(":/img/images/logoblue.png")));
        label_5->setScaledContents(true);

        verticalLayout_2->addWidget(frame_3);

        frame_5 = new QFrame(frame);
        frame_5->setObjectName("frame_5");
        frame_5->setFrameShape(QFrame::StyledPanel);
        frame_5->setFrameShadow(QFrame::Raised);
        verticalLayout = new QVBoxLayout(frame_5);
        verticalLayout->setObjectName("verticalLayout");
        frame_6 = new QFrame(frame_5);
        frame_6->setObjectName("frame_6");
        frame_6->setFrameShape(QFrame::StyledPanel);
        frame_6->setFrameShadow(QFrame::Raised);
        horizontalLayout_4 = new QHBoxLayout(frame_6);
        horizontalLayout_4->setObjectName("horizontalLayout_4");
        label_9 = new QLabel(frame_6);
        label_9->setObjectName("label_9");
        label_9->setMinimumSize(QSize(40, 40));
        label_9->setMaximumSize(QSize(40, 40));
        label_9->setStyleSheet(QString::fromUtf8("image: url(:/images/images/employee.png);"));
        label_9->setPixmap(QPixmap(QString::fromUtf8(":/img/images/employee.png")));
        label_9->setScaledContents(true);

        horizontalLayout_4->addWidget(label_9);

        label_10 = new QLabel(frame_6);
        label_10->setObjectName("label_10");
        QFont font1;
        font1.setFamilies({QString::fromUtf8("MS Shell Dlg 2")});
        font1.setPointSize(10);
        label_10->setFont(font1);

        horizontalLayout_4->addWidget(label_10);


        verticalLayout->addWidget(frame_6);

        frame_7 = new QFrame(frame_5);
        frame_7->setObjectName("frame_7");
        frame_7->setFrameShape(QFrame::StyledPanel);
        frame_7->setFrameShadow(QFrame::Raised);
        horizontalLayout_5 = new QHBoxLayout(frame_7);
        horizontalLayout_5->setObjectName("horizontalLayout_5");
        label_7 = new QLabel(frame_7);
        label_7->setObjectName("label_7");
        label_7->setMinimumSize(QSize(40, 40));
        label_7->setMaximumSize(QSize(40, 40));
        label_7->setStyleSheet(QString::fromUtf8("image: url(:/images/images/incorporation.png);"));
        label_7->setPixmap(QPixmap(QString::fromUtf8(":/img/images/incorporation.png")));
        label_7->setScaledContents(true);

        horizontalLayout_5->addWidget(label_7);

        label_12 = new QLabel(frame_7);
        label_12->setObjectName("label_12");
        QFont font2;
        font2.setPointSize(10);
        label_12->setFont(font2);

        horizontalLayout_5->addWidget(label_12);


        verticalLayout->addWidget(frame_7);

        frame_8 = new QFrame(frame_5);
        frame_8->setObjectName("frame_8");
        frame_8->setFrameShape(QFrame::StyledPanel);
        frame_8->setFrameShadow(QFrame::Raised);
        horizontalLayout_6 = new QHBoxLayout(frame_8);
        horizontalLayout_6->setObjectName("horizontalLayout_6");
        label_13 = new QLabel(frame_8);
        label_13->setObjectName("label_13");
        label_13->setMinimumSize(QSize(40, 40));
        label_13->setMaximumSize(QSize(40, 40));
        label_13->setStyleSheet(QString::fromUtf8("image: url(:/images/images/license-plate.png);"));
        label_13->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_13->setScaledContents(true);

        horizontalLayout_6->addWidget(label_13);

        label_14 = new QLabel(frame_8);
        label_14->setObjectName("label_14");
        label_14->setFont(font1);

        horizontalLayout_6->addWidget(label_14);


        verticalLayout->addWidget(frame_8);

        frame_9 = new QFrame(frame_5);
        frame_9->setObjectName("frame_9");
        frame_9->setFrameShape(QFrame::StyledPanel);
        frame_9->setFrameShadow(QFrame::Raised);
        horizontalLayout_7 = new QHBoxLayout(frame_9);
        horizontalLayout_7->setObjectName("horizontalLayout_7");
        label_15 = new QLabel(frame_9);
        label_15->setObjectName("label_15");
        label_15->setMinimumSize(QSize(40, 40));
        label_15->setMaximumSize(QSize(40, 40));
        label_15->setStyleSheet(QString::fromUtf8("image: url(:/images/images/id-card.png);"));
        label_15->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_15->setScaledContents(true);

        horizontalLayout_7->addWidget(label_15);

        label_16 = new QLabel(frame_9);
        label_16->setObjectName("label_16");
        label_16->setFont(font2);

        horizontalLayout_7->addWidget(label_16);


        verticalLayout->addWidget(frame_9);

        frame_10 = new QFrame(frame_5);
        frame_10->setObjectName("frame_10");
        frame_10->setFrameShape(QFrame::StyledPanel);
        frame_10->setFrameShadow(QFrame::Raised);
        horizontalLayout_8 = new QHBoxLayout(frame_10);
        horizontalLayout_8->setObjectName("horizontalLayout_8");
        label_17 = new QLabel(frame_10);
        label_17->setObjectName("label_17");
        label_17->setMinimumSize(QSize(40, 40));
        label_17->setMaximumSize(QSize(40, 40));
        label_17->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_17->setScaledContents(true);

        horizontalLayout_8->addWidget(label_17);

        label_18 = new QLabel(frame_10);
        label_18->setObjectName("label_18");
        label_18->setFont(font2);

        horizontalLayout_8->addWidget(label_18);


        verticalLayout->addWidget(frame_10);


        verticalLayout_2->addWidget(frame_5);

        label_2 = new QLabel(pagemayssa);
        label_2->setObjectName("label_2");
        label_2->setGeometry(QRect(-10, 120, 1251, 601));
        label_2->setStyleSheet(QString::fromUtf8("background-color: rgb(245, 246, 247);"));
        frame_2 = new QFrame(pagemayssa);
        frame_2->setObjectName("frame_2");
        frame_2->setGeometry(QRect(320, 50, 231, 41));
        frame_2->setFrameShape(QFrame::StyledPanel);
        frame_2->setFrameShadow(QFrame::Raised);
        horizontalLayout = new QHBoxLayout(frame_2);
        horizontalLayout->setObjectName("horizontalLayout");
        label_3 = new QLabel(frame_2);
        label_3->setObjectName("label_3");
        QFont font3;
        font3.setFamilies({QString::fromUtf8("Segoe MDL2 Assets")});
        font3.setPointSize(10);
        font3.setBold(true);
        label_3->setFont(font3);
        label_3->setStyleSheet(QString::fromUtf8("color: rgb(158, 175, 240);"));

        horizontalLayout->addWidget(label_3);

        label = new QLabel(pagemayssa);
        label->setObjectName("label");
        label->setGeometry(QRect(0, 0, 1251, 201));
        label->setStyleSheet(QString::fromUtf8("background-color: rgb(94, 114, 228);"));
        tabWidget = new QTabWidget(pagemayssa);
        tabWidget->setObjectName("tabWidget");
        tabWidget->setGeometry(QRect(310, 150, 1061, 621));
        tabWidget->setStyleSheet(QString::fromUtf8("background-color: rgb(245, 246, 247);"));
        tab = new QWidget();
        tab->setObjectName("tab");
        frame_11 = new QFrame(tab);
        frame_11->setObjectName("frame_11");
        frame_11->setGeometry(QRect(0, 0, 781, 581));
        frame_11->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-top-left-radius: 15px;\n"
"border-bottom-left-radius: 15px;"));
        frame_11->setFrameShape(QFrame::StyledPanel);
        frame_11->setFrameShadow(QFrame::Raised);
        lineEdit_cin = new QLineEdit(frame_11);
        lineEdit_cin->setObjectName("lineEdit_cin");
        lineEdit_cin->setGeometry(QRect(170, 350, 381, 27));
        QFont font4;
        font4.setPointSize(10);
        font4.setBold(false);
        lineEdit_cin->setFont(font4);
        lineEdit_cin->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        label_19 = new QLabel(frame_11);
        label_19->setObjectName("label_19");
        label_19->setGeometry(QRect(240, 20, 371, 51));
        QFont font5;
        font5.setFamilies({QString::fromUtf8("Nirmala UI")});
        font5.setPointSize(15);
        font5.setBold(true);
        label_19->setFont(font5);
        lineEdit_nom = new QLineEdit(frame_11);
        lineEdit_nom->setObjectName("lineEdit_nom");
        lineEdit_nom->setGeometry(QRect(170, 100, 184, 27));
        lineEdit_nom->setFont(font4);
        lineEdit_nom->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        lineEdit_prenom = new QLineEdit(frame_11);
        lineEdit_prenom->setObjectName("lineEdit_prenom");
        lineEdit_prenom->setGeometry(QRect(170, 170, 183, 27));
        lineEdit_prenom->setFont(font4);
        lineEdit_prenom->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        lineEdit_email = new QLineEdit(frame_11);
        lineEdit_email->setObjectName("lineEdit_email");
        lineEdit_email->setGeometry(QRect(170, 260, 381, 27));
        lineEdit_email->setFont(font4);
        lineEdit_email->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        lineEdit_tel = new QLineEdit(frame_11);
        lineEdit_tel->setObjectName("lineEdit_tel");
        lineEdit_tel->setGeometry(QRect(170, 450, 381, 27));
        lineEdit_tel->setFont(font4);
        lineEdit_tel->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        next = new QPushButton(frame_11);
        next->setObjectName("next");
        next->setGeometry(QRect(680, 510, 93, 28));
        next->setStyleSheet(QString::fromUtf8("QPushButton#pushButton{\n"
"	background-color: qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 144, 255, 255), stop:1 rgba(207, 0, 255, 194));\n"
"color:rgba(255, 255, 255, 210);\n"
"border-radius:5px;\n"
"}\n"
"QPushButton#pushButton:hover{\n"
"background-color:qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 0, 255, 255), stop:0.873684 rgba(255, 0, 255, 227))\n"
"}\n"
"QPushButton#pushButton:pressed{\n"
"padding-left:5px;\n"
"padding-top:5px;\n"
"background-color:rgb(170, 0, 255);\n"
"}"));
        ajouterclient_button = new QPushButton(frame_11);
        ajouterclient_button->setObjectName("ajouterclient_button");
        ajouterclient_button->setGeometry(QRect(610, 450, 93, 28));
        ajouterclient_button->setStyleSheet(QString::fromUtf8("QPushButton#pushButton{\n"
"	background-color: qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 144, 255, 255), stop:1 rgba(207, 0, 255, 194));\n"
"color:rgba(255, 255, 255, 210);\n"
"border-radius:5px;\n"
"}\n"
"QPushButton#pushButton:hover{\n"
"background-color:qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 0, 255, 255), stop:0.873684 rgba(255, 0, 255, 227))\n"
"}\n"
"QPushButton#pushButton:pressed{\n"
"padding-left:5px;\n"
"padding-top:5px;\n"
"background-color:rgb(170, 0, 255);\n"
"}"));
        pushButton_3 = new QPushButton(frame_11);
        pushButton_3->setObjectName("pushButton_3");
        pushButton_3->setGeometry(QRect(550, 510, 91, 31));
        tabWidget->addTab(tab, QString());
        tab_2 = new QWidget();
        tab_2->setObjectName("tab_2");
        tableView_Vehicule = new QTableView(tab_2);
        tableView_Vehicule->setObjectName("tableView_Vehicule");
        tableView_Vehicule->setGeometry(QRect(-15, -9, 961, 531));
        frame_4 = new QFrame(tab_2);
        frame_4->setObjectName("frame_4");
        frame_4->setGeometry(QRect(-1, -1, 801, 521));
        frame_4->setFrameShape(QFrame::StyledPanel);
        frame_4->setFrameShadow(QFrame::Raised);
        frame_12 = new QFrame(frame_4);
        frame_12->setObjectName("frame_12");
        frame_12->setGeometry(QRect(0, 0, 931, 581));
        frame_12->setStyleSheet(QString::fromUtf8("background-color: rgb(73, 53, 212);\n"
"border-top-right-radius: 15px;\n"
"border-bottom-right-radius: 15px;"));
        frame_12->setFrameShape(QFrame::StyledPanel);
        frame_12->setFrameShadow(QFrame::Raised);
        lineEdit_matricule = new QLineEdit(frame_12);
        lineEdit_matricule->setObjectName("lineEdit_matricule");
        lineEdit_matricule->setGeometry(QRect(150, 330, 381, 27));
        lineEdit_matricule->setFont(font4);
        lineEdit_matricule->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgb(245, 246, 247);\n"
"color:white;\n"
"padding-bottom:7px;"));
        label_26 = new QLabel(frame_12);
        label_26->setObjectName("label_26");
        label_26->setGeometry(QRect(190, 10, 331, 51));
        label_26->setFont(font);
        label_26->setStyleSheet(QString::fromUtf8("color: rgb(245, 246, 247)"));
        lineEdit_marque = new QLineEdit(frame_12);
        lineEdit_marque->setObjectName("lineEdit_marque");
        lineEdit_marque->setGeometry(QRect(150, 230, 381, 27));
        lineEdit_marque->setFont(font4);
        lineEdit_marque->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgb(245, 246, 247);\n"
"color:white;\n"
"padding-bottom:7px;"));
        lineEdit_modele = new QLineEdit(frame_12);
        lineEdit_modele->setObjectName("lineEdit_modele");
        lineEdit_modele->setGeometry(QRect(150, 160, 131, 27));
        lineEdit_modele->setFont(font4);
        lineEdit_modele->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgb(245, 246, 247);\n"
"color:white;\n"
"padding-bottom:7px;"));
        pushButton = new QPushButton(frame_12);
        pushButton->setObjectName("pushButton");
        pushButton->setGeometry(QRect(600, 440, 121, 31));
        QFont font6;
        font6.setPointSize(12);
        pushButton->setFont(font6);
        pushButton->setStyleSheet(QString::fromUtf8("QPushButton#pushButton{\n"
"	background-color: qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 144, 255, 255), stop:1 rgba(207, 0, 255, 194));\n"
"color:rgba(255, 255, 255, 210);\n"
"border-radius:5px;\n"
"}\n"
"QPushButton#pushButton:hover{\n"
"background-color:qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 0, 255, 255), stop:0.873684 rgba(255, 0, 255, 227))\n"
"}\n"
"QPushButton#pushButton:pressed{\n"
"padding-left:5px;\n"
"padding-top:5px;\n"
"background-color:rgb(170, 0, 255);\n"
"}"));
        update_v = new QPushButton(frame_12);
        update_v->setObjectName("update_v");
        update_v->setGeometry(QRect(610, 380, 101, 31));
        update_v->setStyleSheet(QString::fromUtf8("QPushButton#pushButton{\n"
"	background-color: qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 144, 255, 255), stop:1 rgba(207, 0, 255, 194));\n"
"color:rgba(255, 255, 255, 210);\n"
"border-radius:5px;\n"
"}\n"
"QPushButton#pushButton:hover{\n"
"background-color:qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 0, 255, 255), stop:0.873684 rgba(255, 0, 255, 227))\n"
"}\n"
"QPushButton#pushButton:pressed{\n"
"padding-left:5px;\n"
"padding-top:5px;\n"
"background-color:rgb(170, 0, 255);\n"
"}"));
        tabWidget->addTab(tab_2, QString());
        tab_3 = new QWidget();
        tab_3->setObjectName("tab_3");
        tableView_client = new QTableView(tab_3);
        tableView_client->setObjectName("tableView_client");
        tableView_client->setGeometry(QRect(-10, 0, 1141, 721));
        DeleteButton = new QPushButton(tab_3);
        DeleteButton->setObjectName("DeleteButton");
        DeleteButton->setGeometry(QRect(30, 430, 91, 31));
        lineEdit_2 = new QLineEdit(tab_3);
        lineEdit_2->setObjectName("lineEdit_2");
        lineEdit_2->setGeometry(QRect(30, 340, 131, 31));
        searchButton = new QPushButton(tab_3);
        searchButton->setObjectName("searchButton");
        searchButton->setGeometry(QRect(180, 340, 93, 28));
        searchButton->setStyleSheet(QString::fromUtf8("QPushButton#pushButton{\n"
"	background-color: qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 144, 255, 255), stop:1 rgba(207, 0, 255, 194));\n"
"color:rgba(255, 255, 255, 210);\n"
"border-radius:5px;\n"
"}\n"
"QPushButton#pushButton:hover{\n"
"background-color:qlineargradient(spread:pad, x1:0, y1:0, x2:1, y2:1, stop:0 rgba(0, 0, 255, 255), stop:0.873684 rgba(255, 0, 255, 227))\n"
"}\n"
"QPushButton#pushButton:pressed{\n"
"padding-left:5px;\n"
"padding-top:5px;\n"
"background-color:rgb(170, 0, 255);\n"
"}"));
        trier = new QPushButton(tab_3);
        trier->setObjectName("trier");
        trier->setGeometry(QRect(30, 390, 93, 28));
        Export = new QPushButton(tab_3);
        Export->setObjectName("Export");
        Export->setGeometry(QRect(160, 390, 121, 31));
        showNotification = new QPushButton(tab_3);
        showNotification->setObjectName("showNotification");
        showNotification->setGeometry(QRect(160, 430, 121, 31));
        statistic = new QPushButton(tab_3);
        statistic->setObjectName("statistic");
        statistic->setGeometry(QRect(590, 340, 141, 31));
        statisticsTextEdit = new QTextEdit(tab_3);
        statisticsTextEdit->setObjectName("statisticsTextEdit");
        statisticsTextEdit->setGeometry(QRect(520, 380, 321, 171));
        historique = new QPushButton(tab_3);
        historique->setObjectName("historique");
        historique->setGeometry(QRect(320, 360, 93, 28));
        tabWidget->addTab(tab_3, QString());
        tab_4 = new QWidget();
        tab_4->setObjectName("tab_4");
        tableView_Vehicule_2 = new QTableView(tab_4);
        tableView_Vehicule_2->setObjectName("tableView_Vehicule_2");
        tableView_Vehicule_2->setGeometry(QRect(0, 0, 1081, 561));
        supp_v = new QPushButton(tab_4);
        supp_v->setObjectName("supp_v");
        supp_v->setGeometry(QRect(210, 430, 101, 31));
        lineEdit_3 = new QLineEdit(tab_4);
        lineEdit_3->setObjectName("lineEdit_3");
        lineEdit_3->setGeometry(QRect(70, 370, 171, 31));
        recherche = new QPushButton(tab_4);
        recherche->setObjectName("recherche");
        recherche->setGeometry(QRect(280, 370, 101, 31));
        triv = new QPushButton(tab_4);
        triv->setObjectName("triv");
        triv->setGeometry(QRect(70, 430, 101, 31));
        pushButton_2 = new QPushButton(tab_4);
        pushButton_2->setObjectName("pushButton_2");
        pushButton_2->setGeometry(QRect(140, 480, 131, 28));
        Historique = new QPushButton(tab_4);
        Historique->setObjectName("Historique");
        Historique->setGeometry(QRect(530, 440, 93, 28));
        operationLineEdit = new QLineEdit(tab_4);
        operationLineEdit->setObjectName("operationLineEdit");
        operationLineEdit->setGeometry(QRect(650, 440, 113, 22));
        idLineEdit = new QLineEdit(tab_4);
        idLineEdit->setObjectName("idLineEdit");
        idLineEdit->setGeometry(QRect(670, 480, 113, 22));
        tabWidget->addTab(tab_4, QString());
        label->raise();
        label_2->raise();
        frame->raise();
        frame_2->raise();
        tabWidget->raise();

        retranslateUi(pagemayssa);

        tabWidget->setCurrentIndex(0);


        QMetaObject::connectSlotsByName(pagemayssa);
    } // setupUi

    void retranslateUi(QDialog *pagemayssa)
    {
        pagemayssa->setWindowTitle(QCoreApplication::translate("pagemayssa", "Dialog", nullptr));
        label_6->setText(QCoreApplication::translate("pagemayssa", "SafeClaim", nullptr));
        label_5->setText(QString());
        label_9->setText(QString());
        label_10->setText(QCoreApplication::translate("pagemayssa", "Employees", nullptr));
        label_7->setText(QString());
        label_12->setText(QCoreApplication::translate("pagemayssa", "Client /Vehicle", nullptr));
        label_13->setText(QString());
        label_14->setText(QCoreApplication::translate("pagemayssa", "Contrat", nullptr));
        label_15->setText(QString());
        label_16->setText(QCoreApplication::translate("pagemayssa", "Constat", nullptr));
        label_17->setText(QString());
        label_18->setText(QCoreApplication::translate("pagemayssa", "Collaboration", nullptr));
        label_2->setText(QString());
        label_3->setText(QCoreApplication::translate("pagemayssa", "Pages", nullptr));
        label->setText(QString());
        lineEdit_cin->setText(QString());
        lineEdit_cin->setPlaceholderText(QCoreApplication::translate("pagemayssa", "CIN", nullptr));
        label_19->setText(QCoreApplication::translate("pagemayssa", " Informations of client:", nullptr));
        lineEdit_nom->setPlaceholderText(QCoreApplication::translate("pagemayssa", "Name", nullptr));
        lineEdit_prenom->setPlaceholderText(QCoreApplication::translate("pagemayssa", "Sirname", nullptr));
        lineEdit_email->setText(QString());
        lineEdit_email->setPlaceholderText(QCoreApplication::translate("pagemayssa", "E-mail", nullptr));
        lineEdit_tel->setText(QString());
        lineEdit_tel->setPlaceholderText(QCoreApplication::translate("pagemayssa", "Tel", nullptr));
        next->setText(QCoreApplication::translate("pagemayssa", "Next", nullptr));
        ajouterclient_button->setText(QCoreApplication::translate("pagemayssa", "ajouter", nullptr));
        pushButton_3->setText(QCoreApplication::translate("pagemayssa", "Modifier", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(tab), QCoreApplication::translate("pagemayssa", "Ajouter Client", nullptr));
        lineEdit_matricule->setPlaceholderText(QCoreApplication::translate("pagemayssa", "Registration number", nullptr));
        label_26->setText(QCoreApplication::translate("pagemayssa", " Informations of vehicle :", nullptr));
        lineEdit_marque->setPlaceholderText(QCoreApplication::translate("pagemayssa", "Brand", nullptr));
        lineEdit_modele->setPlaceholderText(QCoreApplication::translate("pagemayssa", "Model", nullptr));
        pushButton->setText(QCoreApplication::translate("pagemayssa", "Done", nullptr));
        update_v->setText(QCoreApplication::translate("pagemayssa", "Modifier", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(tab_2), QCoreApplication::translate("pagemayssa", "AjouterVehicle", nullptr));
        DeleteButton->setText(QCoreApplication::translate("pagemayssa", "Supprimer", nullptr));
        searchButton->setText(QCoreApplication::translate("pagemayssa", "Rechercher ", nullptr));
        trier->setText(QCoreApplication::translate("pagemayssa", "Trier", nullptr));
        Export->setText(QCoreApplication::translate("pagemayssa", "Exporter en PDF", nullptr));
        showNotification->setText(QCoreApplication::translate("pagemayssa", "Notification ON", nullptr));
        statistic->setText(QCoreApplication::translate("pagemayssa", "Statistique des nom ", nullptr));
        historique->setText(QCoreApplication::translate("pagemayssa", "historique", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(tab_3), QCoreApplication::translate("pagemayssa", "Consulter la liste des clients", nullptr));
        supp_v->setText(QCoreApplication::translate("pagemayssa", "Supprimer", nullptr));
        recherche->setText(QCoreApplication::translate("pagemayssa", "Rechercher", nullptr));
        triv->setText(QCoreApplication::translate("pagemayssa", "Trier", nullptr));
        pushButton_2->setText(QCoreApplication::translate("pagemayssa", "Synchronisation", nullptr));
        Historique->setText(QCoreApplication::translate("pagemayssa", "Historique", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(tab_4), QCoreApplication::translate("pagemayssa", "Consulter la liste des Vehicules", nullptr));
    } // retranslateUi

};

namespace Ui {
    class pagemayssa: public Ui_pagemayssa {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_PAGEMAYSSA_H
