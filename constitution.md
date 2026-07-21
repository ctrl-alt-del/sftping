# Project Constitution

<!--
  This constitution governs ALL features in this project. Every spec.md, plan.md,
  and implementation MUST satisfy these principles. Violations require documented
  justification in plan.md's Complexity Tracking section.

  Amendment process: document rationale → review with team → update this file with
  dated entry in the Amendment Log.
-->

## Article I: Spec-First

**No implementation code shall be written before spec.md + plan.md are approved.**

- Every feature MUST have a written spec defining WHAT before HOW
- Every feature MUST have a written plan mapping requirements to technical approach
- Trivial changes (typo fix, log line, single-line config change) are exempt

## Article II: Test-First (TDD)

**Tests MUST be written and confirmed FAILING before implementation code is written.**

- Unit tests for every function/method with business logic
- Contract tests for every API endpoint
- Integration tests for every user story's happy path
- Tests MUST fail before implementation (Red-Green-Refactor)
- This is NON-NEGOTIABLE

## Article III: Modularity

**Every feature MUST begin as a self-contained module with clear boundaries.**

- Features are libraries/modules, not scattered changes across the codebase
- Each module has a single responsibility
- Dependencies between modules are explicit and minimal
- New code is testable in isolation

## Article IV: Simplicity

**Start simple. Add complexity only when proven necessary.**

- Maximum 3 new files per feature without documented justification
- No speculative or "might need" features
- No future-proofing — build for today's requirements
- If you can solve it with a function instead of a class, use a function
- If you can solve it with a standard library feature, don't pull a dependency

## Article V: Anti-Abstraction

**Use framework, language, and library features directly. Do NOT wrap them.**

- No unnecessary abstraction layers (repositories around ORMs, factories for singletons)
- Single model representation until proven insufficient
- Do not create interfaces for single-implementation classes
- Framework utilities are used directly, not hidden behind custom wrappers

## Article VI: Integration Reality

**Prefer real components over mocks. Test in realistic environments.**

- Contract tests are MANDATORY before implementation
- Prefer real databases (test containers, in-memory) over mock repositories
- Prefer real service instances over stubs for integration tests
- Mocks are acceptable only for external boundaries (third-party APIs, payment gateways)

## Article VII: Observability

**Everything MUST be inspectable in production.**

- Every module exposes health/status information
- Errors are logged with context (what was attempted, what went wrong)
- Key operations emit structured logs (not just "it worked")
- Performance-critical paths have metrics or at minimum a timing log
- CLI interfaces accept text in and produce text out (UNIX philosophy)

## Amendment Log

| Date | Article | Change | Rationale |
|------|---------|--------|-----------|
| —    | —       | —      | —         |
