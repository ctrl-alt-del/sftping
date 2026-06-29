# Batch Transfers & Uploaded-File Memory — Takeaways

## What Went Well
- Reusing `transfer_tasks` for uploaded‑file memory avoided any schema change or migration
  — the data was already there.
- Extracting pure helpers (`uploadRemotePath`/`markUploaded`) kept the marking logic in
  fast JVM unit tests while `prepareUpload`'s ContentResolver‑dependent resolution stayed
  in the VM (consistent with the existing untested SAF code).
- The `.first { terminalPredicate }` batch‑observer pattern replaced the old
  `return@collect` leak for both upload and download observers.
- `OpenMultipleDocuments` + in‑app sheet → zero new permissions.

## What We Learned
- SAF `OpenMultipleDocuments` returns non‑null `List<Uri>` (empty on cancel), unlike
  `OpenDocument` which returns nullable — fits cleanly into `prepareUpload`.
- `DocumentFile.fromTreeUri` + `createFile` makes per‑file document creation in a picked
  folder straightforward from a ContentResolver‑based app.

## API / Tech Surprises
- `androidx.documentfile:1.0.1` is tiny, stable, and compatible with everything — no
  catalog version‑conflict concern.
- Mocking `ContentResolver.query` in unit tests hits the `Dispatchers.IO` problem (MEMORY
  #build), so `prepareUpload` is intentionally not unit‑tested; the pure helpers cover the
  decision logic.

## Patterns Worth Reusing
<!-- These get promoted to root MEMORY.md after ship -->
- Batch upload via SAF multi‑pick + in‑app selection sheet with uploaded‑file memory (see
  MEMORY #ui / "Patterns That Worked" and ADR‑010).
- Batch download via tree‑folder picker + per‑file DocumentFile creation.
- `.first { allTerminal(ids) }` on a Room‑backed StateFlow for clean batch‑completion
  observation (fixes the old `return@collect` observer leak).
