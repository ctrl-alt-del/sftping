---
feature_id: "011"
name: "Batch Transfers & Uploaded-File Memory"
status: "✅ Done"
depends_on: ["002", "008"]
touches:
  - "app/src/main/java/com/example/sftping/ui/files/UploadCandidate.kt"
  - "app/src/main/java/com/example/sftping/ui/files/FilesViewModel.kt"
  - "app/src/main/java/com/example/sftping/ui/files/FilesScreen.kt"
  - "app/src/main/java/com/example/sftping/transfer/TransferManager.kt"
  - "gradle/libs.versions.toml"
  - "app/build.gradle.kts"
  - "app/src/test/java/com/example/sftping/ui/files/UploadCandidateTest.kt"
  - "app/src/test/java/com/example/sftping/ui/files/FilesViewModelTest.kt"
  - "app/src/test/java/com/example/sftping/transfer/TransferManagerTest.kt"
created: "2026-06-30"
---

# Batch Transfers & Uploaded-File Memory — Plan

## Approach

**Multi-upload** uses SAF `OpenMultipleDocuments` to pick files, then an in-app
**Upload selection** sheet — Android's system picker can't be annotated, so the
"already uploaded" indication lives in this app surface. `FilesViewModel`
resolves each picked file's name/size, marks `alreadyUploaded` by checking the
target remote path (`currentDir/name`) against completed UPLOAD history, and
shows checkboxes (all checked by default; previously-uploaded files keep an
"Uploaded" badge but stay selectable). Confirming enqueues one UPLOAD task per
checked file (the transfer pipeline already supports N concurrent tasks).

**Uploaded memory** reuses the existing `transfer_tasks` Room table:
`TransferManager.completedUploadPaths()` returns the set of remote paths of
COMPLETED uploads (via `dao.all()`); no schema change. It persists across
restarts and reflects "uploaded into this folder."

**Multi-download** drops the single-file gate in the file list. One file keeps
the `CreateDocument` "save as" flow; multiple files use `OpenDocumentTree` to
pick a destination folder, then create a document per file (`androidx.documentfile`)
and enqueue a DOWNLOAD task each. Directories are excluded.

Pure logic (candidate marking, completed-upload lookup) is unit-tested; SAF /
`DocumentFile` resolution is not JVM-testable (consistent with the existing
untested upload/download code) and is covered by build + manual testing.

## Files to Create / Change

| Action | File | Rationale |
|--------|------|-----------|
| Create | `ui/files/UploadCandidate.kt` | model + pure `buildUploadCandidates()` marker |
| Edit | `ui/files/FilesViewModel.kt` | prepareUpload/toggle/confirm/cancel; downloadFiles; enqueue refactors; batch observer |
| Edit | `ui/files/FilesScreen.kt` | multi-pick launcher, tree picker, selection sheet, multi-download enablement |
| Edit | `transfer/TransferManager.kt` | `completedUploadPaths()` |
| Edit | `gradle/libs.versions.toml`, `app/build.gradle.kts` | `androidx.documentfile` |
| Edit | tests (UploadCandidate, FilesVM, TransferManager) | pure-logic coverage |

## Risks
- SAF/`DocumentFile` code isn't JVM-testable — keep it thin; cover the decision
  logic in pure helpers.
- The download cache→SAF copy is UI-driven on completion (pre-existing); inherited
  by multi-download. Out of scope to fix here (recorded in MEMORY).

## Dependencies
- 002 (transfers), 008 (`currentPath`/`SessionState`).

## ADR-010
"Already uploaded" memory is derived from completed UPLOAD `transfer_tasks`
records keyed by remote path, rather than tracking local file identity (SAF URIs
aren't stable). The in-app Upload selection sheet — not the un-annotatable system
picker — carries the indication.
