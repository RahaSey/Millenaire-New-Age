# 🛠️ Guide du Créateur - Millénaire: New Age

Ce guide détaille le processus de création de contenu pour le mod, de la capture physique des bâtiments à la configuration logique des cultures et des villages.

---

## 🏗️ 1. Création de Structures (Schématiques)

Dans "New Age", les bâtiments ne sont plus définis par des fichiers images (PNG) mais par des **fichiers NBT** (modèles 3D Minecraft) capturés directement en jeu.

### Capture avec la Baguette d'Arpentage
1.  **Sélection** : Utilisez la **Baguette d'Arpentage** (`millenaire-new-age:structure_scanner`) pour définir la zone.
    *   **Clic Gauche** : Pos 1 (Premier coin).
    *   **Clic Droit** : Pos 2 (Deuxième coin).
    *   *Note* : Des particules visualisent la zone sélectionnée.
2.  **Sauvegarde** : Utilisez la commande `/mna creator structure save <culture>/<nom_structure>` (ex: `normans/house_t1`).
    *   **Fichier NBT** : Le modèle physique de la construction.
    *   **Fichier JSON** : Un fichier `_blocks.json` est généré pour permettre la prévisualisation "fantôme" lors du placement.
3.  **Enfouissement Automatique** : Le système analyse la couche Y=0. Si elle est composée majoritairement de blocs naturels (herbe, terre, sable), le bâtiment sera automatiquement enterré de 1 bloc lors de sa génération pour une meilleure intégration au relief.

### Test de Placement
Utilisez la **Baguette de Placement** (`millenaire-new-age:structure_placer`) avec la commande `/mna creator structure place <id>` pour visualiser comment la structure s'insère dans le monde avant de l'intégrer officiellement.

---

## 🌍 2. Configuration d'une Culture

Les cultures sont définies dans des fichiers JSON situés dans les datapacks : `data/<namespace>/culture/<id>.json`.

### Gestion des Villages (`village_types`)
La génération et l'évolution d'un village reposent sur trois listes de bâtiments :

1.  **`required_building_ids`** : Bâtiments **obligatoires** construits immédiatement à l'apparition du village (ex: Mairie, Puits).
2.  **`optional_building_ids`** : Liste de bâtiments utilisés si le village n'a pas atteint son nombre minimal de constructions de départ (`min_starter_buildings`) une fois les bâtiments requis terminés.
3.  **`available_building_ids`** : Pool complet de tous les bâtiments que le village a le droit de construire durant toute sa vie (expansion naturelle).

### Structures de Bordure (OBLIGATOIRES)
Chaque culture **doit** posséder trois structures spéciales définies dans `building_types`. Elles délimitent la superficie du village :
*   **`CORNER`** : Pour les angles du périmètre.
*   **`ENTRANCE`** : Pour les points d'entrée du village.
*   **`PILLAR`** : Pour les limites le long des segments du périmètre.

*Ces 3 structures sont indispensables au fonctionnement du système de territoire.*

---

## 📐 3. Placement et Distance Factor

Le placement des bâtiments est géré par le `distance_factor` (valeur entre 0.0 et 1.0) dans la définition du bâtiment :

*   **Principe** : Il définit la zone (en pourcentage du rayon du village) où le bâtiment peut être placé.
    *   *Exemple* : Un champ avec un facteur proche de `1.0` sera placé en périphérie. Une mairie à `0.1` sera au centre.
*   **Flexibilité** : Si aucune place n'est disponible dans la zone définie, le système agrandit automatiquement le périmètre de recherche pour garantir que le bâtiment puisse être posé.

---

## 📋 4. Rôles des Bâtiments (`role`)

Le rôle définit la priorité et l'importance du bâtiment dans la logique du village :

| Rôle | Description |
| :--- | :--- |
| **`CENTER`** | La Mairie. Point de départ unique du village. |
| **`REQUIRED`** | Indispensable au fonctionnement (ex: Bûcheron pour les ressources). |
| **`CORE`** | Bâtiments économiques majeurs (ex: Forge). |
| **`SECONDARY`**| Bâtiments d'utilité ou de confort (ex: Marché). |
| **`EXTRA`** | Bâtiments de remplissage (ex: Maisons supplémentaires). |

---

## 🕯️ 5. Test en Jeu

Pour générer un village et tester votre configuration :
1.  Posez un **Bloc d'Or**.
2.  Utilisez la **Baguette d'Invocation** (`millenaire-new-age:wand_of_summoning`) sur le bloc.
3.  Sélectionnez votre culture et le type de village souhaité dans l'interface.

---

## 📜 6. Commandes du Mod

Le mod propose plusieurs commandes regroupées sous le préfixe `/mna`. La plupart de ces commandes nécessitent d'être OP (niveau de permission 2).

### Aide
*   `/mna help` : Affiche la liste des commandes disponibles et leur description.

### Cultures
*   `/mna culture list` : Liste toutes les cultures actuellement chargées par le mod.
*   `/mna culture info <id>` : Affiche les détails techniques d'une culture (biomes, types de villages, bâtiments, etc.).

### Villages
*   `/mna village list` : Liste tous les villages actifs dans la dimension actuelle avec leurs coordonnées.
*   `/mna village info [nom]` : Affiche les informations détaillées du village le plus proche ou d'un village spécifique.
*   `/mna village tp <nom>` : Téléporte le joueur au centre du village spécifié.
*   `/mna village remove <nom>` : Supprime un village (à utiliser pour le debug).

### Mode Créateur (Structure Scanner/Placer)
*   `/mna creator tool scanner` : Donne la Baguette d'Arpentage pour sélectionner des zones.
*   `/mna creator tool placer` : Donne la Baguette de Placement (vierge).
*   `/mna creator structure save <id>` : Sauvegarde la sélection actuelle dans un fichier NBT et génère le JSON de prévisualisation.
*   `/mna creator structure list` : Ouvre une interface graphique listant toutes les structures sauvegardées dans le dossier `creator_output`.
*   `/mna creator structure place <id>` : Configure la Baguette de Placement tenue en main pour la structure spécifiée et active la prévisualisation.
*   `/mna creator structure delete <id>` : Supprime définitivement les fichiers d'une structure sauvegardée.
*   `/mna creator structure info <id>` : Affiche les dimensions et les clés de traduction d'une structure.
*   `/mna creator selection clear` : Réinitialise la sélection actuelle du scanner.
*   `/mna creator selection info` : Affiche les coordonnées et la taille de la sélection actuelle.
*   `/mna creator generation <on|off|status>` : Active, désactive ou affiche l'état de la génération naturelle des villages.

---

*Note : Pour toute nouvelle structure, n'oubliez pas d'ajouter les traductions correspondantes dans vos fichiers de 
langue (ou vérifiez le fichier `lang_additions.json` généré par le mode creator).*

*Attention, le mode est en phase de 
création. De nombreux changements peuvent arriver et radicalement changer la structure des fichiers json.*
