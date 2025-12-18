## World Cup Path Explorer

A Spring Boot MVP that lists every possible matchup for a host city and projects bracket paths without external APIs. Tournament structure, rankings, and venues are hardcoded providers to keep the demo self contained and deterministic.

### Features

- Venue lookup: show scheduled knockout matches and every legal pairing that could land in the city
- Slot resolution: recursive winner/loser and pooled third place resolution from static bracket definitions
- Eligibility rules: Top 12 teams only qualify via 1st or 2nd in group; third-place slots exclude Top 12 teams
- Filters: toggle Top 12 only, South American only, qualifying (Top 12 or South American), or all
- Sorting: pairings ordered by lowest average FIFA rank (strongest matchups first)
- Simulator: drag teams within each group (no typing) to project who advances to any venue

### Architecture

- Domain: `Team`, `MatchSlot`, `KnockoutMatch`, DTOs like `PairingView`
- Provider: static data for group compositions (`GroupStageProvider`), knockout schedule (`KnockoutStageProvider`), and FIFA ranks/regions (`FifaRankProvider`)
- Service: slot resolution (`SlotResolverService`), venue queries (`VenueQueryService`), pairing expansion (`VenuePairingsService`), projections (`ProjectionService`), and supporting venue helpers
- Controller: thin MVC controllers (`VenueUIController`, `SimulatorController`) bind services to Thymeleaf templates
- View: Thymeleaf pages (`index.html`, `venue.html`, `simulator.html`) with a cohesive dark theme and no client-side data dependencies

### Data Flow

1. User selects a city on home or simulator pages.
2. Controllers request knockout matches from `KnockoutStageProvider`.
3. `SlotResolverService` expands slots recursively to eligible teams, enforcing Top 12 third-place exclusion.
4. `VenuePairingsService` builds cross-product pairings, applies ranking labels, eligibility rules, and sorting.
5. `ProjectionService` simulates bracket outcomes using user-ordered groups to produce deterministic projections.

### Running Locally

Requirements: Java 17, Maven (wrapper included).

```
./mvnw clean package
./mvnw spring-boot:run
```

Visit `http://localhost:8080` for the home page, `/venue?city=Philadelphia` for a sample city, and `/simulator` for drag-and-drop projections.

### Testing

```
./mvnw test
```

### Deployment Notes

- The app is self contained (no external APIs). Package with `./mvnw clean package` and run the JAR or build a container via `./mvnw spring-boot:build-image -DskipTests`.
- Suitable for low-cost JVM hosts like Fly.io or small VMs; static hosts are not applicable.

### Key Rules Reference

- Top 12 teams: Spain, Argentina, France, England, Brazil, Portugal, Netherlands, Belgium, Germany, Croatia, Morocco, Italy
- South American teams flagged for filtering: Argentina, Brazil, Uruguay, Colombia, Venezuela, Ecuador, Paraguay, Peru, Chile, Bolivia, Guyana, Suriname
