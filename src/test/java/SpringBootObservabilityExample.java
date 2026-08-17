//import io.micrometer.core.instrument.Counter;
//import io.micrometer.core.instrument.MeterRegistry;
//import io.micrometer.core.instrument.Timer;
//import org.springframework.boot.SpringApplication;
//import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
//import org.springframework.boot.autoconfigure.SpringBootApplication;
//import org.springframework.context.annotation.Bean;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.stereotype.Service;
//import org.springframework.http.ResponseEntity;
//import lombok.extern.slf4j.Slf4j;
//
//import java.util.concurrent.TimeUnit;
//
///**
// * Spring Boot application with complete observability setup
// * - Prometheus metrics export
// * - Custom metrics
// * - Request/Response logging
// * - Health checks
// */
//@SpringBootApplication
//@Slf4j
//public class ObservabilityApplication {
//
//    public static void main(String[] args) {
//        SpringApplication.run(ObservabilityApplication.class, args);
//    }
//
//    // ============================================
//    // Micrometer/Prometheus Configuration
//    // ============================================
//
//    /**
//     * Customize meter registry with common tags
//     */
//    @Bean
//    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
//        return registry -> registry
//            .config()
//            .commonTags(
//                "application", "microservice-app",
//                "environment", "production",
//                "team", "backend"
//            );
//    }
//
//    /**
//     * Register custom metrics
//     */
//    @Bean
//    public CustomMetrics customMetrics(MeterRegistry meterRegistry) {
//        return new CustomMetrics(meterRegistry);
//    }
//}
//
///**
// * Custom Metrics Service
// */
//@Service
//@Slf4j
//class CustomMetrics {
//    private final Counter apiCallsCounter;
//    private final Counter apiErrorsCounter;
//    private final Timer apiResponseTime;
//    private final Timer databaseQueryTimer;
//
//    public CustomMetrics(MeterRegistry meterRegistry) {
//        // Counter for total API calls
//        this.apiCallsCounter = Counter.builder("api.calls.total")
//            .description("Total number of API calls")
//            .baseUnit("calls")
//            .register(meterRegistry);
//
//        // Counter for API errors
//        this.apiErrorsCounter = Counter.builder("api.errors.total")
//            .description("Total number of API errors")
//            .baseUnit("errors")
//            .register(meterRegistry);
//
//        // Timer for API response times
//        this.apiResponseTime = Timer.builder("api.response.time")
//            .description("API response time")
//            .baseUnit("milliseconds")
//            .register(meterRegistry);
//
//        // Timer for database queries
//        this.databaseQueryTimer = Timer.builder("database.query.time")
//            .description("Database query execution time")
//            .baseUnit("milliseconds")
//            .register(meterRegistry);
//    }
//
//    public void recordApiCall() {
//        apiCallsCounter.increment();
//    }
//
//    public void recordApiError() {
//        apiErrorsCounter.increment();
//    }
//
//    public <T> T recordApiTiming(java.util.function.Supplier<T> supplier) {
//        return apiResponseTime.recordCallable(supplier);
//    }
//
//    public void recordDatabaseQueryTime(long startTime) {
//        long duration = System.currentTimeMillis() - startTime;
//        databaseQueryTimer.record(duration, TimeUnit.MILLISECONDS);
//    }
//}
//
///**
// * REST Controller with observability annotations
// */
//@RestController
//@RequestMapping("/api")
//@Slf4j
//class UserController {
//
//    private final UserService userService;
//    private final CustomMetrics customMetrics;
//
//    public UserController(UserService userService, CustomMetrics customMetrics) {
//        this.userService = userService;
//        this.customMetrics = customMetrics;
//    }
//
//    @GetMapping("/health")
//    public ResponseEntity<String> health() {
//        log.info("Health check endpoint called");
//        return ResponseEntity.ok("OK");
//    }
//
//    /**
//     * Example GET endpoint with metrics collection
//     */
//    @GetMapping("/users/{id}")
//    public ResponseEntity<User> getUser(@PathVariable Long id) {
//        customMetrics.recordApiCall();
//
//        log.info("Fetching user with id: {}", id);
//
//        try {
//            User user = customMetrics.recordApiTiming(() ->
//                userService.getUserById(id)
//            );
//
//            log.info("User fetched successfully: {}", user.getId());
//            return ResponseEntity.ok(user);
//
//        } catch (Exception e) {
//            customMetrics.recordApiError();
//            log.error("Error fetching user: {}", id, e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//
//    /**
//     * Example POST endpoint
//     */
//    @PostMapping("/users")
//    public ResponseEntity<User> createUser(@RequestBody User user) {
//        customMetrics.recordApiCall();
//
//        log.info("Creating new user: {}", user.getName());
//
//        try {
//            User createdUser = userService.createUser(user);
//            log.info("User created successfully with id: {}", createdUser.getId());
//            return ResponseEntity.ok(createdUser);
//
//        } catch (Exception e) {
//            customMetrics.recordApiError();
//            log.error("Error creating user", e);
//            return ResponseEntity.internalServerError().build();
//        }
//    }
//}
//
///**
// * Service layer with database query timing
// */
//@Service
//@Slf4j
//class UserService {
//
//    private final CustomMetrics customMetrics;
//    // Inject your repository here
//    // private final UserRepository userRepository;
//
//    public UserService(CustomMetrics customMetrics) {
//        this.customMetrics = customMetrics;
//    }
//
//    public User getUserById(Long id) {
//        long startTime = System.currentTimeMillis();
//
//        try {
//            log.debug("Querying database for user id: {}", id);
//            // Simulate database query
//            User user = new User(id, "John Doe", "john@example.com");
//
//            return user;
//
//        } finally {
//            customMetrics.recordDatabaseQueryTime(startTime);
//        }
//    }
//
//    public User createUser(User user) {
//        long startTime = System.currentTimeMillis();
//
//        try {
//            log.debug("Creating user in database");
//            // Simulate database insert
//            user.setId(System.currentTimeMillis());
//            return user;
//
//        } finally {
//            customMetrics.recordDatabaseQueryTime(startTime);
//        }
//    }
//}
//
///**
// * Model class
// */
//@lombok.Data
//@lombok.NoArgsConstructor
//@lombok.AllArgsConstructor
//class User {
//    private Long id;
//    private String name;
//    private String email;
//}
//
///**
// * Global Exception Handler for better error tracking
// */
//@org.springframework.web.bind.annotation.RestControllerAdvice
//@Slf4j
//class GlobalExceptionHandler {
//
//    private final CustomMetrics customMetrics;
//
//    public GlobalExceptionHandler(CustomMetrics customMetrics) {
//        this.customMetrics = customMetrics;
//    }
//
//    @org.springframework.web.bind.annotation.ExceptionHandler(Exception.class)
//    public ResponseEntity<String> handleException(Exception e) {
//        customMetrics.recordApiError();
//        log.error("Global exception caught", e);
//        return ResponseEntity
//            .internalServerError()
//            .body("Internal Server Error: " + e.getMessage());
//    }
//}
//
///**
// * Configuration for Prometheus Micrometer
// */
//@org.springframework.boot.autoconfigure.condition.ConditionalOnClass(
//    io.micrometer.prometheus.PrometheusMeterRegistry.class
//)
//class PrometheusConfiguration {
//
//    /**
//     * Enable Prometheus metrics export
//     * Add dependency: io.micrometer:micrometer-registry-prometheus
//     */
//    @Bean
//    public io.micrometer.prometheus.PrometheusMeterRegistry prometheusMeterRegistry() {
//        return new io.micrometer.prometheus.PrometheusMeterRegistry(
//            io.micrometer.prometheus.PrometheusConfig.DEFAULT
//        );
//    }
//}
