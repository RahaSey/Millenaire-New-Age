# Millenaire: New Age

[![Minecraft 1.21.10](https://img.shields.io/badge/Minecraft-1.21.10-blue.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Loader-Fabric-lightgrey.svg)](https://fabricmc.net/)
[![License](https://img.shields.io/badge/License-CC0--1.0-green.svg)](https://creativecommons.org/publicdomain/zero/1.0/)

Une refonte moderne et ambitieuse du mod légendaire **Millénaire** (originaire de la 1.12.2). Ce projet vise à recréer et étendre l'expérience des civilisations vivantes, des villages autonomes et des cultures riches pour les versions récentes de Minecraft.

---

## 🏛️ Vision du Projet

**Millénaire: New Age** est un **moteur de civilisations** conçu pour la performance et l'extensibilité :
- **Héritage 1.12.2** : Reprend les concepts, les mécaniques et l'âme du mod original tout en réécrivant chaque ligne de code pour les standards modernes.
- **Moteur IA & Économie** : Gestion universelle des villages, du commerce simulé, des quêtes et des comportements complexes des villageois.
- **Civilisations via Datapacks** : Les cultures ne sont plus figées dans le code. Chaque civilisation est un datapack JSON, permettant une personnalisation totale par la communauté.
- **Référence Normande** : La culture Normande historique sert de socle technique et de référence pour toutes les autres civilisations.

## 🚀 Fonctionnalités Actuelles

### 🛠️ Mode Créateur (Structure System)
Le mod dispose déjà d'un puissant système pour capturer et intégrer des bâtiments :
- **Baguette d'Arpentage** : Permet de sélectionner une zone dans le monde (`/mna creator tool scanner`).
- **Sauvegarde Intelligente** : Export automatique en `.nbt` avec génération de noms et clés de traduction.
- **Baguette de Placement** : Visualisation "fantôme" (effet schématique) et placement précis avec rotation (`/mna creator structure list`).
- **Gestion Hybride** : Le mod lit les structures dans ses ressources internes ET dans le dossier `mods/MillenaireNewAge/creator_structures/`.

## 🛠️ Stack Technique

- **Framework** : Fabric Loader & Fabric API
- **Configuration** : Cloth Config API
- **Données** : Cardinal Components API
- **Version Cible** : Minecraft 1.21.10
- **Java** : 21

---

## 🔧 Installation & Développement

### Pour les joueurs / Créateurs
1. Installez **Fabric** pour Minecraft 1.21.10.
2. Ajoutez le mod et ses dépendances dans votre dossier `mods`.
3. Pour ajouter vos propres structures, placez-les dans `.minecraft/mods/MillenaireNewAge/creator_structures/`.

### Pour les développeurs
```bash
git clone https://github.com/mat37dev/Millenaire-New-Age
cd Millenaire-New-Age
./gradlew genSources
```
Utilisez **IntelliJ IDEA** pour une expérience optimale.

---

## 📅 Feuille de Route (Roadmap)

Le développement est structuré en plusieurs phases pour garantir la robustesse du moteur avant d'ajouter le contenu massif.

- **Phase 0** : Setup & Infrastructure (🟢 Terminé)
- **Phase 1** : Architecture des données (🟢 Terminé)
- **Phase 2** : Système de Civilisations (🟢 Terminé)
- **Phase 3** : Génération du Monde (🔴 À faire)
- **Phase 4** : Assets & Items Normands (🟡 En cours)

*Voir [ROADMAP.md](ROADMAP.md) pour le détail complet et le suivi des tâches.*

---

## 🤝 Contribution

Les contributions sont les bienvenues ! Que ce soit pour signaler un bug, proposer une idée ou soumettre du code, merci de consulter notre **[Guide de Contribution](CONTRIBUTING.md)** pour connaître la marche à suivre.

---

## 📜 Licence

Ce projet est distribué sous licence **CC0-1.0** (Public Domain). Vous êtes libre de l'utiliser, le modifier et le distribuer.
