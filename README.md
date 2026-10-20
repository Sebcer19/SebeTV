# Sébé TV

Accueil Android TV gratuit, local et sans publicité.

[Télécharger la version 0.2](https://github.com/Sebcer19/SebeTV/raw/refs/heads/main/SebeTV-0.2.apk)

## Nouveautés 0.2
- Navigation compacte en haut de l’écran et aide discrète en bas.
- Favoris en premier et rangée films, séries et musique. Le classement repose sur une liste de noms connus ; toutes les applications restent disponibles dans Applications.
- Bannières fournies par les applications, avec icônes en remplacement si nécessaire.
- Fond sombre avec reliefs discrets, trois ambiances, focus contrasté.
- Défilement horizontal des rangées à la télécommande.

## Installation
Ouvrir le lien APK dans Downloader sur la télévision, télécharger puis installer. Autoriser cette source si Android le demande. La v0.2 utilise le même identifiant et la même signature que la v0.1 : installer par-dessus pour conserver favoris et thème.

Appui simple sur OK pour ouvrir ; appui long pour ajouter/retirer un favori. Les utilitaires et les applications non classées restent dans Applications.

## Validation
Compilation et Android Lint réussis : 0 erreur, 4 avertissements (attributs Android, orientation TV, bannière et sauvegarde). Signature APK vérifiée et identique à la v0.1. La v0.1 a été essayée sur une Sony ; la nouvelle interface v0.2 reste à vérifier sur appareil. Aucun remplacement automatique de l’accueil Google TV.

## Code source
JDK 17, SDK Android 35, Gradle 8.9. Définir ANDROID_HOME puis lancer `./gradlew assembleDebug lintDebug` (Windows : `gradlew.bat assembleDebug lintDebug`).
APK généré : `app/build/outputs/apk/debug/app-debug.apk`.
La clé privée de signature n’est pas publiée. Une compilation sur un autre ordinateur produira normalement une autre signature ; elle ne pourra pas remplacer l’APK distribué sans désinstaller celui-ci.
