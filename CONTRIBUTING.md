# Guide de Contribution - Millénaire New Age 🏛️

Merci de l'intérêt que vous portez à **Millénaire New Age** ! Ce projet est une aventure communautaire et nous accueillons avec enthousiasme vos contributions pour redonner vie à ce mod légendaire.

Pour garantir la qualité du code et la pérennité du projet, nous avons mis en place des règles strictes de contribution. Merci de les lire attentivement.

---

## 🛡️ Stratégie de Branchement & Protection

Le dépôt suit un modèle de développement rigoureux. Les branches principales sont protégées pour éviter toute régression ou instabilité.

### 🌿 Branches Principales
*   **`main`** : Contient uniquement les versions stables, testées et publiées.
*   **`develop`** : Branche d'intégration active. C'est ici que convergent toutes les nouvelles fonctionnalités.

### 🚫 Règles de Push
> [!IMPORTANT]
> **Le push direct sur les branches `main` et `develop` est formellement interdit.**
> Ces branches sont protégées au niveau du dépôt. Toute modification doit impérativement passer par une **Merge Request (MR)** ou **Pull Request (PR)**.

---

## 🚀 Cycle de Contribution

### 1. Préparation
Toute contribution doit se baser sur la branche **`develop`**.
1.  **Forkez** le projet sur votre compte GitHub.
2.  **Clonez** votre fork localement.
3.  Assurez-vous d'être à jour avec le dépôt original :
    ```bash
    git remote add upstream https://github.com/mat37dev/Millenaire-New-Age.git
    git fetch upstream
    git checkout develop
    git merge upstream/develop
    ```

### 2. Développement
Créez une branche de travail explicite à partir de `develop` :
```bash
git checkout -b feature/ma-fonctionnalite  # Pour une nouvelle feature
# OU
git checkout -b fix/nom-du-bug             # Pour une correction de bug
```

### 3. Merge Request (MR)
Une fois vos modifications terminées et testées :
1.  Poussez votre branche sur votre fork.
2.  Ouvrez une **Pull Request** vers la branche **`develop`** du dépôt principal.
3.  Décrivez précisément vos changements et joignez des captures d'écran si nécessaire.

> [!NOTE]
> **Validation :** En tant que mainteneur, je prendrai le temps d'analyser chaque MR. Je me réserve le droit d'accepter, de demander des modifications ou de refuser une contribution si elle ne correspond pas à la vision ou aux standards de qualité du projet.

---

## 🛠️ Standards Techniques

### Environnement
*   **Java :** Version **21** obligatoire.
*   **Loader :** Fabric API.
*   **Build Tool :** Gradle (utilisez `./gradlew` pour vos commandes).

### Conventions de Code
*   **Style :** Respectez les conventions Java standards (CamelCase, indentation de 4 espaces).
*   **Clean Code :** Un code lisible est préférable à un code complexe. Commentez le *pourquoi* plutôt que le *comment*.
*   **Datapacks :** Pour les nouvelles cultures ou bâtiments, privilégiez le système de fichiers JSON/NBT décrit dans le README.

### Commits
Nous encourageons l'usage des **Conventional Commits** pour garder un historique clair :
*   `feat: ...` pour une nouvelle fonctionnalité.
*   `fix: ...` pour une correction.
*   `docs: ...` pour la documentation.
*   `refactor: ...` pour une modification du code sans changement de comportement.

---

## 💬 Contact & Communauté

Si vous avez des questions sur l'architecture, une idée de fonctionnalité ou si vous souhaitez simplement discuter du projet, n'hésitez pas à me contacter :
*   **Discord :** `mat_37`

---

## 🐛 Signaler un Problème

Si vous trouvez un bug mais ne pouvez pas le corriger :
1.  Vérifiez les **Issues** existantes.
2.  Ouvrez une nouvelle issue avec :
    *   La version exacte du mod et de Minecraft.
    *   Les logs (utilisez [Pastebin](https://pastebin.com/)).
    *   Une procédure de reproduction pas à pas.

Merci de contribuer à la renaissance de Millénaire ! 🌾🏗️