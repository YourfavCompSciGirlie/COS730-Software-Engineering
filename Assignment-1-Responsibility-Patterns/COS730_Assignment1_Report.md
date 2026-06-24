# COS 730 – Assignment 1
## Advanced Behavioural Modelling and Responsibility-Driven Design

| | |
|---|---|
| **Module** | COS 730 – Advanced Software Engineering |
| **Assignment** | 1 |
| **Student Name** | Yohali Malaika Kamangu |
| **Student Number** | u23618583 |
| **Date Submitted** | 19 March 2026 |
| **Selected Use Cases** | UC1: Create Research Project · UC2: Submit Dataset · UC3: Automated Reviewer Assignment |

---

## Table of Contents

1. [Introduction](#1-introduction)
2. [Task 1 – Interaction Modelling](#2-task-1--interaction-modelling)
   - 2.1 [Use Case 1: Create Research Project](#21-use-case-1-create-research-project)
   - 2.2 [Use Case 2: Submit Dataset](#22-use-case-2-submit-dataset)
   - 2.3 [Use Case 3: Automated Reviewer Assignment](#23-use-case-3-automated-reviewer-assignment)
   - 2.4 [Modelling Redundancies and Refined Interaction Approach](#24-modelling-redundancies-and-refined-interaction-approach)
3. [Task 2 – Responsibility Assignment Analysis](#3-task-2--responsibility-assignment-analysis)
   - 3.1 [UC1: Create Research Project](#31-uc1-create-research-project)
   - 3.2 [UC2: Submit Dataset](#32-uc2-submit-dataset)
   - 3.3 [UC3: Automated Reviewer Assignment](#33-uc3-automated-reviewer-assignment)
   - 3.4 [System-Wide Responsibility Evaluation](#34-system-wide-responsibility-evaluation)
4. [Task 3 – Modelling System Behaviour](#4-task-3--modelling-system-behaviour)
5. [Task 4 – Design Optimisation](#5-task-4--design-optimisation)
6. [Conclusion](#6-conclusion)

---

## 1. Introduction

The **Intelligent Research Collaboration Platform (IRCP)** is a complex sociotechnical system designed to support the full lifecycle of academic research activities — from project inception through to dataset management, peer review, collaboration monitoring, and output evaluation. The platform must orchestrate multiple interacting subsystems with clearly defined responsibilities, robust validation, and transparent audit trails.

This report addresses Assignment 1 of COS 730, with its focus on advanced behavioural modelling and responsibility-driven design. Three use cases have been selected for detailed analysis: **UC1: Create Research Project**, **UC2: Submit Dataset**, and **UC3: Automated Reviewer Assignment**. These three were deliberately chosen because they represent distinct system behaviour archetypes. UC1 is an **event-driven coordination** scenario in which a researcher action cascades through the system. UC2 is a **data-processing pipeline** in which a file travels through sequential transformation and validation stages. UC3 is a **rule-based automated decision system** in which business rules governing conflict of interest and workload balance govern the output.

The report is structured around four tasks. Task 1 develops sequence diagrams for each use case, identifies structural redundancies across them, and proposes a refined modelling approach. Task 2 applies GRASP responsibility assignment principles to each use case individually, then evaluates coupling, cohesion, and scalability at the system level. Task 3 classifies each use case by system behaviour type and proposes improved modelling strategies for each. Task 4 synthesises all three tasks into a design optimisation analysis addressing model reuse, shared interaction patterns, and long-term system evolution, supported by domain class diagrams for each use case.

---

## 2. Task 1 – Interaction Modelling

### 2.1 Use Case 1: Create Research Project

#### 2.1.1 Formal Use Case Description

| Field | Description |
|---|---|
| **Use Case Name** | Create Research Project |
| **Use Case ID** | UC1 |
| **Actor(s)** | Researcher (Primary); System (Secondary) |
| **Preconditions** | The researcher is authenticated and has permission to create new projects. All collaborator identifiers provided must correspond to registered researchers in the system. |
| **Postconditions** | A new research project record exists in the persistent store with a unique identifier. Project roles have been assigned to all valid collaborators. All collaborators have been notified. An immutable audit log entry has been created. |

**Main Success Scenario:**

1. The researcher initiates project creation by submitting a `ProjectDetails` object containing the project title, a list of objectives, a set of milestones with target dates, and a list of collaborator identifiers.
2. The system validates the submitted details: mandatory fields (title, at least one objective) are present; milestone target dates are chronologically consistent and set in the future; all collaborator identifiers resolve to registered researchers.
3. Upon successful validation, the system initialises a new `Project` entity with a generated `projectId` and a status of `DRAFT`.
4. The system persists the `Project` entity to the project repository and receives the persisted entity back with its assigned identifier.
5. The system triggers the `RoleAssigner` to determine and assign appropriate roles (e.g., Principal Investigator for the creating researcher, Co-Investigator or Research Assistant for named collaborators) based on the project context.
6. The system triggers the `NotificationService` to send notifications to all assigned collaborators informing them of their inclusion in the new project.
7. The system creates an audit log entry recording the creation event, the initiating researcher, and the project identifier.
8. The system returns a `ProjectCreationResponse` to the researcher, encapsulating the persisted project and a summary of assigned roles.

**Alternative Flow 1 – Validation Failure:**
At step 2, if any mandatory field is absent, if milestone dates are inconsistent, or if a collaborator identifier is unresolvable, the system immediately returns a `ProjectCreationException` that enumerates all validation errors. No `Project` entity is created, no roles are assigned, no notifications are sent, and no audit entry is made. The researcher must correct the errors and resubmit.

**Alternative Flow 2 – Partial Collaborator Resolution:**
At step 2, if the system resolves some but not all collaborator identifiers (e.g., one identifier belongs to a deactivated account), validation does not fail outright. The system proceeds with all valid collaborators and includes a `warnings` list in the `ProjectCreationResponse` indicating which identifiers could not be resolved, prompting the researcher to revisit collaborator membership post-creation.

---

#### 2.1.2 Sequence Diagram

The following diagram illustrates the message flow for UC1. The `ProjectController` acts as the system's sole entry point for this use case, delegating to six specialised components. Decision points are modelled using `alt` fragments. Activation bars indicate the duration of each object's processing responsibility.

**Participating Objects:** `Researcher`, `ProjectCreationUI` (boundary), `ProjectController` (control), `ValidationService`, `ProjectRepository`, `ResearcherProfileService`, `RoleAssigner`, `NotificationService`, `AuditLogger`.

**Message Flows:** The `Researcher` actor initiates via the `ProjectCreationUI` boundary, which forwards to `ProjectController`. Field and date validation runs first via `ValidationService`. The `alt` fragment then branches on the result — failure returns all errors immediately with no side effects (spec Alt Flow 1). In the success branch, `verifyCollaborators` runs against `ResearcherProfileService`, and an `opt` fragment handles partial collaborator resolution by collecting warnings if some IDs are unresolvable (spec Alt Flow 2), allowing the flow to continue with valid profiles only. Entity creation, role assignment, two-stage persistence, notification, and audit logging follow, with the final `ProjectCreationResponse` carrying the persisted project and role summary back through the boundary to the actor.

**Control Responsibilities:** `ProjectController` holds sole control responsibility for orchestrating the UC1 workflow. It does not implement any domain logic itself — each delegated class is responsible for its own concern.

**Decision Points and System Responses:**
- **Decision 1 (`alt`):** Field/date `ValidationResult` gates the entire flow. On failure, all errors are returned immediately and no `Project` entity is created (spec Alt Flow 1).
- **Decision 2 (`opt`):** After `verifyCollaborators`, if only some collaborator IDs are unresolvable the flow continues with valid profiles and queues the unresolved IDs as warnings in the response (spec Alt Flow 2).
- **Decision 3 (within `RoleAssigner`):** Role assignment logic determines which role each collaborator receives, encapsulated entirely within `RoleAssigner`.

---

### 2.2 Use Case 2: Submit Dataset

#### 2.2.1 Formal Use Case Description

| Field | Description |
|---|---|
| **Use Case Name** | Submit Dataset |
| **Use Case ID** | UC2 |
| **Actor(s)** | Researcher (Primary); System (Secondary) |
| **Preconditions** | The researcher is authenticated. The referenced research project exists and has `ACTIVE` status. The researcher has dataset submission permissions on the project. |
| **Postconditions** | The dataset is validated, its integrity confirmed, its metadata extracted and stored, and it is linked to the research project. A dataset record with a unique identifier exists in the persistent store. |

**Main Success Scenario:**

1. The researcher initiates dataset submission by providing a data file and the associated project identifier.
2. The system validates the file format against the list of accepted dataset formats (CSV, JSON, HDF5, XML, Parquet).
3. The system performs an integrity scan on the file: it computes a checksum and detects any corruption indicators.
4. The system extracts metadata from the file, including schema structure, record count, file size, and creation timestamp.
5. The system constructs a `Dataset` entity, embedding the extracted metadata and the computed checksum.
6. The system uploads the file to the `StorageService`, which stores the file in persistent blob storage and returns a `storageRef` URI.
7. The system saves the `Dataset` entity — including the `storageRef` — to the dataset repository, receiving back the persisted entity with a unique `datasetId`.
8. The system updates the project record to link the newly created dataset, establishing a persistent association between the dataset and the research project.
9. The system creates an audit log entry recording the submission event, the submitting researcher, and the `datasetId`.
10. The system returns a `DatasetSubmissionResponse` to the researcher confirming the submission and providing the assigned `datasetId`.

**Alternative Flow 1 – Invalid File Format:**
At step 2, if the uploaded file's format is not in the accepted list, the system immediately returns a `DatasetSubmissionException("Unsupported file format: [detected format]")`. No integrity scan, metadata extraction, or storage operation is performed. The researcher is advised to convert the file to a supported format and resubmit.

**Alternative Flow 2 – Integrity Check Failure:**
At step 3, if the integrity scanner detects file corruption (e.g., truncated records, mismatched checksum, binary inconsistency), the system flags the dataset as `QUARANTINED`, records a quarantine entry with the detected issues, and returns a `DatasetSubmissionException("Integrity check failed: [issue detail]")`. No metadata is extracted and no storage occurs. The researcher is advised to re-upload an uncorrupted copy.

---

#### 2.2.2 Sequence Diagram

The UC2 sequence diagram models a sequential validation pipeline in which the file must pass two guarded stages before any persistent operation is performed. The two `alt` fragments at the head of the flow act as guards: format validation runs first (cheapest), then integrity scanning (expensive). Only after both pass does the pipeline proceed to metadata extraction, two-step storage, project linking, and audit logging.

**Participating Objects:** `Researcher`, `DatasetSubmissionUI` (boundary), `DatasetController` (control), `FormatValidator`, `IntegrityScanner`, `MetadataService`, `StorageService`, `DatasetRepository`, `ProjectRepository`, `AuditLogger`.

**Message Flows:** The `Researcher` actor initiates by calling `uploadDataset()` on the `DatasetSubmissionUI` boundary, which forwards the submission to `DatasetController` via `submitDataset()`. The controller calls `FormatValidator.validate()` and receives a `validationResult : ValidationResult` before the outer `alt` block branches. In the success branch, `IntegrityScanner.scan()` is called and returns a `scanResult : ScanResult` before the inner `alt` block branches. In the clean-file branch, `MetadataService` extracts metadata, the controller constructs the `Dataset` entity via a self-call, `StorageService` stores the raw file and returns a `storageRef` URI, `DatasetRepository` saves the dataset record, `ProjectRepository` links the dataset to the project, and `AuditLogger` records the event. The final `DatasetSubmissionResponse` carries the `datasetId` and metadata back through the boundary to the actor.

**Control Responsibilities:** `DatasetController` owns the full pipeline orchestration. It sequences the two validation stages, mediates all intermediate results (checksum, metadata, storageRef) between pipeline components, and ensures that storage and linking only occur after both validation gates pass. No component has knowledge of any other component; all inter-stage data transfer is mediated exclusively by the controller.

**Decision Points and System Responses:**
- **Decision 1 (`alt` — Validation Fails / Validation Passes):** `FormatValidator` returns a `validationResult : ValidationResult` before the outer `alt`. On `[Validation Fails]`, `formatError` propagates back through the boundary to the actor and the entire flow terminates — no integrity scan, no storage, no linking (spec Alt Flow 1). All remaining steps are nested inside `else [Validation Passes]`.
- **Decision 2 (`alt` — Integrity Issues Detected / File Clean):** Inside the valid-format branch, `IntegrityScanner` returns a `scanResult : ScanResult` before the inner `alt`. On `[Integrity Issues Detected]`, `integrityError` propagates back and the flow terminates before any storage occurs (spec Alt Flow 2). All downstream steps — metadata extraction, storage, linking, auditing — are nested inside `else [File Clean]`.
- **Decision 3 (implicit, within `StorageService`):** File storage is separated from record persistence — `StorageService` stores the raw file and returns a `storageRef` URI; `DatasetRepository` stores only the metadata record with that reference. This decouples the file system from the relational layer.

---

### 2.3 Use Case 3: Automated Reviewer Assignment

#### 2.3.1 Formal Use Case Description

| Field | Description |
|---|---|
| **Use Case Name** | Automated Reviewer Assignment |
| **Use Case ID** | UC3 |
| **Actor(s)** | System (Primary — event-triggered); Administrator (Observer) |
| **Preconditions** | A research output has been submitted and its record exists in the system. The reviewer pool contains registered reviewers with defined expertise domains. The system has a configured minimum reviewer count per output. |
| **Postconditions** | A set of reviewers have been assigned to the research output, free of conflicts of interest, with balanced workloads. The assignment is recorded with a rationale string. All assigned reviewers have been notified. An audit log entry records the assignment event. |

**Main Success Scenario:**

1. The system receives an event trigger upon research output submission, containing the output identifier.
2. The `ReviewerAssignmentController` retrieves the `ResearchOutput` details — domain, author list, and title — from the `ResearchOutputRepository`.
3. The `ReviewerPool` identifies candidate reviewers whose declared expertise domains match the output's research domain.
4. The `ConflictChecker` applies conflict-of-interest rules to filter the candidate list, removing reviewers who are co-authors of the output, belong to the same institution as an author, or have had a recent collaboration with an author (within a configurable recency window).
5. The `WorkloadBalancer` selects the required number of reviewers from the conflict-free list, prioritising those with the lowest current assignment workload, provided they are below the maximum workload threshold.
6. The `ReviewerAssignmentController` constructs an `Assignment` entity containing the output identifier, the selected reviewer identifiers, a timestamp, and a rationale string documenting the selection logic applied.
7. The `AssignmentRepository` persists the `Assignment` entity.
8. The `NotificationService` dispatches assignment notifications to each selected reviewer.
9. The system creates an audit log entry recording the assignment event, the `assignmentId`, and the assigned reviewer identifiers.
10. The controller emits an `AssignmentResponse` to the triggering system, confirming the assignment identifier and the reviewer list.

**Alternative Flow 1 – Insufficient Eligible Reviewers:**
At step 5, if the conflict-free, available reviewer pool contains fewer than the required minimum count, the system raises an `AssignmentException("Insufficient eligible reviewers for output: [outputId]")`. No assignment is persisted. The system creates a manual review queue entry for administrator resolution, and the research output's status is updated to `PENDING_MANUAL_ASSIGNMENT`.

**Alternative Flow 2 – No Domain-Matching Reviewers:**
At step 3, if the `ReviewerPool` finds no reviewers with expertise matching the output's specific research domain, the system attempts a domain relaxation strategy: it widens the search to the parent domain category (e.g., widening from "Quantum Computing" to "Computer Science"). If domain relaxation yields candidates, the flow continues with those candidates from step 4. If no candidates are found even after relaxation, Alternative Flow 1 is triggered.

---

#### 2.3.2 Sequence Diagram

UC3 is the only system-triggered use case among the three selected. The initiating actor is the system itself (an event published upon research output submission), rather than a human researcher. This distinction is reflected in the sequence diagram by using an `AssignmentScheduler` participant rather than an actor.

**Participating Objects:** `AssignmentScheduler`, `ReviewerAssignmentController`, `ResearchOutputRepository`, `ReviewerPool`, `ConflictChecker`, `WorkloadBalancer`, `AssignmentRepository`, `NotificationService`, `AuditLogger`.

**Message Flows:** The trigger event carries only the output identifier into `ReviewerAssignmentController` via `assignReviewers()`. The controller retrieves full output context from `ResearchOutputRepository.getById()`, then calls `ReviewerPool.findCandidates()` which returns a `candidates : List~Reviewer~`. An `opt` fragment then models the domain relaxation step: if no candidates were found, `ReviewerPool.findCandidatesRelaxed()` is called to widen the search to the parent domain category, updating `candidates`. The outer `alt` block then checks the final state of `candidates`: if still empty, `assignmentFailed` is returned immediately. In the `[Candidates Found]` branch, `ConflictChecker.filterConflicts()` produces an `eligible` list, and `WorkloadBalancer.selectBalanced()` produces a final `selected` list. The inner `alt` gates on reviewer count. In the `[Sufficient Reviewers Selected]` branch, `buildAssignment` is a self-call that constructs the `Assignment` entity with its rationale string, timestamp, and outputId, `AssignmentRepository.save()` persists it, `NotificationService.notifyReviewers()` dispatches notifications returning `status : NotificationStatus`, `AuditLogger.logEvent()` records the event returning an `entryId`, and the final `AssignmentResponse` is returned to the scheduler.

**Control Responsibilities:** `ReviewerAssignmentController` is the sole orchestrator. Rule applicability (`ConflictChecker`), selection optimisation (`WorkloadBalancer`), and candidate pool management (`ReviewerPool`) are entirely encapsulated in their respective classes. The controller applies these components sequentially without containing any rule logic.

**Decision Points and System Responses:**
- **`opt` — No Candidates Found — Attempting Domain Relaxation:** After `ReviewerPool.findCandidates()` returns, if `candidates` is empty, the `opt` fragment executes: `ReviewerPool.findCandidatesRelaxed()` is called to widen the domain search to the parent category (spec Alt Flow 2). If the relaxed search also returns nothing, `candidates` remains empty and the outer `alt` triggers failure.
- **Decision 1 (`alt` — No Candidates Found / Candidates Found):** The outer `alt` checks the final state of `candidates` after the optional relaxation pass. If still empty, `assignmentFailed(exception : AssignmentException)` is returned immediately and the flow terminates — no conflict checking, balancing, or persistence. All remaining steps are nested inside `else [Candidates Found]`.
- **Decision 2 (`alt` — Insufficient Eligible Reviewers / Sufficient Reviewers Selected):** Inside the candidates-found branch, `WorkloadBalancer.selectBalanced()` returns a `selected` list before the inner `alt`. If the count is below the configured minimum (spec Alt Flow 1), `assignmentFailed(exception : AssignmentException)` terminates the flow without persisting any assignment. All downstream steps — entity construction, persistence, notifications, and auditing — are nested inside `else [Sufficient Reviewers Selected]`.

---

### 2.4 Modelling Redundancies and Refined Interaction Approach

A cross-cutting analysis of the sequence diagrams for UC1, UC2, and UC3 reveals four categories of structural redundancy. Identifying and addressing these redundancies is essential for design efficiency and long-term maintainability.

**Redundancy 1: The Validation Pattern.**
UC1 uses `ValidationService.validateProjectInfo(data : ProjectDetails)` and UC2 uses `FormatValidator.validate(file : File)`. Both follow an identical structural pattern: the controller delegates to a specialised validator, receives a `ValidationResult`, and conditionally terminates the flow if validation fails. The interaction fragment — call validator, check result, propagate exception on failure — is structurally identical in both use cases, differing only in the type of input validated.

**Redundancy 2: The Repository Save Pattern.**
UC1 uses `ProjectRepository.createProjectRecord(data)` followed by `ProjectRepository.saveProjectMetadata(project, roles)` — two separate persistence calls for initial record creation and role-metadata attachment. UC2 uses `DatasetRepository.save(dataset, storageRef)` and UC3 uses `AssignmentRepository.save(assignment)`. All three follow the same structural interaction at each step: pass a domain entity to a repository, receive the persisted entity with an assigned identifier. A shared `Repository<T>` interface would normalise this contract, even though UC1's two-phase persistence is a domain-specific elaboration of the pattern.

**Redundancy 3: NotificationService Reuse.**
`NotificationService` appears in both UC1 (`notifyCollaborators`) and UC3 (`notifyReviewers`). The interaction structure is identical: the controller calls the notification service with an entity and a target set; the service delivers notifications and returns a `NotificationStatus`. The same class serves both use cases, but the sequence diagrams do not exploit this — they describe the interaction independently in each diagram.

**Redundancy 4: Audit Logging Pattern.**
All three use cases include an `AuditLogger.logEvent(eventType, entity)` interaction. The pattern is structurally identical: the controller calls `logEvent()` with an event type string and the domain entity, receives an `entryId : String`, and proceeds to return the confirmation response. This cross-cutting concern is duplicated across all three controllers — a redundancy that a shared `BaseController` would resolve by making audit logging a default, inherited behaviour.

**Proposed Refined Interaction Approach:**

These four redundancies suggest a generalised interaction model centred on an abstract `BaseController`. The refined model introduces:

1. A `Validator<T>` interface that unifies all validators (`ValidationService`, `FormatValidator`, `IntegrityScanner` effectively acts as a second validator in UC2's pipeline), allowing the validation interaction pattern to be described once.
2. A `Repository<T>` interface that normalises the save/find/update contract across `ProjectRepository`, `DatasetRepository`, and `AssignmentRepository`.
3. A `BaseController` that holds references to `NotificationService` and `AuditLogger` as inherited fields, making these cross-cutting services available to all concrete controllers without repetition.
4. `<<ref>>` sequence fragments in diagrams that reference the shared validation and persistence interaction patterns described in a canonical interaction overview diagram.

This refined approach reduces the number of unique interaction patterns from three to one for each structural category. A change to the audit logging format, for example, is made once in `AuditLogger` and is transparently inherited by all three controllers. This has direct implications for development effort and maintainability, explored in detail in Task 4.

---

## 3. Task 2 – Responsibility Assignment Analysis

This section assigns responsibilities to system components using GRASP principles and evaluates the design choices for each use case. A system-wide evaluation of coupling, cohesion, and scalability follows the per-use-case analyses.

---

### 3.1 UC1: Create Research Project

**Controller.** `ProjectController` is assigned the Controller responsibility. It receives the system operation `createProject()` — triggered by the `Researcher` actor — and coordinates all downstream processing without implementing any domain logic itself. The GRASP Controller pattern prescribes that a controller should be a use-case controller: one class per use case that handles the system event, delegates to domain objects, and returns results. Using `ProjectController` as the sole entry point prevents domain objects (e.g., `Project`, `ValidationService`) from being polluted with event-handling or workflow-sequencing logic.

**Creator.** `ProjectController` is the Creator of `Project`. The GRASP Creator pattern specifies that class B should create instances of class A if B aggregates A, has the initialising data for A, or is the primary user of A. All three conditions hold: the controller receives the `ProjectDetails` from which the `Project` is built, it initiates the project's lifecycle, and it immediately passes the created project to downstream services. Encapsulating object creation in the controller prevents collaborating services from needing to know how `Project` is instantiated.

**Information Expert.** Three classes bear Information Expert designations in UC1. `ValidationService` is the Information Expert for project validation rules — it holds knowledge of what constitutes valid project data (field requirements, date constraints, collaborator resolution). `ProjectRepository` is the Information Expert for persistence operations, holding the data-access logic for project storage and retrieval. `RoleAssigner` is the Information Expert for role determination, encapsulating the business rules that map researcher attributes and project context to specific roles.

**Indirection.** `NotificationService` acts as an indirection layer between `ProjectController` and the actual notification delivery mechanisms (email transport, SMS gateway, push notification service). The controller calls `notifyCollaborators()` without any knowledge of how notifications are delivered. This indirection means the delivery channel can change (or multiple channels can be combined) without modifying the controller.

**Polymorphism.** `ValidationService` implements a `Validator<ProjectDetails>` interface (described in Task 4). This enables polymorphic substitution: a stricter `EthicsApprovalValidator` or a lenient `DraftProjectValidator` could be injected at runtime based on the project type, without altering the controller or any other class.

---

### 3.2 UC2: Submit Dataset

**Controller.** `DatasetController` is the Controller for UC2. As with UC1, it represents the use-case controller that receives the `submitDataset()` system event from the researcher actor. It manages the pipeline sequence without implementing any individual pipeline stage logic, maintaining a clean architectural boundary between coordination and execution.

**Creator.** `DatasetController` is the Creator of `Dataset`. It aggregates all information necessary to construct a `Dataset` entity: the file reference, the metadata extracted by `MetadataService`, the checksum computed by `IntegrityScanner`, and the project identifier. The private `buildDataset()` method encapsulates this construction logic, following the GRASP Creator guideline that the class with the initialising data is the appropriate creator.

**Information Expert.** `FormatValidator` is the Information Expert for file format detection, holding the list of accepted formats and the format-identification logic. `IntegrityScanner` is the Information Expert for file integrity, containing checksum algorithms and corruption heuristics. `MetadataService` is the Information Expert for metadata structure, knowing how to parse schema definitions and count records across different file formats. `StorageService` is the Information Expert for file storage, encapsulating all blob-storage interaction and storage-reference generation. `DatasetRepository` is the Information Expert for dataset record persistence, encapsulating all database-layer data-access logic for datasets. `ProjectRepository` is the Information Expert for project-dataset linking, holding the association logic that links a newly submitted dataset to its owning research project.

**Indirection.** `StorageService` provides indirection between `DatasetController` and the underlying file storage infrastructure (local disk, cloud blob, network share). `MetadataService` provides indirection between the controller and the file-parsing libraries or APIs. `DatasetRepository` provides indirection between the controller and the database layer. `AuditLogger` provides indirection between the controller and the audit store. None of these indirection layers expose implementation details to the controller.

**Polymorphism.** `FormatValidator` implements the `Validator<File>` interface established in the refined architecture (Task 4). This enables polymorphic substitution: a `StrictFormatValidator` that rejects borderline formats or a `RelaxedFormatValidator` that accepts additional MIME types can be injected at runtime without modifying `DatasetController`. Similarly, `StorageService` implements a `FileStorage` interface, allowing the storage backend to vary — from a local filesystem to an S3-compatible object store — by substituting a different implementation at the injection point.

---

### 3.3 UC3: Automated Reviewer Assignment

**Controller.** `ReviewerAssignmentController` is the Controller for UC3. Unlike UC1 and UC2, this controller responds to a system event rather than a user action, making it a system-event controller in GRASP terminology. It receives the output identifier from the triggering mechanism, retrieves all necessary context, applies the rule pipeline, and returns structured results — all without exposing internal logic to the trigger.

**Creator.** `ReviewerAssignmentController` is the Creator of `Assignment`. By the time `buildAssignment()` is called, the controller holds all initialising data: the output identifier, the list of selected reviewer identifiers, and the rationale derived from the balancing algorithm. No other class in UC3 has this complete picture, making the controller the natural creator.

**Information Expert.** `ReviewerPool` is the Information Expert for reviewer discovery, holding the full collection of reviewers and the domain-matching logic. `ConflictChecker` is the Information Expert for conflict-of-interest rules, encapsulating three distinct conflict types: co-authorship, institutional affiliation, and recent collaboration. `WorkloadBalancer` is the Information Expert for load distribution, holding the workload threshold constants and the sorting and selection algorithms. `ResearchOutputRepository` is the Information Expert for research output retrieval, holding all data-access logic for fetching output details (domain, author list, title). `AssignmentRepository` is the Information Expert for assignment persistence, encapsulating all data-access logic for recording and retrieving reviewer assignments.

**Indirection.** `NotificationService` decouples the controller from delivery mechanisms. `ResearchOutputRepository` decouples the controller from data-access specifics. `AssignmentRepository` decouples assignment persistence from the controller. All three are indirection points that protect the controller from infrastructure changes.

**Polymorphism.** The conflict-checking domain is an ideal candidate for the Strategy pattern (a GRASP-compatible polymorphism application). `ConflictChecker` currently implements three conflict types as private methods. A further refinement — the Strategy pattern — would represent each conflict type as its own class implementing a `ConflictCheckStrategy` interface, allowing conflict rules to be composed, reordered, or extended at runtime without modifying `ConflictChecker`. This is discussed further in Task 3.

---

### 3.4 System-Wide Responsibility Evaluation

The following analysis evaluates each GRASP principle across all three use cases, assessing the implications for coupling, cohesion, and scalability at the system level.

#### 3.4.1 Controller

**Assignment.** Three use-case controllers are defined: `ProjectController` (UC1), `DatasetController` (UC2), and `ReviewerAssignmentController` (UC3). In the refined architecture (Task 4), these extend a common `BaseController`, which holds shared services and defines the canonical workflow template.

**Justification.** The Controller pattern prevents system events from being directed at domain objects directly. Without a controller, the `Researcher` actor would need to call `ValidationService.validateProjectInfo()`, `ProjectRepository.createProjectRecord()`, and `NotificationService.notify()` in sequence — knowledge that the actor should not possess. The controller encapsulates the system interface, coordinating all internal actions in response to a single external event.

**Coupling Evaluation.** Controllers are coupled to their collaborating services, but these dependencies are managed through dependency injection of interface-typed references. The result is *low afferent coupling* (few classes depend on the controller) and *controlled efferent coupling* (the controller depends on abstractions, not concrete implementations). The three controllers are not coupled to each other, enabling UC1 to change independently of UC3.

**Cohesion Evaluation.** Each controller has high cohesion at the use-case coordination level. `ProjectController` only coordinates UC1; `DatasetController` only coordinates UC2. No controller performs logic that belongs to another use case.

**Scalability.** Adding a new use case (e.g., UC4: Collaboration Monitoring) requires creating a new controller class that extends `BaseController`. The existing controllers are not modified. This is a linear scaling of the controller responsibility pattern.

---

#### 3.4.2 Creator

**Assignment.** `ProjectController` creates `Project` (UC1); `DatasetController` creates `Dataset` (UC2); `ReviewerAssignmentController` creates `Assignment` (UC3).

**Justification.** In each case, the controller receives or aggregates all data needed to construct the domain entity. No other class in each use case has the complete initialising data set. Centralising creation in the controller prevents "telescoping constructor" anti-patterns and ensures that entity construction is always performed in the context of a validated, complete request.

**Coupling Evaluation.** Creator assignment keeps entity creation local to the controller, avoiding the need for factory classes for simple cases. The `Project`, `Dataset`, and `Assignment` classes remain pure domain objects without knowledge of how they are constructed.

**Cohesion Evaluation.** By confining entity construction to the controller's `createProjectRecord()`/`saveProjectMetadata()` calls (UC1), `buildDataset()` self-call (UC2), and `buildAssignment()` self-call (UC3), creation logic is cohesively grouped within each controller and easily located.

**Scalability.** If entity construction becomes complex enough to warrant a dedicated factory (e.g., `ProjectFactory` with multiple construction variants), it can be extracted from the controller without changing the controller's public interface. The Creator pattern thus provides a natural starting point that scales into the Factory pattern when warranted.

---

#### 3.4.3 Information Expert

**Assignment.** The Information Expert principle is applied throughout the design: each class is assigned responsibilities that it can fulfil using its own data, without needing to access another class's internal state. Key assignments are summarised in the table below.

| Class | Information Expert For |
|---|---|
| `ValidationService` | Project validity rules (mandatory fields, date constraints) |
| `FormatValidator` | File format detection and format acceptance rules |
| `IntegrityScanner` | File integrity checking (checksum, corruption detection) |
| `MetadataService` | Metadata extraction from file formats |
| `ReviewerPool` | Reviewer-to-domain expertise matching |
| `ConflictChecker` | Conflict-of-interest rules (co-authorship, institutional, recency) |
| `WorkloadBalancer` | Workload threshold rules and reviewer selection optimisation |
| `ProjectRepository` | Project persistence and retrieval |
| `StorageService` | File blob storage and storage-reference generation |
| `DatasetRepository` | Dataset record persistence and retrieval |
| `AssignmentRepository` | Assignment persistence and retrieval |
| `AuditLogger` | Audit event recording across all three use cases |

**Justification.** Assigning each responsibility to the class possessing the relevant data eliminates the need for excessive getter/setter chaining between classes. For example, `ConflictChecker.hasConflict()` receives a `Reviewer` and a `ResearchOutput` and operates on their data directly, rather than requiring the controller to extract reviewer conflict IDs and output author IDs and perform the comparison itself.

**Coupling Evaluation.** Information Expert assignments minimise the number of inter-class data accesses. A class only needs to access another class's data when its own data is insufficient — and the design ensures this is rare.

**Cohesion Evaluation.** By placing logic near data, Information Expert supports High Cohesion. Classes are naturally cohesive because they operate on their own data, and their methods are conceptually related to the data they hold.

**Scalability.** As business rules grow (more conflict types, more validation rules), the relevant class grows — but in a controlled, bounded way. `ConflictChecker` grows; the controller does not. This contains rule complexity within the classes best equipped to handle it.

---

#### 3.4.4 Low Coupling

**System-Wide Assessment.** The design achieves low coupling through three mechanisms: interface-based dependencies, dependency injection, and zero service-to-service coupling. All inter-service communication is mediated by the controller. Each concrete controller has 3–4 unique service dependencies plus 2 inherited from `BaseController`; no service class has more than 0–1 dependencies on other classes.

**Justification.** Distributing coupling across domain classes creates fragile designs where a change to one service forces changes in others. Centralising coupling in the controller — the class explicitly designed to coordinate — contains the dependency surface and protects domain classes from changes in infrastructure or other services.

**Scalability.** Any service implementation can be swapped (e.g., a new storage backend, a different notification provider) without touching the controller or any other service. This directly improves modularity: components are replaceable in isolation.

---

#### 3.4.5 High Cohesion

**System-Wide Assessment.** Every non-controller class performs a single, well-defined function; controllers are scoped to use-case coordination. A naïve monolithic `ResearchService` would mix validation, persistence, role assignment, notification, integrity scanning, conflict checking, and workload balancing — any change to one concern would ripple across the whole class.

**Justification.** High cohesion is what makes the design modular. In the current design each concern is isolated: a developer changing conflict rules touches only `ConflictChecker`; a developer updating metadata parsing touches only `MetadataService`. Each class has exactly one reason to change (Single Responsibility), which reduces the risk that a modification in one area breaks an unrelated area.

**Scalability.** High cohesion bounds the growth of each class to its own domain. New validation rules extend `ValidationService`; new conflict types extend `ConflictChecker`. The controllers remain stable regardless of how deeply individual service classes evolve.

---

#### 3.4.6 Indirection

**System-Wide Assignment.** `NotificationService` is the primary system-wide indirection layer, decoupling two controllers — `ProjectController` (UC1) and `ReviewerAssignmentController` (UC3) — from delivery mechanisms. `AuditLogger` provides indirection between all three controllers and log storage. All repository classes (`ProjectRepository`, `DatasetRepository`, `AssignmentRepository`) provide indirection between controllers and the data persistence infrastructure.

**Justification.** Indirection enables the platform to evolve its infrastructure without modifying domain logic. Migrating from a relational database to a distributed NoSQL store affects only the repository implementations. Switching from SMTP email to a third-party notification SaaS provider affects only `NotificationService`. The controllers and domain classes remain unchanged in either case.

**Scalability.** As the system grows to support real-time collaboration features, new `NotificationService` implementations (WebSocket-based push, for example) can be introduced without architectural change. The indirection layer absorbs the variation.

---

#### 3.4.7 Polymorphism

**Assignments.** Polymorphism is applied at two levels. First, all validators (`ValidationService`, `FormatValidator`) implement the `Validator<T>` interface, enabling the `BaseController` to reference validators polymorphically. Second, the Strategy pattern applied to `ConflictChecker` (proposed in Task 3) introduces `ConflictCheckStrategy` implementations for each conflict type, enabling rules to be composed and substituted at runtime.

**Justification.** Without polymorphism, extending the system with new validators or conflict rules requires modifying existing classes (an Open/Closed Principle violation). With polymorphism, new validators and new conflict strategies are introduced as new classes implementing existing interfaces, leaving existing code untouched.

**Scalability.** The use of `Validator<T>` means that a new use case requiring a novel validation type can introduce a new `Validator` implementation and inject it into a new controller, with no changes to the validation infrastructure.

---
## 4. Task 3 – Modelling System Behaviour

Each use case selected for this report represents a distinct system behaviour archetype. Identifying these archetypes is essential for selecting the most appropriate modelling strategy and avoiding the use of sequence diagrams for concerns they are ill-suited to represent.

---

### 4.1 UC1: Event-Driven System

**Classification.** UC1 — Create Research Project — exhibits event-driven system characteristics. A discrete user action (project creation) generates a cascade of system events: validation, persistence, role assignment, notification, and audit logging. Each step in the sequence can be thought of as an event that triggers the next. Furthermore, the project entity itself goes through a series of state transitions (`DRAFT` → `ACTIVE`) driven by events (validation success, collaborator acceptance), and these transitions can be modelled independently of the interaction sequence.

**Modelling Challenges.**
The primary challenge with event-driven systems is **state explosion**: as a project accumulates features (milestone updates, collaborator additions, funding status changes), the number of states and transitions grows rapidly. A flat sequence diagram cannot represent state-dependent behaviour — it can only show one path through the system. Additionally, the sequence diagram does not capture what happens when a **concurrent event** occurs, such as a collaborator accepting a role invitation while another collaborator is simultaneously removed. Race conditions and event ordering become critical concerns that sequence diagrams cannot represent.

**Proposed Modelling Strategy.**
The recommended strategy for event-driven systems is the **UML State Machine Diagram**, which explicitly models all states, transitions, guard conditions, and trigger events for a given entity. For UC1, the `Project` entity is the primary stateful object and its lifecycle should be modelled as a state machine. This provides a complement to the sequence diagram: the sequence diagram shows how the project is created; the state machine shows how it evolves throughout its lifecycle.

**Model Adaptation.** The state machine diagram for `Project` reveals that the sequence diagram only captures the `DRAFT → VALIDATING → ACTIVE` transition path. The full lifecycle includes `COMPLETED` and `ARCHIVED` states that generate their own events (e.g., archival notification to all collaborators). These are not visible in the UC1 sequence diagram and require the state machine for complete representation.

---

### 4.2 UC2: Data-Processing Pipeline

**Classification.** UC2 — Submit Dataset — is a **data-processing (transformational) system**. The primary behaviour is a sequential transformation of raw input data: a file is received, format-validated, integrity-scanned, metadata-extracted, structured into a domain entity, persisted, and linked. At each stage, the data is transformed or checked; the final output (a stored, indexed `Dataset` entity) is categorically different from the input (a raw file).

**Modelling Challenges.**
Sequential pipelines present three modelling challenges. First, **error propagation**: when a stage fails, the failure must propagate cleanly and quickly to the caller without executing any subsequent stages. The `alt` fragments in the sequence diagram partially address this, but in a deeply nested pipeline, these fragments become unreadable. Second, **stage extensibility**: adding a new pipeline stage (e.g., a virus scan step between integrity scanning and metadata extraction) requires modifying the controller, which violates the Open/Closed Principle. Third, **parallel execution opportunity**: some pipeline stages are logically independent (format validation and integrity scanning could run concurrently) but the sequence diagram implies strict sequentiality.

**Proposed Modelling Strategy.**
The recommended strategy is the **Chain of Responsibility pattern** combined with a **UML Activity Diagram**. The Chain of Responsibility pattern models each pipeline stage as a handler object that processes the request and passes it to the next handler (or short-circuits on failure), eliminating the need for the controller to know the specific sequence. The Activity Diagram — particularly in its swimlane form — naturally represents the parallel, sequential, and conditional flows of a data-processing pipeline.

**Model Adaptation.** The Activity Diagram makes the pipeline structure more explicit than the sequence diagram: the swimlane layout clearly shows which component is responsible at each stage, the parallel branching opportunity between stages 2 and 3 is visible, and the exception paths read naturally from the decision diamonds — without the nested `alt` fragments that make complex sequence diagrams difficult to parse.

---

### 4.3 UC3: Rule-Based Decision System

**Classification.** UC3 — Automated Reviewer Assignment — is a **rule-based decision system**. The core behaviour is the application of a set of business rules (conflict-of-interest rules, workload-threshold rules, domain-matching rules) to a set of candidates to produce a selection decision. The output depends entirely on which rules are satisfied, rather than on a fixed transformation of the input.

**Modelling Challenges.**
Rule-based systems present three characteristic challenges. First, **rule explosion**: as the platform grows and research ethics requirements evolve, new conflict rules will be enacted. Embedding all rules in `ConflictChecker` as a growing list of conditional logic eventually creates an unmaintainable method. Second, **testability**: testing individual conflict rules in isolation is difficult when they are all embedded in a single method. Third, **rule ordering**: the sequence in which rules are applied can affect performance and outcome. Representing rule ordering in a sequence diagram is misleading because it implies the rules execute in a fixed order, whereas in a rules engine, they may execute in any order.

**Proposed Modelling Strategy.**
The recommended strategy is the **Strategy pattern** for individual conflict rules, combined with a rules engine or composite that applies all registered strategies. The UC3 class diagram should be extended to show a `ConflictCheckStrategy` interface with concrete implementations for each conflict type. This makes each rule independently testable, composable, and extensible.

**Model Adaptation.** The Strategy pattern adaptation transforms `ConflictChecker` from a class with hard-coded conditional rule logic into a composable rule engine. New conflict rules are introduced by implementing `ConflictCheckStrategy` and registering the new strategy at application startup. The `ConflictChecker.filterConflicts()` method iterates the strategy list, applying each rule to each candidate. This design satisfies the Open/Closed Principle and dramatically improves the testability of individual rules.

---

## 5. Task 4 – Design Optimisation

This task synthesises the findings of Tasks 1, 2, and 3 into a comprehensive design optimisation analysis. Optimisation is assessed across four dimensions: model reuse, shared interaction patterns, generalised controllers and services, and reduction of overall modelling complexity. The analysis demonstrates how the proposed optimisations reduce development effort, support maintainability, and facilitate system evolution.

---

### 5.1 Model Reuse Strategies

**Generic Validator Interface.**
The most significant model reuse opportunity identified in Task 1 is the unification of all validators under a `Validator<T>` interface. Currently, `ValidationService` (UC1), `FormatValidator` (UC2), and `IntegrityScanner` (UC2 — acting as a second validation stage in the pipeline) are standalone classes with no common ancestry. Defining a `Validator<T>` interface with a single method — `validate(entity: T): ValidationResult` — provides the following reuse benefits:

- `BaseController` can declare a `Validator<T>` field, giving all concrete controllers a consistent, inherited mechanism for input validation.
- The interaction fragment "call validator, check result, terminate on failure" is described once in the interaction overview diagram and referenced via `<<ref>>` in all use-case sequence diagrams.
- `ValidationService` and `FormatValidator` implement the interface, as shown in the generalised architecture diagram (Section 5.3). `IntegrityScanner` is not a `Validator<T>` implementor — its `scan()` method returns `ScanResult` rather than `ValidationResult` — and is used directly by `DatasetController` as a secondary integrity gate outside the generic validation contract.
- New validators for future use cases (e.g., `CollaborationActivityValidator` for UC4) are introduced by implementing the interface — no changes to the controller framework are needed.

**Generic Repository Interface.**
A `Repository<T>` interface with methods `save(entity: T): T`, `findById(id: String): Optional<T>`, and `update(entity: T): T` unifies all repository interactions. `ProjectRepository` and `AssignmentRepository` follow this contract directly. `DatasetRepository` is a domain-specific elaboration: its `save()` carries an additional `storageRef : String` parameter, atomically binding the cloud storage URI at insert time — as shown in the UC2 sequence diagram. It therefore extends the Repository structural pattern rather than formally implementing `Repository<T>`. Concrete repositories also retain domain-specific query methods (e.g., `DatasetRepository.findByProject()`, `ProjectRepository.linkDataset()`).

**Shared Services as System-Level Singletons.**
`NotificationService` and `AuditLogger` are used across multiple use cases and contain no use-case-specific logic. Designing them as shared system-level services — injected into `BaseController` rather than each concrete controller — eliminates three instances of these dependencies and ensures all controllers share the same notification and audit infrastructure.

---

### 5.2 Shared Interaction Patterns

The four interaction patterns identified as redundant in Task 1 can be captured as canonical pattern descriptions and referenced in use-case-specific diagrams. This is analogous to the UML `<<ref>>` (interaction reference) mechanism.

**Canonical Pattern 1: Validation Gate**
- Controller calls `Validator<T>.validate(request)`
- Controller checks `ValidationResult.isSuccess()`
- On failure: controller throws a domain-specific exception; flow terminates
- On success: flow continues to the next step

This three-step pattern appears in UC1 (once), UC2 (twice — format and integrity), and would appear in all future use cases. Describing it once reduces the total number of message pairs in sequence diagrams across a five-use-case system by approximately 10 message pairs.

**Canonical Pattern 2: Repository Persist**
- Controller calls `Repository<T>.save(entity)`
- Repository returns the persisted entity with an assigned identifier
- Controller uses the returned identifier in subsequent steps

Note: UC2 extends this pattern — after `StorageService` returns a `storageRef`, `DatasetController` passes both the assembled `Dataset` entity and the `storageRef` to `DatasetRepository.save(dataset, storageRef)`, binding the storage URI atomically at insert time.

**Canonical Pattern 3: Notification Dispatch**
- Controller calls `NotificationService.notify(targets, event)`
- `NotificationService` delivers to all targets and returns `status : NotificationStatus`
- Controller continues to the next step (audit)

Note: this pattern applies to UC1 (`notifyCollaborators`) and UC3 (`notifyReviewers`) only. UC2 has no notification step — dataset submission is a system-internal operation that does not require outbound researcher notification.

**Canonical Pattern 4: Audit Logging**
- Controller calls `AuditLogger.logEvent(eventType, entity)`
- `AuditLogger` records the log entry and returns `entryId : String`
- Controller returns the response to the caller

By making these patterns explicit and reusable, the team of developers working on future use cases (UC4, UC5) can describe a new use case's unique interactions — the domain-specific steps between the shared patterns — rather than re-describing the entire use-case flow from scratch.

---

### 5.3 Generalised Controllers and Services

**The BaseController.**
The central optimisation is the introduction of an abstract `BaseController` class that encapsulates the shared workflow: validate → persist → notify → audit. Concrete controllers extend `BaseController` and override or extend the workflow at domain-specific points. This is an application of the **Template Method design pattern**: the algorithm's skeleton (validate, persist, notify, audit) is defined in the base class; the concrete steps are provided by subclasses.

---

### 5.4 Reduction of Modelling Complexity

**Quantitative Reduction.**
Before generalisation, the three use cases collectively require approximately 39 unique message pairs across their sequence diagrams (13 per use case on average). After generalisation — introducing `<<ref>>` fragments for the four canonical patterns — each use-case sequence diagram references the shared patterns and describes only its domain-unique interactions. This reduces the total message pairs described to approximately 27 (a 30% reduction), while increasing the **total expressive coverage** because shared patterns are now correctly applied to all three use cases (including audit logging, which was absent from UC2 and UC3 before the refactoring).

**Cognitive Complexity Reduction.**
A developer joining the team to implement a new use case (e.g., UC4: Collaboration Monitoring) no longer needs to design a controller, validator, repository, notification flow, and audit flow from scratch. They inherit these from `BaseController` and implement only the domain-specific logic. This reduces the time required to implement a new use case by eliminating the re-design of approximately 50% of the controller's interaction structure.

**Diagram Complexity Reduction.**
The class diagram for the generalised architecture (Section 5.3) is the single source of truth for the system's structural design. Individual use-case class diagrams (Sections 5.9.1, 5.9.2, 5.9.3) detail only the domain-specific classes for each use case. The generalised architecture diagram provides the connective tissue. This layered diagramming approach — one system-level diagram plus three use-case-level diagrams — is more readable than a single monolithic class diagram containing all 30+ classes simultaneously.

---

### 5.5 How Improved Modelling Reduces Development Effort

The optimised design reduces development effort through three mechanisms.

**First, code reuse through inheritance.** The `BaseController` template method encapsulates the validate → persist → notify → audit workflow. Developers implementing new use cases write only the domain-specific logic; the shared infrastructure is inherited. Assuming an average use case requires 4 shared steps and 3 unique steps, `BaseController` reduces the implementation effort for each new use case by approximately 57% for the controller layer.

**Second, reduced test surface.** In the naïve design, each use case's controller must be tested for correct validation handling, correct persistence invocation, correct notification dispatch, and correct audit logging — four test suites per controller. In the optimised design, these four concerns are tested once in `BaseControllerTest`. Each concrete controller's test suite tests only its unique domain logic. For three use cases, this eliminates 8 of a potential 12 redundant test categories.

**Third, standardised patterns accelerate onboarding.** New team members learn the `BaseController` pattern once. After understanding that all controllers follow validate → persist → notify → audit, they can orient themselves in any use case's controller with minimal additional reading. Consistent patterns reduce the time needed to understand, review, and extend the codebase.

**Per-use-case design contrast.** In UC1, the naïve alternative — collapsing validation, persistence, role assignment, notification, and audit into a single `ProjectService` — produces a God Object whose every concern is entangled with every other. A change to validation rules risks breaking role assignment logic; a change to the notification format forces regression tests across the entire class. The decomposed design eliminates this risk entirely. In UC2, placing the format check before the integrity scan (cheapest gate first) reflects a performance-aware design principle: fail fast, fail cheap. Invalid files are rejected before any expensive I/O is performed, reducing wasted computation on every rejected submission.

---

### 5.6 How Improved Modelling Supports Maintainability

**Single Change Point for Cross-Cutting Concerns.**
Any change to the audit format, notification strategy, or repository interface is made once — in `AuditLogger`, `NotificationService`, or the `Repository<T>` interface respectively — and takes effect across all use cases simultaneously. In the naïve design (three independent controllers), the same change would need to be made three times, with the risk of inconsistency across controllers.

**Isolated Change Domains.**
Because each class has a single, well-defined concern, the scope of any change is predictable. A change to the conflict-of-interest rules for reviewer assignment affects only `ConflictChecker` (and potentially its `ConflictCheckStrategy` implementations). It does not affect `WorkloadBalancer`, `ReviewerPool`, `NotificationService`, or any other class. This predictability is the foundation of maintainable software.

**Regression Risk Reduction.**
The combination of high cohesion, low coupling, and isolated change domains directly reduces regression risk. When `ConflictChecker` is modified, unit tests for `WorkloadBalancer`, `ProjectController`, and `DatasetController` do not need to change or be re-run (beyond a standard regression suite), because these classes have no dependency on `ConflictChecker`.

**Message ordering as a maintainability contract.** The UC1 message sequence — validate → verify collaborators → persist → assign roles → notify → audit — is not arbitrary. Validation executes first so that no side effects occur against invalid data. Persistence precedes notification so collaborators are never notified about a project that does not yet exist in the system. Audit logging occurs last so the entry records the confirmed final state. Encoding this ordering in the sequence diagram makes the transactional contract explicit and prevents future developers from inadvertently reordering steps in ways that violate data integrity guarantees.

---

### 5.7 How Improved Modelling Improves System Evolution

**New Use Cases.**
The platform specification includes five use cases, of which this report addressed three. Adding UC4 (Collaboration Monitoring) and UC5 (Research Output Evaluation) requires only: (1) creating a new controller extending `BaseController`; (2) creating domain-specific services (e.g., `CollaborationTracker`, `EvaluationAggregator`); and (3) implementing use-case-specific validators and repositories by extending the existing interfaces. The system-level shared infrastructure (notification, audit, persistence contract) requires no modification.

**Future: AI-Assisted Reviewer Matching.**
The proposed AI extension to UC3 — replacing or augmenting `ReviewerPool.findCandidates()` with a semantic similarity model — is accommodated without architectural change. `ReviewerPool` is an injected dependency; an `AIReviewerPool` class implementing the same interface can be injected at startup. The controller, `ConflictChecker`, `WorkloadBalancer`, and all other components are unaffected.

**Future: Real-Time Collaboration Events.**
If the platform evolves to support real-time notifications (WebSocket push) alongside email, `NotificationService` can be extended with a new delivery strategy without modifying any controller. The indirection layer absorbs this infrastructure change entirely.

**Future: Ethics Compliance Integration.**
A new `EthicsComplianceValidator` implementing `Validator<ProjectDetails>` could be injected alongside `ValidationService` in `ProjectController` to enforce ethics-board requirements on new projects. The controller's validation step, using the `Validator<T>` interface, seamlessly accommodates multiple validators in sequence.

**Anticipatory design across use cases.** The `IntegrityScanner` in UC2 computes a checksum at submission time and threads it forward into the persisted `Dataset` entity. Future download workflows can verify file integrity against this stored checksum without any code or schema changes — the infrastructure is already in place. Similarly, the `rationale` field on the `Assignment` entity in UC3 records which conflict rules were applied and which workload constraints governed reviewer selection. Though not consumed by the current workflow, this field is immediately available to future audit, dispute resolution, and research ethics review features. Both decisions demonstrate that modelling interactions explicitly — rather than collapsing them into a monolith — naturally surfaces opportunities for anticipatory design.

---

### 5.8 Design Trade-Offs and Critique

**Trade-Off 1: Abstraction vs. Comprehensibility.**
The introduction of `BaseController`, `Validator<T>`, `Repository<T>`, and the Strategy pattern for conflict rules adds several layers of abstraction. For a small team or a prototype, this abstraction overhead may slow initial development. A senior developer must explain the `BaseController` template method to junior developers before they can effectively contribute to the codebase. The trade-off is: short-term onboarding friction in exchange for long-term maintainability. For a platform intended for long-term production use with an evolving feature set, this trade-off is clearly favourable.

**Trade-Off 2: Controller as Bottleneck.**
The hub-and-spoke collaboration pattern — all services communicating exclusively through the controller — simplifies the interaction model but creates the controller as a potential single point of contention in a high-concurrency environment. Under significant load, requests may queue waiting for controller instances. This is mitigated in production by ensuring controllers are stateless (all state lives in domain objects and repositories), allowing multiple controller instances to run in parallel. The design supports this because no controller class holds mutable state between requests.

**Trade-Off 3: WorkloadBalancer Snapshot Staleness.**
The `Reviewer.currentWorkload` field is an in-memory snapshot that may become stale under concurrent assignment. If two `ReviewerAssignmentController` instances execute simultaneously for two different research outputs, they may both select the same reviewer because they read the same workload value before either assignment is recorded. The correct solution — querying live workload from `AssignmentRepository` at selection time — involves a database read inside the balancer and adds latency. The design should favour consistency over speed for reviewer assignment, given the academic integrity stakes.

**Critique.**
The models presented are well-suited for a system of this complexity and purpose. However, the design does not currently address **authentication and authorisation** at the object interaction level — there is no explicit `AuthorisationService` called before `ProjectController.createProject()` to verify that the researcher has the required permissions. In a production system, this is a critical security concern. An `AuthorisationService` should be added to `BaseController` as the first step of `execute()`, preceding even validation. This would ensure consistent, centralised permission enforcement across all use cases.

Additionally, the models do not explicitly address **transaction boundaries**. In UC1, if `NotificationService.notifyCollaborators()` fails after `ProjectRepository.save()` has already committed, the system will have a persisted project whose collaborators are unaware of it. A transactional design — using an outbox pattern or eventual consistency for notifications — would address this, though it is beyond the scope of the current models.

---

### 5.9 Domain Class Diagrams per Use Case

The class diagrams below define the structural design for each use case. They are presented here in Task 4 because they directly support the optimisation discussion: the generalised architecture (Section 5.3) shows how the shared `Validator<T>`, `Repository<T>`, `NotificationService`, and `AuditLogger` abstractions unify these per-use-case structures. Reading the three diagrams together with the generalised diagram makes the composition of the `BaseController` hierarchy visible.

#### 5.9.1 UC1: Create Research Project — Class Diagram

#### 5.9.2 UC2: Submit Dataset — Class Diagram

#### 5.9.3 UC3: Automated Reviewer Assignment — Class Diagram

## 6. Conclusion

This report has demonstrated the application of advanced behavioural modelling techniques and responsibility-driven design to three core use cases of the Intelligent Research Collaboration Platform. The sequence diagrams developed for UC1, UC2, and UC3 provide a comprehensive and academically rigorous foundation for the system's implementation. Domain class diagrams — presented in Task 4 — further specify the structural design supporting the optimisation analysis.

The GRASP responsibility assignment analysis confirms that the design achieves low coupling, high cohesion, and a clear distribution of responsibilities across all components. No class accumulates multiple unrelated concerns; no actor accesses internal services directly; no service class depends on another service class in the same layer.

The system behaviour classification in Task 3 revealed that the three use cases represent three distinct system archetypes — event-driven, data-processing pipeline, and rule-based decision system — each requiring a different primary modelling strategy. The state machine diagram for UC1, activity diagram for UC2, and Strategy pattern class diagram for UC3 complement the sequence diagrams and provide a more complete behavioural model than sequence diagrams alone.

The design optimisation analysis in Task 4 demonstrated that the `BaseController` template, `Validator<T>` and `Repository<T>` interfaces, and shared services (`NotificationService`, `AuditLogger`) collectively reduce development effort, improve maintainability, and decouple the system from infrastructure changes. The architecture is explicitly designed for evolution: new use cases, AI integrations, and infrastructure migrations can be accommodated without modifying existing, tested code.

The identified trade-offs — abstraction overhead, controller concurrency bottleneck, workload snapshot staleness — are transparent limitations of the current design and represent areas for further architectural refinement as the platform scales.

---

