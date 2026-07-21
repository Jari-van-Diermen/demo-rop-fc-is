# Demo-ROP-FC-IS

A demonstration of **Railway Oriented Programming (ROP)** and **Functional Core, Imperative Shell (FC-IS)** patterns in Scala 3.

## Concepts

### Railway Oriented Programming (ROP)

Railway Oriented Programming is a functional programming pattern where each step in a pipeline is like a train switch — success cases follow the "happy track" and errors follow the "error track". Both tracks proceed in parallel through each step, and at every switch, you decide which track to continue on.

In Scala, this is typically implemented using `Either[Error, Result]` types:

- `Right(value)` represents the success track (the happy path)
- `Left(error)` represents the error track

By chaining operations with `.flatMap` or for-comprehensions, errors automatically propagate through the pipeline without explicit try-catch blocks or null checks.

This error propagation approach allows you to handle specific errors at the **end of the pipeline**, rather than the object-oriented style where you are more likely to handle all error types at the start of your program. With ROP, each step can decide which track to continue on, and the actual error handling happens where you have the most context about what went wrong.

### Functional Core, Imperative Shell (FC-IS)

Functional Core, Imperative Shell is an architectural pattern that separates business logic from side effects:

- **Functional Core:** Pure, deterministic, easily testable business logic with no side effects
- **Imperative Shell:** Handles I/O operations, side effects, and application orchestration

The shell wraps the core, feeding input to it and handling its output. This separation makes the core logic unit-testable without needing mock databases, file systems, or network services.

### Prerequisites

- **Java Development Kit (JDK)** 11 or higher installed (required to run sbt)
- **[sbt](https://www.scala-sbt.org/)** — the build tool used for this project

> **Note:** You do **not** need to install Scala separately. sbt automatically downloads and manages the correct Scala compiler (Scala 3.8.4) for this project.

## Running the Projects

This repository contains two SBT subprojects:

| Project | Description |
|---------|-------------|
| **DemoClean** | A reference implementation showing the clean pipeline using ROP and FC-IS patterns |
| **DemoMessy** | A flawed version that needs to be improved |

### Commands

```bash
sbt projects               # Provides an overview of the project
sbt demoClean/run          # Runs the clean reference implementation
sbt demoMessy/run          # Runs the messy version
```

## Exercise Goals

This repository is designed as a hands-on workshop. Use the components in `DemoMessy` to refactor and improve the functions using ROP-style error handling and separate the functional core from the imperative shell.

**Important:** Try solving the challenges yourself first! Only look at `DemoClean` when you're stuck or want to compare your solution.

### Task 1: Assemble the Pipeline

Refactor the `mainClass.scala` in `DemoMessy` so that the pipeline can be run using a series of `.map` or `.flatMap` method calls chained after each other (or using a for-comprehension, which does the same thing under the hood).

**Target result:** Make the pipeline chain flow smoothly like in the `DemoClean` reference.

```scala
Right(path)
  .flatMap(streamParse)
  .flatMap(schemaValidatePassthrough)
  .flatMap(filterLines)
  .flatMap(transformRows)
  .flatMap(mockSaveCustomers)
```

Run it using: `sbt demoMessy/run`

### Task 2: Split Core from Shell

Refactor the `DemoMessy` project to properly separate the functional core (pure business logic) from the imperative shell (I/O operations and side effects).

Identify which functions are pure (no side effects, deterministic) and which involve I/O. Move the pure logic into the core module and keep the I/O operations in the shell.

## Project Structure

```
.
├── build.sbt                    # SBT build definition
├── DemoClean/                   # Reference implementation
│   └── src/main/scala/nl/sogyo/demo/
│       ├── core/                # Pure functional core
│       │   ├── CSVWorkflow.scala
│       │   ├── WorkflowError.scala
│       │   └── WorkflowUtils.scala
│       └── shell/               # Imperative shell
│           ├── CSVReader.scala
│           ├── MockRepository.scala
│           └── mainClass.scala
└── DemoMessy/                   # Exercise — refactor this
    └── src/main/scala/nl/sogyo/demo/
        ├── core/
        ├── shell/
        └── mainClass.scala
```

## Additional Resources

- Railway Oriented Programming
  - [Domain Modeling Made Functional](https://drive.google.com/drive/u/0/folders/0AOL57-RXZYM4Uk9PVA)
  - [F# for Fun and Profit](https://fsharpforfunandprofit.com/rop/)

- Functional programming
  - [Grokking functional programming](https://drive.google.com/drive/u/0/folders/1ww0ePnhunH0bQ2yIgFgjFMVgMj1pGLxi)

- Scala
  - [Programming in Scala 3th edition](https://drive.google.com/drive/u/0/folders/1ww0ePnhunH0bQ2yIgFgjFMVgMj1pGLxi)

- Functional Core, Imperative shell
  - [functional-architecture.org](https://functional-architecture.org/functional_core_imperative_shell/)
  - [Functional-core, Imperative-shell - A different architectural style](https://github.com/allousas/functional-core-imperative-shell)

---
