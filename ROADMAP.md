# Millenaire: New Age — Feuille de Route

> **Mod :** Millenaire: New Age
> **Framework :** Fabric (Fabric Loader + Fabric API)
> **Cible :** Minecraft Java Edition 1.21.10
> **Philosophie :** Moteur générique de cultures + culture Normande comme référence.
> **OldSource :** Concepts, mécaniques, assets visuels uniquement. Aucun code repris.

---

## Vision du projet

Un **moteur de cultures** extensible par la communauté :
- Le mod est le moteur (IA, village, bâtiments, économie, quêtes)
- Les cultures sont des **datapacks** — partageables, sans code
- Les cultures nécessitant de nouveaux items/blocs utilisent un **mod compagnon** minimal
- La culture **Normande** est la référence intégrée au mod de base

### Modèle de données

```
Culture (données JSON/datapack)
  ├── VillageType[]      → hameau, village, bourg, forteresse, monastère...
  │     └── BuildingType[] → maison, forge, mairie, tour de guet...
  │           └── StructureTemplate (.nbt)
  ├── VillagerType[]     → paysan, forgeron, garde, chef, marchand...
  ├── TradeGoodDef[]     → biens de commerce par catégorie
  ├── QuestDef[]         → quêtes disponibles
  ├── Language           → noms de villageois, de villages, dialogues
  └── ItemSet            → crops, nourriture, décorations

Village (instance runtime, persistée par monde)
  ├── culture, type, nom, position
  ├── buildings: Building[]
  ├── villagers: MillVillager[]
  ├── reputation: Map<PlayerUUID, Int>
  └── stock: ResourceStock

Building (instance runtime)
  ├── type: BuildingType
  ├── health: Int / maxHealth: Int
  ├── state: PLANNED | UNDER_CONSTRUCTION | INTACT | DAMAGED | RUINED | DESTROYED
  └── residents: MillVillager[]

MillVillager (entité mob)
  ├── culture, type, village
  ├── home: Building, workplace: Building
  └── Brain (IA comportementale)
```

### Système d'extension (add-ons)

| Niveau | Pour qui | Ce que ça permet | Requis |
|--------|----------|-----------------|--------|
| **Datapack** | Tous les créateurs | Bâtiments, quêtes, langue, commerce, config villageois | JSON + textures |
| **Mod compagnon** | Créateurs avancés | Nouveaux blocs, items, entités custom | Java + Fabric |
| **API core** | Développeurs | Intégration programmatique complète | Java + Fabric |

### Items — Architecture à 3 couches

1. **Items génériques** intégrés au mod (cultures, nourriture, matériaux) — utilisables par toutes les cultures via retexture
2. **Items Normands** intégrés directement (culture de base)
3. **Items cultures externes** — via mod compagnon déclarant ses items à l'API

### Santé des bâtiments

```
INTACT (100%)     → aspect normal
DAMAGED (50-99%)  → blocs partiellement remplacés (variantes "abîmées")
RUINED (1-49%)    → grande partie détruite, villageois en alerte
DESTROYED (0%)    → décombres, reconstruisable
```

Dégâts : attaques ennemies, explosions, feu.
Réparation : villageois (automatique, lent) ou joueur (rapide, coûte des matériaux).

---

## Stack technique

| Composant | Choix |
|-----------|-------|
| Framework | Fabric Loader + Fabric API |
| Configuration | Cloth Config |
| Persistance monde | Cardinal Components API |
| Build | Gradle + Fabric Loom |
| Java | 21 |
| IDE | IntelliJ IDEA |

---

## Méthodologie

### Sprints de 2 semaines
- Objectif clair et livrable testable en jeu
- Commit + tag git en fin de sprint
- Mise à jour du statut dans ce fichier
- Revue des blocages avant le sprint suivant

### Règles
1. **Moteur avant contenu** — les systèmes génériques précèdent le remplissage
2. **Normands d'abord** — ils définissent le template de tout le reste (une fois le moteur prêt)
3. **Data-driven** — tout ce qui peut être en JSON l'est
4. **Séparation client/serveur** — stricte dès le départ
5. **OldSource = documentation** — lire pour comprendre, réécrire proprement
6. **Commits sémantiques** — `feat:`, `fix:`, `refactor:`, `doc:`
7. **Indépendance OldSource** — toute texture/asset récupéré depuis OldSource est immédiatement copié dans `src/main/resources/` du projet. OldSource doit pouvoir être supprimé à tout moment sans casser le mod.
8. **Internationalisation continue** — chaque texte visible par le joueur (bloc, item, GUI, message) est localisé en `en_us` ET `fr_fr` dès son ajout. Jamais de texte en dur hors fichier lang.

---

## Vue d'ensemble des phases

```
Phase 0  │ Setup & Infrastructure de base              │ ~2 semaines
Phase 1  │ Architecture des entités de données         │ ~2 semaines
Phase 2  │ Système de cultures (datapack)         │ ~3 semaines
Phase 3  │ Types de villages & génération monde        │ ~4 semaines
Phase 4  │ Assets Normands (Blocs, Items, Textures)    │ ~4 semaines
Phase 5  │ Entité Villageois & rendu                   │ ~3 semaines
Phase 6  │ IA & comportements (améliorée)              │ ~5 semaines
Phase 7  │ Santé des bâtiments & construction dyn.     │ ~3 semaines
Phase 8  │ Économie & commerce                         │ ~3 semaines
Phase 9  │ Quêtes                                      │ ~3 semaines
Phase 10 │ Creator Mode (outil de création in-game)    │ ~3 semaines
Phase 11 │ Interfaces utilisateur                      │ ~3 semaines
Phase 12 │ Réseau & multijoueur                        │ ~2 semaines
Phase 13 │ Avancements & progression                   │ ~2 semaines
Phase 14 │ 2ème culture (validation du système)   │ ~4 semaines
Phase 15 │ Bandits, diplomatie, villages contrôlés     │ Post-v1.0
Phase 16 │ Polish, tests & release                     │ ~2 semaines
```

---

## Phase 0 — Setup & Infrastructure

**Objectif :** Projet Fabric qui démarre dans MC 1.21.10, base de code propre en place.

### 0.1 — Environnement
- [x] Vérifier JDK 21 (`java --version`)
- [x] Télécharger Fabric MDK pour MC 1.21.10
- [x] Importer dans IntelliJ, vérifier `runClient` et `runServer`

### 0.2 — Configuration du projet
- [x] `gradle.properties` : `mod_id=millenaire_new_age`, version, group
- [x] `fabric.mod.json` : metadata, entrypoints, dependencies
- [x] Dépendances `build.gradle` : `fabric-api`, `cloth-config`, `cardinal-components-api`

### 0.3 — Structure de packages
```
com.mat37dev/
├── MillenaireNewAge.java           ← Entry point (serveur + commun)
├── MillenaireNewAgeClient.java     ← Entry point client uniquement
├── MillenaireNewAgeDataGenerator.java ← Data generation
├── init/
│   ├── MillBlocks.java
│   ├── MillItems.java
│   └── MillEntities.java
├── culture/               ← Modèle de données culture
│   ├── Culture.java
│   ├── VillageType.java
│   ├── BuildingType.java
│   ├── VillagerTypeDef.java
│   └── CultureLanguage.java
├── village/                    ← Instances runtime
│   ├── Village.java
│   ├── Building.java
│   ├── BuildingState.java
│   └── VillageManager.java
├── entity/
│   ├── MillVillagerEntity.java
│   └── ai/
├── economy/
├── quest/
├── world/
├── network/
├── creator/                    ← Creator Mode
├── client/                     ← (dans src/client/java/com/mat37dev/)
│   ├── gui/
│   └── render/
├── data/                       ← Loaders JSON/datapack
└── util/
```

### 0.4 — Configuration & logging
- [x] `MillConfig.java` (Cloth Config) : rayon villages, fréquence gen, debug
- [x] Logger dédié `LogManager.getLogger("millenaire-new-age")`

### 0.5 — Git
- [x] `.gitignore` configuré (`.gradle/`, `run/`, `build/`)
- [x] Commit initial + tag `v0.1.0-alpha`

**Livrable :** Mod démarre, log "Millenaire: New Age initialized" visible.

---

## Phase 1 — Architecture des Entités de Données

**Objectif :** Les classes Java qui représentent les concepts de culture, village, bâtiment.
Ces classes sont le **squelette** sur lequel tout le reste s'appuie.

### 1.1 — Culture (données immuables, chargées depuis JSON)
```java
public record Culture(
    String id,
    String displayName,
    CultureLanguage language,
    List<String> compatibleBiomes,
    List<VillageType> villageTypes,
    List<VillagerTypeDef> villagerTypes,
    List<TradeGoodDef> tradeGoods,
    List<String> knownCrops
) {}
```
- [x] `Culture.java`
- [x] `VillageType.java` (hameau, village, bourg, forteresse, monastère)
- [x] `BuildingType.java` (mairie, maison, forge, ferme, tour, mur...)
- [x] `VillagerTypeDef.java` (paysan, forgeron, garde, chef, marchand...)
- [x] `CultureLanguage.java` (pools de noms, dialogues)
- [x] `TradeGoodDef.java`
- [x] `CultureRegistry.java` — registre des cultures chargées

### 1.2 — Village (état runtime, persisté)
```java
public class Village {
    UUID id;
    String name;
    Culture culture;
    VillageType type;
    BlockPos center;
    List<Building> buildings;
    List<UUID> villagerIds;
    Map<UUID, Integer> reputation;     // par joueur
    ResourceStock stock;               // ressources du village
    VillageState state;                // GROWING | STABLE | THREATENED | ABANDONED
}
```
- [x] `Village.java`
- [x] `VillageState.java` (enum)
- [x] `ResourceStock.java` (stocks agrégés par catégorie)

### 1.3 — Building (état runtime, persisté)
```java
public class Building {
    UUID id;
    UUID villageId;
    BuildingType type;
    BlockPos origin;
    Direction facing;
    BuildingState state;               // PLANNED | UNDER_CONSTRUCTION | INTACT | DAMAGED | RUINED | DESTROYED
    int currentHealth;
    int maxHealth;
    List<UUID> residentIds;
}
```
- [x] `Building.java`
- [x] `BuildingState.java` (enum + méthodes utilitaires)
- [x] `MillStructureTemplate.java` (wrapper léger — placement Phase 3)

### 1.4 — Persistance (Cardinal Components)
- [x] Composant `WorldVillageData` → liste de tous les villages du monde
- [x] Sérialisation/désérialisation avec `ValueInput`/`ValueOutput` (MC 1.21.10)
- [x] `VillageManager.java` — accès statique, tick placeholder

### 1.5 — Tests unitaires (optionnel mais recommandé)
- [ ] Test de sérialisation/désérialisation
- [ ] Test de registry des cultures

**Livrable :** Classes compilées, log de debug affichant les structures de données.
**Tag :** `v0.2.0-alpha`

---

## Phase 2 — Système de Cultures (Datapack)

**Objectif :** Charger les cultures depuis des datapacks. Normands intégrés comme référence.

### 2.1 — Format JSON des cultures
```json
// data/millenaire_new_age/cultures/normans.json
{
  "id": "normans",
  "display_name": { "fr_fr": "Normands", "en_us": "Normans" },
  "language": "normans",
  "compatible_biomes": ["minecraft:plains", "minecraft:forest", "minecraft:meadow"],
  "village_types": ["normans:hamlet", "normans:village", "normans:fortress"],
  "known_crops": ["minecraft:wheat", "millenaire_new_age:apple_tree_sapling"],
  "trade_categories": ["construction", "agriculture", "crafting", "weapons"]
}
```
```json
// data/millenaire_new_age/village_types/normans/village.json
{
  "id": "normans:village",
  "culture": "normans",
  "display_name": { "fr_fr": "Village Normand", "en_us": "Norman Village" },
  "min_buildings": 8,
  "max_buildings": 15,
  "required_buildings": ["normans:townhall"],
  "optional_buildings": ["normans:house", "normans:forge", "normans:farm", "normans:chapel"],
  "has_walls": false,
  "villager_types": ["normans:farmer", "normans:blacksmith", "normans:guard", "normans:chief"]
}
```
- [x] Schéma JSON embarqué dans `culture/<id>.json` : language, village_types, building_types, villager_types, trade_goods, known_crops
  - Note : format actuel = types embarqués dans la culture. Refactorisable en fichiers séparés si besoin.

### 2.2 — Chargeur datapack (ResourceReloadListener + Codec)
- [x] `CultureLoader.java` — lit tous les `data/*/culture/*.json` via `Culture.CODEC`
- [x] Support multi-datapacks (plusieurs cultures en parallèle)
- [x] Validation automatique par Codec + messages d'erreur clairs dans les logs
- [x] API publique : `MilenaireApi.registerCulture(...)` pour les mods compagnons

### 2.3 — Culture Normande intégrée
- [x] `normans.json` — définition principale complète
- [x] Types de villages : hameau (3-5), village (8-15), forteresse (militaire)
- [x] Types de bâtiments : mairie, maison, forge, ferme, chapelle, donjon, caserne
- [x] Types de villageois : paysan, forgeron, garde, chef, marchand
- [x] Langue : noms historiques normands (Guillaume, Mathilde…) + villes (Caen, Rouen…)
- [x] Catalogue de commerce normand (blé, pain, pierre, outil, laine)

### 2.4 — Commandes de debug culture
- [x] `/mna culture list` — liste les cultures chargées
- [x] `/mna culture info <id>` — détails d'une culture

**Livrable :** Log de chargement des cultures OK, structures JSON définies.
**Tag :** `v0.3.0-alpha`

---

## Phase 3 — Types de Villages & Génération Monde

**Objectif :** Des villages normands de différents types apparaissent dans le monde.
**Référence OldSource :** `common/world/` — algorithme de placement.
**Stratégie :** 3 étapes séquentielles — création manuelle → persistance → génération automatique.

### 3.1 — Outils créateur (✅ implémenté — voir Phase 10)
- [x] Structures NBT enregistrées (4 bâtiments normands : castle_t1, forest_t1, guard_t1, lumberjack_t1)
- [x] `StructureScannerItem` — sélection de zone (Pos1/Pos2)
- [x] `StructurePlacerItem` — placement fantôme + rotation + placement réel
- [x] `StructureSaveManager` — sauvegarde/chargement/liste des structures
- [x] Preview fantôme client-side (vrais blocs semi-transparents)

### 3.2 — Création manuelle de village (Baguette d'Invocation) ✅ Implémenté
**Objectif :** Créer un village en jeu via item + GUI.

#### Config (`mods/MillenaireNewAge/config/village_config.json`)
- [x] `VillageConfig.java` — lecture/écriture du fichier config JSON
- [x] Config par défaut créée au premier lancement (village_size, village_spacing, max_villagers, building_spacing)
- [x] Dossier `mods/MillenaireNewAge/` = répertoire racine du mod

#### Item `WandOfSummoningItem`
- [x] Clic droit sur bloc d'or → ouvre le GUI de création de village
- [x] Envoi payload S→C avec la liste des cultures disponibles
- [x] Vérification `villageSpacing` — message d'erreur rouge si trop proche
- [x] Bloc d'or supprimé après création

#### GUI `VillageCreationScreen`
- [x] Onglets par culture (un onglet = une culture chargée)
- [x] Dans chaque onglet : liste des types de villages avec infos (nb bâtiments, murailles)
- [x] Bouton "Créer" → envoie `CreateVillagePayload` au serveur
- [x] Bouton "Annuler" → ferme le GUI sans action

#### `VillagePlacer.java`
- [x] Reçoit : culture, type de village, position centrale (bloc d'or)
- [x] Détermine les bâtiments à placer (requis + aléatoire parmi optionnels)
- [x] Tri par proximity (CENTER → NEAR → FAR), centrage du bâtiment principal sur le bloc d'or
- [x] `TerrainAdapter` — nivelle le terrain (creuse / remblaye / nettoie végétation)
- [x] Place chaque bâtiment via `StructureSaveManager.placeStructure()`
- [x] Crée les instances `Village` + `Building` et les enregistre dans `VillageManager`
- [x] Indicateur de nom flottant (ArmorStand invisible + nom en jaune gras)
- [x] `normans.json` mis à jour : fortress utilise les 4 structures enregistrées

#### Réseau
- [x] `OpenVillageCreationPayload` (S→C) — liste des cultures pour le GUI
- [x] `CreateVillagePayload` (C→S) — demande de création (civ + type + pos)

#### Test : forteresse normande
- [x] 4 bâtiments placés autour du bloc d'or cliqué
- [x] Instance `Village` créée et accessible via `VillageManager`
- [x] Messages de confirmation en jeu

### 3.3 — Persistance des villages ✅ Implémenté
**Objectif :** Les villages survivent au redémarrage du monde.

- [x] Vérifier sérialisation complète `Village` + `Building` via Cardinal Components
- [x] Test : créer un village → quitter → recharger → village toujours présent
- [x] Commandes de debug :
  - [x] `/mna village list` — liste tous les villages du monde courant
  - [x] `/mna village info [id]` — infos sur le village le plus proche (ou par ID)
  - [x] `/mna village tp <id>` — téléporter vers un village
  - [x] `/mna village remove <id>` — supprimer un village (debug)

### 3.4 — Génération naturelle (Worldgen Fabric)
**Objectif :** Villages générés automatiquement selon le biome.

- [x] Algorithme de sélection biome/culture
- [x] Distance minimale entre villages (lire `village_spacing` depuis config)
- [x] Détection terrain plat (rayon = `village_size / 2`)
- [x] Enregistrement Structure/Feature Fabric
- [x] Placement au chunkload (sans conflits de génération)

**Livrable :** Villages normands générés naturellement + persistants + créables manuellement.
**Tag :** `v0.4.0-alpha`

---

## Phase 4 — Assets Normands (Blocs, Items & Textures)

**Objectif :** Infrastructure d'enregistrement et ensemble complet des assets normands (Blocs et Items).
Cette phase intervient une fois que le moteur de base et la génération sont en place.

### 4.1 — Infrastructure d'enregistrement
- [ ] Registres Fabric (`MillBlocks`, `MillItems`)
- [ ] Onglet créatif "Millenaire: New Age" avec icône
- [ ] Pattern de registre propre, documenté (futur template pour add-ons)

### 4.2 — Blocs normands (depuis OldSource)
Ces blocs servent de base de construction pour les villages.
- [ ] Briques normandes (pierre, calcaire, variantes)
- [ ] Bois normand (chêne, variantes de planches)
- [ ] Blocs décoratifs (rosaces, moulures, bardages)
- [ ] Bloc de chemin (terre battue)
- [ ] Blocs fonctionnels : foyer, coffre verrouillé, lit de village

### 4.3 — Items génériques (socle commun)
Ces items sont le socle commun que toutes les cultures peuvent utiliser :
- [ ] `generic_grain` — céréale générique (retexturable)
- [ ] `generic_bread` — pain générique
- [ ] `generic_fruit` — fruit générique
- [ ] `generic_vegetable` — légume générique
- [ ] `generic_cloth` — tissu générique
- [ ] `generic_leather_item` — cuir travaillé générique
- [ ] `generic_tool_wood` / `generic_tool_stone` — outils génériques
- [ ] `parchment` — parchemin (quêtes, livres)
- [ ] `travel_book` — livre de voyage
- [ ] `debug_wand` — baguette de debug (mode créateur)

### 4.4 — Items normands spécifiques
- [ ] Blé, pain normand, fromage
- [ ] Cidre (pomme, variantes)
- [ ] Équipements de soldat normand
- [ ] Bannière normande

### 4.5 — Blocs agricoles normands
- [ ] Pommier (sapling + arbre + feuilles de fruits)
- [ ] Vigne (grimpe sur murs)
- [ ] Blé (utilise le vanilla, pas de bloc custom nécessaire)

### 4.6 — Assets visuels
- [x] Récupérer et importer textures depuis OldSource (indépendance complète)
- [x] `blockstates/*.json`, `models/block/*.json`, `models/item/*.json`
- [x] `lang/en_us.json` — noms anglais de tous les blocs Phase 4
- [x] `lang/fr_fr.json` — noms français de tous les blocs Phase 4

**Livrable :** Écosystème visuel et matériel complet pour la culture de référence.
**Tag :** `v0.5.0-alpha`

---

## Phase 5 — Entité Villageois & Rendu

**Objectif :** Les villageois existent avec leur apparence, appartiennent à un village, ont une identité (nom, profession, âge) et un cycle de vie complet.
**Référence OldSource :** `client/render/`, `common/entity/MillVillager.java` — concepts uniquement, tout est réécrit.

### 5.1 — Entité de base (côté serveur)
- [x] `MillVillagerEntity.java` extends `Mob` (pathfinding + Brain API intégrés)
- [x] Attributs : HP (20 base, variable par type), vitesse (0.55), portée de vision (16 blocs), force
- [x] NBT persistant : `cultureId`, `villagerTypeId`, `villageId`, `sex`, `name`, `familyName`, `age`, `homeId`, `workplaceId`
- [x] Enregistrement `EntityType` — **pas de SpawnEgg** (spawn uniquement via village)
- [x] Mort : résurrection possible selon `noresurrect` dans `VillagerTypeDef`

### 5.2 — Rendu (côté client uniquement)
- [x] Modèle humanoïde custom (`MillVillagerModel`) — corps, bras, jambes, tête
- [x] Renderer `MillVillagerEntityRenderer`
- [x] Couche de vêtements (`MillVillagerClothingLayer`) par type de villageois + culture
- [x] Textures normandes récupérées de OldSource (indépendance totale)
- [x] Nametag au-dessus avec nom + rôle (ex : "Guillaume — Forgeron")
- [x] Rendu enfant : scale réduit (0.6) jusqu'à maturité

### 5.3 — Cycle de vie
- [ ] **Enfant** : spawn dans les maisons du village, scale réduit, consomme `foodsGrowth`
- [ ] **Croissance** : grandit progressivement sur ~20 nuits MC
- [ ] **Maturité** : cherche une maison + un emploi libres et déménage (`BecomeAdultBehavior`)
- [ ] **Reproduction** : femme adulte + partenaire compatible + `foodsConception` → naissance
- [ ] **Famille** : enfants héritent du `familyName` de la mère (noms historiques normands)
- [ ] **Mort** : bâtiment libéré, slot de population disponible, remplacement éventuel

### 5.4 — Intégration village
- [ ] Spawn à la génération du village selon les types définis dans `VillagerTypeDef`
- [ ] Attribution type/village/nom via `CultureLanguage` (pools de prénoms + noms de famille)
- [ ] Assignation `homeBuilding` (résidence) + `workplaceBuilding` (lieu de travail)
- [ ] Population contrôlée : pas de spawn si `maxVillagersPerVillage` atteint

### 5.5 — Interaction joueur de base
- [ ] Clic droit → ouvre l'interface de dialogue (placeholder, implémentée en Phase 11)
- [ ] Affichage : nom, profession, village d'appartenance
- [ ] Réaction basique selon réputation (message de bienvenue / méfiance / hostilité)
- [ ] Garde : réaction agressive si réputation < seuil critique (appel aux autres gardes)

**Livrable :** Villageois normands visibles, correctement nommés, assignés à leur village avec cycle de vie fonctionnel.
**Tag :** `v0.6.0-alpha`

---

## Phase 6 — IA, Comportements, Commerce & Quêtes

**Objectif :** Les villageois sont pleinement vivants — ils travaillent selon leur métier, dorment, se promènent, font du commerce avec le joueur, se défendent et donnent des quêtes.
**Référence OldSource :** `common/goal/` (45+ Goals) — concepts à réécrire proprement avec Brain API.
**Architecture :** Brain API (MC 1.21) — Activities, Behaviors, Sensors, Schedules.

### 6.1 — Architecture Brain API

#### Mémoires
- [ ] `HOME_POS`, `WORK_POS` — positions assignées (domicile + lieu de travail)
- [ ] `CURRENT_ACTIVITY` — activité en cours (WORK, REST, SLEEP, SOCIALIZE, DEFEND, TRADE)
- [ ] `NEAREST_PLAYER` — joueur le plus proche (commerce, quêtes, dialogue)
- [ ] `THREAT_TARGET` — cible hostile détectée (mob ou joueur agressif)
- [ ] `ACTIVE_QUEST` — quête en cours (état + étape)
- [ ] `DIALOGUE_PARTNER` — interlocuteur social actuel

#### Sensors
- [ ] `NearestPlayerSensor` — joueurs dans le rayon de vision
- [ ] `NearestVillagerSensor` — autres villageois (pour socialisation)
- [ ] `ThreatSensor` — monstres / joueurs hostiles
- [ ] `NearestBuildingSensor` — bâtiments accessibles (shops, chantiers, resources)

#### Schedules (horaires MC)
- [ ] `CIVILIAN_SCHEDULE` : WAKING(6h) → WORKING(7h–17h) → LEISURE(17h–20h) → SLEEPING(20h)
- [ ] `GUARD_SCHEDULE` : alternance quarts jour/nuit — toujours en alerte
- [ ] `MERCHANT_SCHEDULE` : WORKING(8h–18h) avec pauses socialisation
- [ ] Variante météo : rentre sous la pluie (interruption WORKING)

### 6.2 — Routine quotidienne

- [ ] Lever : se dirige vers son lieu de travail (`GoToWorkplaceBehavior`)
- [ ] Journée : comportements de travail selon profession (voir 6.3)
- [ ] Soir : rentre à la maison (`GoHomeBehavior`)
- [ ] Nuit : dort sur son lit (`SleepAtHomeBehavior`) — entité posée horizontalement
  - Fallback : sol dégagé à 6 blocs si pas de lit disponible
- [ ] Loisir (fenêtre 17h–20h) : socialisation, visite auberge, repos, balade

### 6.3 — Comportements par profession

**Chaque profession est liée à un bâtiment de travail (`workplaceBuilding`) avec des points de ressource spécifiques marqués dans le template NBT.**

#### Fermier
- [ ] Laboure le sol aux points `soils` du bâtiment de ferme
- [ ] Plante les cultures définies dans `VillagerTypeDef.knownCrops`
- [ ] Récolte quand les cultures sont mûres
- [ ] Rapporte la récolte au coffre du village (`BringResourcesHomeBehavior`)

#### Bûcheron
- [ ] Travaille dans le bâtiment `grove` (zone boisée du village)
- [ ] Coupe les arbres présents, récolte bois + sapling
- [ ] Replante les saplings pour renouveler la ressource
- [ ] Rapporte le bois au stock

#### Mineur
- [ ] Travaille à la mine (bâtiment avec points de ressource `sources`)
- [ ] Extrait pierre / sable / argile / gravier selon les points définis
- [ ] Rapporte au stock du village

#### Pêcheur
- [ ] Travaille aux points `fishingspots` du bâtiment de pêche (eau adjacente)
- [ ] Lance la ligne, attend (~500 ticks), récolte le poisson
- [ ] Rapporte au stock

#### Berger / Éleveur
- [ ] Travaille dans l'enclos (bâtiment avec tags `cattle`, `sheeps`, `pig`, `chicken`)
- [ ] Nourrit les animaux pour déclencher la reproduction (`BreedAnimalsBehavior`)
- [ ] Tond les moutons (`ShearSheepBehavior`) → laine dans le stock
- [ ] Collecte les œufs, le lait si applicable

#### Constructeur
- [ ] Surveille les bâtiments en état `PLANNED` ou `UNDER_CONSTRUCTION`
- [ ] Récupère les matériaux requis dans le stock du village (`GetResourcesForBuildBehavior`)
- [ ] Place les blocs selon le plan (`ConstructionStepByStepBehavior`) — couche par couche, saute si bloqué
- [ ] Construit les chemins entre les bâtiments

#### Marchand villageois (commerce inter-bâtiments)
- [ ] Approvisionne les bâtiments-boutiques en goods manquants
- [ ] Effectue des visites entre bâtiments pour équilibrer les stocks (`MerchantVisitBuildingBehavior`)
- [ ] Peut sortir du village pour visiter des villages normands voisins

#### Garde
- [ ] Patrouille le périmètre (points de patrouille dans le bâtiment de garde / tour de guet)
- [ ] Attaque les monstres détectés (`HuntMonsterBehavior`)
- [ ] `DefendVillageBehavior` (priorité maximale) déclenché par `ThreatSensor`
- [ ] Peut utiliser arc (`isArcher = true` dans VillagerTypeDef) ou arme de mêlée

#### Chef
- [ ] Reste au bâtiment principal (`isTownhall = true`)
- [ ] Supervise les priorités de construction
- [ ] Point d'entrée pour le commerce et les quêtes joueur

#### Vendeurs de bâtiments spécialisés
- [ ] Forgeron (forge) : vend outils / armes / armures
- [ ] Boulanger (boulangerie) : vend pain / nourriture préparée
- [ ] Paysan (ferme) : vend cultures, graines
- [ ] Tout bâtiment marqué `isShop = true` peut avoir un vendeur (`isSellerBehavior`)
- [ ] Le vendeur se dirige vers le comptoir quand un joueur s'approche (rayon 7 blocs)

### 6.4 — Comportements sociaux et balade

- [ ] `WanderAroundVillageBehavior` — se promène dans le périmètre du village au hasard
- [ ] `GoSocializeBehavior` — cherche un villageois libre, s'approche
- [ ] `ChatBehavior` — échange de phrases culturelles aléatoires (~40 ticks)
- [ ] `GoRestBehavior` — pause loisir (s'assoit, regarde autour)
- [ ] `VisitInnBehavior` — va à l'auberge, "consomme" une boisson (si auberge présente)
- [ ] `LookAtPlayerBehavior` — tourne la tête vers le joueur dans un rayon de 6 blocs

### 6.5 — Commerce avec le joueur

#### Bâtiment principal (Townhall)
- [ ] Interface de commerce complète (`TradeScreen`) — achat et vente d'items
- [ ] Catalogue `TradeGood` définis dans `normans.json` (blé, pain, pierre, outils, laine, bois...)
- [ ] Prix de base modifié par la réputation du joueur

#### Bâtiments spécialisés (shops)
- [ ] Chaque bâtiment `isShop = true` propose son propre catalogue (`TradeGood` filtré par building)
- [ ] Forge : outils, armes normandes
- [ ] Boulangerie : pain, nourriture
- [ ] Ferme : grains, végétaux, pommes
- [ ] Interface identique à celle du Townhall, catalogue réduit à la spécialité

#### Marchands étrangers
- [ ] Apparaissent au marché du village pour quelques jours (cooldown configurable)
- [ ] Vendent des goods importés (non produits localement) à prix différent
- [ ] Restent à leur stall (`ForeignMerchantKeepStallBehavior`) jusqu'à épuisement du stock

#### Système de prix et réputation
- [ ] Multiplicateur : Inconnu(×1.5) → Étranger(×1.2) → Ami(×1.0) → Allié(×0.9) → Chef(×0.8)
- [ ] Niveaux réputation : `UNKNOWN`, `STRANGER`, `FRIEND`, `ALLY`, `CHIEF`
- [ ] Gains : commerce, quêtes, dons, aide à la construction
- [ ] Pertes : vol, attaque, destruction de bâtiments
- [ ] Commande debug : `/mna reputation set <joueur> <villageId> <valeur>`

### 6.6 — Système de défense

- [ ] Points de défense définis dans les bâtiments de garde (template NBT)
- [ ] `DefendVillageBehavior` (priorité absolue) : tous les villageois avec `helpInAttacks = true` convergent vers le point de défense et attaquent la cible
- [ ] `HuntMonsterBehavior` : patrouilles actives, chasse les monstres proches du village
- [ ] Civils (`helpInAttacks = false`) : fuient vers le bâtiment central lors d'une attaque
- [ ] Réaction joueur agressif :
  - 1ère attaque → avertissement textuel
  - Récidive → gardes agressifs
  - Réputation < seuil → bannissement (refus de commerce et dialogue)

### 6.7 — Gestion des ressources du village

#### Points de ressource (définis dans les templates NBT des bâtiments)
- [ ] `fishingspots` — blocs marqueurs dans l'eau adjacente au bâtiment de pêche
- [ ] `sources` — blocs cibles à miner à la mine
- [ ] `soils` — blocs de terre labourable à la ferme
- [ ] `spawns` — points d'apparition animaux dans l'enclos
- [ ] `stalls` — comptoirs de vente au marché
- [ ] Les blocs marqueurs sont lus à l'initialisation du bâtiment et stockés dans `BuildingResManager`

#### Stock du village
- [ ] Items stockés dans les coffres des bâtiments (lus via `BuildingResManager.countGoods`)
- [ ] `ResourceStock` agrège un résumé au niveau du village (accessible au Chef + joueur)
- [ ] Limite `townhallLimit` pour éviter l'accumulation infinie
- [ ] Alerte en log (puis dialogue chef) si stock critique descend sous un seuil
- [ ] Commande debug : `/mna village stock <id>`

### 6.8 — Système de quêtes (infrastructure)

*Infrastructure complète ici. Contenu narratif et avancé en Phase 9.*

#### Modèle de données (JSON/Codec)
- [ ] `QuestDefinition` : `key`, `chancePerHour`, `maxSimultaneous`, `minReputation`, `steps[]`
- [ ] `QuestStep` : durée, donneur, objectif, récompense (items + deniers + réputation), textes i18n
- [ ] `QuestInstance` (runtime, CCA par joueur) : état, étape courante, deadline
- [ ] Système de tags : `playerTags`, `globalTags`, `villagerTags` — permettent des chaînes de quêtes

#### Types d'objectifs (`QuestObjective`)
- [ ] `DELIVER_ITEMS` — apporter des items à un NPC précis
- [ ] `BRING_BACK` — aller chercher des items et les rapporter
- [ ] `KILL_MOBS` — éliminer des mobs dans une zone ou un rayon
- [ ] `HELP_BUILD` — contribuer à la construction d'un bâtiment (poser N blocs)
- [ ] `VISIT_LOCATION` — se rendre à un endroit (autre village, position)
- [ ] `GATHER_RESOURCE` — récolter des ressources dans le monde

#### Donneurs de quêtes
- [ ] Flag `isQuestGiver = true` dans `VillagerTypeDef` désigne les donneurs
- [ ] Dialogue d'offre intégré à l'interface de dialogue (Phase 11)
- [ ] Filtrage par réputation minimum et tags requis

#### Récompenses
- [ ] Items + Deniers (monnaie normande)
- [ ] Points de réputation
- [ ] Tags globaux / joueur (débloquent la quête suivante dans une chaîne)

#### Quêtes normandes initiales (8 quêtes de base)
- [ ] "La première livraison" — apporter du blé au chef (DELIVER_ITEMS)
- [ ] "La forge a besoin de charbon" — livrer charbon au forgeron (DELIVER_ITEMS)
- [ ] "Bois pour construire" — ramener du bois de chêne (BRING_BACK)
- [ ] "Le troupeau a faim" — livrer du blé au berger (DELIVER_ITEMS)
- [ ] "Nuisibles au grenier" — tuer des monstres dans la cave (KILL_MOBS)
- [ ] "La route est dangereuse" — escorter un marchand (KILL_MOBS dans un rayon mobile)
- [ ] "Coup de main au chantier" — participer à la construction d'un bâtiment (HELP_BUILD)
- [ ] "Exploration" — trouver et visiter un autre village normand (VISIT_LOCATION)

**Livrable :** Villageois pleinement vivants — routines complètes, commerce multi-bâtiments, défense active, quêtes de base jouables.
**Tag :** `v0.7.0-alpha`

---

## Phase 7 — Santé des Bâtiments & Construction Dynamique

**Objectif :** Bâtiments qui prennent des dégâts, se dégradent visuellement, se réparent.

### 7.1 — Système de santé
- [ ] HP et maxHP stockés dans `Building.java`
- [ ] Système d'écoute des dégâts de blocs dans le rayon d'un bâtiment
- [ ] Calcul des HP selon les blocs détruits (ratio blocs restants / blocs originaux)
- [ ] Transitions d'état automatiques (INTACT → DAMAGED → RUINED → DESTROYED)

### 7.2 — Dégradation visuelle
- [ ] Remplacement progressif de blocs par des variantes "abîmées" (blocs craquelés, noircis)
- [ ] Blocs de décombres à l'état RUINED/DESTROYED
- [ ] Particules de poussière et sons à la transition d'état

### 7.3 — Réparation
- [ ] **Réparation villageois** : le BuildBehavior détecte les bâtiments endommagés et les répare
  - Coût : matériaux pris dans le stock du village
  - Vitesse : lente, plusieurs villageois peuvent collaborer
- [ ] **Réparation joueur** : clic droit avec des matériaux sur un bâtiment endommagé
  - Feedback visuel du coût de réparation
  - Gain de réputation

### 7.4 — Construction progressive
- [ ] À la génération : seul le bâtiment ancre est posé complet, les autres sont PLANNED
- [ ] Les villageois collectent des matériaux et construisent progressivement (couche par couche)
- [ ] Visualisation : Ghost blocks (blocs semi-transparents) aux emplacements planifiés
- [ ] `BuildingProject.java` — gère l'état d'un projet en cours

**Livrable :** Bâtiments qui se dégradent sous les attaques et se réparent.
**Tag :** `v0.8.0-alpha`

---

## Phase 8 — Économie & Commerce

**Objectif :** Commerce joueur-villageois fonctionnel, économie interne simulée.

### 8.1 — Modèle économique
- [ ] `ResourceStock.java` — stock agrégé par catégorie (pas item par item)
- [ ] Production simulée au tick selon les professions actives du village
- [ ] Consommation selon taille, état du village, événements

### 8.2 — Interface de commerce
- [ ] `TradeScreen.java` + `TradeScreenHandler.java`
- [ ] Offres du marchand basées sur le stock du village
- [ ] Prix dynamiques (offre/demande : manque de bois → prix du bois monte)
- [ ] Limite de stock quotidienne
- [ ] Restriction selon niveau de réputation

### 8.3 — Réputation
- [ ] Stockée par village + UUID joueur
- [ ] Niveaux : Inconnu → Étranger → Ami → Allié → Chef de Village
- [ ] Sources positives : commerce, quêtes, cadeaux, aide à la construction
- [ ] Sources négatives : vol, attaque, destruction de bâtiments
- [ ] `/mna reputation set <joueur> <village_id> <valeur>`

**Livrable :** Commerce fonctionnel, réputation influente.
**Tag :** `v0.9.0-alpha`

---

## Phase 9 — Quêtes

**Objectif :** Les villageois proposent des quêtes avec objectifs et récompenses.

### 9.1 — Infrastructure
- [ ] `QuestDefinition.java` (depuis JSON)
- [ ] `QuestInstance.java` (état par joueur)
- [ ] `QuestStep.java` (objectif individuel)
- [ ] Composant Cardinal `PlayerQuestData`

### 9.2 — Types d'objectifs
- [ ] `DELIVER_ITEMS` — apporter des items
- [ ] `KILL_MOBS` — tuer des mobs
- [ ] `HELP_BUILD` — contribuer à un bâtiment
- [ ] `EXPLORE` — visiter une position/village
- [ ] `GATHER` — récolter des ressources

### 9.3 — Interface de quête
- [ ] Dialogue villageois → proposition de quête
- [ ] `QuestJournalScreen.java` — journal des quêtes actives
- [ ] Toast de complétion / d'échec
- [ ] Récompenses : items + réputation

### 9.4 — Quêtes normandes
- [ ] 8-10 quêtes normandes variées (livraison, construction, protection, exploration)

**Livrable :** Quêtes normandes fonctionnelles de bout en bout.
**Tag :** `v0.10.0-alpha`

---

## Phase 10 — Creator Mode (Outil de Création In-Game)

**Objectif :** Permettre la création de cultures directement en jeu, avec export en datapack.

### 10.1 — Activation du mode créateur
- [ ] `/mna creator` — toggle le mode créateur (admin seulement)
- [ ] Visual feedback (particle d'activation, message dans le tchat)
- [ ] Débloque les commandes et outils de création

### 10.2 — Création de culture (GUI)
- [ ] `/mna creator culture new` → ouvre `CultureBuilderScreen`
  - **Page 1** : Nom, ID, langue de base
  - **Page 2** : Biomes compatibles (sélection visuelle)
  - **Page 3** : Types de villages à inclure
  - **Page 4** : Résumé + validation
- [ ] Génère un fichier JSON dans `config/millenaire_new_age/cultures/`

### 10.3 — Outil de scan de structure
- [ ] Item `StructureScannerWand`
- [ ] Clic gauche = corner 1, Clic droit = corner 2 → scan et sauvegarde `.nbt`
- [ ] Commande : `/mna creator structure scan <nom>` — sauvegarde la sélection
- [ ] Commande : `/mna creator structure list` — liste les structures sauvegardées

### 10.4 — Création de type de bâtiment
- [ ] `/mna creator building new` → GUI pour définir un bâtiment
  - Nom, structure associée, rôle, nombre de résidents, HP max
- [ ] Association structure NBT ↔ définition JSON

### 10.5 — Création de type de villageois
- [ ] `/mna creator villager new` → GUI pour définir un type de villageois
  - Nom, profession, behaviors associés, texture
- [ ] Sélection de texture depuis les ressources existantes ou upload

### 10.6 — Export en datapack
- [ ] `/mna creator export <culture_id>` — génère un dossier datapack complet
  - Structure : `data/<culture_id>/...` + `pack.mcmeta`
  - Peut être zippé et partagé directement
- [ ] Log du contenu exporté (JSON générés, structures incluses)

### 10.7 — Test en jeu du creator
- [ ] `/mna creator village test <type>` — génère un village test à la position du joueur
- [ ] `/mna creator reload` — recharge les données sans redémarrer le jeu

**Livrable :** Cycle complet : créer une culture simple → tester → exporter → réimporter.
**Tag :** `v0.11.0-alpha`

---

## Phase 11 — Interfaces Utilisateur

**Objectif :** Toutes les interfaces joueur sont complètes et polies.

### 11.1 — Interface village
- [ ] `VillageScreen` : infos, bâtiments, population, réputation, projets en cours

### 11.2 — Livre de voyage
- [ ] Item paginé : villages visités, fiches détaillées, navigation

### 11.3 — Autres interfaces
- [ ] Foyer (cuisson culture-spécifique)
- [ ] Coffre verrouillé (accès selon réputation)
- [ ] Dialogue villageois (base + branchement quêtes/commerce)
- [ ] Interface de configuration (Cloth Config)

### 11.4 — Notifications & HUD
- [ ] Toast "Village découvert"
- [ ] Toast de réputation (gain/perte)
- [ ] Toast de quête complétée/échouée
- [ ] Overlay HUD optionnel (réputation village proche)

**Livrable :** Toutes les interfaces accessibles et fonctionnelles.
**Tag :** `v0.12.0-alpha`

---

## Phase 12 — Réseau & Multijoueur

**Objectif :** Fonctionnement correct en multijoueur.

### 12.1 — Paquets réseau (Fabric Networking)
- [ ] `CustomPayload` records pour chaque type de sync
- [ ] S→C : données village au login
- [ ] S→C : état des villageois (position, animation)
- [ ] C→S : actions GUI (trade, quête)
- [ ] S→C : réputation mise à jour

### 12.2 — Séparation client/serveur
- [ ] Audit complet : aucun appel client en contexte serveur
- [ ] Tests avec `runServer` + client séparé

**Livrable :** Mod fonctionnel en multijoueur.
**Tag :** `v0.13.0-alpha`

---

## Phase 13 — Avancements & Progression

### 13.1 — Triggers custom
- [ ] `VillageDiscoveredTrigger`
- [ ] `TradeCompletedTrigger`
- [ ] `QuestCompletedTrigger`
- [ ] `ReputationReachedTrigger`

### 13.2 — Avancements normands
- [ ] "Premier pas" — s'approcher d'un village normand
- [ ] "Marchand" — premier échange
- [ ] "Quêteur" — première quête complétée
- [ ] "Bienfaiteur" — atteindre rang Allié
- [ ] "Bâtisseur" — aider à construire un bâtiment
- [ ] + avancements culturels normands (10+)

**Livrable :** Avancements normands complets.
**Tag :** `v0.14.0-alpha`

---

## Phase 14 — 2ème Culture (Validation du Système)

**Objectif :** Prouver que le système est générique en ajoutant une deuxième culture.
Culture choisie : **Byzantins** (architecture distincte, commerce avancé).

- [ ] Créer tous les JSON Byzantins (culture, villages, bâtiments, villageois)
- [ ] Créer les structures NBT des bâtiments byzantins
- [ ] Textures byzantines (depuis OldSource)
- [ ] Quêtes byzantines (5+)
- [ ] Avancements byzantins
- [ ] Corriger tout problème de généricité découvert

**Livrable :** Deux cultures jouables, système prouvé générique.
**Tag :** `v0.15.0-alpha`

---

## Phase 15 — Extensions Post-v1.0

À traiter après la release initiale :
- [ ] **Bandits & Raiders** — attaques de villages, système de défense
- [ ] **Villages contrôlés par le joueur** — devenir chef, prendre des décisions
- [ ] **Diplomatie** — relations entre cultures, commerce inter-villages, guerres
- [ ] **Cultures supplémentaires** — Japonais, Indiens, Inuits, Mayas, Seldjoukides
- [ ] **Structures Nether/End** — ruines, avant-postes de cultures disparues
- [ ] **Saisons** — compatibilité Serene Seasons
- [ ] **Compatibilité mods** — Create, etc.

---

## Phase 16 — Polish, Tests & Release

### 16.1 — Tests
- [ ] Scénario complet Normands + Byzantins
- [ ] Tests Creator Mode (créer une culture simple)
- [ ] Tests performance (15+ villages actifs)
- [ ] Tests multijoueur

### 16.2 — Optimisation
- [ ] Profiling (IntelliJ Profiler)
- [ ] Optimisation hibernation des villages inactifs
- [ ] Optimisation pathfinding

### 16.3 — Documentation
- [ ] `README.md` — installation, présentation
- [ ] Guide gameplay (Modrinth/wiki)
- [ ] **Guide Creator** — créer une culture custom (priorité communauté)
- [ ] Changelog

### 16.4 — Release
- [ ] `v1.0.0-beta`
- [ ] Publication **Modrinth** (priorité) + CurseForge
- [ ] Page de présentation avec screenshots

---

## Tableau de suivi

| Phase | Statut | Tag |
|-------|--------|-----|
| 0 — Setup | 🟢 Terminé | v0.1.0 |
| 1 — Architecture données | 🟢 Terminé | v0.2.0 |
| 2 — Système cultures | 🟢 Terminé | v0.3.0 |
| 3 — Types villages & génération | 🟢 Terminé | v0.4.0 |
| 4 — Assets Normands | 🟡 En cours (autre contrib.) | v0.5.0 |
| 5 — Entité Villageois & rendu | 🔴 À faire | v0.6.0 |
| 6 — IA, Comportements, Commerce & Quêtes | 🔴 À faire | v0.7.0 |
| 7 — Santé bâtiments & construction dyn. | 🔴 À faire | v0.8.0 |
| 8 — Économie avancée (inter-villages) | 🔴 À faire | v0.9.0 |
| 9 — Quêtes avancées & journal | 🔴 À faire | v0.10.0 |
| 10 — Creator Mode | 🟡 En cours (Bloc A terminé) | v0.11.0 |
| 11 — Interfaces utilisateur | 🔴 À faire | v0.12.0 |
| 12 — Réseau & multijoueur | 🔴 À faire | v0.13.0 |
| 13 — Avancements | 🔴 À faire | v0.14.0 |
| 14 — 2ème culture (Byzantins) | 🔴 À faire | v0.15.0 |
| 15 — Extensions Post-v1.0 | ⏸ Plus tard | — |
| 16 — Polish & Release | 🔴 À faire | v1.0.0 |

**Légende :** 🔴 À faire | 🟡 En cours | 🟢 Terminé | ⚫ Bloqué | ⏸ Plus tard
