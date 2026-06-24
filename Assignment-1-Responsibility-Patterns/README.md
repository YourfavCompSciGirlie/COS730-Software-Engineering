# COS 730 – Assignment 1: Advanced Behavioural Modelling and Responsibility-Driven Design

**Student:** Yohali Malaika Kamangu (u23618583)  
**Module:** COS 730 – Software Engineering (I)  
**Due Date:** 19 March 2026

---

## Assignment Overview

This assignment focuses on **advanced behavioural modelling** and **responsibility-driven design** for the Intelligent Research Collaboration Platform (IRCP). Three use cases were selected to represent distinct system behaviour archetypes:

| Use Case | Description | Behaviour Type |
|----------|-------------|----------------|
| **UC1** | Create Research Project | Event-Driven |
| **UC2** | Submit Dataset | Data-Processing Pipeline |
| **UC3** | Automated Reviewer Assignment | Rule-Based Decision System |

---

## Contents

This folder contains only the final submission report:

- [`u23618583_YM_Kamangu_730_Assignment_1_Report.pdf`](./u23618583_YM_Kamangu_730_Assignment_1_Report.pdf) — Full assignment report with:
  - Sequence diagrams for all three use cases
  - GRASP responsibility analysis (Controller, Creator, Information Expert, Indirection, Polymorphism)
  - Complementary behavioural models (State Machine, Activity Diagram, Strategy Pattern)
  - Design optimisation and system-wide evaluation

---

## Report Highlights

- **Task 1:** Interaction modelling with sequence diagrams; identified redundancies across use cases.
- **Task 2:** GRASP principle analysis; evaluated coupling, cohesion, and scalability.
- **Task 3:** Classified each use case by behaviour type and applied complementary modelling techniques.
- **Task 4:** Design optimisation through model reuse, generalised controllers (`BaseController`), and generic interfaces (`Validator<T>`, `Repository<T>`).

---

## How to Review

Simply open the PDF report in your preferred viewer. All UML diagrams are embedded within the document.

```bash
open u23618583_YM_Kamangu_730_Assignment_1_Report.pdf