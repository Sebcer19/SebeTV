# Sébé TV

Accueil Android TV gratuit, local et sans publicité.

## Version 0.3
- Suppression des phrases d’introduction et des descriptions secondaires.
- Suppression du texte d’aide et du numéro de version en bas de l’écran.
- Onglets permanents **Accueil** et **Applications**, avec indication de l’onglet sélectionné.
- Menu Applications : grille de toutes les applications TV détectées.
- Favoris, rangée divertissement et trois ambiances conservés.

[Télécharger Sébé TV 0.3](https://github.com/Sebcer19/SebeTV/raw/refs/heads/main/SebeTV-0.3.apk)

## Installation depuis 0.1 / 0.2
La clé de développement des premières versions n’a pas été conservée. La version 0.3 utilise une nouvelle clé dédiée, sauvegardée séparément de ce dépôt.

Il faut désinstaller la version 0.1/0.2 avant d’installer 0.3. Les favoris et le thème de l’ancienne installation seront perdus : notez votre sélection avant de désinstaller. Les prochaines versions doivent réutiliser la clé 0.3 afin de permettre des mises à jour normales.

Ouvrir le lien APK dans Downloader sur la télévision, télécharger puis installer. Autoriser cette source si Android le demande. Lancer Sébé TV depuis les applications. Aucun remplacement automatique de l’accueil Google TV.

OK ouvre une application ; appui long OK ajoute ou retire un favori. Le bouton Retour revient de Applications à Accueil.

## Compilation
JDK 17, SDK Android 35, Gradle 8.9.

Pour un essai avec votre propre clé de développement : `./gradlew assembleDebug lintDebug`.

Pour une version signée avec la clé dédiée : définir `SEBETV_KEYSTORE` (chemin absolu), `SEBETV_STORE_PASSWORD` et `SEBETV_KEY_PASSWORD`, puis lancer `./gradlew assembleRelease lintRelease`. Alias de clé : `sebetv`. Ne jamais publier le fichier de clé ni ses mots de passe.

La compilation et la vérification de signature ne remplacent pas un essai sur télévision. La nouvelle interface 0.3 reste à vérifier sur appareil.
