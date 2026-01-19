# Service Design: {service-name}

## 1. Responsibility
- **Domain**: {Owned Domain}
- **Boundaries**: {What it does vs what it doesn't do}
- **Data Management**: {Managed entities and storage}

## 2. API Contract
- **Base Path**: `/api/v1/{service-name}`
- **Endpoints**:
  - `METHOD /path` - {Description}
- **Authentication**: {JWT / Internal / Public}

## 3. Domain Model
- **Entities**: {List core entities}
- **Value Objects**: {List value objects}
- **Events**: {Events published to Kafka}

## 4. Integration Points
- **Consumes from**: {Topics / Services}
- **Produces to**: {Topics}
- **Calls**: {External APIs / Other Services}
