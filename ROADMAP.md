# Millenaire: New Age — Feuille de Route

> **Mod :** Millenaire: New Age
> **Framework :** Fabric (Fabric Loader + Fabric API)
> **Cible :** Minecraft Java Edition 1.21.10
> **Philosophie :** Moteur générique de civilisations + civilisation Normande comme référence.
> **OldSource :** Concepts, mécaniques, assets visuels uniquement. Aucun code repris.

---

## Vision du projet

Un **moteur de civilisations** extensible par la communauté :
- Le mod est le moteur (IA, village, bâtiments, économie, quêtes)
- Les civilisations sont des **datapacks** — partageables, sans code
- Les civilisations nécessitant de nouveaux items/blocs utilisent un **mod compagnon** minimal
- La civilisation **Normande** est la référence intégrée au mod de base

### Modèle de données

```
Civilization (données JSON/datapack)
  ├── VillageType[]      → hameau, village, bourg, forteresse, monastère...
  │     └── BuildingType[] → maison, forge, mairie, tour de guet...
  │           └── StructureTemplate (.nbt)
  ├── VillagerType[]     → paysan, forgeron, garde, chef, marchand...
  ├── TradeGoodDef[]     → biens de commerce par catégorie
  ├── QuestDef[]         → quêtes disponibles
  ├── Language           → noms de villageois, de villages, dialogues
  └── ItemSet            → crops, nourriture, décorations

Village (instance runtime, persistée par monde)
  ├── civilization, type, nom, position
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
  ├── civilization, type, village
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

1. **Items génériques** intégrés au mod (cultures, nourriture, matériaux) — utilisables par toutes les civilisations via retexture
2. **Items Normands** intégrés directement (civilisation de base)
3. **Items civilisations externes** — via mod compagnon déclarant ses items à l'API

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
Phase 2  │ Système de civilisations (datapack)         │ ~3 semaines
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
Phase 14 │ 2ème civilisation (validation du système)   │ ~4 semaines
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
├── civilization/               ← Modèle de données civilisation
│   ├── Civilization.java
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

**Objectif :** Les classes Java qui représentent les concepts de civilisation, village, bâtiment.
Ces classes sont le **squelette** sur lequel tout le reste s'appuie.

### 1.1 — Civilization (données immuables, chargées depuis JSON)
```java
public record Civilization(
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
- [x] `Civilization.java`
- [x] `VillageType.java` (hameau, village, bourg, forteresse, monastère)
- [x] `BuildingType.java` (mairie, maison, forge, ferme, tour, mur...)
- [x] `VillagerTypeDef.java` (paysan, forgeron, garde, chef, marchand...)
- [x] `CultureLanguage.java` (pools de noms, dialogues)
- [x] `TradeGoodDef.java`
- [x] `CivilizationRegistry.java` — registre des civilisations chargées

### 1.2 — Village (état runtime, persisté)
```java
public class Village {
    UUID id;
    String name;
    Civilization civilization;
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
- [ ] Test de registry des civilisations

**Livrable :** Classes compilées, log de debug affichant les structures de données.
**Tag :** `v0.2.0-alpha`

---

## Phase 2 — Système de Civilisations (Datapack)

**Objectif :** Charger les civilisations depuis des datapacks. Normands intégrés comme référence.

### 2.1 — Format JSON des civilisations
```json
// data/millenaire_new_age/civilizations/normans.json
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
  "civilization": "normans",
  "display_name": { "fr_fr": "Village Normand", "en_us": "Norman Village" },
  "min_buildings": 8,
  "max_buildings": 15,
  "required_buildings": ["normans:townhall"],
  "optional_buildings": ["normans:house", "normans:forge", "normans:farm", "normans:chapel"],
  "has_walls": false,
  "villager_types": ["normans:farmer", "normans:blacksmith", "normans:guard", "normans:chief"]
}
```
- [x] Schéma JSON embarqué dans `civilization/<id>.json` : language, village_types, building_types, villager_types, trade_goods, known_crops
  - Note : format actuel = types embarqués dans la civilisation. Refactorisable en fichiers séparés si besoin.

### 2.2 — Chargeur datapack (ResourceReloadListener + Codec)
- [x] `CivilizationLoader.java` — lit tous les `data/*/civilization/*.json` via `Civilization.CODEC`
- [x] Support multi-datapacks (plusieurs civilisations en parallèle)
- [x] Validation automatique par Codec + messages d'erreur clairs dans les logs
- [x] API publique : `MilenaireApi.registerCivilization(...)` pour les mods compagnons

### 2.3 — Civilisation Normande intégrée
- [x] `normans.json` — définition principale complète
- [x] Types de villages : hameau (3-5), village (8-15), forteresse (militaire)
- [x] Types de bâtiments : mairie, maison, forge, ferme, chapelle, donjon, caserne
- [x] Types de villageois : paysan, forgeron, garde, chef, marchand
- [x] Langue : noms historiques normands (Guillaume, Mathilde…) + villes (Caen, Rouen…)
- [x] Catalogue de commerce normand (blé, pain, pierre, outil, laine)

### 2.4 — Commandes de debug civilisation
- [x] `/mna civilization list` — liste les civilisations chargées
- [x] `/mna civilization info <id>` — détails d'une civilisation

**Livrable :** Log de chargement des civilisations OK, structures JSON définies.
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
- [x] Envoi payload S→C avec la liste des civilisations disponibles
- [x] Vérification `villageSpacing` — message d'erreur rouge si trop proche
- [x] Bloc d'or supprimé après création

#### GUI `VillageCreationScreen`
- [x] Onglets par civilisation (un onglet = une civilisation chargée)
- [x] Dans chaque onglet : liste des types de villages avec infos (nb bâtiments, murailles)
- [x] Bouton "Créer" → envoie `CreateVillagePayload` au serveur
- [x] Bouton "Annuler" → ferme le GUI sans action

#### `VillagePlacer.java`
- [x] Reçoit : civilisation, type de village, position centrale (bloc d'or)
- [x] Détermine les bâtiments à placer (requis + aléatoire parmi optionnels)
- [x] Tri par proximity (CENTER → NEAR → FAR), centrage du bâtiment principal sur le bloc d'or
- [x] `TerrainAdapter` — nivelle le terrain (creuse / remblaye / nettoie végétation)
- [x] Place chaque bâtiment via `StructureSaveManager.placeStructure()`
- [x] Crée les instances `Village` + `Building` et les enregistre dans `VillageManager`
- [x] Indicateur de nom flottant (ArmorStand invisible + nom en jaune gras)
- [x] `normans.json` mis à jour : fortress utilise les 4 structures enregistrées

#### Réseau
- [x] `OpenVillageCreationPayload` (S→C) — liste des civilisations pour le GUI
- [x] `CreateVillagePayload` (C→S) — demande de création (civ + type + pos)

#### Test : forteresse normande
- [x] 4 bâtiments placés autour du bloc d'or cliqué
- [x] Instance `Village` créée et accessible via `VillageManager`
- [x] Messages de confirmation en jeu

### 3.3 — Persistance des villages
**Objectif :** Les villages survivent au redémarrage du monde.

- [ ] Vérifier sérialisation complète `Village` + `Building` via Cardinal Components
- [ ] Test : créer un village → quitter → recharger → village toujours présent
- [ ] Commandes de debug :
  - [ ] `/mna village list` — liste tous les villages du monde courant
  - [ ] `/mna village info` — infos sur le village le plus proche du joueur
  - [ ] `/mna village tp <id>` — téléporter vers un village
  - [ ] `/mna village remove <id>` — supprimer un village (debug)

### 3.4 — Génération naturelle (Worldgen Fabric)
**Objectif :** Villages générés automatiquement selon le biome.

- [ ] Algorithme de sélection biome/civilisation
- [ ] Distance minimale entre villages (lire `village_spacing` depuis config)
- [ ] Détection terrain plat (rayon = `village_size / 2`)
- [ ] Enregistrement Structure/Feature Fabric
- [ ] Placement au chunkload (sans conflits de génération)
- [ ] `normans:hamlet` dans biomes plaines/forêt
- [ ] `normans:village` dans biomes plaines/prairie
- [ ] `normans:fortress` dans biomes collines/montagne

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
Ces items sont le socle commun que toutes les civilisations peuvent utiliser :
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

**Livrable :** Écosystème visuel et matériel complet pour la civilisation de référence.
**Tag :** `v0.5.0-alpha`

---

## Phase 5 — Entité Villageois & Rendu

**Objectif :** Les villageois existent avec leur apparence, appartiennent à un village.
**Référence OldSource :** `client/render/`, `common/entity/` — concepts visuels uniquement.

### 5.1 — Entité de base (côté serveur)
- [ ] `MillVillagerEntity.java` extends `PathAwareEntity`
- [ ] Attributs : HP, vitesse, portée de vision, force
- [ ] NBT persistant : `civilizationId`, `villagerTypeId`, `villageId`, `sex`, `name`
- [ ] Enregistrement EntityType + SpawnEgg (debug)

### 5.2 — Rendu (côté client uniquement)
- [ ] Modèle humanoïde custom (ou couche sur biped vanilla)
- [ ] Renderer `MillVillagerEntityRenderer`
- [ ] Layer de vêtements par type de villageois + civilisation
- [ ] Textures normandes récupérées de OldSource
- [ ] Nametag avec nom + rôle (ex: "Guillaume — Forgeron")

### 5.3 — Intégration village
- [ ] Spawn à la génération du village selon les types définis
- [ ] Attribution type/village/nom (via CultureLanguage)
- [ ] Assignation logement (home building) + lieu de travail (workplace)

### 5.4 — Interaction joueur de base
- [ ] Clic droit → ouvre interface de dialogue (placeholder Phase 11)
- [ ] Réaction basique selon réputation (accueil / méfiance)

**Livrable :** Villageois normands visibles avec bonne apparence, nommés correctement.
**Tag :** `v0.6.0-alpha`

---

## Phase 6 — IA & Comportements (Améliorée)

**Objectif :** IA crédible et réactive, nettement meilleure que l'original.
**Référence OldSource :** `common/goal/` — comprendre les comportements, réécrire avec Brain API.

### 6.1 — Architecture Brain API
- [ ] `MillVillagerBrain.java` configure le Brain
- [ ] Mémoires : `HOME_POS`, `WORK_POS`, `CURRENT_STATE`, `NEAREST_PLAYER`, `THREAT_LEVEL`
- [ ] Sensors : `NearestPlayerSensor`, `NearestVillagerSensor`, `ThreatSensor`
- [ ] Schedules (horaires) : WORK_SCHEDULE, SLEEP_SCHEDULE, GUARD_SCHEDULE

### 6.2 — Behaviors de base
- [ ] `WanderAroundVillageBehavior` — déambulation dans le périmètre
- [ ] `SleepAtHomeBehavior` — dormir la nuit dans le home building
- [ ] `LookAtPlayerBehavior` — interaction passive avec le joueur
- [ ] `ReturnHomeBehavior` — rentrer avant la nuit
- [ ] `SocialInteractBehavior` — interagir avec les autres villageois

### 6.3 — Behaviors de travail par profession
- [ ] `FarmBehavior` — labourer, planter, récolter
- [ ] `ChopWoodBehavior` — couper, replanter
- [ ] `SmithBehavior` — produire des outils/armes (simulation)
- [ ] `MerchantBehavior` — gérer le stock, accueillir les joueurs
- [ ] `GuardPatrolBehavior` — patrouille avec alertes
- [ ] `ChiefBehavior` — supervise les projets de construction, prend les décisions
- [ ] `BuildBehavior` — contribue aux projets de construction en cours

### 6.4 — Cycle jour/nuit structuré
- [ ] `WAKING (6h-7h)` → `WORKING (7h-18h)` → `RETURNING (18h-20h)` → `SLEEPING (20h-6h)`
- [ ] Variantes par profession (gardes font des quarts de nuit)
- [ ] Variation selon la météo (villageois rentrent sous la pluie)

### 6.5 — Réactions et alertes
- [ ] Détection de menace (monstres, bandits) → alerte village
- [ ] Comportement de fuite par profession (civils fuient, gardes défendent)
- [ ] Comportement de réparation d'urgence (si bâtiment RUINED)
- [ ] Réaction à la réputation joueur (méfiance, bienvenue)

**Livrable :** Villageois qui travaillent, dorment, réagissent, varient par profession.
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

**Objectif :** Permettre la création de civilisations directement en jeu, avec export en datapack.

### 10.1 — Activation du mode créateur
- [ ] `/mna creator` — toggle le mode créateur (admin seulement)
- [ ] Visual feedback (particle d'activation, message dans le tchat)
- [ ] Débloque les commandes et outils de création

### 10.2 — Création de civilisation (GUI)
- [ ] `/mna creator civilization new` → ouvre `CivilizationBuilderScreen`
  - **Page 1** : Nom, ID, langue de base
  - **Page 2** : Biomes compatibles (sélection visuelle)
  - **Page 3** : Types de villages à inclure
  - **Page 4** : Résumé + validation
- [ ] Génère un fichier JSON dans `config/millenaire_new_age/civilizations/`

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
- [ ] `/mna creator export <civilization_id>` — génère un dossier datapack complet
  - Structure : `data/<civilization_id>/...` + `pack.mcmeta`
  - Peut être zippé et partagé directement
- [ ] Log du contenu exporté (JSON générés, structures incluses)

### 10.7 — Test en jeu du creator
- [ ] `/mna creator village test <type>` — génère un village test à la position du joueur
- [ ] `/mna creator reload` — recharge les données sans redémarrer le jeu

**Livrable :** Cycle complet : créer une civilisation simple → tester → exporter → réimporter.
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

## Phase 14 — 2ème Civilisation (Validation du Système)

**Objectif :** Prouver que le système est générique en ajoutant une deuxième civilisation.
Civilisation choisie : **Byzantins** (architecture distincte, commerce avancé).

- [ ] Créer tous les JSON Byzantins (culture, villages, bâtiments, villageois)
- [ ] Créer les structures NBT des bâtiments byzantins
- [ ] Textures byzantines (depuis OldSource)
- [ ] Quêtes byzantines (5+)
- [ ] Avancements byzantins
- [ ] Corriger tout problème de généricité découvert

**Livrable :** Deux civilisations jouables, système prouvé générique.
**Tag :** `v0.15.0-alpha`

---

## Phase 15 — Extensions Post-v1.0

À traiter après la release initiale :
- [ ] **Bandits & Raiders** — attaques de villages, système de défense
- [ ] **Villages contrôlés par le joueur** — devenir chef, prendre des décisions
- [ ] **Diplomatie** — relations entre civilisations, commerce inter-villages, guerres
- [ ] **Civilisations supplémentaires** — Japonais, Indiens, Inuits, Mayas, Seldjoukides
- [ ] **Structures Nether/End** — ruines, avant-postes de civilisations disparues
- [ ] **Saisons** — compatibilité Serene Seasons
- [ ] **Compatibilité mods** — Create, etc.

---

## Phase 16 — Polish, Tests & Release

### 16.1 — Tests
- [ ] Scénario complet Normands + Byzantins
- [ ] Tests Creator Mode (créer une civilisation simple)
- [ ] Tests performance (15+ villages actifs)
- [ ] Tests multijoueur

### 16.2 — Optimisation
- [ ] Profiling (IntelliJ Profiler)
- [ ] Optimisation hibernation des villages inactifs
- [ ] Optimisation pathfinding

### 16.3 — Documentation
- [ ] `README.md` — installation, présentation
- [ ] Guide gameplay (Modrinth/wiki)
- [ ] **Guide Creator** — créer une civilisation custom (priorité communauté)
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
| 2 — Système civilisations | 🟢 Terminé | v0.3.0 |
| 3 — Types villages & génération | 🟡 En cours (3.1 ✅) | v0.4.0 |
| 4 — Assets Normands | 🟡 En cours | v0.5.0 |
| 5 — Entité Villageois & rendu | 🔴 À faire | v0.6.0 |
| 6 — IA & comportements | 🔴 À faire | v0.7.0 |
| 7 — Santé bâtiments & construction | 🔴 À faire | v0.8.0 |
| 8 — Économie & commerce | 🔴 À faire | v0.9.0 |
| 9 — Quêtes | 🔴 À faire | v0.10.0 |
| 10 — Creator Mode | 🟡 En cours (Bloc A terminé) | v0.11.0 |
| 11 — Interfaces utilisateur | 🔴 À faire | v0.12.0 |
| 12 — Réseau & multijoueur | 🔴 À faire | v0.13.0 |
| 13 — Avancements | 🔴 À faire | v0.14.0 |
| 14 — 2ème civilisation (Byzantins) | 🔴 À faire | v0.15.0 |
| 15 — Extensions Post-v1.0 | ⏸ Plus tard | — |
| 16 — Polish & Release | 🔴 À faire | v1.0.0 |

**Légende :** 🔴 À faire | 🟡 En cours | 🟢 Terminé | ⚫ Bloqué | ⏸ Plus tard
