# CarHome — écran d'accueil + navigateur pour box CarPlay Android

CarHome remplace l'écran d'accueil d'une box CarPlay qui tourne sous Android.
Il affiche de grosses tuiles façon CarPlay, un navigateur web et tes applis épinglées.

## Ce que fait l'appli

- **Accueil** : musique en cours avec pochette et commandes (Spotify, Deezer, radio…), ta voiture (dessin de Peugeot 308 T9 dans ta couleur, ou ta propre photo), heure, date, météo (Open-Meteo, gratuite et sans clé), vitesse GPS, tuiles Navigateur / Favoris / Applications / Réglages, plus tes applis épinglées.
- **Navigateur** : barre d'adresse (site ou recherche), précédent, suivant, actualiser, ajout aux favoris. La page reste ouverte quand tu reviens à l'accueil.
- **Favoris** : Google, YouTube, Météo-France et Wikipédia au départ. Appui long pour en supprimer un.
- **Applications** : toutes les applis de la box. Appui long pour en épingler une à l'accueil (Waze, Spotify…).
- **Réglages** : nom, couleur et photo de la voiture, seuil de vitesse, version ordinateur des sites, moteur de recherche, bouton pour définir CarHome comme écran d'accueil.
- **Sécurité** : au-dessus du seuil (5 km/h par défaut), le navigateur est remplacé par l'écran « Disponible à l'arrêt » et les vidéos sont coupées. Sans position GPS récente (pas encore de signal, tunnel, GPS coupé), le navigateur reste bloqué par sécurité.

## Version iPhone (web)

Le dossier `web/` contient une version web installable sur iPhone : heure, météo, vitesse GPS,
ta voiture, favoris et raccourcis Waze / Google Maps / Plans / Spotify.
Elle est publiée automatiquement sur GitHub Pages :
`https://brahimbissaragane-byte.github.io/CARHOME/`
Dans Safari : Partager > « Sur l'écran d'accueil ». Elle s'affiche sur l'iPhone, pas dans CarPlay
(Apple ne le permet pas).

## 1. Ouvrir le projet

1. Installe [Android Studio](https://developer.android.com/studio) (gratuit).
2. Décompresse `CarHome.zip`.
3. Dans Android Studio : **File > Open** et choisis le dossier `CarHome`.
4. Attends la fin de la synchronisation Gradle (la première fois, ça télécharge les dépendances : quelques minutes).
   Si Android Studio propose de mettre à jour le plugin Gradle ou Kotlin, tu peux accepter.

## 2. Tester sur l'ordinateur

1. **Tools > Device Manager > Create device** : choisis une tablette (ex. *Pixel Tablet*), Android 13 ou 14.
2. Lance l'émulateur, puis le bouton ▶ **Run 'app'**.
3. Accepte la demande de localisation.
4. Pour simuler la voiture qui roule : dans l'émulateur, **⋯ > Location > Routes**, crée un trajet et clique sur *Play route*. Le navigateur doit se bloquer.

## Compilation automatique (sans ordinateur)

À chaque mise à jour du code, GitHub compile l'appli et la publie ici :
`https://github.com/brahimbissaragane-byte/CARHOME/releases/download/apk-latest/CarHome.apk`

Pour que les mises à jour s'installent par-dessus l'ancienne version, les versions sont signées
avec une clé fixe, fournie par deux secrets GitHub (Settings > Secrets and variables > Actions) :
`CARHOME_KEYSTORE_BASE64` (la clé) et `CARHOME_KEYSTORE_PASSWORD` (son mot de passe).
Sans eux, GitHub produit une version debug dont les mises à jour demandent de désinstaller l'appli.
La clé ne doit jamais être mise dans le dépôt.

## 3. Créer l'APK

**Build > Build App Bundle(s) / APK(s) > Build APK(s)**.
Le fichier est créé dans `app/build/outputs/apk/debug/app-debug.apk`.

## 4. Installer sur la box CarPlay

1. Copie `app-debug.apk` sur une clé USB ou envoie-le dans un cloud (Google Drive…).
2. Sur la box, ouvre le fichier avec le gestionnaire de fichiers.
3. Autorise « Installer des applis inconnues » quand Android le demande.
4. Ouvre CarHome et accepte la localisation.
5. Dans **Réglages > Définir CarHome comme écran d'accueil**, choisis CarHome.
   Si la box n'a pas ce menu, cherche « Appli d'accueil » ou « Launcher » dans les réglages Android.

## Structure du code

```
app/src/main/java/com/brahim/carhome/
├── MainActivity.kt           plein écran, permission GPS
├── CarHomeApp.kt             navigation entre écrans + logique de blocage
├── data/
│   ├── Prefs.kt              favoris, applis épinglées, réglages
│   ├── Apps.kt               liste et lancement des applis
│   ├── SpeedMonitor.kt       vitesse GPS
│   ├── Weather.kt            météo (Open-Meteo)
│   ├── CarPhoto.kt           photo perso de la voiture
│   ├── NowPlaying.kt         musique en cours (sessions média)
│   └── BrowserController.kt  le WebView (navigateur)
└── ui/
    ├── HomeScreen.kt         écran d'accueil
    ├── CarDrawing.kt         dessin de la 308 (vectoriel)
    ├── MediaCard.kt          carte musique de l'accueil
    ├── BrowserScreen.kt      navigateur
    ├── FavoritesScreen.kt    favoris
    ├── AppsScreen.kt         toutes les applis
    ├── SettingsScreen.kt     réglages
    ├── Components.kt         tuiles, en-têtes, écran « à l'arrêt »
    └── Theme.kt              couleurs
```

## Idées pour la v2

- Onglets et bloqueur de pubs
- Fonds d'écran animés et thèmes
- Commande vocale (« Ouvre Waze »)
- Lecture vidéo plein écran (à l'arrêt uniquement)

## Rappel légal

En France, le conducteur ne doit pas avoir dans son champ de vision un écran qui affiche autre chose qu'une aide à la conduite ou à la navigation (Code de la route, art. R412-6-2). Ne retire pas le blocage en roulant.
