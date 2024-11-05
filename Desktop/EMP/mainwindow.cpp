#include "mainwindow.h"
#include "ui_mainwindow.h"
#include <QMessageBox>
#include "employe.h"
#include <QSqlQuery>

MainWindow::MainWindow(QWidget *parent)
    : QMainWindow(parent)
    , ui(new Ui::MainWindow)
{
    ui->setupUi(this);

    employe p;
    ui->emptable->setModel(p.afficherEmploye());
}

MainWindow::~MainWindow()
{
    delete ui;
}

void MainWindow::on_bouttonajouter_clicked()
{
    employe p;

    QString nom = ui->ajouternom->text();
    QString prenom = ui->ajouterprenom->text();
    QString mail = ui->ajoutermail->text();
    QString mot_de_passe = ui->ajoutermotdepasse->text();
    int num_tel = ui->ajouternumtel->text().toInt();
    QString adresse = ui->ajouteradresse->text();
    p.setNom(nom);
    p.setPrenom(prenom);
    p.setMail(mail);
    p.setMotDePasse(mot_de_passe);
    p.setNumTel(num_tel);
    p.setAdresse(adresse);

    if (p.ajouterEmploye(p)) {
        QMessageBox::information(this, "Succès", "Employe ajouté avec succès!");
        ui->ajouternom->clear();
        ui->ajouterprenom->clear();
        ui->ajoutermail->clear();
        ui->ajoutermotdepasse->clear();
        ui->ajouternumtel->clear();
        ui->ajouteradresse->clear();

        ui->emptable->setModel(p.afficherEmploye());

        qDebug() << "Employe ajouté avec succès!";
    } else {
        QMessageBox::critical(this, "Erreur", "Échec de l'ajout de l'employe.");
        qDebug() << "Échec de l'ajout dans la base";
    }
}

void MainWindow::on_pushButton_clicked()
{
    employe p;
    QString nom = ui->ajouternom_2->text();
    QString prenom = ui->ajouterprenom_2->text();
    QString mail = ui->ajoutermail_2->text();
    QString mot_de_passe = ui->ajoutermotdepasse_2->text();
    int num_tel = ui->ajouternumerodetelephone_2->text().toInt();
    QString adresse = ui->ajouteradresse_2->text();

    if (p.modifierEmploye(nom, prenom, mail, mot_de_passe, num_tel, adresse)) {
        qDebug() << "Employe modifié avec succès!";
        QMessageBox::information(this, "Succès", "Employe modifié avec succès!");

        ui->ajouternom_2->clear();
        ui->ajouterprenom_2->clear();
        ui->ajoutermail_2->clear();
        ui->ajouternumerodetelephone_2->clear();
        ui->ajoutermotdepasse_2->clear();
        ui->ajouteradresse_2->clear();

        ui->emptable->setModel(p.afficherEmploye());
    } else {
        QMessageBox::critical(this, "Erreur", "Échec de la modification.");
    }
}

void MainWindow::on_button_supprimer_clicked()
{
    employe p;
    int id_emp = ui->idsupprimer->text().toInt();

    QSqlQuery checkQuery;
    checkQuery.prepare("SELECT COUNT(*) FROM employe WHERE id_emp = :id_emp");
    checkQuery.bindValue(":id_emp", id_emp);

    if (!checkQuery.exec() || !checkQuery.next() || checkQuery.value(0).toInt() == 0) {
        QMessageBox::warning(this, "Employe introuvable", "Aucun employe trouvé avec cet ID.");
        return;
    }

    if (p.supprimerEmploye(id_emp)) {
        qDebug() << "Employe supprimé avec succès!";
        QMessageBox::information(this, "Succès", "Employe supprimé avec succès!");

        ui->idsupprimer->clear();
        ui->emptable->setModel(p.afficherEmploye());
    } else {
        QMessageBox::critical(this, "Erreur", "Échec de la suppression");
    }
}

void MainWindow::on_pushButton_2_clicked()
{
    int id_emp = ui->idemployemodif->text().toInt();

    if (id_emp <= 0) {
        QMessageBox::warning(this, "Erreur", "Veuillez entrer un ID produit valide.");
        return;
    }
    QSqlQuery query;
    query.prepare("SELECT nom, prenom, mail, mot_de_passe, num_tel, adresse FROM employe WHERE id_emp = :id_emp");
    query.bindValue(":id_emp", id_emp);

    qDebug() << "id employe:" << id_emp;

    if (!query.exec()) {
        QMessageBox::critical(this, "Erreur", "Erreur lors de l'exécution de la requête.");
        return;
    }

    if (query.next()) {
        QString nom = query.value("nom").toString();
        QString prenom = query.value("prenom").toString();
        QString mail = query.value("mail").toString();
        QString mot_de_passe = query.value("mot_de_passe").toString();
        int num_tel = query.value("num_tel").toInt();
        QString adresse = query.value("adresse").toString();

        qDebug() << "Nom:" << nom << "Prenom:" << prenom << "Mail:" << mail;

        ui->ajouternom_2->setText(nom);
        ui->ajouterprenom_2->setText(prenom);
        ui->ajoutermail_2->setText(mail);
        ui->ajoutermotdepasse_2->setText(mot_de_passe);
        ui->ajouternumerodetelephone_2->setText(QString::number(num_tel));
        ui->ajouteradresse_2->setText(adresse);
    } else {
        QMessageBox::warning(this, "Employe introuvable", "Aucun Employe trouvé avec cet ID.");
    }

}
