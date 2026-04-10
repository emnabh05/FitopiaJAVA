/********************************************************************************
** Form generated from reading UI file 'moduleconstat.ui'
**
** Created by: Qt User Interface Compiler version 6.6.3
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_MODULECONSTAT_H
#define UI_MODULECONSTAT_H

#include <QtCore/QVariant>
#include <QtQuickWidgets/QQuickWidget>
#include <QtWidgets/QApplication>
#include <QtWidgets/QCalendarWidget>
#include <QtWidgets/QComboBox>
#include <QtWidgets/QDateEdit>
#include <QtWidgets/QDialog>
#include <QtWidgets/QFrame>
#include <QtWidgets/QGroupBox>
#include <QtWidgets/QHBoxLayout>
#include <QtWidgets/QHeaderView>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QPushButton>
#include <QtWidgets/QTabWidget>
#include <QtWidgets/QTableView>
#include <QtWidgets/QTableWidget>
#include <QtWidgets/QTextEdit>
#include <QtWidgets/QTimeEdit>
#include <QtWidgets/QVBoxLayout>
#include <QtWidgets/QWidget>

QT_BEGIN_NAMESPACE

class Ui_moduleconstat
{
public:
    QGroupBox *groupBox;
    QLabel *violet;
    QLabel *blanc;
    QLabel *label;
    QTabWidget *tabWidget;
    QWidget *AJOUTER;
    QLabel *label_3;
    QLabel *label_4;
    QFrame *frame_4;
    QLabel *label_5;
    QLineEdit *lineEdit_numero;
    QLineEdit *lineEdit_lieu;
    QDateEdit *dateEdit_date;
    QLabel *label_point;
    QTextEdit *textEdit_obesrvation;
    QLabel *label_circonstance;
    QTextEdit *textEdit_avis;
    QPushButton *Valider;
    QTimeEdit *timeEdit_heure;
    QComboBox *comboBox_circonstance;
    QComboBox *comboBox_point;
    QLineEdit *lineEdit_matricule;
    QWidget *Afficherconstat;
    QFrame *frame_2;
    QFrame *frame_3;
    QLineEdit *affichage_rechercher;
    QPushButton *pushButton_Rechercher;
    QPushButton *pushButton_Modifier;
    QPushButton *pushButton_supprimer;
    QPushButton *pushButton_PDF;
    QPushButton *trier;
    QPushButton *EMAIL;
    QCalendarWidget *calendarWidget;
    QLabel *label_13;
    QTableView *tableView;
    QWidget *Statistique;
    QPushButton *refresh;
    QTableWidget *tableWidget;
    QTableWidget *tableWidget_2;
    QWidget *localisation;
    QQuickWidget *quickWidget;
    QWidget *Arduino;
    QPushButton *On;
    QPushButton *Off;
    QPushButton *plus;
    QPushButton *moins;
    QPushButton *pushButton_recherche_matricule;
    QLineEdit *lineEdit_matricule_2;
    QTableView *tableView_2;
    QLineEdit *lineEdit_cconstat;
    QLabel *label_2;
    QLabel *label_logo;
    QLabel *label_6;
    QFrame *frame_5;
    QVBoxLayout *verticalLayout_2;
    QFrame *frame_6;
    QHBoxLayout *horizontalLayout_3;
    QLabel *label_14;
    QFrame *frame_7;
    QHBoxLayout *horizontalLayout_4;
    QLabel *label_16;
    QLabel *label_17;
    QFrame *frame_8;
    QHBoxLayout *horizontalLayout_5;
    QLabel *label_18;
    QLabel *label_19;
    QFrame *frame_9;
    QHBoxLayout *horizontalLayout_6;
    QLabel *label_20;
    QLabel *label_21;
    QFrame *frame_10;
    QHBoxLayout *horizontalLayout_7;
    QLabel *label_22;
    QLabel *label_23;
    QFrame *frame_11;
    QHBoxLayout *horizontalLayout_8;
    QLabel *label_24;
    QLabel *label_25;

    void setupUi(QDialog *moduleconstat)
    {
        if (moduleconstat->objectName().isEmpty())
            moduleconstat->setObjectName("moduleconstat");
        moduleconstat->resize(1157, 730);
        groupBox = new QGroupBox(moduleconstat);
        groupBox->setObjectName("groupBox");
        groupBox->setEnabled(true);
        groupBox->setGeometry(QRect(10, 0, 1151, 681));
        groupBox->setStyleSheet(QString::fromUtf8("#tab{ background-color: transparent !important;\n"
"}"));
        violet = new QLabel(groupBox);
        violet->setObjectName("violet");
        violet->setGeometry(QRect(-10, 0, 1161, 181));
        violet->setStyleSheet(QString::fromUtf8("background-color: rgb(94, 114, 228);"));
        blanc = new QLabel(groupBox);
        blanc->setObjectName("blanc");
        blanc->setGeometry(QRect(0, 180, 1171, 501));
        blanc->setStyleSheet(QString::fromUtf8("background-color: rgb(245, 246, 247);"));
        label = new QLabel(groupBox);
        label->setObjectName("label");
        label->setGeometry(QRect(20, 40, 281, 631));
        label->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        tabWidget = new QTabWidget(groupBox);
        tabWidget->setObjectName("tabWidget");
        tabWidget->setGeometry(QRect(320, 60, 821, 621));
        tabWidget->setStyleSheet(QString::fromUtf8(" background-color: transparent;\n"
"background-color: rgb(248, 248, 248);"));
        AJOUTER = new QWidget();
        AJOUTER->setObjectName("AJOUTER");
        label_3 = new QLabel(AJOUTER);
        label_3->setObjectName("label_3");
        label_3->setGeometry(QRect(479, -2, 331, 601));
        label_3->setStyleSheet(QString::fromUtf8("background-color: rgb(73, 53, 212);\n"
"border-top-right-radius: 30px;\n"
"border-bottom-right-radius: 30px;"));
        label_4 = new QLabel(AJOUTER);
        label_4->setObjectName("label_4");
        label_4->setGeometry(QRect(520, 20, 191, 20));
        label_4->setStyleSheet(QString::fromUtf8("background-color: rgb(73, 53, 212);"));
        frame_4 = new QFrame(AJOUTER);
        frame_4->setObjectName("frame_4");
        frame_4->setGeometry(QRect(10, 0, 471, 591));
        frame_4->setStyleSheet(QString::fromUtf8("background-color: rgb(248, 248, 248);\n"
"border-top-left-radius: 30px;\n"
"border-bottom-left-radius: 30px;"));
        frame_4->setFrameShape(QFrame::StyledPanel);
        frame_4->setFrameShadow(QFrame::Raised);
        label_5 = new QLabel(frame_4);
        label_5->setObjectName("label_5");
        label_5->setGeometry(QRect(130, 0, 161, 16));
        lineEdit_numero = new QLineEdit(frame_4);
        lineEdit_numero->setObjectName("lineEdit_numero");
        lineEdit_numero->setGeometry(QRect(40, 60, 261, 20));
        lineEdit_numero->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        lineEdit_lieu = new QLineEdit(frame_4);
        lineEdit_lieu->setObjectName("lineEdit_lieu");
        lineEdit_lieu->setGeometry(QRect(40, 110, 261, 20));
        lineEdit_lieu->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        dateEdit_date = new QDateEdit(frame_4);
        dateEdit_date->setObjectName("dateEdit_date");
        dateEdit_date->setGeometry(QRect(50, 170, 110, 22));
        dateEdit_date->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        label_point = new QLabel(frame_4);
        label_point->setObjectName("label_point");
        label_point->setGeometry(QRect(10, 240, 146, 20));
        textEdit_obesrvation = new QTextEdit(frame_4);
        textEdit_obesrvation->setObjectName("textEdit_obesrvation");
        textEdit_obesrvation->setGeometry(QRect(60, 280, 281, 81));
        textEdit_obesrvation->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        label_circonstance = new QLabel(frame_4);
        label_circonstance->setObjectName("label_circonstance");
        label_circonstance->setGeometry(QRect(10, 390, 145, 20));
        textEdit_avis = new QTextEdit(frame_4);
        textEdit_avis->setObjectName("textEdit_avis");
        textEdit_avis->setGeometry(QRect(110, 420, 251, 81));
        textEdit_avis->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        Valider = new QPushButton(frame_4);
        Valider->setObjectName("Valider");
        Valider->setGeometry(QRect(200, 550, 81, 31));
        Valider->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius: 30px;"));
        timeEdit_heure = new QTimeEdit(frame_4);
        timeEdit_heure->setObjectName("timeEdit_heure");
        timeEdit_heure->setGeometry(QRect(190, 170, 118, 22));
        timeEdit_heure->setStyleSheet(QString::fromUtf8("\n"
"background-color: rgb(255, 255, 255);"));
        comboBox_circonstance = new QComboBox(frame_4);
        comboBox_circonstance->setObjectName("comboBox_circonstance");
        comboBox_circonstance->setGeometry(QRect(131, 390, 331, 21));
        comboBox_circonstance->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-color: rgb(0, 0, 0);"));
        comboBox_point = new QComboBox(frame_4);
        comboBox_point->setObjectName("comboBox_point");
        comboBox_point->setGeometry(QRect(170, 240, 131, 22));
        comboBox_point->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        lineEdit_matricule = new QLineEdit(frame_4);
        lineEdit_matricule->setObjectName("lineEdit_matricule");
        lineEdit_matricule->setGeometry(QRect(120, 520, 241, 31));
        lineEdit_matricule->setStyleSheet(QString::fromUtf8("background-color:rgba(0,0,0,0);\n"
"border:none;\n"
"border-bottom:2px solid rgba(46,82,101,200);\n"
"color:rgba(0,0,0,240);\n"
"padding-bottom:7px;"));
        tabWidget->addTab(AJOUTER, QString());
        frame_4->raise();
        label_4->raise();
        label_3->raise();
        Afficherconstat = new QWidget();
        Afficherconstat->setObjectName("Afficherconstat");
        frame_2 = new QFrame(Afficherconstat);
        frame_2->setObjectName("frame_2");
        frame_2->setGeometry(QRect(570, 150, 241, 211));
        frame_2->setFrameShape(QFrame::StyledPanel);
        frame_2->setFrameShadow(QFrame::Raised);
        frame_3 = new QFrame(frame_2);
        frame_3->setObjectName("frame_3");
        frame_3->setGeometry(QRect(10, 20, 221, 41));
        frame_3->setFrameShape(QFrame::StyledPanel);
        frame_3->setFrameShadow(QFrame::Raised);
        affichage_rechercher = new QLineEdit(frame_3);
        affichage_rechercher->setObjectName("affichage_rechercher");
        affichage_rechercher->setGeometry(QRect(0, 10, 113, 20));
        pushButton_Rechercher = new QPushButton(frame_3);
        pushButton_Rechercher->setObjectName("pushButton_Rechercher");
        pushButton_Rechercher->setGeometry(QRect(140, 10, 75, 23));
        pushButton_Modifier = new QPushButton(frame_2);
        pushButton_Modifier->setObjectName("pushButton_Modifier");
        pushButton_Modifier->setGeometry(QRect(10, 70, 231, 23));
        pushButton_supprimer = new QPushButton(frame_2);
        pushButton_supprimer->setObjectName("pushButton_supprimer");
        pushButton_supprimer->setGeometry(QRect(10, 110, 221, 23));
        pushButton_PDF = new QPushButton(frame_2);
        pushButton_PDF->setObjectName("pushButton_PDF");
        pushButton_PDF->setGeometry(QRect(160, 180, 75, 23));
        trier = new QPushButton(frame_2);
        trier->setObjectName("trier");
        trier->setGeometry(QRect(20, 180, 75, 23));
        EMAIL = new QPushButton(frame_2);
        EMAIL->setObjectName("EMAIL");
        EMAIL->setGeometry(QRect(160, 140, 75, 23));
        calendarWidget = new QCalendarWidget(Afficherconstat);
        calendarWidget->setObjectName("calendarWidget");
        calendarWidget->setGeometry(QRect(570, 380, 241, 181));
        calendarWidget->setStyleSheet(QString::fromUtf8("background-color: rgb(230, 230, 230);\n"
"border-top-color: rgb(0, 0, 0);\n"
"\n"
"border-radius:25px;"));
        label_13 = new QLabel(Afficherconstat);
        label_13->setObjectName("label_13");
        label_13->setGeometry(QRect(0, 0, 811, 601));
        label_13->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:40px;"));
        tableView = new QTableView(Afficherconstat);
        tableView->setObjectName("tableView");
        tableView->setGeometry(QRect(10, 40, 541, 531));
        tableView->setMinimumSize(QSize(0, 0));
        tableView->setStyleSheet(QString::fromUtf8(""));
        tabWidget->addTab(Afficherconstat, QString());
        label_13->raise();
        frame_2->raise();
        calendarWidget->raise();
        tableView->raise();
        Statistique = new QWidget();
        Statistique->setObjectName("Statistique");
        refresh = new QPushButton(Statistique);
        refresh->setObjectName("refresh");
        refresh->setGeometry(QRect(690, 40, 75, 23));
        tableWidget = new QTableWidget(Statistique);
        tableWidget->setObjectName("tableWidget");
        tableWidget->setGeometry(QRect(30, 130, 361, 451));
        tableWidget_2 = new QTableWidget(Statistique);
        tableWidget_2->setObjectName("tableWidget_2");
        tableWidget_2->setGeometry(QRect(390, 130, 421, 451));
        tabWidget->addTab(Statistique, QString());
        localisation = new QWidget();
        localisation->setObjectName("localisation");
        quickWidget = new QQuickWidget(localisation);
        quickWidget->setObjectName("quickWidget");
        quickWidget->setGeometry(QRect(9, 9, 801, 581));
        quickWidget->setResizeMode(QQuickWidget::SizeRootObjectToView);
        tabWidget->addTab(localisation, QString());
        Arduino = new QWidget();
        Arduino->setObjectName("Arduino");
        On = new QPushButton(Arduino);
        On->setObjectName("On");
        On->setGeometry(QRect(660, 80, 75, 23));
        Off = new QPushButton(Arduino);
        Off->setObjectName("Off");
        Off->setGeometry(QRect(660, 40, 75, 23));
        plus = new QPushButton(Arduino);
        plus->setObjectName("plus");
        plus->setGeometry(QRect(660, 120, 75, 23));
        moins = new QPushButton(Arduino);
        moins->setObjectName("moins");
        moins->setGeometry(QRect(670, 190, 75, 23));
        pushButton_recherche_matricule = new QPushButton(Arduino);
        pushButton_recherche_matricule->setObjectName("pushButton_recherche_matricule");
        pushButton_recherche_matricule->setGeometry(QRect(530, 80, 75, 23));
        lineEdit_matricule_2 = new QLineEdit(Arduino);
        lineEdit_matricule_2->setObjectName("lineEdit_matricule_2");
        lineEdit_matricule_2->setGeometry(QRect(400, 80, 113, 20));
        tableView_2 = new QTableView(Arduino);
        tableView_2->setObjectName("tableView_2");
        tableView_2->setGeometry(QRect(40, 130, 611, 261));
        lineEdit_cconstat = new QLineEdit(Arduino);
        lineEdit_cconstat->setObjectName("lineEdit_cconstat");
        lineEdit_cconstat->setGeometry(QRect(660, 310, 113, 20));
        label_2 = new QLabel(Arduino);
        label_2->setObjectName("label_2");
        label_2->setGeometry(QRect(670, 290, 91, 16));
        tabWidget->addTab(Arduino, QString());
        label_logo = new QLabel(groupBox);
        label_logo->setObjectName("label_logo");
        label_logo->setGeometry(QRect(40, 50, 61, 61));
        label_logo->setStyleSheet(QString::fromUtf8(""));
        label_logo->setPixmap(QPixmap(QString::fromUtf8("../../../../Downloads/logo2.png")));
        label_logo->setScaledContents(true);
        label_6 = new QLabel(groupBox);
        label_6->setObjectName("label_6");
        label_6->setGeometry(QRect(130, 80, 111, 31));
        label_6->setStyleSheet(QString::fromUtf8("color: rgb(102, 115, 231);"));
        frame_5 = new QFrame(groupBox);
        frame_5->setObjectName("frame_5");
        frame_5->setGeometry(QRect(30, 170, 263, 396));
        frame_5->setFrameShape(QFrame::StyledPanel);
        frame_5->setFrameShadow(QFrame::Raised);
        verticalLayout_2 = new QVBoxLayout(frame_5);
        verticalLayout_2->setObjectName("verticalLayout_2");
        frame_6 = new QFrame(frame_5);
        frame_6->setObjectName("frame_6");
        frame_6->setFrameShape(QFrame::StyledPanel);
        frame_6->setFrameShadow(QFrame::Raised);
        horizontalLayout_3 = new QHBoxLayout(frame_6);
        horizontalLayout_3->setObjectName("horizontalLayout_3");
        label_14 = new QLabel(frame_6);
        label_14->setObjectName("label_14");
        label_14->setMinimumSize(QSize(40, 40));
        label_14->setMaximumSize(QSize(40, 40));
        label_14->setPixmap(QPixmap(QString::fromUtf8(":/img/images/incorporation.png")));
        label_14->setScaledContents(true);

        horizontalLayout_3->addWidget(label_14);


        verticalLayout_2->addWidget(frame_6);

        frame_7 = new QFrame(frame_5);
        frame_7->setObjectName("frame_7");
        frame_7->setFrameShape(QFrame::StyledPanel);
        frame_7->setFrameShadow(QFrame::Raised);
        horizontalLayout_4 = new QHBoxLayout(frame_7);
        horizontalLayout_4->setObjectName("horizontalLayout_4");
        label_16 = new QLabel(frame_7);
        label_16->setObjectName("label_16");
        label_16->setMinimumSize(QSize(40, 40));
        label_16->setMaximumSize(QSize(40, 40));
        label_16->setPixmap(QPixmap(QString::fromUtf8(":/img/images/employee.png")));
        label_16->setScaledContents(true);

        horizontalLayout_4->addWidget(label_16);

        label_17 = new QLabel(frame_7);
        label_17->setObjectName("label_17");
        QFont font;
        font.setFamilies({QString::fromUtf8("Segoe UI Emoji")});
        font.setPointSize(10);
        label_17->setFont(font);

        horizontalLayout_4->addWidget(label_17);


        verticalLayout_2->addWidget(frame_7);

        frame_8 = new QFrame(frame_5);
        frame_8->setObjectName("frame_8");
        frame_8->setFrameShape(QFrame::StyledPanel);
        frame_8->setFrameShadow(QFrame::Raised);
        horizontalLayout_5 = new QHBoxLayout(frame_8);
        horizontalLayout_5->setObjectName("horizontalLayout_5");
        label_18 = new QLabel(frame_8);
        label_18->setObjectName("label_18");
        label_18->setMinimumSize(QSize(40, 40));
        label_18->setMaximumSize(QSize(40, 40));
        label_18->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_18->setScaledContents(true);

        horizontalLayout_5->addWidget(label_18);

        label_19 = new QLabel(frame_8);
        label_19->setObjectName("label_19");
        label_19->setFont(font);

        horizontalLayout_5->addWidget(label_19);


        verticalLayout_2->addWidget(frame_8);

        frame_9 = new QFrame(frame_5);
        frame_9->setObjectName("frame_9");
        frame_9->setFrameShape(QFrame::StyledPanel);
        frame_9->setFrameShadow(QFrame::Raised);
        horizontalLayout_6 = new QHBoxLayout(frame_9);
        horizontalLayout_6->setObjectName("horizontalLayout_6");
        label_20 = new QLabel(frame_9);
        label_20->setObjectName("label_20");
        label_20->setMinimumSize(QSize(40, 40));
        label_20->setMaximumSize(QSize(40, 40));
        label_20->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_20->setScaledContents(true);

        horizontalLayout_6->addWidget(label_20);

        label_21 = new QLabel(frame_9);
        label_21->setObjectName("label_21");
        label_21->setFont(font);

        horizontalLayout_6->addWidget(label_21);


        verticalLayout_2->addWidget(frame_9);

        frame_10 = new QFrame(frame_5);
        frame_10->setObjectName("frame_10");
        frame_10->setFrameShape(QFrame::StyledPanel);
        frame_10->setFrameShadow(QFrame::Raised);
        horizontalLayout_7 = new QHBoxLayout(frame_10);
        horizontalLayout_7->setObjectName("horizontalLayout_7");
        label_22 = new QLabel(frame_10);
        label_22->setObjectName("label_22");
        label_22->setMinimumSize(QSize(40, 40));
        label_22->setMaximumSize(QSize(40, 40));
        label_22->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_22->setScaledContents(true);

        horizontalLayout_7->addWidget(label_22);

        label_23 = new QLabel(frame_10);
        label_23->setObjectName("label_23");
        label_23->setFont(font);

        horizontalLayout_7->addWidget(label_23);


        verticalLayout_2->addWidget(frame_10);

        frame_11 = new QFrame(frame_5);
        frame_11->setObjectName("frame_11");
        frame_11->setFrameShape(QFrame::StyledPanel);
        frame_11->setFrameShadow(QFrame::Raised);
        horizontalLayout_8 = new QHBoxLayout(frame_11);
        horizontalLayout_8->setObjectName("horizontalLayout_8");
        label_24 = new QLabel(frame_11);
        label_24->setObjectName("label_24");
        label_24->setMinimumSize(QSize(40, 40));
        label_24->setMaximumSize(QSize(40, 40));
        label_24->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_24->setScaledContents(true);

        horizontalLayout_8->addWidget(label_24);

        label_25 = new QLabel(frame_11);
        label_25->setObjectName("label_25");
        label_25->setFont(font);

        horizontalLayout_8->addWidget(label_25);


        verticalLayout_2->addWidget(frame_11);

        blanc->raise();
        violet->raise();
        label->raise();
        tabWidget->raise();
        label_logo->raise();
        label_6->raise();
        frame_5->raise();

        retranslateUi(moduleconstat);

        tabWidget->setCurrentIndex(4);


        QMetaObject::connectSlotsByName(moduleconstat);
    } // setupUi

    void retranslateUi(QDialog *moduleconstat)
    {
        moduleconstat->setWindowTitle(QCoreApplication::translate("moduleconstat", "Dialog", nullptr));
        groupBox->setTitle(QString());
        violet->setText(QString());
        blanc->setText(QString());
        label->setText(QString());
        label_3->setText(QString());
        label_4->setText(QCoreApplication::translate("moduleconstat", "<html><head/><body><p><span style=\" font-size:12pt; font-weight:600; text-decoration: underline; color:#ffffff;\">Rechecher un constat </span></p></body></html>", nullptr));
        label_5->setText(QCoreApplication::translate("moduleconstat", "<html><head/><body><p><span style=\" font-size:12pt; font-weight:600; text-decoration: underline;\">Ajouter un constat</span></p></body></html>", nullptr));
        lineEdit_numero->setPlaceholderText(QCoreApplication::translate("moduleconstat", "Numero de constat", nullptr));
        lineEdit_lieu->setPlaceholderText(QCoreApplication::translate("moduleconstat", "LIEU", nullptr));
        label_point->setText(QCoreApplication::translate("moduleconstat", "<html><head/><body><p><span style=\" font-size:10pt; font-weight:600;\">POINT DE CHOC</span><span style=\" font-size:10pt; font-weight:600; vertical-align:super;\">::</span><span style=\" font-size:10pt; font-weight:600; vertical-align:sub;\">::</span></p></body></html>", nullptr));
        textEdit_obesrvation->setPlaceholderText(QCoreApplication::translate("moduleconstat", "OBSERVATION", nullptr));
        label_circonstance->setText(QCoreApplication::translate("moduleconstat", "<html><head/><body><p><span style=\" font-size:10pt; font-weight:600;\">CIRCONSTANCE</span><span style=\" font-size:10pt; font-weight:600; vertical-align:super;\">::</span><span style=\" font-size:10pt; font-weight:600; vertical-align:sub;\">::</span></p></body></html>", nullptr));
        textEdit_avis->setPlaceholderText(QCoreApplication::translate("moduleconstat", "AVIS AXPERT", nullptr));
        Valider->setText(QCoreApplication::translate("moduleconstat", "Valider", nullptr));
        comboBox_point->setCurrentText(QString());
        lineEdit_matricule->setPlaceholderText(QCoreApplication::translate("moduleconstat", "MATRICULE", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(AJOUTER), QCoreApplication::translate("moduleconstat", "AJOUTER", nullptr));
        affichage_rechercher->setPlaceholderText(QCoreApplication::translate("moduleconstat", "RECHERCHER", nullptr));
        pushButton_Rechercher->setText(QCoreApplication::translate("moduleconstat", "rechercher", nullptr));
        pushButton_Modifier->setText(QCoreApplication::translate("moduleconstat", "modifier", nullptr));
        pushButton_supprimer->setText(QCoreApplication::translate("moduleconstat", "supprimer", nullptr));
        pushButton_PDF->setText(QCoreApplication::translate("moduleconstat", "PDF", nullptr));
        trier->setText(QCoreApplication::translate("moduleconstat", "trier", nullptr));
        EMAIL->setText(QCoreApplication::translate("moduleconstat", "Email", nullptr));
        label_13->setText(QString());
        tabWidget->setTabText(tabWidget->indexOf(Afficherconstat), QCoreApplication::translate("moduleconstat", "AFFICHAGE", nullptr));
        refresh->setText(QCoreApplication::translate("moduleconstat", "Rafra\303\256chir", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(Statistique), QCoreApplication::translate("moduleconstat", "STATISTIQUE", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(localisation), QCoreApplication::translate("moduleconstat", "LOCALISATION", nullptr));
        On->setText(QCoreApplication::translate("moduleconstat", "ON", nullptr));
        Off->setText(QCoreApplication::translate("moduleconstat", "OFF", nullptr));
        plus->setText(QCoreApplication::translate("moduleconstat", "+", nullptr));
        moins->setText(QCoreApplication::translate("moduleconstat", "-", nullptr));
        pushButton_recherche_matricule->setText(QCoreApplication::translate("moduleconstat", "recherche", nullptr));
        label_2->setText(QCoreApplication::translate("moduleconstat", "nombre  constats", nullptr));
        tabWidget->setTabText(tabWidget->indexOf(Arduino), QCoreApplication::translate("moduleconstat", "Arduino", nullptr));
        label_logo->setText(QString());
        label_6->setText(QCoreApplication::translate("moduleconstat", "<html><head/><body><p><span style=\" font-size:16pt; font-weight:600;\">SafeClaim</span></p></body></html>", nullptr));
        label_14->setText(QString());
        label_16->setText(QString());
        label_17->setText(QCoreApplication::translate("moduleconstat", "Employees", nullptr));
        label_18->setText(QString());
        label_19->setText(QCoreApplication::translate("moduleconstat", "Vehicles", nullptr));
        label_20->setText(QString());
        label_21->setText(QCoreApplication::translate("moduleconstat", "Collaboration", nullptr));
        label_22->setText(QString());
        label_23->setText(QCoreApplication::translate("moduleconstat", "Clients", nullptr));
        label_24->setText(QString());
        label_25->setText(QCoreApplication::translate("moduleconstat", "Contrat", nullptr));
    } // retranslateUi

};

namespace Ui {
    class moduleconstat: public Ui_moduleconstat {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_MODULECONSTAT_H
