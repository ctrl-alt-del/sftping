# Batch Transfers & Uploaded-File Memory — Test Plan

## Unit Tests

### TransferManager (JVM)
- [ ] **completedUploadPaths**: Arrange tasks (UPLOAD/COMPLETED, UPLOAD/RUNNING,
  DOWNLOAD/COMPLETED); Act `completedUploadPaths()`; Assert only the
  UPLOAD+COMPLETED remote paths returned.

### UploadCandidate / buildUploadCandidates (JVM)
- [ ] **marks already-uploaded by target path**: Arrange files `[a.txt, b.txt]`,
  currentDir `/d`, uploadedPaths `{"/d/a.txt"}`; Act build; Assert `a.txt`
  `alreadyUploaded == true`, `b.txt == false`.
- [ ] **all selected by default**: Assert every candidate `selected == true`.
- [ ] **root path join**: currentDir `/` → remote path `"/name"`.

### FilesViewModel (JVM)
- [ ] **toggleUploadCandidate flips selection**: Arrange candidates; Act toggle one;
  Assert its `selected` flipped, others unchanged.
- [ ] **cancelUpload clears sheet**: Act `cancelUpload()`; Assert `showUploadSheet
  == false` and `uploadCandidates` empty.
- [ ] **existing tests still pass** (loadFiles/navigate/sort/search/onEnterScreen).

## Integration / UI (manual)
- [ ] Pick multiple files → sheet shows them with badges → confirm → N uploads run.
- [ ] Previously uploaded file shows "Uploaded" badge but stays checked.
- [ ] Select multiple remote files → pick destination folder → all download into it.
- [ ] Selecting a directory for download is excluded.

## Edge Cases
- [ ] Cancel picker (no files) → no-op.
- [ ] Uncheck all → confirm uploads nothing.
- [ ] Badge persists after app restart (history-backed).
