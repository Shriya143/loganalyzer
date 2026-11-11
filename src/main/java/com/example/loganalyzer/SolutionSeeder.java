package com.example.loganalyzer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SolutionSeeder implements CommandLineRunner {

    private final SolutionRepository solutionRepository;

    public SolutionSeeder(SolutionRepository solutionRepository) {
        this.solutionRepository = solutionRepository;
    }

    @Override
    public void run(String... args) {
        if (solutionRepository.count() > 0) {
            System.out.println("Solution table already seeded (" + solutionRepository.count() + " entries).");
            return;
        }

        List<Solution> solutions = new ArrayList<>();

        // 1. NullPointerException
        solutions.add(new Solution(
                "NullPointerException, NPE, java.lang.NullPointerException, Cannot invoke, null object, dereference, null pointer",
                "Fixing java.lang.NullPointerException (NPE)",
                "Cause: An attempt was made to access a method, field, or property on an object reference that is currently null.\n\n" +
                "How to fix:\n" +
                "1. Check the line reported in the stack trace and verify which variable is null before dereferencing it.\n" +
                "2. Add defensive guard checks: if (obj != null) { obj.doSomething(); }\n" +
                "3. In modern Java, leverage java.util.Optional<T> or Objects.requireNonNull(obj, \"message\").\n" +
                "4. In Spring Boot, ensure dependencies are properly annotated with @Autowired or injected via constructor injection."
        ));

        // 2. NoSuchFileException / FileNotFoundException
        solutions.add(new Solution(
                "NoSuchFileException, FileNotFoundException, java.nio.file.NoSuchFileException, java.io.FileNotFoundException, file not found, system cannot find the file specified",
                "Fixing NoSuchFileException & FileNotFoundException",
                "Cause: The application attempted to read, open, or write a file at a path that does not exist or lacks read permissions.\n\n" +
                "How to fix:\n" +
                "1. Inspect the path: relative paths resolve against the process working directory (System.getProperty(\"user.dir\")).\n" +
                "2. For files inside src/main/resources, do not use new File(\"...\"). Instead use Spring's ClassPathResource(\"filename\") or getClass().getResourceAsStream(\"/filename\").\n" +
                "3. When creating new files, ensure parent directories exist by calling Files.createDirectories(path.getParent())."
        ));

        // 3. Port 8080 already in use
        solutions.add(new Solution(
                "port 8080, already in use, WebServerException, Port 8080 was already in use, Address already in use, BindException, port conflict",
                "Fixing Web Server Failed to Start: Port 8080 already in use",
                "Cause: Another application or an orphaned instance of your Spring Boot server is already bound to port 8080.\n\n" +
                "How to fix:\n" +
                "1. Terminate the conflicting process:\n" +
                "   - Windows: Run 'netstat -ano | findstr :8080' to find the PID, then run 'taskkill /PID <PID> /F'\n" +
                "   - Linux / Mac: Run 'lsof -ti:8080 | xargs kill -9'\n" +
                "2. Or change the application port in src/main/resources/application.properties:\n" +
                "   server.port=8081 (or server.port=0 for dynamic random port allocation)."
        ));

        // 4. Table not found / BadSqlGrammarException
        solutions.add(new Solution(
                "Table not found, BadSqlGrammarException, relation does not exist, Table not found, SQL error table, table does not exist",
                "Fixing SQL Error: Table Not Found (BadSqlGrammarException)",
                "Cause: The SQL query or repository method referenced a database table that does not exist in the current schema.\n\n" +
                "How to fix:\n" +
                "1. If using Hibernate / Spring Data JPA, configure automatic DDL generation in application.properties:\n" +
                "   spring.jpa.hibernate.ddl-auto=update\n" +
                "2. Ensure the entity class is annotated with @Entity and is placed in the scanned package hierarchy.\n" +
                "3. In H2 Database, unquoted table names are capitalized by default. Verify casing or specify @Table(name = \"TABLE_NAME\")."
        ));

        // 5. ClassNotFoundException / NoClassDefFoundError
        solutions.add(new Solution(
                "ClassNotFoundException, NoClassDefFoundError, java.lang.ClassNotFoundException, java.lang.NoClassDefFoundError, class not found",
                "Fixing ClassNotFoundException & NoClassDefFoundError",
                "Cause: The JVM cannot locate the requested compiled bytecode (.class file) in the runtime classpath.\n\n" +
                "How to fix:\n" +
                "1. Verify pom.xml or build.gradle contains the required dependency and that its scope is not restricted to <scope>test</scope>.\n" +
                "2. Run 'mvn clean install -U' to force-refresh local Maven repositories.\n" +
                "3. In your IDE (IntelliJ / Eclipse), reload the Maven/Gradle project and rebuild the project module."
        ));

        // 6. OutOfMemoryError: Java heap space
        solutions.add(new Solution(
                "OutOfMemoryError, Java heap space, java.lang.OutOfMemoryError, GC overhead limit exceeded, heap space, memory leak",
                "Fixing java.lang.OutOfMemoryError: Java heap space",
                "Cause: The JVM has exhausted its maximum allocated heap memory, usually caused by large data ingestion, memory leaks, or unclosed streams.\n\n" +
                "How to fix:\n" +
                "1. Increase the JVM heap allocation using flags: java -Xmx2g -jar app.jar (or -Xmx4g for large memory workloads).\n" +
                "2. Paginate database queries with Spring Data Pageable instead of using repository.findAll() on massive tables.\n" +
                "3. Stream large files with BufferedReader or Files.lines() instead of reading the entire file into memory at once."
        ));

        // 7. StackOverflowError
        solutions.add(new Solution(
                "StackOverflowError, java.lang.StackOverflowError, recursion, infinite loop, circular reference, infinite recursion",
                "Fixing java.lang.StackOverflowError",
                "Cause: The thread execution call stack exceeded its depth limit, almost always caused by infinite recursion or circular object references during JSON serialization.\n\n" +
                "How to fix:\n" +
                "1. Check recursive methods for valid base exit conditions that are guaranteed to terminate.\n" +
                "2. In bidirectional JPA relationships (@OneToMany / @ManyToOne), break infinite JSON loops using @JsonManagedReference and @JsonBackReference, or @JsonIgnore.\n" +
                "3. Ensure Lombok @ToString and @EqualsAndHashCode exclude reciprocal navigational fields."
        ));

        // 8. ClassCastException
        solutions.add(new Solution(
                "ClassCastException, java.lang.ClassCastException, cannot be cast to, type casting error, incompatible types",
                "Fixing java.lang.ClassCastException",
                "Cause: Code attempted to cast an object reference to a class type that is not an instance or subtype of the object.\n\n" +
                "How to fix:\n" +
                "1. Test compatibility before casting using pattern matching: if (obj instanceof MyClass myClass) { myClass.action(); }\n" +
                "2. Avoid using raw types like List or Map; always specify generic type parameters (e.g., List<String>).\n" +
                "3. When using Spring Data native SQL queries, ensure the return mapping matches the expected DTO or Tuple projection."
        ));

        // 9. ArrayIndexOutOfBoundsException / IndexOutOfBoundsException
        solutions.add(new Solution(
                "ArrayIndexOutOfBoundsException, IndexOutOfBoundsException, Index out of bounds, Index out of range, index 0 out of bounds",
                "Fixing ArrayIndexOutOfBoundsException & IndexOutOfBoundsException",
                "Cause: An attempt was made to access an array or List index that is negative or greater than or equal to the collection size.\n\n" +
                "How to fix:\n" +
                "1. Always guard indices: if (index >= 0 && index < list.size()) { ... }\n" +
                "2. Verify collections are not empty before accessing index 0: if (!list.isEmpty()) { list.get(0); }\n" +
                "3. Prefer enhanced for-each loops: for (Item item : items) { ... } or Stream API over manual index counter loops."
        ));

        // 10. IllegalArgumentException
        solutions.add(new Solution(
                "IllegalArgumentException, java.lang.IllegalArgumentException, invalid argument, parameter is not valid, illegal argument",
                "Fixing java.lang.IllegalArgumentException",
                "Cause: A method received an argument that violates its contract, is out of expected range, or is in an invalid format.\n\n" +
                "How to fix:\n" +
                "1. Check the stack trace to determine which parameter caused the exception.\n" +
                "2. Validate arguments at method entry: Objects.requireNonNull(param, \"param must not be null\").\n" +
                "3. In Spring MVC controller endpoints, ensure @RequestParam and path variables match the expected data types."
        ));

        // 11. ConcurrentModificationException
        solutions.add(new Solution(
                "ConcurrentModificationException, java.util.ConcurrentModificationException, collection modified during iteration, remove during loop",
                "Fixing java.util.ConcurrentModificationException",
                "Cause: A Java collection was structurally modified (elements added or deleted) while iterating over it with a for-each loop or standard iterator.\n\n" +
                "How to fix:\n" +
                "1. In Java 8+, use the predicate remover: list.removeIf(item -> item.isExpired());\n" +
                "2. When using an explicit iterator, use iterator.remove() instead of list.remove().\n" +
                "3. In concurrent multi-threaded environments, use CopyOnWriteArrayList or ConcurrentHashMap."
        ));

        // 12. BeanCreationException / NoSuchBeanDefinitionException
        solutions.add(new Solution(
                "BeanCreationException, NoSuchBeanDefinitionException, UnsatisfiedDependencyException, Error creating bean, No qualifying bean of type, consider defining a bean",
                "Fixing Spring Boot BeanCreationException & NoSuchBeanDefinitionException",
                "Cause: Spring's IoC container failed to create or wire a bean because a required dependency could not be found or multiple candidates clashed.\n\n" +
                "How to fix:\n" +
                "1. Ensure the target class has a stereotype annotation: @Component, @Service, @Repository, or @Controller.\n" +
                "2. Confirm the class is located inside the root package or sub-packages of the @SpringBootApplication main class.\n" +
                "3. If multiple beans match an interface, use @Qualifier(\"beanName\") or @Primary to resolve ambiguity."
        ));

        // 13. HttpRequestMethodNotSupportedException (405 Method Not Allowed)
        solutions.add(new Solution(
                "HttpRequestMethodNotSupportedException, 405 Method Not Allowed, Request method not supported, Method Not Allowed, 405 error",
                "Fixing 405 Method Not Allowed (HttpRequestMethodNotSupportedException)",
                "Cause: The client submitted an HTTP request with a method (e.g. GET or POST) that is not supported by the controller endpoint.\n\n" +
                "How to fix:\n" +
                "1. Inspect your @RestController and ensure the mapping annotation matches your client request: @GetMapping, @PostMapping, @PutMapping, or @DeleteMapping.\n" +
                "2. Check frontend fetch() or HTML forms: submitting an HTML form with default method=\"GET\" to a @PostMapping endpoint causes a 405 error.\n" +
                "3. Verify endpoint URLs for typos and trailing slash differences."
        ));

        // 14. HttpMediaTypeNotSupportedException (415 Unsupported Media Type)
        solutions.add(new Solution(
                "HttpMediaTypeNotSupportedException, 415 Unsupported Media Type, Content type not supported, Content-Type, application/json, 415 error",
                "Fixing 415 Unsupported Media Type (HttpMediaTypeNotSupportedException)",
                "Cause: The client sent a request with a Content-Type header that the Spring controller method cannot consume.\n\n" +
                "How to fix:\n" +
                "1. When sending JSON bodies with fetch(), always include the header: headers: { 'Content-Type': 'application/json' }.\n" +
                "2. If sending HTML form data (application/x-www-form-urlencoded), do not annotate controller parameters with @RequestBody; use @RequestParam or standard POJO binding.\n" +
                "3. Verify the controller mapping: @PostMapping(value = \"/endpoint\", consumes = MediaType.APPLICATION_JSON_VALUE)."
        ));

        // 15. DataIntegrityViolationException / ConstraintViolationException
        solutions.add(new Solution(
                "DataIntegrityViolationException, ConstraintViolationException, Unique index or primary key violation, duplicate key, foreign key constraint, integrity violation",
                "Fixing Spring DataIntegrityViolationException (Unique or Foreign Key Violation)",
                "Cause: A database write operation violated a database constraint, such as inserting duplicate unique values or referencing a non-existent foreign key.\n\n" +
                "How to fix:\n" +
                "1. For auto-incrementing IDs, use @GeneratedValue(strategy = GenerationType.IDENTITY) and avoid passing explicit ID values during insert.\n" +
                "2. Before saving, check if the record exists with repository.existsById() or repository.existsByEmail().\n" +
                "3. Verify that parent records exist before inserting dependent child entities with foreign keys."
        ));

        // 16. SQLSyntaxErrorException / Syntax error in SQL statement
        solutions.add(new Solution(
                "SQLSyntaxErrorException, Syntax error in SQL statement, JDBC syntax error, BadSqlGrammar, SQL grammar, unexpected token",
                "Fixing SQLSyntaxErrorException & SQL Syntax Errors",
                "Cause: The SQL statement contains invalid syntax, misplaced commas, unescaped reserved keywords, or unsupported dialect grammar.\n\n" +
                "How to fix:\n" +
                "1. Check if table or column names clash with SQL reserved keywords (e.g. USER, ORDER, GROUP, KEY). In H2/JPA, escape them or use @Table(name = \"USER_ACCOUNT\").\n" +
                "2. Never concatenate SQL strings manually; use parameterized queries with PreparedStatement or JdbcTemplate query(sql, params...).\n" +
                "3. Verify that the correct SQL dialect is specified for your database in application.properties."
        ));

        // 17. CannotGetJdbcConnectionException / Connection Refused
        solutions.add(new Solution(
                "CannotGetJdbcConnectionException, Communications link failure, Connection refused, Unable to connect to database, datasource connection error, connection to localhost failed",
                "Fixing CannotGetJdbcConnectionException (Database Connection Refused)",
                "Cause: The application failed to establish a TCP or file connection to the configured database.\n\n" +
                "How to fix:\n" +
                "1. For file-based H2, ensure the data directory exists and is writable: spring.datasource.url=jdbc:h2:file:./data/logdb.\n" +
                "2. If another tool or server instance locked the H2 database file, append ';AUTO_SERVER=TRUE' to the JDBC URL.\n" +
                "3. For external databases (MySQL, PostgreSQL), ensure the database service is running and ports/firewalls allow traffic."
        ));

        // 18. LazyInitializationException
        solutions.add(new Solution(
                "LazyInitializationException, could not initialize proxy - no Session, failed to lazily initialize a collection, Hibernate LazyInitializationException, no Session",
                "Fixing Hibernate LazyInitializationException: could not initialize proxy",
                "Cause: Code attempted to access a lazily-loaded JPA relation (@OneToMany, @ManyToMany) outside an active database transaction / Hibernate Session.\n\n" +
                "How to fix:\n" +
                "1. Add @Transactional to the service method accessing the lazy collections.\n" +
                "2. Use JOIN FETCH in your JPQL query: @Query(\"SELECT p FROM Parent p JOIN FETCH p.children WHERE p.id = :id\").\n" +
                "3. Use @EntityGraph on the repository method to load the relationship eagerly for specific operations."
        ));

        // 19. HttpMessageNotReadableException / JSON Parse Error
        solutions.add(new Solution(
                "HttpMessageNotReadableException, JSON parse error, Required request body is missing, Cannot deserialize instance, JsonParseException, JSON format error",
                "Fixing HttpMessageNotReadableException / JSON Parse Error",
                "Cause: Jackson failed to deserialize incoming JSON into the target Java model because the payload is malformed or types do not match.\n\n" +
                "How to fix:\n" +
                "1. Ensure your Java model has a public default no-argument constructor: public MyModel() {}.\n" +
                "2. Ensure field names match JSON keys, or annotate them with @JsonProperty(\"field_name\").\n" +
                "3. For dates and timestamps, annotate fields with @JsonFormat(pattern = \"yyyy-MM-dd HH:mm:ss\").\n" +
                "4. Make sure the HTTP request body is valid JSON and not empty or null."
        ));

        // 20. MethodArgumentNotValidException / Validation Failed
        solutions.add(new Solution(
                "MethodArgumentNotValidException, Validation failed for argument, ConstraintViolation, Field error, @Valid, validation error",
                "Fixing MethodArgumentNotValidException / Bean Validation Failure",
                "Cause: A controller model annotated with @Valid failed one or more Jakarta Bean Validation constraints (@NotBlank, @NotNull, @Size, @Email).\n\n" +
                "How to fix:\n" +
                "1. Inspect the error response to identify which field failed and what constraint was violated.\n" +
                "2. Inject a BindingResult parameter immediately following @Valid in your controller method to handle validation errors gracefully.\n" +
                "3. Create a @RestControllerAdvice with an @ExceptionHandler(MethodArgumentNotValidException.class) to return clear, user-friendly field error maps."
        ));

        solutionRepository.saveAll(solutions);
        System.out.println("Seeded " + solutions.size() + " solutions into SOLUTION table.");
    }
}
