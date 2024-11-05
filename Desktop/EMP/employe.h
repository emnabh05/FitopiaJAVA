#ifndef PRODUITS_H
#define PRODUITS_H

#include <QString>
#include <QSqlQueryModel>
#include <QSqlQuery>
#include <QList>
#include <QDate>

class employe
{
private:
    int id_emp;
    QString nom;
    QString prenom;
    QString mail;
    QString mot_de_passe;
    int num_tel;
    QString adresse;

public:
    employe();
    employe(const QString& nom, const QString& prenom,  const QString& mail,  const QString& mot_de_passe, int num_tel,  const QString& adresse);

    // Getters
    QString getNom() const { return nom; }
    QString getMail() const { return mail; }
    QString getMotDePasse() const { return mot_de_passe; }
    int getNumTel() const { return num_tel; }
    QString getAdresse() const { return adresse; }
    QString getPrenom() const { return prenom; }
    // Setters
    void setNom(const QString& nm) { nom = nm; }
    void setMail(const QString& email) { mail = email; }
    void setMotDePasse(const QString& mdp) { mot_de_passe = mdp; }
    void setPrenom(const QString& pr) { prenom = pr; }
    void setAdresse(const QString& ad) { adresse = ad; }
    void setNumTel(int nmt) { num_tel = nmt; }
    // CRUD
    bool ajouterEmploye(employe p);
    QSqlQueryModel* afficherEmploye();
    bool supprimerEmploye(int id_emp);
    bool modifierEmploye(const QString& nom, const QString& prenom, const QString& mail, const QString& mot_de_passe, int num_tel,const QString& adresse);
    QList<employe> retrouverListEmployes();
};

#endif // PRODUITS_H
