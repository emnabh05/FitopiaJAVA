#include "employe.h"
#include <QSqlQuery>
#include <QSqlQueryModel>
#include <QDebug>
#include <QtSql>
#include <QMessageBox>


employe::employe(const QString& nom, const QString& prenom,  const QString& mail,  const QString& mot_de_passe,  int num_tel,  const QString& adresse)
    : nom(nom), prenom(prenom), mail(mail), mot_de_passe(mot_de_passe), num_tel(num_tel), adresse(adresse)
{
}
employe::employe() {}

QSqlQueryModel* employe::afficherEmploye() {
    QSqlQueryModel *model = new QSqlQueryModel();
    model->setQuery("SELECT * FROM employe");
    if (model->lastError().isValid()) {
        qDebug() << "Erreur requête:" << model->lastError().text();
    } else {
        qDebug() << model->rowCount() << " enregistrements trouvés";
    }

    return model;
}

// Supprimer produit
bool employe::supprimerEmploye(int idemp) {
    QSqlQuery query;
    query.prepare("DELETE FROM employe WHERE id_emp = :id_emp");
    query.bindValue(":id_emp", idemp);

    if (!query.exec()) {
        qDebug() << "Erreur lors de la suppression:" << query.lastError().text();
        return false;
    }
    return true;
}

// Modifier employe
bool employe::modifierEmploye(const QString& nom, const QString& prenom, const QString& mail, const QString& mot_de_passe, int num_tel,const QString& adresse)
{
    QSqlQuery query;
    query.prepare("UPDATE Produits SET nom = :nom, prenom = :prenom, mail = :mail, "
                  "mot_de_passe = :mot_de_passe, num_tel = :num_tel, adresse = :adresse WHERE id_emp = :id_emp");

    query.bindValue(":id_emp", id_emp);
    query.bindValue(":nom", nom);
    query.bindValue(":prenom", prenom);
    query.bindValue(":mail", mail);
    query.bindValue(":mot_de_passe", mot_de_passe);
    query.bindValue(":num_tel", num_tel);
    query.bindValue(":adresse",adresse);

    if (!query.exec()) {
        qDebug() << "Erreur lors de la mise à jour:" << query.lastError().text();
        return false;
    }
    return true;
}

// Retrouver liste de produits
/*QList<employe> employe::retrouverEmploye() {
    QList<employe> listemployes;
    QSqlQuery query;

    if (query.exec("SELECT nomproduit, dateExpiration, quantiteDisponible, humidite, temperature, dateDebut FROM Produits")) {
        while (query.next()) {
            QString nomproduit = query.value(0).toString();
            QDate dateExpiration = QDate::fromString(query.value(1).toString(), "yyyy-MM-dd");
            int quantiteDisponible = query.value(2).toInt();
            int humidite = query.value(3).toInt();
            int temperature = query.value(4).toInt();
            QDate dateDebut = QDate::fromString(query.value(5).toString(), "yyyy-MM-dd"); // Convert QString to QDate

            listeProduits.append(produits(nomproduit, dateExpiration, quantiteDisponible, humidite, temperature, dateDebut));
        }
    } else {
        qDebug() << "Impossible de retrouver la liste des produits: " << query.lastError().text();
    }

    return listeProduits;
}*/

// Ajouter employe
bool employe::ajouterEmploye(employe p) {
    QSqlQuery query;
    query.prepare("INSERT INTO employe (nom, prenom, mail, mot_de_passe,num_tel, adresse) "
                  "VALUES (:nom, :prenom, :mail, :mot_de_passe, :num_tel, :adresse)");

    query.bindValue(":nom", p.nom);
    query.bindValue(":prenom", p.prenom);
    query.bindValue(":mail", p.mail);
    query.bindValue(":mot_de_passe", p.mot_de_passe);
    query.bindValue(":num_tel", p.num_tel);
    query.bindValue(":adresse", p.adresse);

    if (!query.exec()) {
        qDebug() << "Erreur lors de l'insertion:" << query.lastError().text();
        return false;
    }
    return true;
}
