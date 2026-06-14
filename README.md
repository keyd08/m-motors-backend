# M-Motors Backend

Backend Java Spring Boot de la plateforme M-Motors.

Ce projet fait partie du développement d'une solution digitale pour M-Motors, dans le cadre du bloc **Développement d'une solution digitale avec Java**.

## Objectif

L'objectif du backend est de fournir une API REST permettant de gérer :

* les utilisateurs ;
* les véhicules disponibles à l'achat ou à la location ;
* les dossiers d'achat ou de location ;
* les documents justificatifs liés aux dossiers ;
* les rôles client et administrateur ;
* le suivi et la validation des dossiers ;
* la surveillance technique de l'application.

## Technologies utilisées

* Java 17
* Spring Boot
* Spring Web
* Spring Data JPA
* Spring Security
* PostgreSQL
* H2 pour les tests
* Maven
* JUnit
* Mockito
* Spring Boot Actuator

## Fonctionnalités principales

Le backend permet notamment de :

* consulter les véhicules disponibles ;
* filtrer les véhicules selon leur mode : achat ou location ;
* créer et administrer des véhicules ;
* créer un dossier d'achat ou de location côté client ;
* suivre l'avancement d'un dossier ;
* consulter et mettre à jour les dossiers côté administration ;
* ajouter des documents justificatifs à un dossier ;
* consulter les documents liés à un dossier ;
* sécuriser les accès selon les rôles `CLIENT` et `ADMIN` ;
* exposer des endpoints de surveillance.

## Comptes de démonstration

Les comptes suivants sont créés automatiquement au démarrage de l'application si les données n'existent pas encore :

| Rôle   | Email                                             | Mot de passe |
| ------ | ------------------------------------------------- | ------------ |
| ADMIN  | [admin@mmotors.demo](mailto:admin@mmotors.demo)   | Admin123!    |
| CLIENT | [client@mmotors.demo](mailto:client@mmotors.demo) | Client123!   |

## Lancement des tests

```bash
./mvnw test
```

## Déploiement

Le backend peut être déployé avec un profil de production Spring Boot.

### Profil de production

Pour lancer l'application en production :

```bash
SPRING_PROFILES_ACTIVE=prod
```

### Variables d'environnement nécessaires

```bash
DATABASE_URL=jdbc:postgresql://host:port/database
DATABASE_USERNAME=nom_utilisateur
DATABASE_PASSWORD=mot_de_passe
PORT=8080
APP_CORS_ALLOWED_ORIGINS=https://url-du-frontend
```

### Base de données

En développement et pendant les tests, le projet utilise H2.

En production, le backend est prévu pour être connecté à une base PostgreSQL via les variables d'environnement définies ci-dessus.

### Endpoints de surveillance

```text
GET /actuator/health
GET /api/monitoring/status
```

L'endpoint `/actuator/health` permet de vérifier l'état technique de l'application.

L'endpoint `/api/monitoring/status` permet de vérifier le statut applicatif côté administration.

## Sécurité

La sécurité est gérée avec Spring Security.

* Les endpoints publics permettent la consultation des véhicules.
* Les actions d'administration sont réservées au rôle `ADMIN`.
* Les actions liées aux dossiers et documents côté client sont réservées au rôle `CLIENT`.
* Les mots de passe sont encodés avec BCrypt.
* Les erreurs sont centralisées via un gestionnaire global d'exceptions.

## Organisation Git

Le développement est organisé avec des branches de fonctionnalités :

* `main` : branche stable principale ;
* `develop` : branche d'intégration ;
* `feature/...` : branches dédiées à chaque évolution fonctionnelle ou technique.

Chaque fonctionnalité est intégrée dans `develop` via une Pull Request.
