# Guide de Contribution - Millénaire New Age 🏛️

Merci de l'intérêt que vous portez à **Millénaire New Age** ! Ce projet est open-source et nous accueillons avec plaisir les améliorations, les corrections de bugs et les nouvelles fonctionnalités.

Pour maintenir la qualité du code et assurer la stabilité du mod, merci de suivre ces directives.

---

## 🚀 Notre Stratégie de Branchement (IMPORTANT)

Nous utilisons un modèle de développement basé sur deux branches principales :

* **`main`** : Contient uniquement le code des versions stables et publiées. **Ne travaillez pas sur cette branche.**
* **`develop`** : C'est la branche active. Elle contient les dernières modifications et corrections. **Toute contribution doit être basée sur cette branche.**

> [!WARNING]
> Toute Pull Request (PR) ouverte vers la branche `main` sera automatiquement refusée ou demandée d'être redirigée vers `develop`.

---

## 🛠️ Comment contribuer ?

1.  **Forkez le dépôt** sur votre compte GitHub.
2.  **Clonez votre fork** localement.
3.  **Créez une branche de travail** à partir de la branche `develop` :
    ```bash
    git checkout develop
    git pull origin develop
    git checkout -b feature/ma-super-idee
    ```
4.  **Configurez votre environnement** de développement (Minecraft/Forge/Fabric via Gradle).
5.  **Codez !** N'oubliez pas de tester vos modifications en jeu.
6.  **Commitez vos changements** avec des messages clairs (ex: `feat: ajout de la culture du riz au village`).
7.  **Poussez sur votre fork** et **ouvrez une Pull Request** vers la branche `develop` du dépôt principal.

---

## 📝 Règles de Code & Style

* **Commentaires :** Si votre code est complexe, expliquez-le. Nous aimons comprendre pourquoi un villageois refuse de construire sa maison !
* **Lisibilité :** Suivez les conventions de nommage Java standards.
* **Atomisation :** Essayez de garder vos PR concentrées sur un seul sujet (ne mélangez pas une correction de bug et l'ajout d'un nouveau bâtiment).

---

## 🐛 Signaler un bug

Si vous ne savez pas coder mais que vous avez trouvé un bug :
1. Vérifiez que le bug n'est pas déjà listé dans l'onglet **Issues**.
2. Ouvrez une nouvelle Issue en précisant :
    * Votre version de Minecraft et du Mod.
    * La liste des autres mods installés (si pertinent).
    * Les logs d'erreur (via Pastebin ou bloc de code).
    * Les étapes pour reproduire le bug.

---

Merci encore de nous aider à faire revivre Millénaire ! 🌾🏗️
