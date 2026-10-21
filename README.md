# Sébé TV

Accueil Android TV gratuit, local et sans publicité.

## Version 0.4
- Accueil sans titre de rangée ni phrases d’introduction.
- Bouton **Gérer mes applications** : cochez les applications à placer dans la première rangée, puis choisissez **Enregistrer**.
- Les autres applications TV restent accessibles dans une grille sous cette rangée.
- Onglet **Applications** : grille complète des applications TV détectées et accès au gestionnaire.
- Ambiance animée à la sélection : rouge pour Netflix et YouTube, bleu pour Disney+, cyan pour Prime Video, orange pour Crunchyroll, vert pour Spotify, etc.
- Les sélections sont conservées sur la télévision. Les anciens favoris servent de sélection initiale ; sans favoris, les applications de divertissement sont présélectionnées.

[Télécharger Sébé TV 0.4](https://github.com/Sebcer19/SebeTV/raw/refs/heads/main/SebeTV-0.4.apk)

## Installation
Depuis la version **0.3**, installer la 0.4 directement par-dessus : même clé de signature, réglages conservés.

Depuis la version **0.1 ou 0.2**, désinstaller l’ancienne version avant l’installation. Leur clé de développement n’a pas été conservée. Les réglages de cette ancienne installation seront perdus : notez votre sélection avant de désinstaller. La clé dédiée utilisée depuis 0.3 est sauvegardée séparément de ce dépôt.

Ouvrir le lien APK dans Downloader sur la télévision, télécharger puis installer. Autoriser cette source si Android le demande. Lancer Sébé TV depuis les applications. Aucun remplacement automatique de l’accueil Google TV.

OK ouvre une application ; un appui long sur OK permet de l’ajouter à la première rangée ou de l’en retirer. Le bouton Retour revient de Applications à Accueil.

## Compilation
JDK 17, SDK Android 35, Gradle 8.9.

Pour un essai avec votre propre clé de développement : `./gradlew assembleDebug lintDebug`.

Pour une version signée avec la clé dédiée : définir `SEBETV_KEYSTORE` (chemin absolu), `SEBETV_STORE_PASSWORD` et `SEBETV_KEY_PASSWORD`, puis lancer `./gradlew assembleRelease lintRelease`. Alias de clé : `sebetv`. Ne jamais publier le fichier de clé ni ses mots de passe.

La compilation et la vérification de signature ne remplacent pas un essai sur télévision. La nouvelle interface 0.4 reste à vérifier sur appareil.
