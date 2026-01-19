# Feature Flag Configuration

## Tool
- **Recommended**: LaunchDarkly (SaaS) or Unleash (Self-hosted)
- **Fallback**: Spring Cloud Config with custom toggle logic

## Dependencies
```kotlin
// build.gradle.kts
dependencies {
    implementation("io.getunleash:unleash-client-java:9.0.0")
    // OR
    implementation("com.launchdarkly:launchdarkly-java-server-sdk:6.3.0")
}
```

## Usage Pattern
```java
@Service
public class OrderService {
    private final FeatureToggle featureToggle;
    
    public void processOrder(Order order) {
        if (featureToggle.isEnabled("new-payment-flow", order.getUserId())) {
            // New implementation
        } else {
            // Legacy implementation
        }
    }
}
```

## Lifecycle Management
1. **Creation**: Document flag purpose and expected lifetime
2. **Rollout**: Gradual rollout (10% → 50% → 100%)
3. **Cleanup**: Remove flag after 2 sprints of 100% rollout
4. **Monitoring**: Alert if flag age > 90 days (stale flag)

## Best Practices
- ✅ Use descriptive flag names: `enable-saga-payment-flow`
- ✅ Set default to `false` (safe fallback)
- ✅ Log flag evaluations for debugging
- ❌ Don't nest flags (max 1 level deep)
- ❌ Don't use flags for permanent configuration
