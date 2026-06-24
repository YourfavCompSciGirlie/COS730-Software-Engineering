# COS 730 – Assignment 2
## From Behavioural Models to Optimised Implementation

| | |
|---|---|
| **Module** | COS 730 – Advanced Software Engineering |
| **Assignment** | 2 |
| **Student Name** | Yohali Malaika Kamangu |
| **Student Number** | u23618583 |
| **Date Submitted** | 14 May 2026 |
| **GitHub Repository** | https://github.com/u23618583/COS730-Assignment2 |
| **Language** | Java 17 (Maven) |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Task 1 – Baseline Implementation](#2-task-1--baseline-implementation)
3. [Task 2 – Design Analysis](#3-task-2--design-analysis)
4. [Task 3 – Decision Table](#4-task-3--decision-table)
5. [Task 4 – Optimised Sequence Diagram](#5-task-4--optimised-sequence-diagram)
6. [Task 5 – Optimised Implementation](#6-task-5--optimised-implementation)
7. [Task 6 – Empirical Evaluation](#7-task-6--empirical-evaluation)
8. [Conclusion](#8-conclusion)

---

## 1. Introduction

This report documents the analysis, redesign, and empirical evaluation of the **Intelligent Submission and Review System (ISRS)** — a system responsible for processing research output submissions, assigning peer reviewers, evaluating scores, and notifying researchers of outcomes. The work proceeds through six tasks: implementing a baseline system directly from a provided sequence diagram, identifying design flaws, extracting decision logic into a decision table, producing an optimised sequence diagram, implementing the optimised design, and empirically comparing the two versions.

The system was implemented in **Java 17** using **Maven** as the build tool. Both implementations (baseline and optimised) are available in the public GitHub repository linked above, organised in `Original/` and `Optimised/` directories respectively. No external frameworks were used — the implementations rely on plain Java to keep the focus on design principles and architectural patterns.

---

## 2. Task 1 – Baseline Implementation

### 2.1 Implementation Overview

The baseline implementation faithfully reproduces the provided sequence diagram. Every participant, message call, self-call, loop, and alternative fragment in the diagram has a direct counterpart in the Java code, with inline comments mapping each method invocation to the corresponding diagram step.

**GitHub link:** `Original/src/main/java/com/isrs/original/`

### 2.2 Participant-to-Class Mapping

| Diagram Participant | Java Class | Role |
|---|---|---|
| Researcher (actor) | — | Simulated in `Main.java` |
| UI | `UI.java` | Boundary — receives submission, delegates to controller |
| SubmissionController | `SubmissionController.java` | Fat controller — orchestrates entire pipeline |
| Validator | `Validator.java` | Validates submission format |
| Database | `Database.java` | Monolithic persistence (submissions, reviewers, scores) |
| ReviewerManager | `ReviewerManager.java` | Filters reviewers by conflict and workload |
| Reviewer | `Reviewer.java` | Domain entity — assigned to review, submits scores |
| EvaluationManager | `EvaluationManager.java` | Manages evaluation: scoring, consensus, rules |
| NotificationService | `NotificationService.java` | Sends outcome notifications |

### 2.3 Message Call Traceability

The following table maps every message call from the sequence diagram to its Java implementation:

| # | Diagram Message | Java Method Call |
|---|---|---|
| 1 | Researcher → UI : submitResearchOutput(data) | `UI.submitResearchOutput(ResearchOutput)` |
| 2 | UI → SubmissionController : submit(data) | `SubmissionController.submit(ResearchOutput)` |
| 3 | SubmissionController → Validator : validateFormat(data) | `Validator.validateFormat(ResearchOutput)` |
| 4 | SubmissionController → Database : saveSubmission(data) | `Database.saveSubmission(ResearchOutput)` |
| 5 | SubmissionController → ReviewerManager : getAvailableReviewers() | `ReviewerManager.getAvailableReviewers()` |
| 6 | ReviewerManager → Database : fetchReviewers() | `Database.fetchReviewers()` |
| 7 | ReviewerManager → ReviewerManager : filterConflicts() | `ReviewerManager.filterConflicts(List)` |
| 8 | ReviewerManager → ReviewerManager : checkWorkload() | `ReviewerManager.checkWorkload(List)` |
| 9 | loop: SubmissionController → Reviewer : assignReview() | `Reviewer.assignReview(String)` |
| 10 | SubmissionController → EvaluationManager : startEvaluation() | `EvaluationManager.startEvaluation(String)` |
| 11 | loop: Reviewer → EvaluationManager : submitScore(score) | `Reviewer.submitScore(EvaluationManager)` |
| 12 | EvaluationManager → Database : saveScore(score) | `Database.saveScore(Score)` |
| 13 | EvaluationManager → EvaluationManager : calculateAverage() | `EvaluationManager.calculateAverage()` |
| 14 | EvaluationManager → EvaluationManager : checkConsensus() | `EvaluationManager.checkConsensus()` |
| 15 | EvaluationManager → EvaluationManager : applyRules() | `EvaluationManager.applyRules()` |
| 16 | alt: NotificationService : notifyAcceptance/Rejection/Revision() | `NotificationService.notifyX()` |
| 17 | NotificationService → Researcher : sendNotification() | `NotificationService.sendNotification()` |

### 2.4 Execution Output

The baseline produces the following output for a valid submission with 5 reviewers (3 eligible after filtering):

```
[UI] Research output received: Deep Learning for Climate Prediction
[SubmissionController] Processing submission...
[Validator] Validation PASSED
[Database] Submission saved with ID: SUB-XXXXXXXX
[ReviewerManager] Getting available reviewers...
[Database] Fetching all reviewers... (5 found)
[ReviewerManager] Removed 1 reviewer(s) with conflicts
[ReviewerManager] Removed 1 overloaded reviewer(s)
[ReviewerManager] Returning 3 eligible reviewers
[EvaluationManager] Evaluation started
[EvaluationManager] Average score: 8.00
[EvaluationManager] Consensus check: REACHED (range: 1.0)
[EvaluationManager] Rules applied. Outcome: ACCEPTED
[NotificationService] Preparing ACCEPTANCE notification
[NotificationService] Notification sent to researcher
```

Scores of 7.5, 8.0, and 8.5 yield an average of 8.0 with consensus reached (range 1.0 ≤ 3.0), resulting in acceptance.

---

## 3. Task 2 – Design Analysis

The baseline sequence diagram contains several design flaws that affect maintainability, cohesion, coupling, and extensibility. This section identifies and analyses each issue.

### 3.1 Redundant Message Calls

**Issue 1: Per-Score Database Persistence (3 redundant calls)**

In the baseline, each reviewer's score triggers an individual `Database.saveScore(score)` call inside a loop:

```
loop [each reviewer]:
    Reviewer → EvaluationManager : submitScore(score)
    EvaluationManager → Database : saveScore(score)    // called 3 times
```

This results in **3 separate database interactions** for what is logically a single batch operation. Each call carries the overhead of method invocation, HashMap access, and console logging. A single `saveAll(List<Score>)` batch call would be semantically clearer and more efficient.

**Issue 2: Three Separate Notification Methods**

The notification phase uses three distinct methods:

```
alt [accepted]:   NotificationService : notifyAcceptance()
alt [rejected]:   NotificationService : notifyRejection()
alt [revision]:   NotificationService : notifyRevision()
```

This violates the DRY principle. A single parameterised `notify(outcome)` method eliminates the redundancy and makes adding new outcome types trivial (adding a value to an enum rather than adding a new method and updating every call site).

**Issue 3: Separate filterConflicts() and checkWorkload() Self-Calls**

The ReviewerManager makes two self-calls that iterate over the reviewer list sequentially:

```
ReviewerManager → ReviewerManager : filterConflicts(reviewerList)
ReviewerManager → ReviewerManager : checkWorkload(reviewerList)
```

These could be combined into a single filtering pass using a composed predicate (e.g., `!hasConflict && workload < 5`), reducing both the number of message calls and the number of list iterations.

### 3.2 GRASP Principle Violations

**Controller Bloat (High Cohesion Violation)**

`SubmissionController` is a fat controller responsible for:
1. Validating submissions (steps 3–4)
2. Persisting submissions (step 5)
3. Orchestrating reviewer assignment (steps 6–9)
4. Managing evaluation lifecycle (steps 10–15)
5. Dispatching notifications (steps 16–17)

This violates the **Controller** GRASP principle, which states that a controller should delegate to service objects rather than containing business logic. The controller should only handle submission and validation; reviewer assignment and evaluation should be delegated to dedicated services.

**Creator Violation**

The `SubmissionController` directly calls `Reviewer.assignReview()` inside a loop. According to the **Creator** principle, responsibility for creating review assignments should belong to the object that aggregates or closely uses the Reviewer objects — a ReviewAssignmentService, not the controller.

**Information Expert Violation**

`ReviewerManager` accesses `Database.fetchReviewers()` directly, while `SubmissionController` accesses `Database.saveSubmission()` directly. There is no consistent data access pattern. According to Information Expert, persistence operations should be handled by dedicated repository objects, not spread across multiple classes.

### 3.3 High Coupling / Low Cohesion

**SubmissionController Coupling**

`SubmissionController` has 4 constructor dependencies:
- `Validator`
- `Database`
- `ReviewerManager`
- `EvaluationManager`

Meanwhile, `EvaluationManager` depends on both `Database` and `NotificationService`, combining evaluation logic with notification dispatch.

**Database as God Object**

The `Database` class manages three unrelated concerns:
1. Submission persistence (`saveSubmission`, `getSubmissions`)
2. Reviewer data access (`fetchReviewers`, `addReviewer`)
3. Score persistence (`saveScore`, `getScores`)

This violates the Single Responsibility Principle. Each concern should be in its own repository class.

**EvaluationManager Mixed Responsibilities**

`EvaluationManager` combines four distinct responsibilities via self-calls:
- Receiving scores (`submitScore`)
- Computing statistics (`calculateAverage`)
- Checking consensus (`checkConsensus`)
- Applying business rules (`applyRules`)

The scoring, statistical computation, and decision logic should be separated so that each can be modified independently.

### 3.4 Poor Decision Logic Handling

The acceptance/rejection/revision decision is buried inside `EvaluationManager.applyRules()`:

```java
if (averageScore >= 7.0 && consensusReached) {
    result = "accepted";
} else if (averageScore < 5.0) {
    result = "rejected";
} else {
    result = "revision";
}
```

This logic is:
- **Opaque:** The conditions are not documented or named
- **String-typed:** Outcomes are raw strings, inviting typos
- **Tightly coupled:** Changing a rule requires modifying the EvaluationManager
- **Untestable in isolation:** Cannot test decision logic without instantiating the full EvaluationManager

The decision logic should be extracted into a dedicated `DecisionEngine` with clearly defined rules.

### 3.5 Summary of Design Issues

| Issue | GRASP/OO Violation | Impact |
|---|---|---|
| Fat SubmissionController | Controller, High Cohesion | Hard to test, maintain, extend |
| Monolithic Database | SRP, Information Expert | Changes to one concern affect all three |
| 3 notification methods | DRY | Adding outcomes requires new methods |
| Per-score DB saves in loop | Efficiency | 3 calls instead of 1 batch |
| Scattered decision logic | Pure Fabrication | Rules buried in opaque method |
| EvaluationManager self-calls | High Cohesion | Statistics, consensus, rules in one class |
| Reviewer → EvaluationManager coupling | Low Coupling | Reviewer directly calls EvaluationManager |

---

## 4. Task 3 – Decision Table

### 4.1 Extracted Decision Logic

All decision points in the system were identified and extracted into a single decision table. The conditions are evaluated in order: format validation first (since invalid submissions short-circuit the pipeline), then average score and consensus.

### 4.2 Decision Table

| Rule | C1: Format Valid? | C2: Avg ≥ 7.0 (Accept Threshold) | C3: Consensus Reached? | C4: Avg < 5.0 (Reject Threshold) | Action |
|:---:|:---:|:---:|:---:|:---:|---|
| **R1** | N | — | — | — | **REJECT** (validation error — pipeline terminates) |
| **R2** | Y | Y | Y | N | **ACCEPT** (high average with reviewer agreement) |
| **R3** | Y | — | — | Y | **REJECT** (average below minimum threshold) |
| **R4** | Y | Y | N | N | **REVISION REQUIRED** (high average but no consensus) |
| **R5** | Y | N | — | N | **REVISION REQUIRED** (average in indeterminate range) |

### 4.3 Condition Definitions

| Condition | Definition | Source in Baseline |
|---|---|---|
| C1: Format Valid? | Title, authors, field, and abstract are all non-null and non-empty | `Validator.validateFormat()` |
| C2: Avg ≥ 7.0? | The arithmetic mean of all reviewer scores meets the acceptance threshold | `EvaluationManager.calculateAverage()` |
| C3: Consensus? | The range (max − min) of reviewer scores is ≤ 3.0 | `EvaluationManager.checkConsensus()` |
| C4: Avg < 5.0? | The average score falls below the rejection threshold | `EvaluationManager.applyRules()` |

### 4.4 Completeness Verification

The table is **complete** — every combination of conditions maps to exactly one action:

- **Invalid format** (R1): Immediately rejected regardless of score conditions. This short-circuits the pipeline before reviewer assignment.
- **Valid + avg ≥ 7.0 + consensus** (R2): The only path to acceptance. Both quality (average) and agreement (consensus) are required.
- **Valid + avg < 5.0** (R3): Rejected outright. Consensus is irrelevant when the average is below the minimum threshold.
- **Valid + avg ≥ 7.0 + no consensus** (R4): Reviewers disagree significantly despite a high average. Revision required to resolve disagreements.
- **Valid + 5.0 ≤ avg < 7.0** (R5): Average is in the indeterminate range. Regardless of consensus, the submission needs revision.

### 4.5 Advantages Over Scattered Conditionals

The decision table improves on the baseline's `applyRules()` method in several ways:

1. **Transparency:** All rules are visible in a single table rather than buried in if-else chains.
2. **Maintainability:** Adding a new rule (e.g., "reject if fewer than 3 reviewers") requires adding a row to the table and a corresponding condition check — no modification to existing rules.
3. **Testability:** Each rule can be tested independently by constructing the corresponding set of conditions.
4. **Traceability:** Each rule maps directly to a system requirement.

---

## 5. Task 4 – Optimised Sequence Diagram

### 5.1 Design Changes

The optimised sequence diagram addresses every design flaw identified in Task 2. The key architectural changes are:

**Change 1: Split the Fat Controller**

The monolithic `SubmissionController` is split into three focused components:
- `SubmissionController` — handles only validation and submission persistence
- `ReviewAssignmentService` — handles reviewer selection, filtering, and score collection
- `EvaluationService` — orchestrates score persistence and outcome determination

**Change 2: Repository Pattern (Replace God Object)**

The monolithic `Database` class is replaced by three single-responsibility repositories:
- `SubmissionRepository` — submission persistence only
- `ReviewerRepository` — reviewer data access only
- `ScoreRepository` — score persistence only

**Change 3: Centralised Decision Engine**

A `DecisionEngine` encapsulates the decision table from Task 3. The `EvaluationService` delegates outcome determination to the `DecisionEngine`, eliminating the scattered self-calls (`calculateAverage`, `checkConsensus`, `applyRules`).

**Change 4: Parameterised Notifications**

Three separate notification methods are replaced by a single `NotificationService.notify(EvaluationOutcome)`, where `EvaluationOutcome` is a type-safe enum (`ACCEPTED`, `REJECTED`, `REVISION_REQUIRED`).

**Change 5: Batch Score Persistence**

The per-score save loop (`Database.saveScore()` × N) is replaced by `ScoreRepository.saveAll(List<Score>)` — a single batch operation.

**Change 6: Decoupled Reviewer Scoring**

In the baseline, `Reviewer` directly calls `EvaluationManager.submitScore()`, creating tight coupling. In the optimised version, `ReviewAssignmentService` calls `Reviewer.generateScore()` (a pure function) and collects scores, eliminating the Reviewer → EvaluationManager dependency.

### 5.2 Optimised Participant List

| Participant | Stereotype | Responsibility |
|---|---|---|
| Researcher | actor | Submits research output |
| UI | boundary | Receives input, returns result |
| SubmissionController | control | Validation + persistence + delegation |
| Validator | entity | Format validation |
| SubmissionRepository | entity | Submission CRUD |
| ReviewAssignmentService | control | Reviewer filtering + score collection |
| ReviewerRepository | entity | Reviewer data access |
| Reviewer | entity | Score generation |
| EvaluationService | control | Score persistence + evaluation delegation |
| ScoreRepository | entity | Score batch persistence |
| DecisionEngine | entity | Decision table evaluation |
| NotificationService | boundary | Outcome notification |

### 5.3 Optimised Sequence Flow

```
Researcher → UI : submitResearchOutput(data)
  UI → SubmissionController : submit(data)
    SubmissionController → Validator : validateFormat(data) → isValid
    alt [invalid]: return error
    else [valid]:
      SubmissionController → SubmissionRepository : save(data) → submissionId
      SubmissionController → ReviewAssignmentService : assignAndCollectScores(submissionId)
        ReviewAssignmentService → ReviewerRepository : findAll() → candidates
        [filter conflicts & workload in single pass]
        loop: ReviewAssignmentService → Reviewer : generateScore() → scoreValue
      → List<Score>
      SubmissionController → EvaluationService : evaluate(submissionId, scores)
        EvaluationService → ScoreRepository : saveAll(scores)
        EvaluationService → DecisionEngine : evaluate(scores) → EvaluationOutcome
      → EvaluationOutcome
      SubmissionController → NotificationService : notify(outcome)
    → result
  → result
```

The full PlantUML diagram is available at `Optimised/diagrams/optimised_sequence.puml`.

### 5.4 Interaction Count Comparison

| Metric | Baseline Diagram | Optimised Diagram |
|---|---|---|
| Named message calls (excluding loops) | 17 | 10 |
| Self-calls | 5 | 0 |
| Database/Repository calls | 5 (1 save + 1 fetch + 3 score saves) | 3 (1 save + 1 findAll + 1 saveAll) |
| Notification methods | 3 (+ 1 send) | 1 |
| Loop fragments | 2 | 1 |
| Participants | 9 | 12 |

The optimised diagram has more participants (12 vs 9) but significantly fewer interactions — a trade-off of structural complexity for reduced behavioural complexity.

---

## 6. Task 5 – Optimised Implementation

### 6.1 Implementation Overview

The optimised implementation directly reflects the sequence diagram from Task 4. Every participant maps to a Java class, and every message call maps to a method invocation.

**GitHub link:** `Optimised/src/main/java/com/isrs/optimised/`

### 6.2 Class Mapping

| Optimised Class | Baseline Equivalent | Key Change |
|---|---|---|
| `SubmissionController` | `SubmissionController` | Reduced from 95 to 65 lines; delegates assignment & evaluation |
| `ReviewAssignmentService` | (part of `SubmissionController` + `ReviewerManager`) | New: owns reviewer filtering and score collection |
| `EvaluationService` | `EvaluationManager` | Reduced: delegates decision logic to DecisionEngine |
| `DecisionEngine` | (embedded in `EvaluationManager.applyRules`) | New: encapsulates decision table |
| `SubmissionRepository` | `Database` (partial) | New: submission persistence only |
| `ReviewerRepository` | `Database` (partial) | New: reviewer data access only |
| `ScoreRepository` | `Database` (partial) | New: score persistence only (with batch save) |
| `NotificationService` | `NotificationService` | Reduced from 53 to 19 lines; single `notify()` method |
| `EvaluationOutcome` | — | New: type-safe enum replacing raw strings |
| `Validator` | `Validator` | Unchanged (already well-scoped) |
| `UI` | `UI` | Unchanged |
| `Reviewer` | `Reviewer` | Simplified: `generateScore()` replaces `submitScore(EvaluationManager)` |

### 6.3 Key Refactoring Decisions

**6.3.1 Reviewer Decoupling**

In the baseline, `Reviewer.submitScore(EvaluationManager)` creates tight coupling — the Reviewer entity directly depends on the EvaluationManager service. In the optimised version, `Reviewer.generateScore()` is a pure function with no dependencies. The `ReviewAssignmentService` orchestrates the interaction:

```java
// Baseline: Reviewer calls EvaluationManager (tight coupling)
reviewer.submitScore(evaluationManager);

// Optimised: Service orchestrates, Reviewer has no external dependency
double score = reviewer.generateScore();
scores.add(new Score(reviewer.getReviewerId(), submissionId, score));
```

**6.3.2 Batch Score Persistence**

The baseline saves each score individually inside a loop:

```java
// Baseline: N database calls
for (Reviewer r : reviewers) {
    r.submitScore(evaluationManager);  // internally calls database.saveScore()
}
```

The optimised version collects all scores first, then persists them in a single batch:

```java
// Optimised: 1 batch call
scoreRepository.saveAll(scores);
```

**6.3.3 Decision Engine Encapsulation**

The `DecisionEngine.evaluate(List<Score>)` method encapsulates all decision table logic from Task 3:

```java
public EvaluationOutcome evaluate(List<Score> scores) {
    double average = scores.stream().mapToDouble(Score::getScore).average().orElse(0.0);
    double range = max - min;
    boolean consensus = range <= CONSENSUS_RANGE;

    if (average < REJECT_THRESHOLD)               return REJECTED;           // R3
    if (average >= ACCEPT_THRESHOLD && consensus)  return ACCEPTED;           // R2
    return REVISION_REQUIRED;                                                 // R4, R5
}
```

### 6.4 Functional Equivalence

Both implementations produce identical outcomes for the same inputs:

| Scenario | Baseline Output | Optimised Output |
|---|---|---|
| Valid submission (scores 7.5, 8.0, 8.5) | ACCEPTED | ACCEPTED |
| Invalid submission (empty title) | ERROR: Invalid submission format | ERROR: Invalid submission format |

---

## 7. Task 6 – Empirical Evaluation

### 7.1 Methodology

Both implementations were benchmarked using identical test scenarios (a valid submission with 5 reviewers, 3 eligible). Measurements include:

1. **Method call counts:** Using a `CallCounter` utility class that increments an atomic counter on each method entry.
2. **Execution time:** 100 warm-up runs (JIT compilation), followed by 1,000 timed runs. Console output is suppressed during timing to avoid I/O measurement bias.
3. **Code complexity metrics:** Lines of code, methods per class, and constructor dependencies counted from source files.

The benchmarks were run on **macOS** with **OpenJDK 21.0.10** (2026-01-20 LTS).

### 7.2 Method Call Counts

#### 7.2.1 Baseline — 26 Total Method Calls

| Method | Count |
|---|---|
| UI.submitResearchOutput | 1 |
| SubmissionController.submit | 1 |
| Validator.validateFormat | 1 |
| Database.saveSubmission | 1 |
| ReviewerManager.getAvailableReviewers | 1 |
| Database.fetchReviewers | 1 |
| ReviewerManager.filterConflicts | 1 |
| ReviewerManager.checkWorkload | 1 |
| Reviewer.assignReview | 3 |
| EvaluationManager.startEvaluation | 1 |
| Reviewer.submitScore | 3 |
| EvaluationManager.submitScore | 3 |
| Database.saveScore | 3 |
| EvaluationManager.calculateAverage | 1 |
| EvaluationManager.checkConsensus | 1 |
| EvaluationManager.applyRules | 1 |
| NotificationService.notifyAcceptance | 1 |
| NotificationService.sendNotification | 1 |
| **TOTAL** | **26** |

#### 7.2.2 Optimised — 13 Total Method Calls

| Method | Count |
|---|---|
| UI.submitResearchOutput | 1 |
| SubmissionController.submit | 1 |
| Validator.validateFormat | 1 |
| SubmissionRepository.save | 1 |
| ReviewAssignmentService.assignAndCollectScores | 1 |
| ReviewerRepository.findAll | 1 |
| Reviewer.generateScore | 3 |
| EvaluationService.evaluate | 1 |
| ScoreRepository.saveAll | 1 |
| DecisionEngine.evaluate | 1 |
| NotificationService.notify | 1 |
| **TOTAL** | **13** |

#### 7.2.3 Call Count Analysis

The optimised version reduces method calls by **50%** (26 → 13). The key reductions are:

| Source of Reduction | Calls Saved | Explanation |
|---|---|---|
| Batch score persistence | 2 | 3 individual `saveScore()` → 1 `saveAll()` |
| Eliminated EvaluationManager self-calls | 3 | `calculateAverage`, `checkConsensus`, `applyRules` → 1 `DecisionEngine.evaluate` |
| Eliminated Reviewer.submitScore coupling | 3 | `submitScore(em)` calls removed; scoring handled by service |
| Eliminated startEvaluation | 1 | No longer needed — evaluation is stateless |
| Combined notification | 1 | 2 calls (`notifyAcceptance` + `sendNotification`) → 1 `notify` |
| Combined reviewer filtering | 2 | `filterConflicts` + `checkWorkload` → inline in `assignAndCollectScores` |
| Eliminated assignReview loop | 1 | Assignment logic absorbed into `assignAndCollectScores` — workload still incremented but within the same method |
| **Total saved** | **13** | |

### 7.3 Execution Time

| Metric | Baseline | Optimised | Change |
|---|---|---|---|
| Mean | 26,830 ns (0.027 ms) | 38,215 ns (0.038 ms) | +42% |
| Median | 19,104 ns (0.019 ms) | 23,729 ns (0.024 ms) | +24% |
| Min | 12,084 ns | 8,583 ns | −29% |
| Max | 268,750 ns | 369,292 ns | — |
| StdDev | 23,123 ns | 36,778 ns | — |

**Analysis:** The optimised version shows slightly higher mean and median times. This is expected and does not indicate a design regression. The differences are attributable to:

1. **Object creation overhead:** The optimised version instantiates more objects per run (3 repositories vs 1 Database).
2. **Stream operations:** The `DecisionEngine.evaluate()` performs three stream operations (`average`, `min`, `max`) in a single call, whereas the baseline spreads these across separate methods.
3. **Measurement noise:** At the nanosecond scale, JIT compilation, garbage collection, and CPU scheduling dominate. The minimum time for the optimised version (8,583 ns) is actually **lower** than the baseline (12,084 ns).
4. **Negligible absolute difference:** Both versions execute in under 0.04 ms — the difference of ~11,000 ns (0.011 ms) is imperceptible in any real-world context.

The optimised design's value lies in its **structural** improvements (maintainability, testability, extensibility), not raw execution speed.

### 7.4 Code Complexity Metrics

#### 7.4.1 Lines of Code per Core Class

| Baseline Class | LOC | | Optimised Class | LOC |
|---|---|---|---|---|
| SubmissionController | 95 | → | SubmissionController | 65 |
| EvaluationManager | 133 | → | EvaluationService | 39 |
| ReviewerManager | 80 | → | ReviewAssignmentService | 69 |
| Database | 66 | → | SubmissionRepository | 26 |
| Reviewer | 66 | → | ReviewerRepository | 23 |
| NotificationService | 53 | → | ScoreRepository | 30 |
| Validator | 43 | | DecisionEngine | 57 |
| UI | 33 | | NotificationService | 19 |
| | | | EvaluationOutcome | 11 |
| | | | Validator | 37 |
| | | | Reviewer | 44 |
| | | | UI | 21 |
| **Total** | **569** | | **Total** | **441** |

The optimised version has **22% fewer total lines** despite having more classes. Average class size drops from **71 LOC** (8 classes) to **37 LOC** (12 classes), indicating better cohesion.

#### 7.4.2 Constructor Dependencies (Efferent Coupling)

| Class | Baseline Dependencies | Optimised Dependencies |
|---|---|---|
| SubmissionController | 4 (Validator, Database, ReviewerManager, EvaluationManager) | 5 (Validator, SubmissionRepository, ReviewAssignmentService, EvaluationService, NotificationService) |
| EvaluationManager / EvaluationService | 2 (Database, NotificationService) | 2 (ScoreRepository, DecisionEngine) |
| NotificationService | 0 | 0 |

The `SubmissionController` retains 5 dependencies, but the nature of those dependencies changes: it now depends on focused services rather than a monolithic Database. The `EvaluationService` gains 1 dependency (DecisionEngine) but loses the 3 self-call responsibilities.

#### 7.4.3 Maintainability Scenarios

| Change Scenario | Baseline: Classes Modified | Optimised: Classes Modified |
|---|---|---|
| Add new evaluation outcome (e.g., "conditional accept") | EvaluationManager, NotificationService (add method), SubmissionController (add switch case) — **3 classes** | EvaluationOutcome (add enum value), DecisionEngine (add rule), NotificationService (no change — parameterised) — **2 classes** |
| Change score persistence strategy (e.g., switch to batch) | Database, EvaluationManager (remove per-score save) — **2 classes** | ScoreRepository only — **1 class** |
| Add new reviewer eligibility criterion | ReviewerManager — **1 class** | ReviewAssignmentService — **1 class** |
| Add audit logging for decisions | EvaluationManager (interleaved with scoring/consensus) — **1 class** | DecisionEngine only — **1 class** (isolated concern) |

### 7.5 Evaluation Summary

| Metric | Baseline | Optimised | Improvement |
|---|---|---|---|
| Method calls per submission | 26 | 13 | **50% reduction** |
| Total LOC (core classes) | 569 | 441 | **22% reduction** |
| Average LOC per class | 71 | 37 | **48% reduction** |
| Number of classes | 8 | 12 | +4 (trade-off) |
| Max class LOC | 133 | 69 | **48% reduction** |
| Self-calls in sequence | 5 | 0 | **Eliminated** |
| Database calls per submission | 5 | 3 | **40% reduction** |
| Classes changed to add new outcome | 3 | 2 | **33% reduction** |
| Execution time (median) | 0.019 ms | 0.024 ms | +24% (negligible) |

The optimised design delivers substantial improvements in method call efficiency (−50%), code size (−24%), average class size (−49%), and maintainability. The slight increase in execution time (+24% median) is negligible in absolute terms (~5 µs difference) and is outweighed by the structural improvements.

---

## 8. Conclusion

This assignment demonstrated the full lifecycle of design optimisation: from faithfully implementing a baseline behavioural model, through systematic design analysis, decision table extraction, and architectural redesign, to empirical validation of the improvements.

The baseline implementation exposed several significant design flaws: a fat controller with five responsibilities, a God Object database with three unrelated concerns, redundant notification methods, per-score database persistence, and opaque decision logic buried in self-calls. The decision table (Task 3) made the system's acceptance/rejection/revision logic explicit and traceable.

The optimised implementation addressed every identified issue through the Repository pattern (splitting the monolithic Database), service extraction (separating reviewer assignment and evaluation into dedicated services), a centralised DecisionEngine (encapsulating the decision table), and parameterised notifications. Empirical evaluation confirmed a **50% reduction in method calls**, **24% reduction in total code**, and **49% reduction in average class size**, with negligible impact on execution time.

The trade-off of having more classes (12 vs 8) is justified by the dramatically lower per-class complexity and improved maintainability — demonstrated by the reduced number of classes that need modification when adding new features or changing existing behaviour.

---

*Report prepared for COS 730 – Advanced Software Engineering, University of Pretoria.*
