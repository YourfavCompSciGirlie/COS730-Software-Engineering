# COS 730 – Assignment 2
## From Behavioural Models to Optimised Implementation

**Student:** Yohali Malaika Kamangu (u23618583)

### Project Structure

```
├── Original/          # Task 1: Baseline implementation (as per sequence diagram)
│   └── src/main/java/com/isrs/original/
├── Optimised/         # Task 5: Optimised implementation
│   └── src/main/java/com/isrs/optimised/
└── COS730_Assignment2_Report.pdf
```

### System: Intelligent Submission and Review System (ISRS)

The system models the process of submitting a research artefact, validating it,
assigning reviewers, performing evaluation, and producing a final outcome.

### Running (Pre-built JARs)

**Prerequisites:** Java 17+

```bash
# Baseline demo
java -jar Original/target/original-1.0-SNAPSHOT.jar

# Optimised demo
java -jar Optimised/target/optimised-1.0-SNAPSHOT.jar

# Baseline benchmark (method call counts + timing)
java -cp Original/target/original-1.0-SNAPSHOT.jar com.isrs.original.Benchmark

# Optimised benchmark
java -cp Optimised/target/optimised-1.0-SNAPSHOT.jar com.isrs.optimised.Benchmark
```

### Building from Source

**Prerequisites:** Java 17+ and Maven 3.6+

```bash
# Build baseline (compiles + produces JAR)
cd Original
mvn clean package

# Build optimised
cd Optimised
mvn clean package
```
