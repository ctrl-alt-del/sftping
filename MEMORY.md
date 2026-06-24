# MEMORY — Accumulated Project Knowledge

## 🧠 Tech Gotchas
<!-- Tagged: #api #ui #build — AI searches by tag -->
<!-- ⚡ = broke in production, non-negotiable guardrail -->

## 🔧 Patterns That Worked
<!-- Reusable patterns discovered across features -->

## 📐 Architecture Decisions
<!-- ADRs made during spec-driven development -->
- ADR-001: Specs in specs/ separate from docs/ (permanent reference vs per-feature artifacts)

## 📂 Code Ownership Map

| File | Touched By | Why |
|------|-----------|-----|
| — | — | — |

## 🐛 Common Bugs Fixed

## 🧠 AI Workflow Rule

Before writing any spec, read in order:
1. `AGENTS.md` or `CLAUDE.md` — project conventions
2. `specs/SDD.md` — SDD workflow
3. `MEMORY.md` — search for relevant #tags
4. `specs/index.md` — check for feature file conflicts

After shipping a feature:
1. Write `takeaways.md` in the feature folder
2. Curate findings into `MEMORY.md` (tagged, ⚡ for critical)
3. Update code ownership map
