# Sébé TV

Accueil Android TV gratuit, local et sans publicité.

## Version 0.6
- Accueil allégé : uniquement les applications choisies, sans grille d’applications système en dessous. Gestion des applications et raccourcis dans Réglages ; liste complète dans Applications.
- Fonds animés intégrés : rideaux de lumière rouges pour Netflix, arc étoilé bleu pour Disney+, ondes aux couleurs des autres applications. Il s’agit d’animations originales, pas des génériques officiels.
- Appui long sur une tuile → **Choisir une vidéo de fond** : associer un fichier vidéo accessible à la télévision (stockage local ou clé USB via le sélecteur Android). Les génériques officiels ne sont pas fournis.
- Vidéo en boucle, sans son, lancée après un court délai sur la tuile ; arrêt quand on change de sélection ou quitte Sébé TV.
- Appui long → **Retirer la vidéo de fond** pour revenir à l’animation intégrée.
- Ambiance → **Désactiver les fonds animés** pour revenir à un fond fixe.
- Si un fichier devient inaccessible ou illisible, retour à l’animation intégrée. Le sélecteur de fichiers compatible Android et le décodage du fichier doivent être disponibles sur la TV.

[Télécharger Sébé TV 0.6](https://github.com/Sebcer19/SebeTV/raw/refs/heads/main/SebeTV-0.6.apk)

## Installation
Depuis les versions **0.3, 0.4 ou 0.5**, installer la 0.6 directement par-dessus : même clé de signature, réglages conservés.

Depuis la version **0.1 ou 0.2**, désinstaller l’ancienne version avant l’installation. Leur clé de développement n’a pas été conservée. Les réglages de cette ancienne installation seront perdus : notez votre sélection avant de désinstaller. La clé dédiée utilisée depuis 0.3 est sauvegardée séparément de ce dépôt.

Ouvrir le lien APK dans Downloader sur la télévision, télécharger puis installer. Autoriser cette source si Android le demande. Lancer Sébé TV depuis les applications. Aucun remplacement automatique de l’accueil Google TV.

OK ouvre une application ; un appui long sur OK ouvre ses options. Le bouton Retour revient de Applications à Accueil.

## Compilation
JDK 17, SDK Android 35, Gradle 8.9.

Pour un essai avec votre propre clé de développement : `./gradlew assembleDebug lintDebug`.

Pour une version signée avec la clé dédiée : définir `SEBETV_KEYSTORE` (chemin absolu), `SEBETV_STORE_PASSWORD` et `SEBETV_KEY_PASSWORD`, puis lancer `./gradlew assembleRelease lintRelease`. Alias de clé : `sebetv`. Ne jamais publier le fichier de clé ni ses mots de passe.

La compilation et la vérification de signature ne remplacent pas un essai sur télévision. La nouvelle interface 0.6 reste à vérifier sur appareil.
