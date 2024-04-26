# Complex Search Engine

<img src="./res/adp.png" alt="ADP" width="70"/>

## Introduction

This repository contains the traces of my work done during my apprenticeship at ADP.

> The entire README is written in English, but also available in French in the file [README.md](./README.md).

---

> L'entièreté du README est rédigé en anglais, mais également disponible en français dans le fichier [README.md](./README.md).

---

> The main content of this repository is found in the technical documentation, available in the `docs/` folder at the root of the project.

## Table of Contents

- [Complex Search Engine](#complex-search-engine)
  - [Introduction](#introduction)
  - [Table of Contents](#table-of-contents)
  - [Keywords](#keywords)
  - [Context](#context)
  - [Objective](#objective)
    - [Goal](#goal)
  - [Notes - Anonymization](#notes---anonymization)
  - [Documentation](#documentation)
  - [Technologies used](#technologies-used)
  - [Analysis](#analysis)
    - [Identification of problems](#identification-of-problems)
      - [Improving the decoder](#improving-the-decoder)
      - [Search engine mutualization](#search-engine-mutualization)
      - [Wildcard implementation, search optimization](#wildcard-implementation-search-optimization)
      - [Extension of the Search Engine](#extension-of-the-search-engine)
  - [Acknowledgements](#acknowledgements)
  - [License](#license)

## Keywords

- Java
- Oracle SQL
- Search Engine
- Refactoring
- Regex
- Performance Analysis
- Concurrency
- Multithreading
- Factory Pattern
- Complex SQL Query

## Context

I did my apprenticeship at **ADP**, a company specialized in payroll and human resources management. I worked in a development team, and I was brought to work on a complete overhaul project of a search engine system that was starting to get old.

## Objective

The objective of this project was to set up a search engine for expressions in BLOBs stored in a database.

The purpose of this project was to improve the performance of the search engine, to mutualize it for other uses, and also to implement the use of wildcards in searches.

### Goal

The main goal of this repository is to keep a trace of my work, and to be able to show it to people outside the company later. It has no functional value, and cannot be used as is. It is simply intended to showcase the major parts of this custom search engine.

## Notes - Anonymization

The data used in this project is anonymized data and does not represent real data. The names of tables, internal classes and algorithms / internal resources have been erased or modified for confidentiality reasons. The purpose of this repository is to show the engine created for the occasion, working independently of the data. Everything related to database access has been deleted, and modified for obvious security reasons.
The two applications have been renamed to `Apple` and `Pear` for any reference to real applications.

## Documentation

The entire technical documentation of the project is available and allows to navigate in the source code to target what is interesting to see. It is available in the `docs/` folder at the root of the project. We can also find the user documentation written for the occasion.

## Technologies used

- Java 8
- Oracle SQL

## Analysis

### Identification of problems

The first step of the refactoring was to identify the essential steps of the old search, and then to target the elements causing performance problems. I was able to target two distinct systems, the search system itself, and the BLOB decoding system.
We find these two elements in the package `src/decoder`and `src/rechercherExpression`.

#### Improving the decoder

The improvements regarding BLOB decoding were carried out in two distinct parts. The first involved rewriting the decoder, which is unavailable here for confidentiality reasons. The rewriting of the `byte[]` converters into other types can be found in the `src/decoder/Converter.java` source file, highlighting the enhanced operations on the bytes.
The second step was to write a system to take advantage of multithreading to decrypt multiple BLOBs simultaneously, using a [Database Connection Pool](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ExecutorService.html) to manage the threads. This system can be found in the `src/decoder/DecoderMultiple.java` source.

Additionally, the implementation of a cache between threads significantly reduced the decoding time of BLOBs by avoiding re-decoding pieces of BLOBs that had already been decoded.

#### Search engine mutualization

The search engine has been rewritten to be more generic, allowing for searching expressions in any context. Using the src/rechercherExpression/ContexteRecherche.java class, we can define a search context and customize it for each use.

This context can then be provided to a RechercheFactory ([Factory Pattern](https://en.wikipedia.org/wiki/Factory_method_pattern)) to obtain a ready-to-use search engine.

> This results in a search engine that is ready-to-use and customizable for each use.

#### Wildcard implementation, search optimization

Adding wildcards was a challenge as it required implementing them without impacting the search engine's performance. I implemented a wildcard search system using regular expressions and optimized them to ensure minimal impact on search engine performance. The generation of regular expressions is handled in the `src/rechercherExpression/solutionRecherche/ExpressionMatcher.java` class.

> More details on wildcard implementation can be found in the technical documentation.

#### Extension of the Search Engine

Once the results are available, the application utilizing them must leverage them within the environment and context in which these solutions are used. This is significantly more tailored to the application and required a vast number of SQL queries to obtain extended results. Therefore, I engineered a massive yet optimized SQL query to obtain extended results in a single query and utilize them in the application, as detailed in the technical documentation.

## Acknowledgements

I would like to thank the development team at ADP for allowing me to work on this project and giving me the opportunity to build a project of this magnitude, with great freedom on architecture and technical choices.

## License

This project isn't authorized for use, reproduction, or distribution. It is intended for educational purposes only. All rights reserved.
