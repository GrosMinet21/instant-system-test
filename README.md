# Instant System — Parking API

API REST Spring Boot exposant la disponibilité en temps réel des parkings à proximité d'une position géographique.

**Temps consacré :** ~X heures

---

## Lancement rapide

```bash
mvn spring-boot:run
```

L'application démarre sur le port **8080** avec une base PostgreSQL embarquée (Zonky).  
Swagger UI disponible sur : `http://localhost:8080/swagger-ui/index.html`

---

## API

```
GET /parkings?lat=&lon=&radiusKm=
```

| Paramètre | Obligatoire | Description |
|---|---|---|
| `lat` | oui | Latitude de l'utilisateur |
| `lon` | oui | Longitude de l'utilisateur |
| `radiusKm` | oui | Rayon de recherche en kilomètres |

Les résultats sont triés par distance croissante. Chaque parking inclut un champ `distanceMeters`.

**Exemple de réponse :**
```json
[
  {
    "name": "THEATRE",
    "capacity": 320,
    "availableSpaces": 50,
    "occupancyRate": 84.3,
    "latitude": 46.58,
    "longitude": 0.34,
    "city": "POITIERS",
    "distanceMeters": 243.7
  }
]
```

**Codes de retour :**
- `200` — liste de parkings (vide si aucun dans le rayon)
- `400` — paramètre manquant ou invalide (ProblemDetail RFC 9457)

---

## Architecture

```
parking/
  controller/          → ParkingController (REST)
  cron/                → ParkingSyncCron (déclenche tous les ParkingSyncService)
  service/             → ParkingService (filtrage Haversine, lecture seule)
  dto/                 → ParkingDto, CityName (enum)
  mapper/              → ParkingMapper (MapStruct)
  exception/           → GlobalExceptionHandler (ProblemDetail)
  util/                → HaversineUtil
  database/
    entity/            → City, Parking (JPA)
    repository/
      ParkingReadRepository   → lecture (query side)
      ParkingWriteRepository  → écriture (command side)
  poitiers/            → intégration spécifique Poitiers
    service/           → PoitiersParkingsService (implements ParkingSyncService)
    dto/               → désérialisation de l'API source
    mapper/            → transformation vers l'entité Parking
```

### Choix techniques

- **PostgreSQL embarqué (Zonky)** : zéro configuration, l'application est autonome. Un vrai PostgreSQL s'active via `embedded.postgres.enabled=false` + `spring.datasource.*` dans `application.properties`.
- **Synchronisation par cron** : les données sont mises en cache en base toutes les 5 minutes. L'API mobile ne dépend jamais de la disponibilité de la source externe — si l'API Poitiers est down, les dernières données connues restent servies.
- **Plugin registry (ParkingSyncService)** : chaque source de données implémente `ParkingSyncService`. Le cron générique itère sur toutes les implémentations détectées par Spring. Ajouter une nouvelle ville revient à créer un nouveau package avec son propre client Feign, son mapper et son service, sans toucher au reste.
- **CQRS léger** : `ParkingReadRepository` (étend `Repository`, lecture seule) est utilisé par le contrôleur ; `ParkingWriteRepository` (étend `JpaRepository`) est réservé à la synchronisation. Même table, deux contrats d'accès distincts.
- **CityName enum** : le nom de ville est un enum typé jusqu'à l'entité JPA (`@Enumerated(EnumType.STRING)`), éliminant les comparaisons de chaînes libres.
- **Calcul de distance Haversine** : réalisé en mémoire côté Java. Justifié par le faible volume (quelques dizaines de parkings). Pour des volumes importants, PostGIS serait plus adapté.
- **ProblemDetail (RFC 9457)** : format d'erreur HTTP standard, activé nativement par Spring Boot 3+.
- **MapStruct** : génération de code au compile-time, sans réflexion à l'exécution.

---

## Problèmes identifiés non traités

- **Authentification / sécurité** : l'API est publique. En production, il faudrait un mécanisme d'authentification (API key, OAuth2).
- **Pagination** : pour des zones denses avec de nombreux parkings, une pagination serait nécessaire.
- **CityName en configuration** : aujourd'hui ajouter une ville nécessite d'étendre l'enum et d'écrire du code. Une approche config-driven (sources déclarées dans `application.yml`) serait plus flexible, au prix d'une perte de typage fort.
- **Monitoring** : Spring Actuator est présent mais aucune alerte n'est configurée sur les échecs de sync.
- **Race condition sur le cron** : en déploiement multi-instance, deux nœuds pourraient synchroniser en parallèle. ShedLock (`@ScheduledLock`) réglerait ce point.
- **Multi-module Maven** : le package `poitiers/` est isolé architecturalement mais pas au niveau du build. Un module Maven dédié renforcerait le découplage si plusieurs équipes travaillent sur des sources différentes.
