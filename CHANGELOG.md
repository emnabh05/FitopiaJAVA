# Fitopia — Changelog (branche modifcrud)

## Fonctionnalités ajoutées

### JavaFX (FitopiaHomeController)
- **Vidéos exercices** : ressources MP4 + images GIF/WebP copiées dans `src/main/resources/media/fitness/`
- **Audio workout** : fichiers cardio/hiit/strength dans `src/main/resources/media/fitness/audio/`
- **Coach IA TTS** : voix synthétique via Windows SAPI (CoachTtsService) — conseils vocaux pendant l'entraînement
- **Section Coach** : 6 cartes coachs avec photos, formulaire de réservation, créneaux disponibles calculés automatiquement, envoi email SMTP Gmail
- **Section Explore** : feed vidéo (5 locales + 7 YouTube), autoplay, filtre par thème, playlist scrollable
- **Section Plans** : tracker de performances — saisie séances (exercices + séries + poids), graphique barres horizontal par semaine, historique
- **Programmes** : boutons améliorés (texte bas-gauche, hover animé, gradients riches), 3 nouveaux groupes musculaires

### Symfony (Web)
- **Module Coach** : page `/coaches`, formulaire réservation `/coaches/{id}/book`, email HTML au coach
- **Performance tracking** : 4 routes API + entités Doctrine + migration BDD
- **Explore web** : feed TikTok/Reels avec vidéos locales + YouTube, like, share, filtre IA
- **Fix Twig** : correction apostrophes imbriquées dans `asset()`, ajout `exerciseImageMapJs`
- **SMTP** : configuration Gmail dans `.env` et `mailer.yaml`
