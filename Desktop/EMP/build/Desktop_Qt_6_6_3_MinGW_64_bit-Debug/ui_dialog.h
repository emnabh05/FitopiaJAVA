/********************************************************************************
** Form generated from reading UI file 'dialog.ui'
**
** Created by: Qt User Interface Compiler version 6.6.3
**
** WARNING! All changes made in this file will be lost when recompiling UI file!
********************************************************************************/

#ifndef UI_DIALOG_H
#define UI_DIALOG_H

#include <QtCore/QVariant>
#include <QtWidgets/QApplication>
#include <QtWidgets/QDateEdit>
#include <QtWidgets/QDialog>
#include <QtWidgets/QGroupBox>
#include <QtWidgets/QLabel>
#include <QtWidgets/QLineEdit>
#include <QtWidgets/QPlainTextEdit>
#include <QtWidgets/QTextEdit>
#include <QtWidgets/QTimeEdit>

QT_BEGIN_NAMESPACE

class Ui_Dialog
{
public:
    QGroupBox *groupBox;
    QLabel *label_numero_D;
    QLabel *label_date_D;
    QLabel *label_3;
    QLabel *label_point_D;
    QLabel *label_observation_D;
    QLabel *label_circonstance_D;
    QLabel *label_avis_D;
    QLabel *label_lieu_D;
    QLineEdit *lineEdit_numero_D;
    QDateEdit *dateEdit_D;
    QTimeEdit *timeEdit_heure_D;
    QPlainTextEdit *TextEdit_observation_D;
    QTextEdit *textEdit_avis_D;
    QLineEdit *lineEdit_lieu_D;
    QLabel *label;
    QLabel *label_2;
    QLabel *label_4;
    QLabel *label_5;
    QLineEdit *lineEdit_circonstance_D;
    QLineEdit *lineEdit_point_D;
    QLabel *label_6;
    QLineEdit *lineEdit_matricule_D;
    QLineEdit *lineEdit_recurrence;

    void setupUi(QDialog *Dialog)
    {
        if (Dialog->objectName().isEmpty())
            Dialog->setObjectName("Dialog");
        Dialog->resize(802, 544);
        groupBox = new QGroupBox(Dialog);
        groupBox->setObjectName("groupBox");
        groupBox->setEnabled(true);
        groupBox->setGeometry(QRect(0, 0, 801, 541));
        label_numero_D = new QLabel(groupBox);
        label_numero_D->setObjectName("label_numero_D");
        label_numero_D->setGeometry(QRect(40, 190, 121, 16));
        label_date_D = new QLabel(groupBox);
        label_date_D->setObjectName("label_date_D");
        label_date_D->setGeometry(QRect(60, 250, 41, 16));
        label_3 = new QLabel(groupBox);
        label_3->setObjectName("label_3");
        label_3->setGeometry(QRect(60, 310, 47, 14));
        label_point_D = new QLabel(groupBox);
        label_point_D->setObjectName("label_point_D");
        label_point_D->setGeometry(QRect(70, 460, 91, 16));
        label_observation_D = new QLabel(groupBox);
        label_observation_D->setObjectName("label_observation_D");
        label_observation_D->setGeometry(QRect(440, 350, 81, 16));
        label_circonstance_D = new QLabel(groupBox);
        label_circonstance_D->setObjectName("label_circonstance_D");
        label_circonstance_D->setGeometry(QRect(60, 400, 81, 16));
        label_avis_D = new QLabel(groupBox);
        label_avis_D->setObjectName("label_avis_D");
        label_avis_D->setGeometry(QRect(440, 460, 71, 16));
        label_lieu_D = new QLabel(groupBox);
        label_lieu_D->setObjectName("label_lieu_D");
        label_lieu_D->setGeometry(QRect(70, 360, 47, 14));
        lineEdit_numero_D = new QLineEdit(groupBox);
        lineEdit_numero_D->setObjectName("lineEdit_numero_D");
        lineEdit_numero_D->setGeometry(QRect(190, 190, 113, 20));
        lineEdit_numero_D->setReadOnly(true);
        dateEdit_D = new QDateEdit(groupBox);
        dateEdit_D->setObjectName("dateEdit_D");
        dateEdit_D->setGeometry(QRect(200, 250, 110, 22));
        dateEdit_D->setReadOnly(true);
        timeEdit_heure_D = new QTimeEdit(groupBox);
        timeEdit_heure_D->setObjectName("timeEdit_heure_D");
        timeEdit_heure_D->setGeometry(QRect(190, 310, 118, 22));
        timeEdit_heure_D->setReadOnly(true);
        TextEdit_observation_D = new QPlainTextEdit(groupBox);
        TextEdit_observation_D->setObjectName("TextEdit_observation_D");
        TextEdit_observation_D->setGeometry(QRect(550, 300, 191, 101));
        TextEdit_observation_D->setReadOnly(true);
        textEdit_avis_D = new QTextEdit(groupBox);
        textEdit_avis_D->setObjectName("textEdit_avis_D");
        textEdit_avis_D->setGeometry(QRect(550, 410, 191, 101));
        textEdit_avis_D->setReadOnly(true);
        lineEdit_lieu_D = new QLineEdit(groupBox);
        lineEdit_lieu_D->setObjectName("lineEdit_lieu_D");
        lineEdit_lieu_D->setGeometry(QRect(200, 360, 113, 20));
        lineEdit_lieu_D->setReadOnly(true);
        label = new QLabel(groupBox);
        label->setObjectName("label");
        label->setGeometry(QRect(0, 0, 801, 161));
        label->setStyleSheet(QString::fromUtf8("background-color: rgb(94, 114, 228);"));
        label_2 = new QLabel(groupBox);
        label_2->setObjectName("label_2");
        label_2->setGeometry(QRect(0, 160, 801, 381));
        label_2->setStyleSheet(QString::fromUtf8("background-color: rgb(245, 246, 247);"));
        label_4 = new QLabel(groupBox);
        label_4->setObjectName("label_4");
        label_4->setGeometry(QRect(16, 173, 771, 361));
        label_4->setStyleSheet(QString::fromUtf8("background-color: rgb(255, 255, 255);\n"
"border-radius:25px;"));
        label_5 = new QLabel(groupBox);
        label_5->setObjectName("label_5");
        label_5->setGeometry(QRect(170, 60, 461, 51));
        lineEdit_circonstance_D = new QLineEdit(groupBox);
        lineEdit_circonstance_D->setObjectName("lineEdit_circonstance_D");
        lineEdit_circonstance_D->setGeometry(QRect(180, 400, 211, 20));
        lineEdit_point_D = new QLineEdit(groupBox);
        lineEdit_point_D->setObjectName("lineEdit_point_D");
        lineEdit_point_D->setGeometry(QRect(190, 460, 201, 20));
        label_6 = new QLabel(groupBox);
        label_6->setObjectName("label_6");
        label_6->setGeometry(QRect(440, 230, 61, 16));
        lineEdit_matricule_D = new QLineEdit(groupBox);
        lineEdit_matricule_D->setObjectName("lineEdit_matricule_D");
        lineEdit_matricule_D->setGeometry(QRect(570, 220, 151, 20));
        lineEdit_matricule_D->setReadOnly(true);
        lineEdit_recurrence = new QLineEdit(groupBox);
        lineEdit_recurrence->setObjectName("lineEdit_recurrence");
        lineEdit_recurrence->setGeometry(QRect(590, 180, 113, 20));
        lineEdit_recurrence->setReadOnly(true);
        label_2->raise();
        label->raise();
        label_4->raise();
        label_numero_D->raise();
        lineEdit_numero_D->raise();
        label_date_D->raise();
        dateEdit_D->raise();
        timeEdit_heure_D->raise();
        label_3->raise();
        TextEdit_observation_D->raise();
        label_observation_D->raise();
        textEdit_avis_D->raise();
        label_lieu_D->raise();
        label_circonstance_D->raise();
        label_avis_D->raise();
        label_point_D->raise();
        lineEdit_lieu_D->raise();
        label_5->raise();
        lineEdit_circonstance_D->raise();
        lineEdit_point_D->raise();
        label_6->raise();
        lineEdit_matricule_D->raise();
        lineEdit_recurrence->raise();

        retranslateUi(Dialog);

        QMetaObject::connectSlotsByName(Dialog);
    } // setupUi

    void retranslateUi(QDialog *Dialog)
    {
        Dialog->setWindowTitle(QCoreApplication::translate("Dialog", "Dialog", nullptr));
        groupBox->setTitle(QString());
        label_numero_D->setText(QCoreApplication::translate("Dialog", "NUMERO DU CONSTAT", nullptr));
        label_date_D->setText(QCoreApplication::translate("Dialog", "DATE", nullptr));
        label_3->setText(QCoreApplication::translate("Dialog", "HEURE", nullptr));
        label_point_D->setText(QCoreApplication::translate("Dialog", "POINT DE CHOC", nullptr));
        label_observation_D->setText(QCoreApplication::translate("Dialog", "OBSERVATION", nullptr));
        label_circonstance_D->setText(QCoreApplication::translate("Dialog", "CIRCONSTANCE", nullptr));
        label_avis_D->setText(QCoreApplication::translate("Dialog", "AVIS EXPERT", nullptr));
        label_lieu_D->setText(QCoreApplication::translate("Dialog", "LIEU", nullptr));
        label->setText(QString());
        label_2->setText(QString());
        label_4->setText(QString());
        label_5->setText(QCoreApplication::translate("Dialog", "<html><head/><body><p><span style=\" font-size:16pt; font-weight:600; color:#ffffff;\">Vous venez d'ajouter un nouveau constat</span></p></body></html>", nullptr));
        label_6->setText(QCoreApplication::translate("Dialog", "MATRICULE", nullptr));
    } // retranslateUi

};

namespace Ui {
    class Dialog: public Ui_Dialog {};
} // namespace Ui

QT_END_NAMESPACE

#endif // UI_DIALOG_H
