<?php

declare(strict_types=1);

namespace DoctrineMigrations;

use Doctrine\DBAL\Schema\Schema;
use Doctrine\Migrations\AbstractMigration;

/**
 * Suivi des performances utilisateur dans le module Plans.
 *
 * Tables créées :
 *  - exercise_catalog    : bibliothèque d'exercices (partagée entre tous les users)
 *  - training_session    : séance d'entraînement (date, user, notes)
 *  - performance_log     : entrée de performance par exercice dans une séance
 *  - performance_set     : détail de chaque série (poids × reps) dans une entrée
 */
final class Version20260425000001 extends AbstractMigration
{
    public function getDescription(): string
    {
        return 'Create performance tracking tables: exercise_catalog, training_session, performance_log, performance_set';
    }

    public function up(Schema $schema): void
    {
        // ── 1. Bibliothèque d'exercices ──────────────────────────────────────
        // Exercices prédéfinis (ou créés par l'user).
        // muscle_group : Poitrine, Dos, Jambes, Épaules, Biceps, Triceps, Abdos, Cardio
        // equipment    : Barre, Haltères, Machine, Poids du corps, Élastique, Câble
        $this->addSql('
            CREATE TABLE exercise_catalog (
                id            INT AUTO_INCREMENT NOT NULL,
                user_id       INT          DEFAULT NULL,          -- NULL = exercice global/partagé
                name          VARCHAR(150) NOT NULL,
                muscle_group  VARCHAR(80)  NOT NULL,
                equipment     VARCHAR(80)  DEFAULT NULL,
                description   LONGTEXT     DEFAULT NULL,
                is_custom     TINYINT(1)   NOT NULL DEFAULT 0,    -- 1 = créé par l'user
                created_at    DATETIME     NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
                PRIMARY KEY(id),
                INDEX idx_exercise_catalog_user   (user_id),
                INDEX idx_exercise_catalog_muscle (muscle_group),
                CONSTRAINT fk_exercise_catalog_user
                    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE SET NULL
            ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB
        ');

        // ── 2. Séance d'entraînement ─────────────────────────────────────────
        // Une séance = un jour d'entraînement pour un user.
        // Un user peut avoir plusieurs séances par semaine.
        $this->addSql('
            CREATE TABLE training_session (
                id            INT AUTO_INCREMENT NOT NULL,
                user_id       INT          NOT NULL,
                session_date  DATE         NOT NULL,              -- date de la séance
                week_number   TINYINT      NOT NULL,              -- semaine ISO (1-53)
                year          SMALLINT     NOT NULL,              -- année
                label         VARCHAR(120) DEFAULT NULL,          -- ex: "Séance poitrine lundi"
                notes         LONGTEXT     DEFAULT NULL,
                duration_min  SMALLINT     DEFAULT NULL,          -- durée totale en minutes
                created_at    DATETIME     NOT NULL COMMENT \'(DC2Type:datetime_immutable)\',
                PRIMARY KEY(id),
                INDEX idx_training_session_user        (user_id),
                INDEX idx_training_session_date        (session_date),
                INDEX idx_training_session_week_year   (year, week_number),
                CONSTRAINT fk_training_session_user
                    FOREIGN KEY (user_id) REFERENCES user(id) ON DELETE CASCADE
            ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB
        ');

        // ── 3. Entrée de performance ─────────────────────────────────────────
        // Lie un exercice à une séance. Contient les méta-données globales
        // de l'exercice dans cette séance (ordre, notes).
        $this->addSql('
            CREATE TABLE performance_log (
                id            INT AUTO_INCREMENT NOT NULL,
                session_id    INT          NOT NULL,
                exercise_id   INT          NOT NULL,
                exercise_order TINYINT     NOT NULL DEFAULT 1,    -- ordre dans la séance
                rest_seconds  SMALLINT     DEFAULT NULL,          -- repos entre séries (s)
                notes         VARCHAR(255) DEFAULT NULL,
                PRIMARY KEY(id),
                INDEX idx_perf_log_session  (session_id),
                INDEX idx_perf_log_exercise (exercise_id),
                CONSTRAINT fk_perf_log_session
                    FOREIGN KEY (session_id)  REFERENCES training_session(id) ON DELETE CASCADE,
                CONSTRAINT fk_perf_log_exercise
                    FOREIGN KEY (exercise_id) REFERENCES exercise_catalog(id) ON DELETE CASCADE
            ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB
        ');

        // ── 4. Détail de chaque série ────────────────────────────────────────
        // Une entrée performance_log peut avoir N séries.
        // Ex : Série 1 → 10 kg × 12 reps, Série 2 → 12.5 kg × 10 reps
        $this->addSql('
            CREATE TABLE performance_set (
                id            INT AUTO_INCREMENT NOT NULL,
                log_id        INT            NOT NULL,
                set_number    TINYINT        NOT NULL,            -- numéro de la série (1, 2, 3...)
                weight_kg     DECIMAL(6,2)   DEFAULT NULL,        -- poids en kg (NULL = poids du corps)
                repetitions   SMALLINT       DEFAULT NULL,        -- nombre de répétitions
                duration_sec  SMALLINT       DEFAULT NULL,        -- durée (pour cardio/planche)
                rpe           TINYINT        DEFAULT NULL,        -- effort perçu 1-10 (optionnel)
                is_warmup     TINYINT(1)     NOT NULL DEFAULT 0,  -- 1 = série d'échauffement
                PRIMARY KEY(id),
                INDEX idx_perf_set_log (log_id),
                CONSTRAINT fk_perf_set_log
                    FOREIGN KEY (log_id) REFERENCES performance_log(id) ON DELETE CASCADE
            ) DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci ENGINE = InnoDB
        ');

        // ── 5. Données initiales : exercices globaux ─────────────────────────
        $this->addSql("
            INSERT INTO exercise_catalog (name, muscle_group, equipment, is_custom, created_at) VALUES
            ('Développé couché avec barre',    'Poitrine',  'Barre',         0, NOW()),
            ('Développé incliné haltères',     'Poitrine',  'Haltères',      0, NOW()),
            ('Dips pectoraux',                 'Poitrine',  'Poids du corps',0, NOW()),
            ('Tractions',                      'Dos',       'Poids du corps',0, NOW()),
            ('Rowing barre',                   'Dos',       'Barre',         0, NOW()),
            ('Tirage poulie haute',            'Dos',       'Câble',         0, NOW()),
            ('Squat barre',                    'Jambes',    'Barre',         0, NOW()),
            ('Presse à cuisses',               'Jambes',    'Machine',       0, NOW()),
            ('Fentes haltères',                'Jambes',    'Haltères',      0, NOW()),
            ('Développé militaire',            'Épaules',   'Barre',         0, NOW()),
            ('Élévations latérales',           'Épaules',   'Haltères',      0, NOW()),
            ('Curl biceps haltères',           'Biceps',    'Haltères',      0, NOW()),
            ('Curl barre EZ',                  'Biceps',    'Barre',         0, NOW()),
            ('Extension triceps poulie',       'Triceps',   'Câble',         0, NOW()),
            ('Dips triceps banc',              'Triceps',   'Poids du corps',0, NOW()),
            ('Crunch abdominaux',              'Abdos',     'Poids du corps',0, NOW()),
            ('Planche',                        'Abdos',     'Poids du corps',0, NOW()),
            ('Course à pied',                  'Cardio',    NULL,            0, NOW()),
            ('Corde à sauter',                 'Cardio',    NULL,            0, NOW())
        ");
    }

    public function down(Schema $schema): void
    {
        $this->addSql('ALTER TABLE performance_set  DROP FOREIGN KEY fk_perf_set_log');
        $this->addSql('ALTER TABLE performance_log  DROP FOREIGN KEY fk_perf_log_session');
        $this->addSql('ALTER TABLE performance_log  DROP FOREIGN KEY fk_perf_log_exercise');
        $this->addSql('ALTER TABLE training_session DROP FOREIGN KEY fk_training_session_user');
        $this->addSql('ALTER TABLE exercise_catalog DROP FOREIGN KEY fk_exercise_catalog_user');
        $this->addSql('DROP TABLE performance_set');
        $this->addSql('DROP TABLE performance_log');
        $this->addSql('DROP TABLE training_session');
        $this->addSql('DROP TABLE exercise_catalog');
    }
}
