# Sébé TV

Accueil Android TV gratuit, local et sans publicité.

## Version 0.9
- Les applications de l’accueil sont alignées en bas de l’écran pour dégager le centre des animations.
- Un dégradé assombrit le bas de l’écran pour garder les tuiles lisibles. Le catalogue Applications conserve sa grille en haut.

- Les six vidéos fournies sont embarquées dans l’application : Netflix, Disney+, CANAL+, Crunchyroll, YouTube et Apple TV.
- À la sélection d’une tuile, sa vidéo démarre automatiquement après 450 ms et tourne en boucle sans son. Aucune sélection de fichier ni connexion Internet nécessaire pour ces vidéos intégrées.
- Les fichiers ont été adaptés en H.264, 960 × 540, 24 images/s, sans piste audio. Le cadrage complet est conservé, avec bandes noires si nécessaire.
- Une vidéo personnalisée déjà choisie reste prioritaire. Appui long → **Utiliser le fond intégré** pour revenir à la vidéo fournie.
- Les autres applications conservent les fonds animés de la version 0.6.
- L’accueil conserve uniquement les applications choisies. Toutes les autres restent dans Applications.
- Ambiance → **Désactiver les fonds animés** désactive aussi les vidéos. Le lecteur est arrêté dès que Sébé TV passe en arrière-plan.

[Télécharger Sébé TV 0.9](https://github.com/Sebcer19/SebeTV/raw/refs/heads/main/SebeTV-0.9.apk)

### Vidéos fournies pour ce projet
| Application | Fichier source fourni |
| --- | --- |
| Netflix | Netflix - Netflix New Logo Animation 2019.mp4 |
| Disney+ | Meme Lord - Disney+ Logo (March 27, 2024-present).mp4 |
| CANAL+ | nomeslogos - Canal Plus.mp4 |
| Crunchyroll | Match & Pencil Studios - Crunchyroll Logo.mp4 |
| YouTube | Intbar - YouTube on TV Startup Sound (Full).mp4 |
| Apple TV | Apple TV - This is just the beginning..mp4 |

## Installation
Depuis les versions **0.3 à 0.8**, installer la 0.9 directement par-dessus : même clé de signature, réglages conservés.

Depuis la version **0.1 ou 0.2**, désinstaller l’ancienne version avant l’installation. Leur clé de développement n’a pas été conservée. Les réglages de cette ancienne installation seront perdus : notez votre sélection avant de désinstaller. La clé dédiée utilisée depuis 0.3 est sauvegardée séparément de ce dépôt.

Ouvrir le lien APK dans Downloader sur la télévision, télécharger puis installer. Autoriser cette source si Android le demande. Lancer Sébé TV depuis les applications. Aucun remplacement automatique de l’accueil Google TV.

OK ouvre une application ; un appui long sur OK ouvre ses options. Le bouton Retour revient de Applications à Accueil.

## Compilation
JDK 17, SDK Android 35, Gradle 8.9.

Pour un essai avec votre propre clé de développement : `./gradlew assembleDebug lintDebug`.

Pour une version signée avec la clé dédiée : définir `SEBETV_KEYSTORE` (chemin absolu), `SEBETV_STORE_PASSWORD` et `SEBETV_KEY_PASSWORD`, puis lancer `./gradlew assembleRelease lintRelease`. Alias de clé : `sebetv`. Ne jamais publier le fichier de clé ni ses mots de passe.

La compilation et la vérification de signature ne remplacent pas un essai sur télévision. La lecture des vidéos intégrées reste à vérifier sur télévision.
