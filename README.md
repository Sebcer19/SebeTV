# Sébé TV

Accueil Android TV gratuit, local et sans publicité.

## Version 0.5
- Appui long sur une application : déplacer, masquer / réafficher sur l’accueil, mettre en avant / retirer de la sélection.
- Déplacement : flèches gauche / droite, puis OK ou Retour pour terminer. L’ordre est sauvegardé au fil des déplacements. Depuis l’accueil, le déplacement reste dans le groupe sélectionné ou dans les autres applications ; depuis Applications, il porte sur toute la liste.
- Les applications masquées restent accessibles et identifiables dans l’onglet Applications.
- Réglages : trois tailles de tuiles, une à trois rangées mises en avant, quatre intensités de halo (dont désactivé). Les applications supplémentaires défilent horizontalement ; la grille du dessous reste accessible verticalement.
- Tuiles arrondies, dégradé discret, logos centrés, agrandissement et ombre à la sélection.
- Halo élargi avec transition de couleur douce par application.
- Raccourcis : entrées TV / HDMI, réglages TV, ambiance plein écran. Les entrées dépendent de leur disponibilité sur la télévision ; sinon un message indique d’utiliser la touche Source / Input.
- Ambiance plein écran sans texte permanent. OK ou Retour pour sortir ; aucun blocage de la veille du téléviseur.

[Télécharger Sébé TV 0.5](https://github.com/Sebcer19/SebeTV/raw/refs/heads/main/SebeTV-0.5.apk)

## Installation
Depuis les versions **0.3 ou 0.4**, installer la 0.5 directement par-dessus : même clé de signature, réglages conservés.

Depuis la version **0.1 ou 0.2**, désinstaller l’ancienne version avant l’installation. Leur clé de développement n’a pas été conservée. Les réglages de cette ancienne installation seront perdus : notez votre sélection avant de désinstaller. La clé dédiée utilisée depuis 0.3 est sauvegardée séparément de ce dépôt.

Ouvrir le lien APK dans Downloader sur la télévision, télécharger puis installer. Autoriser cette source si Android le demande. Lancer Sébé TV depuis les applications. Aucun remplacement automatique de l’accueil Google TV.

OK ouvre une application ; un appui long sur OK ouvre ses options. Le bouton Retour revient de Applications à Accueil.

## Compilation
JDK 17, SDK Android 35, Gradle 8.9.

Pour un essai avec votre propre clé de développement : `./gradlew assembleDebug lintDebug`.

Pour une version signée avec la clé dédiée : définir `SEBETV_KEYSTORE` (chemin absolu), `SEBETV_STORE_PASSWORD` et `SEBETV_KEY_PASSWORD`, puis lancer `./gradlew assembleRelease lintRelease`. Alias de clé : `sebetv`. Ne jamais publier le fichier de clé ni ses mots de passe.

La compilation et la vérification de signature ne remplacent pas un essai sur télévision. La nouvelle interface 0.5 reste à vérifier sur appareil.
