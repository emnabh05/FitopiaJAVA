/********************************************************************************
** Form generated from reading UI file 'pageali.ui'
**
** Created by: Qt User Interface Compiler version 6.6.3
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_PAGEALI_H
#define UI_PAGEALI_H

#include <QtCore/QVariant>
#include <QtGui/QIcon>
#include <QtWidgets/QApplication>
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
#include <QtWidgets/QTableView>
#include <QtWidgets/QVBoxLayout>

QT_BEGIN_NAMESPACE

class Ui_pageali
{
public:
    QGroupBox *grp_modifier;
    QFrame *frame_14;
    QFrame *frame_2;
    QVBoxLayout *verticalLayout_3;
    QFrame *frame_3;
    QLabel *label_9;
    QLabel *label_10;
    QFrame *frame_5;
    QVBoxLayout *verticalLayout;
    QFrame *frame_4;
    QHBoxLayout *horizontalLayout_3;
    QLabel *label_21;
    QLabel *label_22;
    QFrame *frame_6;
    QHBoxLayout *horizontalLayout_4;
    QLabel *label_23;
    QLabel *label_24;
    QFrame *frame_7;
    QHBoxLayout *horizontalLayout_5;
    QLabel *label_25;
    QLabel *label_26;
    QFrame *frame_8;
    QHBoxLayout *horizontalLayout_6;
    QLabel *label_27;
    QLabel *label_28;
    QFrame *frame_9;
    QHBoxLayout *horizontalLayout_7;
    QLabel *label_36;
    QLabel *label_37;
    QFrame *frame_10;
    QHBoxLayout *horizontalLayout_8;
    QLabel *label_38;
    QLabel *label_39;
    QFrame *frame_12;
    QFrame *frame_13;
    QFrame *frame_15;
    QHBoxLayout *horizontalLayout_10;
    QFrame *frame_16;
    QLabel *label_29;
    QDateEdit *m_date_crea;
    QFrame *frame_17;
    QLabel *label_30;
    QDateEdit *date_ter;
    QFrame *frame_18;
    QLabel *label_3;
    QLineEdit *duration_cont;
    QPushButton *con_modifier;
    QFrame *frame_19;
    QLabel *label_4;
    QLineEdit *montant_c;
    QLineEdit *id_c;
    QComboBox *categorie_c;
    QLabel *label_8;
    QLabel *label_13;
    QGroupBox *grp_contart;
    QLabel *label;
    QPushButton *creer;
    QPushButton *suprimer;
    QPushButton *update;
    QPushButton *pdf;
    QPushButton *filtrer;
    QLineEdit *recherche;
    QLabel *label_2;
    QTableView *tableView;
    QLabel *label_16;
    QPushButton *pushButton;
    QLabel *qrcode_3;
    QPushButton *qrcode;
    QLineEdit *qrcode_2;
    QLabel *label_18;
    QPushButton *pushButton_3;
    QLabel *label_20;
    QLabel *arduino;
    QGroupBox *grp_ajout;
    QFrame *frame_11;
    QLabel *label_19;
    QLabel *label_11;
    QLineEdit *montant;
    QLabel *label_12;
    QLabel *label_14;
    QLineEdit *duree;
    QDateEdit *date_creation;
    QLabel *label_15;
    QPushButton *confir_ajout;
    QLabel *label_7;
    QDateEdit *date_fin;
    QFrame *frame;
    QVBoxLayout *verticalLayout_2;
    QLabel *label_17;
    QLabel *label_5;
    QLabel *label_6;
    QComboBox *categorie;
    QPushButton *pushButton_2;
    QLineEdit *id;

    void setupUi(QDialog *pageali)
    {
        if (pageali->objectName().isEmpty())
            pageali->setObjectName("pageali");
        pageali->resize(1900, 1000);
        grp_modifier = new QGroupBox(pageali);
        grp_modifier->setObjectName("grp_modifier");
        grp_modifier->setGeometry(QRect(0, 0, 1900, 1000));
        frame_14 = new QFrame(grp_modifier);
        frame_14->setObjectName("frame_14");
        frame_14->setGeometry(QRect(0, -40, 1900, 1000));
        frame_14->setStyleSheet(QString::fromUtf8("background-color: rgb(85, 85, 255);\n"
"border-top-left-radius: 15px;\n"
"border-bottom-left-radius: 15px;"));
        frame_14->setFrameShape(QFrame::StyledPanel);
        frame_14->setFrameShadow(QFrame::Raised);
        frame_2 = new QFrame(frame_14);
        frame_2->setObjectName("frame_2");
        frame_2->setGeometry(QRect(40, 60, 350, 750));
        frame_2->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        frame_2->setFrameShape(QFrame::StyledPanel);
        frame_2->setFrameShadow(QFrame::Raised);
        verticalLayout_3 = new QVBoxLayout(frame_2);
        verticalLayout_3->setObjectName("verticalLayout_3");
        frame_3 = new QFrame(frame_2);
        frame_3->setObjectName("frame_3");
        frame_3->setFrameShape(QFrame::StyledPanel);
        frame_3->setFrameShadow(QFrame::Raised);
        label_9 = new QLabel(frame_3);
        label_9->setObjectName("label_9");
        label_9->setGeometry(QRect(-20, 20, 130, 130));
        label_9->setMinimumSize(QSize(130, 130));
        label_9->setMaximumSize(QSize(130, 130));
        label_9->setPixmap(QPixmap(QString::fromUtf8("image/logo.png")));
        label_9->setScaledContents(true);
        label_10 = new QLabel(frame_3);
        label_10->setObjectName("label_10");
        label_10->setGeometry(QRect(110, 50, 131, 51));
        QFont font;
        font.setFamilies({QString::fromUtf8("Nirmala UI")});
        font.setPointSize(15);
        font.setBold(true);
        label_10->setFont(font);
        label_10->setStyleSheet(QString::fromUtf8("color: rgb(102, 115, 231);"));

        verticalLayout_3->addWidget(frame_3);

        frame_5 = new QFrame(frame_2);
        frame_5->setObjectName("frame_5");
        frame_5->setFrameShape(QFrame::StyledPanel);
        frame_5->setFrameShadow(QFrame::Raised);
        verticalLayout = new QVBoxLayout(frame_5);
        verticalLayout->setObjectName("verticalLayout");
        frame_4 = new QFrame(frame_5);
        frame_4->setObjectName("frame_4");
        frame_4->setFrameShape(QFrame::StyledPanel);
        frame_4->setFrameShadow(QFrame::Raised);
        horizontalLayout_3 = new QHBoxLayout(frame_4);
        horizontalLayout_3->setObjectName("horizontalLayout_3");
        label_21 = new QLabel(frame_4);
        label_21->setObjectName("label_21");
        label_21->setMinimumSize(QSize(40, 40));
        label_21->setMaximumSize(QSize(40, 40));
        label_21->setPixmap(QPixmap(QString::fromUtf8(":/img/images/incorporation.png")));
        label_21->setScaledContents(true);

        horizontalLayout_3->addWidget(label_21);

        label_22 = new QLabel(frame_4);
        label_22->setObjectName("label_22");
        QFont font1;
        font1.setFamilies({QString::fromUtf8("Segoe UI Emoji")});
        font1.setPointSize(10);
        label_22->setFont(font1);
        label_22->setStyleSheet(QString::fromUtf8("MS Shell Dlg 2"));

        horizontalLayout_3->addWidget(label_22);


        verticalLayout->addWidget(frame_4);

        frame_6 = new QFrame(frame_5);
        frame_6->setObjectName("frame_6");
        frame_6->setFrameShape(QFrame::StyledPanel);
        frame_6->setFrameShadow(QFrame::Raised);
        horizontalLayout_4 = new QHBoxLayout(frame_6);
        horizontalLayout_4->setObjectName("horizontalLayout_4");
        label_23 = new QLabel(frame_6);
        label_23->setObjectName("label_23");
        label_23->setMinimumSize(QSize(40, 40));
        label_23->setMaximumSize(QSize(40, 40));
        label_23->setPixmap(QPixmap(QString::fromUtf8(":/img/images/employee.png")));
        label_23->setScaledContents(true);

        horizontalLayout_4->addWidget(label_23);

        label_24 = new QLabel(frame_6);
        label_24->setObjectName("label_24");
        label_24->setFont(font1);

        horizontalLayout_4->addWidget(label_24);


        verticalLayout->addWidget(frame_6);

        frame_7 = new QFrame(frame_5);
        frame_7->setObjectName("frame_7");
        frame_7->setFrameShape(QFrame::StyledPanel);
        frame_7->setFrameShadow(QFrame::Raised);
        horizontalLayout_5 = new QHBoxLayout(frame_7);
        horizontalLayout_5->setObjectName("horizontalLayout_5");
        label_25 = new QLabel(frame_7);
        label_25->setObjectName("label_25");
        label_25->setMinimumSize(QSize(40, 40));
        label_25->setMaximumSize(QSize(40, 40));
        label_25->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_25->setScaledContents(true);

        horizontalLayout_5->addWidget(label_25);

        label_26 = new QLabel(frame_7);
        label_26->setObjectName("label_26");
        label_26->setFont(font1);

        horizontalLayout_5->addWidget(label_26);


        verticalLayout->addWidget(frame_7);

        frame_8 = new QFrame(frame_5);
        frame_8->setObjectName("frame_8");
        frame_8->setFrameShape(QFrame::StyledPanel);
        frame_8->setFrameShadow(QFrame::Raised);
        horizontalLayout_6 = new QHBoxLayout(frame_8);
        horizontalLayout_6->setObjectName("horizontalLayout_6");
        label_27 = new QLabel(frame_8);
        label_27->setObjectName("label_27");
        label_27->setMinimumSize(QSize(40, 40));
        label_27->setMaximumSize(QSize(40, 40));
        label_27->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_27->setScaledContents(true);

        horizontalLayout_6->addWidget(label_27);

        label_28 = new QLabel(frame_8);
        label_28->setObjectName("label_28");
        label_28->setFont(font1);

        horizontalLayout_6->addWidget(label_28);


        verticalLayout->addWidget(frame_8);

        frame_9 = new QFrame(frame_5);
        frame_9->setObjectName("frame_9");
        frame_9->setFrameShape(QFrame::StyledPanel);
        frame_9->setFrameShadow(QFrame::Raised);
        horizontalLayout_7 = new QHBoxLayout(frame_9);
        horizontalLayout_7->setObjectName("horizontalLayout_7");
        label_36 = new QLabel(frame_9);
        label_36->setObjectName("label_36");
        label_36->setMinimumSize(QSize(40, 40));
        label_36->setMaximumSize(QSize(40, 40));
        label_36->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_36->setScaledContents(true);

        horizontalLayout_7->addWidget(label_36);

        label_37 = new QLabel(frame_9);
        label_37->setObjectName("label_37");
        label_37->setFont(font1);

        horizontalLayout_7->addWidget(label_37);


        verticalLayout->addWidget(frame_9);

        frame_10 = new QFrame(frame_5);
        frame_10->setObjectName("frame_10");
        frame_10->setFrameShape(QFrame::StyledPanel);
        frame_10->setFrameShadow(QFrame::Raised);
        horizontalLayout_8 = new QHBoxLayout(frame_10);
        horizontalLayout_8->setObjectName("horizontalLayout_8");
        label_38 = new QLabel(frame_10);
        label_38->setObjectName("label_38");
        label_38->setMinimumSize(QSize(40, 40));
        label_38->setMaximumSize(QSize(40, 40));
        label_38->setPixmap(QPixmap(QString::fromUtf8(":/img/images/license-plate.png")));
        label_38->setScaledContents(true);

        horizontalLayout_8->addWidget(label_38);

        label_39 = new QLabel(frame_10);
        label_39->setObjectName("label_39");
        label_39->setFont(font1);

        horizontalLayout_8->addWidget(label_39);


        verticalLayout->addWidget(frame_10);


        verticalLayout_3->addWidget(frame_5);

        frame_12 = new QFrame(frame_14);
        frame_12->setObjectName("frame_12");
        frame_12->setGeometry(QRect(-110, 290, 2131, 711));
        frame_12->setStyleSheet(QString::fromUtf8("background-color:rgb(245,246,247);"));
        frame_12->setFrameShape(QFrame::StyledPanel);
        frame_12->setFrameShadow(QFrame::Raised);
        frame_13 = new QFrame(frame_14);
        frame_13->setObjectName("frame_13");
        frame_13->setGeometry(QRect(440, 140, 1221, 551));
        frame_13->setStyleSheet(QString::fromUtf8("background-color: rgb(85, 85, 255);\n"
"border-radius:40px;"));
        frame_13->setFrameShape(QFrame::StyledPanel);
        frame_13->setFrameShadow(QFrame::Raised);
        frame_15 = new QFrame(frame_13);
        frame_15->setObjectName("frame_15");
        frame_15->setGeometry(QRect(40, 130, 1131, 81));
        frame_15->setFrameShape(QFrame::StyledPanel);
        frame_15->setFrameShadow(QFrame::Raised);
        horizontalLayout_10 = new QHBoxLayout(frame_15);
        horizontalLayout_10->setObjectName("horizontalLayout_10");
        frame_16 = new QFrame(frame_15);
        frame_16->setObjectName("frame_16");
        frame_16->setFrameShape(QFrame::StyledPanel);
        frame_16->setFrameShadow(QFrame::Raised);
        label_29 = new QLabel(frame_16);
        label_29->setObjectName("label_29");
        label_29->setGeometry(QRect(2, 9, 161, 41));
        label_29->setStyleSheet(QString::fromUtf8("font: 12pt \"MS Shell Dlg 2\";"));
        m_date_crea = new QDateEdit(frame_16);
        m_date_crea->setObjectName("m_date_crea");
        m_date_crea->setGeometry(QRect(240, 0, 271, 51));
        m_date_crea->setStyleSheet(QString::fromUtf8(""));
        frame_17 = new QFrame(frame_16);
        frame_17->setObjectName("frame_17");
        frame_17->setGeometry(QRect(510, 0, 581, 63));
        frame_17->setFrameShape(QFrame::StyledPanel);
        frame_17->setFrameShadow(QFrame::Raised);
        label_30 = new QLabel(frame_17);
        label_30->setObjectName("label_30");
        label_30->setGeometry(QRect(50, 0, 181, 51));
        label_30->setStyleSheet(QString::fromUtf8("font: 12pt \"MS Shell Dlg 2\";"));
        date_ter = new QDateEdit(frame_17);
        date_ter->setObjectName("date_ter");
        date_ter->setGeometry(QRect(300, 0, 241, 51));

        horizontalLayout_10->addWidget(frame_16);

        frame_18 = new QFrame(frame_13);
        frame_18->setObjectName("frame_18");
        frame_18->setGeometry(QRect(40, 250, 1121, 80));
        frame_18->setFrameShape(QFrame::StyledPanel);
        frame_18->setFrameShadow(QFrame::Raised);
        label_3 = new QLabel(frame_18);
        label_3->setObjectName("label_3");
        label_3->setGeometry(QRect(20, 20, 291, 41));
        label_3->setStyleSheet(QString::fromUtf8("font: 12pt \"MS Shell Dlg 2\";"));
        duration_cont = new QLineEdit(frame_18);
        duration_cont->setObjectName("duration_cont");
        duration_cont->setGeometry(QRect(530, 20, 261, 31));
        duration_cont->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:rgb(245,246,247);"));
        con_modifier = new QPushButton(frame_13);
        con_modifier->setObjectName("con_modifier");
        con_modifier->setGeometry(QRect(470, 480, 241, 51));
        QFont font2;
        font2.setPointSize(12);
        con_modifier->setFont(font2);
        con_modifier->setStyleSheet(QString::fromUtf8("QPushButton#pushButton{\n"
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
        frame_19 = new QFrame(frame_13);
        frame_19->setObjectName("frame_19");
        frame_19->setGeometry(QRect(40, 380, 1121, 80));
        frame_19->setFrameShape(QFrame::StyledPanel);
        frame_19->setFrameShadow(QFrame::Raised);
        label_4 = new QLabel(frame_19);
        label_4->setObjectName("label_4");
        label_4->setGeometry(QRect(30, 30, 361, 21));
        label_4->setStyleSheet(QString::fromUtf8("font: 12pt \"MS Shell Dlg 2\";"));
        montant_c = new QLineEdit(frame_19);
        montant_c->setObjectName("montant_c");
        montant_c->setGeometry(QRect(520, 20, 241, 31));
        montant_c->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        id_c = new QLineEdit(frame_13);
        id_c->setObjectName("id_c");
        id_c->setGeometry(QRect(1010, 30, 181, 81));
        id_c->setStyleSheet(QString::fromUtf8("background-color:white;"));
        categorie_c = new QComboBox(frame_13);
        categorie_c->addItem(QString());
        categorie_c->addItem(QString());
        categorie_c->addItem(QString());
        categorie_c->addItem(QString());
        categorie_c->setObjectName("categorie_c");
        categorie_c->setGeometry(QRect(400, 50, 221, 41));
        categorie_c->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        label_8 = new QLabel(frame_13);
        label_8->setObjectName("label_8");
        label_8->setGeometry(QRect(880, 50, 91, 31));
        label_8->setStyleSheet(QString::fromUtf8("font: 12pt \"MS Shell Dlg 2\";"));
        label_13 = new QLabel(frame_13);
        label_13->setObjectName("label_13");
        label_13->setGeometry(QRect(50, 50, 271, 31));
        label_13->setStyleSheet(QString::fromUtf8("font: 12pt \"MS Shell Dlg 2\";"));
        frame_15->raise();
        frame_18->raise();
        con_modifier->raise();
        frame_19->raise();
        categorie_c->raise();
        label_8->raise();
        label_13->raise();
        id_c->raise();
        frame_12->raise();
        frame_2->raise();
        frame_13->raise();
        grp_contart = new QGroupBox(pageali);
        grp_contart->setObjectName("grp_contart");
        grp_contart->setGeometry(QRect(0, 0, 1900, 1000));
        label = new QLabel(grp_contart);
        label->setObjectName("label");
        label->setGeometry(QRect(-10, 0, 1900, 1000));
        label->setStyleSheet(QString::fromUtf8("background-color: rgb(73, 53, 212);\n"
"border-top-left-radius: 15px;\n"
"border-bottom-left-radius: 15px;"));
        creer = new QPushButton(grp_contart);
        creer->setObjectName("creer");
        creer->setGeometry(QRect(50, 330, 248, 37));
        creer->setStyleSheet(QString::fromUtf8("background-image: url(:/new/prefix1/image/creer.png);\n"
"border-radius : 30px;"));
        suprimer = new QPushButton(grp_contart);
        suprimer->setObjectName("suprimer");
        suprimer->setGeometry(QRect(390, 330, 248, 37));
        suprimer->setStyleSheet(QString::fromUtf8("background-image: url(:/new/prefix1/image/387333297_1779468429146928_8689424470225080288_n.png);\n"
"border-radius : 30px;"));
        update = new QPushButton(grp_contart);
        update->setObjectName("update");
        update->setGeometry(QRect(730, 330, 248, 37));
        update->setStyleSheet(QString::fromUtf8("background-image: url(:/new/prefix1/image/387332704_1343940753154891_2029766032942571898_n.png);\n"
"border-radius :30px;"));
        pdf = new QPushButton(grp_contart);
        pdf->setObjectName("pdf");
        pdf->setGeometry(QRect(1530, 330, 205, 37));
        pdf->setStyleSheet(QString::fromUtf8("background-image: url(:/new/prefix1/image/385541660_555250976809252_8580406311245941901_n.png);\n"
"border-radius : 30px;"));
        filtrer = new QPushButton(grp_contart);
        filtrer->setObjectName("filtrer");
        filtrer->setGeometry(QRect(1210, 330, 131, 41));
        filtrer->setStyleSheet(QString::fromUtf8("\n"
"background-image: url(:/new/prefix1/image/393114558_880180833545879_7951550619624183475_n.png);\n"
"border-radius : 15px;"));
        recherche = new QLineEdit(grp_contart);
        recherche->setObjectName("recherche");
        recherche->setGeometry(QRect(390, 430, 261, 31));
        recherche->setStyleSheet(QString::fromUtf8("background-color : transparent;\n"
"border :none ;\n"
"font: 14pt \"MS Shell Dlg 2\";"));
        label_2 = new QLabel(grp_contart);
        label_2->setObjectName("label_2");
        label_2->setGeometry(QRect(440, 240, 261, 2));
        label_2->setStyleSheet(QString::fromUtf8("background-color:red;"));
        tableView = new QTableView(grp_contart);
        tableView->setObjectName("tableView");
        tableView->setGeometry(QRect(30, 470, 1791, 471));
        label_16 = new QLabel(grp_contart);
        label_16->setObjectName("label_16");
        label_16->setGeometry(QRect(30, 20, 411, 281));
        pushButton = new QPushButton(grp_contart);
        pushButton->setObjectName("pushButton");
        pushButton->setGeometry(QRect(1220, 400, 111, 31));
        pushButton->setStyleSheet(QString::fromUtf8("background-color: rgb(191, 194, 197);\n"
"\n"
"\n"
"\n"
"color: rgb(170, 0, 127);\n"
"font: 87 10pt \"Arial Black\";\n"
"text-decoration: underline;"));
        qrcode_3 = new QLabel(grp_contart);
        qrcode_3->setObjectName("qrcode_3");
        qrcode_3->setGeometry(QRect(1490, 40, 200, 200));
        qrcode_3->setStyleSheet(QString::fromUtf8(""));
        qrcode = new QPushButton(grp_contart);
        qrcode->setObjectName("qrcode");
        qrcode->setGeometry(QRect(1760, 160, 111, 41));
        qrcode->setStyleSheet(QString::fromUtf8("font: 87 10pt \"Arial Black\";\n"
"text-decoration: underline;"));
        qrcode_2 = new QLineEdit(grp_contart);
        qrcode_2->setObjectName("qrcode_2");
        qrcode_2->setGeometry(QRect(1760, 120, 113, 31));
        qrcode_2->setStyleSheet(QString::fromUtf8("background-color: rgb(85, 0, 127);\n"
"font: 9pt \"MV Boli\";"));
        label_18 = new QLabel(grp_contart);
        label_18->setObjectName("label_18");
        label_18->setGeometry(QRect(1750, 80, 141, 31));
        label_18->setStyleSheet(QString::fromUtf8("\n"
"\n"
"\n"
"\n"
"color: rgb(170, 0, 127);\n"
"font: 87 10pt \"Arial Black\";\n"
"text-decoration: underline;"));
        pushButton_3 = new QPushButton(grp_contart);
        pushButton_3->setObjectName("pushButton_3");
        pushButton_3->setGeometry(QRect(1530, 390, 201, 41));
        pushButton_3->setStyleSheet(QString::fromUtf8("background-color: rgb(191, 194, 197);\n"
"\n"
"\n"
"\n"
"color: rgb(170, 0, 127);\n"
"font: 87 10pt \"Arial Black\";\n"
"text-decoration: underline;"));
        label_20 = new QLabel(grp_contart);
        label_20->setObjectName("label_20");
        label_20->setGeometry(QRect(1460, 280, 261, 2));
        label_20->setStyleSheet(QString::fromUtf8("background-color:red;"));
        arduino = new QLabel(grp_contart);
        arduino->setObjectName("arduino");
        arduino->setGeometry(QRect(450, 130, 231, 91));
        arduino->setStyleSheet(QString::fromUtf8("background-color : transparent;\n"
"border :none ;\n"
"font: 14pt \"MS Shell Dlg 2\";"));
        grp_ajout = new QGroupBox(pageali);
        grp_ajout->setObjectName("grp_ajout");
        grp_ajout->setGeometry(QRect(0, 0, 1900, 1000));
        frame_11 = new QFrame(grp_ajout);
        frame_11->setObjectName("frame_11");
        frame_11->setGeometry(QRect(0, 0, 1900, 1000));
        frame_11->setStyleSheet(QString::fromUtf8("background-color: rgb(85, 85, 255);\n"
"border-top-left-radius: 15px;\n"
"border-bottom-left-radius: 15px;"));
        frame_11->setFrameShape(QFrame::StyledPanel);
        frame_11->setFrameShadow(QFrame::Raised);
        label_19 = new QLabel(frame_11);
        label_19->setObjectName("label_19");
        label_19->setGeometry(QRect(680, 300, 241, 51));
        QFont font3;
        font3.setFamilies({QString::fromUtf8("MS Shell Dlg 2")});
        font3.setPointSize(16);
        font3.setBold(false);
        font3.setItalic(false);
        label_19->setFont(font3);
        label_19->setStyleSheet(QString::fromUtf8("font: 75 16pt \"MS Shell Dlg 2\";\n"
"color: rgb(0, 0, 0);"));
        label_11 = new QLabel(frame_11);
        label_11->setObjectName("label_11");
        label_11->setGeometry(QRect(670, 600, 261, 31));
        label_11->setStyleSheet(QString::fromUtf8("font: 75 16pt \"MS Shell Dlg 2\";\n"
"color: rgb(0, 0, 0);"));
        montant = new QLineEdit(frame_11);
        montant->setObjectName("montant");
        montant->setGeometry(QRect(980, 670, 141, 31));
        montant->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        label_12 = new QLabel(frame_11);
        label_12->setObjectName("label_12");
        label_12->setGeometry(QRect(670, 390, 251, 31));
        label_12->setStyleSheet(QString::fromUtf8("font: 75 16pt \"MS Shell Dlg 2\";\n"
"color: rgb(0, 0, 0);"));
        label_14 = new QLabel(frame_11);
        label_14->setObjectName("label_14");
        label_14->setGeometry(QRect(660, 670, 281, 41));
        label_14->setStyleSheet(QString::fromUtf8("font: 75 16pt \"MS Shell Dlg 2\";\n"
"color: rgb(0, 0, 0);"));
        duree = new QLineEdit(frame_11);
        duree->setObjectName("duree");
        duree->setGeometry(QRect(970, 610, 141, 31));
        duree->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        date_creation = new QDateEdit(frame_11);
        date_creation->setObjectName("date_creation");
        date_creation->setGeometry(QRect(970, 460, 194, 51));
        QFont font4;
        font4.setPointSize(7);
        font4.setBold(true);
        font4.setItalic(false);
        date_creation->setFont(font4);
        date_creation->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        date_creation->setReadOnly(false);
        date_creation->setCalendarPopup(true);
        label_15 = new QLabel(frame_11);
        label_15->setObjectName("label_15");
        label_15->setGeometry(QRect(670, 470, 261, 31));
        label_15->setStyleSheet(QString::fromUtf8("font: 75 16pt \"MS Shell Dlg 2\";\n"
"color: rgb(0, 0, 0);\n"
""));
        confir_ajout = new QPushButton(frame_11);
        confir_ajout->setObjectName("confir_ajout");
        confir_ajout->setGeometry(QRect(970, 730, 241, 51));
        confir_ajout->setFont(font2);
        confir_ajout->setStyleSheet(QString::fromUtf8("QPushButton#pushButton{\n"
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
        label_7 = new QLabel(frame_11);
        label_7->setObjectName("label_7");
        label_7->setGeometry(QRect(670, 540, 291, 31));
        label_7->setStyleSheet(QString::fromUtf8("font: 75 16pt \"MS Shell Dlg 2\";\n"
"color: rgb(0, 0, 0);\n"
""));
        date_fin = new QDateEdit(frame_11);
        date_fin->setObjectName("date_fin");
        date_fin->setGeometry(QRect(980, 540, 171, 31));
        date_fin->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        frame = new QFrame(frame_11);
        frame->setObjectName("frame");
        frame->setGeometry(QRect(30, 80, 341, 131));
        frame->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        frame->setFrameShape(QFrame::StyledPanel);
        frame->setFrameShadow(QFrame::Raised);
        verticalLayout_2 = new QVBoxLayout(frame);
        verticalLayout_2->setObjectName("verticalLayout_2");
        label_17 = new QLabel(frame);
        label_17->setObjectName("label_17");
        label_17->setMinimumSize(QSize(130, 130));
        label_17->setMaximumSize(QSize(130, 130));
        label_17->setPixmap(QPixmap(QString::fromUtf8("image/logo.png")));
        label_17->setScaledContents(true);

        verticalLayout_2->addWidget(label_17);

        label_5 = new QLabel(frame_11);
        label_5->setObjectName("label_5");
        label_5->setGeometry(QRect(30, 190, 130, 130));
        label_5->setMinimumSize(QSize(130, 130));
        label_5->setMaximumSize(QSize(130, 130));
        label_5->setStyleSheet(QString::fromUtf8("background-color : transparent;"));
        label_5->setPixmap(QPixmap(QString::fromUtf8(":/img/images/logoblue.png")));
        label_5->setScaledContents(true);
        label_6 = new QLabel(frame_11);
        label_6->setObjectName("label_6");
        label_6->setGeometry(QRect(200, 100, 141, 91));
        label_6->setFont(font);
        label_6->setStyleSheet(QString::fromUtf8("color: rgb(102, 115, 231);\n"
"background-color: transparent"));
        categorie = new QComboBox(frame_11);
        categorie->addItem(QString());
        categorie->addItem(QString());
        categorie->addItem(QString());
        categorie->addItem(QString());
        categorie->setObjectName("categorie");
        categorie->setGeometry(QRect(960, 390, 221, 41));
        categorie->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);"));
        pushButton_2 = new QPushButton(frame_11);
        pushButton_2->setObjectName("pushButton_2");
        pushButton_2->setGeometry(QRect(1800, 10, 91, 71));
        QIcon icon;
        icon.addFile(QString::fromUtf8(":/new/prefix1/image/nonvalide.png"), QSize(), QIcon::Normal, QIcon::Off);
        pushButton_2->setIcon(icon);
        pushButton_2->setIconSize(QSize(50, 50));
        id = new QLineEdit(frame_11);
        id->setObjectName("id");
        id->setGeometry(QRect(1130, 300, 211, 61));
        id->setStyleSheet(QString::fromUtf8("background-color : white;"));

        retranslateUi(pageali);

        QMetaObject::connectSlotsByName(pageali);
    } // setupUi

    void retranslateUi(QDialog *pageali)
    {
        pageali->setWindowTitle(QCoreApplication::translate("pageali", "Dialog", nullptr));
        grp_modifier->setTitle(QString());
        label_9->setText(QString());
        label_10->setText(QCoreApplication::translate("pageali", "SafeClaim", nullptr));
        label_21->setText(QString());
        label_22->setText(QCoreApplication::translate("pageali", "AddEmployee", nullptr));
        label_23->setText(QString());
        label_24->setText(QCoreApplication::translate("pageali", "Employees", nullptr));
        label_25->setText(QString());
        label_26->setText(QCoreApplication::translate("pageali", "Vehicles", nullptr));
        label_27->setText(QString());
        label_28->setText(QCoreApplication::translate("pageali", "Placeholder", nullptr));
        label_36->setText(QString());
        label_37->setText(QCoreApplication::translate("pageali", "Placeholder", nullptr));
        label_38->setText(QString());
        label_39->setText(QCoreApplication::translate("pageali", "Placeholder", nullptr));
        label_29->setText(QCoreApplication::translate("pageali", "Date of Creation :", nullptr));
        label_30->setText(QCoreApplication::translate("pageali", "Date of Termination", nullptr));
        label_3->setText(QCoreApplication::translate("pageali", "Duration  of  Contract   :", nullptr));
        con_modifier->setText(QCoreApplication::translate("pageali", "register contract", nullptr));
        label_4->setText(QCoreApplication::translate("pageali", "Montant :", nullptr));
        categorie_c->setItemText(0, QCoreApplication::translate("pageali", "TYPE CONTRACT", nullptr));
        categorie_c->setItemText(1, QCoreApplication::translate("pageali", "VOLS", nullptr));
        categorie_c->setItemText(2, QCoreApplication::translate("pageali", "TOUS RISQUES", nullptr));
        categorie_c->setItemText(3, QCoreApplication::translate("pageali", "ACCIDENT", nullptr));

        label_8->setText(QCoreApplication::translate("pageali", "ID exist\303\251 :", nullptr));
        label_13->setText(QCoreApplication::translate("pageali", "choose the type of contract :", nullptr));
        grp_contart->setTitle(QString());
        label->setText(QString());
        creer->setText(QString());
        suprimer->setText(QString());
        update->setText(QString());
        pdf->setText(QString());
        filtrer->setText(QString());
        recherche->setPlaceholderText(QCoreApplication::translate("pageali", "Recherche", nullptr));
        label_2->setText(QString());
        label_16->setText(QString());
        pushButton->setText(QCoreApplication::translate("pageali", "stat", nullptr));
        qrcode_3->setText(QString());
        qrcode->setText(QCoreApplication::translate("pageali", "QR_CODE", nullptr));
        qrcode_2->setText(QString());
        label_18->setText(QCoreApplication::translate("pageali", "SET ID HERE", nullptr));
        pushButton_3->setText(QCoreApplication::translate("pageali", "Export EXCEL", nullptr));
        label_20->setText(QString());
        arduino->setText(QString());
        grp_ajout->setTitle(QString());
        label_19->setText(QCoreApplication::translate("pageali", "ID_CONTRACT", nullptr));
        label_11->setText(QCoreApplication::translate("pageali", "<html><head/><body><p>DURATION :<span style=\" color:#00ffff; vertical-align:super;\">::</span><span style=\" color:#00ffff; vertical-align:sub;\">::</span></p></body></html>", nullptr));
        label_12->setText(QCoreApplication::translate("pageali", "<html><head/><body><p>TYPE OF CONTRACT<span style=\" color:#00ffff; vertical-align:super;\">::</span><span style=\" color:#00ffff; vertical-align:sub;\">::</span></p></body></html>", nullptr));
        label_14->setText(QCoreApplication::translate("pageali", "<html><head/><body><p>MONTANT :<span style=\" color:#00ffff; vertical-align:super;\">::</span><span style=\" color:#00ffff; vertical-align:sub;\">::</span></p></body></html>", nullptr));
        duree->setText(QString());
        label_15->setText(QCoreApplication::translate("pageali", "<html><head/><body><p>DATE OF CREATION :<span style=\" vertical-align:super;\">:</span><span style=\" color:#00ffff; vertical-align:super;\">:</span><span style=\" color:#00ffff; vertical-align:sub;\">::</span></p></body></html>", nullptr));
        confir_ajout->setText(QCoreApplication::translate("pageali", "Register Contract", nullptr));
        label_7->setText(QCoreApplication::translate("pageali", "DATE OF TERMINATION", nullptr));
        label_17->setText(QString());
        label_5->setText(QString());
        label_6->setText(QCoreApplication::translate("pageali", "SafeClaim", nullptr));
        categorie->setItemText(0, QCoreApplication::translate("pageali", "Choose the type of Contract", nullptr));
        categorie->setItemText(1, QCoreApplication::translate("pageali", "Vols", nullptr));
        categorie->setItemText(2, QCoreApplication::translate("pageali", "Accident", nullptr));
        categorie->setItemText(3, QCoreApplication::translate("pageali", "Tous Risques", nullptr));

        pushButton_2->setText(QString());
    } // retranslateUi

};

namespace Ui {
    class pageali: public Ui_pageali {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_PAGEALI_H
