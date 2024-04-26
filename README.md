# Moteur de recherche complexe

<img src="./img/adp.png" alt="ADP" width="70"/>

## Introduction

Ce repository contient les traces de mon travail effectué dans le cadre de mon alternance chez ADP.

> L'entièreté du README est rédigé en français, mais également disponible en anglais dans le fichier [README_EN.md](./README_EN.md).

---

> The entire README is written in French, but also available in English in the file [README_EN.md](./README_EN.md).

## Table des matières

- [Moteur de recherche complexe](#moteur-de-recherche-complexe)
  - [Introduction](#introduction)
  - [Table des matières](#table-des-matières)
  - [Mots clés](#mots-clés)
  - [Contexte](#contexte)
  - [Objectif](#objectif)
    - [But](#but)
  - [Notes - Anonymisation](#notes---anonymisation)
  - [Documentation](#documentation)
  - [Technologies utilisées](#technologies-utilisées)
  - [Analyse](#analyse)
    - [Identification des problèmes](#identification-des-problèmes)
      - [Amélioration du décodeur](#amélioration-du-décodeur)
      - [Mutualisation du moteur de recherche](#mutualisation-du-moteur-de-recherche)
      - [Ajout des jokers, optimisation des recherches](#ajout-des-jokers-optimisation-des-recherches)
      - [Extension du moteur de recherche](#extension-du-moteur-de-recherche)
  - [Remerciements](#remerciements)
  - [Licence](#licence)

## Mots clés

- Java
- Oracle SQL
- Moteur de recherche
- Refactoring
- Regex
- Performance Analysis
- Concurrency
- Multithreading
- Factory Pattern
- Complex SQL Query

## Contexte

J'ai effectué, au cours de ma seconde et troisième année d'étude en informatique, mon alternance chez **ADP**, une entreprise spécialisée dans la gestion de la paie et des ressources humaines. J'ai travaillé dans une équipe de développement, et j'ai été amené à travailler sur un projet de refonte complète d'un système de recherche d'expressions qui commençait à dater.

## Objectif

L'objectif de ce projet étant de mettre en place un moteur de recherche d'expressions dans des BLOBs stockés dans une base de données.

Le but de ce projet étant d'améliorer les performances du moteur de recherche, de le mutualiser pour d'autres utilisations, et également d'implémenter l'utilisation de joker (wildcard) dans les recherches.

### But

Le but premier de ce repository étant de pouvoir garder un trace de mon travail, et de pouvoir le montrer à des personnes extérieures à l'entreprise par la suite. Il n'a aucune valeur fonctionnelle, et ne peut pas être utilisé tel quel. Il a pour simple but de faire vitrine des parties majeures de ce moteur de recherche sur-mesure.

## Notes - Anonymisation

Les données utilisées dans ce projet sont des données anonymisées et ne représentent pas des données réelles. Les noms des tables, des classes internes et algorithmes / ressources internes ont été effacés ou modifiés pour des raisons de confidentialité. Le but de ce repository étant de montrer le moteur créé pour l'occasion, fonctionnant indépendamment des données. Tout ce qui concerne les accès à la base de données a été supprimé, et modifié pour des raisons évidentes de sécurité.
Les deux applications ont été renommées en `Apple` et `Pear` pour toute référence à des applications réelles.

## Documentation

L'entièreté de la documentation technique du projet est disponible et permet de se diriger dans le code source pour cibler ce qui est intéressant à voir. Elle est disponible dans le dossier `docs/` à la racine du projet. On peut également y retrouver la documentation utilisateur rédigée pour l'occasion.

## Technologies utilisées

- Java 8
- Oracle SQL

## Analyse

### Identification des problèmes

L'étape première du refactoring a été d'identifier les étapes essentielles de l'ancienne recherche, et ensuite de cibler les éléments causant des problèmes de performance. J'ai pu cibler deux systèmes distincts, le système de recherche lui même, et celui de décodage des BLOBs.
On retrouve ces deux éléments dans le package `src/decoder`et `src/rechercherExpression`.

#### Amélioration du décodeur

Les améliorations apportées concernant le décodage des BLOBs se sont faites en deux morceaux distincts. La première étant de réécrire le décodeur, ici indisponible pour des raisons de confidentialité. On retrouvera la réécritures des convertisseurs de `byte[]` en d'autres types dans le source `src/decoder/Converter.java` soulignant les opérations améliorées sur les bytes.
La seconde étape a été d'écrire un système permettant de profiter du multithreading pour déchiffrer plusieurs BLOBs simultanément, en usant d'un [Pool de Connexion de base de données](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html) pour gérer les threads. On retrouvera ce système dans le source `src/decoder/DecoderMultiple.java`.
Également, l'implémentation d'un cache entre les threads a permis de réduire drastriquement le temps de décodage des BLOBs, en évitant de re-décoder des morceaux de BLOBs déjà décodés.

#### Mutualisation du moteur de recherche

Le moteur de recherche a été réécrit pour être plus générique, et permettre de rechercher des expressions dans n'importe quel contexte. à l'aide de la classe `src/rechercherExpression/ContexteRecherche.java`, on peut définir un contexte de recherche, et le personnaliser pour chaque utilisation.
Ce contexte pourra alors être donné dans un `RechercheFactory` ([Factory Pattern](https://en.wikipedia.org/wiki/Factory_method_pattern)) pour obtenir un moteur de recherche prêt à l'emploi.

> On obtient alors un moteur de recherche prêt à l'emploi, et personnalisable pour chaque utilisation.

#### Ajout des jokers, optimisation des recherches

L'ajout des jokers a été un défi, car il fallait les implémenter sans impacter les performances du moteur de recherche. J'ai donc implémenté un système de recherche de jokers, en utilisant des expressions régulières pour les recherches, et en les optimisant pour ne pas impacter les performances du moteur de recherche. La génération des expressions régulières se fait dans la classe `src/rechercherExpression/solutionRecherche/ExpressionMatcher.java`.

> Plus de détails sur l'implémentation des jokers dans la documentation technique.

#### Extension du moteur de recherche

Une fois les résultats disponible, l'application qui les utilise doit les exploiter dans l'environnement et le contexte dans lequel ces solutions sont utilisées. Cela est nettement plus propre à l'application et nécessitait un nombre immense de requêtes SQL pour obtenir les résultats étendus. J'ai donc ingénieré une requête SQL imposante mais optimisée pour obtenir les résultats étendus en une seule requête, et les exploiter dans l'application, trouvable dans la documentation technique.

## Remerciements

Je tiens à remercier l'équipe de développement chez ADP pour m'avoir permis de travailler sur ce projet, et de m'avoir donné l'opportunité de monter un projet de cette envergure, en ayant une grande liberté sur l'architecture et les choix techniques.

## Licence

Ce projet n'est pas autorisé à être utilisé, copié, ou distribué sans l'autorisation de l'auteur.
