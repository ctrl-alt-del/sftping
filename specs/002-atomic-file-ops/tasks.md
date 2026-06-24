# Atomic File Operations — Tasks

> To be detailed when this feature is started (depends on 001).
> One task = one commit; build (`./gradlew assembleDebug`) + tests
> (`./gradlew testDebug`) must pass per task.

## Block 0: Spec & Design
- [ ] Produce download / delete / rename mockups (upload already mocked)
- [ ] `doc-coauthoring`: spec + plan approved
- [ ] test_plan.md detailed

## Block 1: SFTP operations (TBD)
- [ ] Implement upload (SAF InputStream → JSch put)
- [ ] Implement download (JSch get → private cache → copy to SAF)
- [ ] Implement delete (file / recursive folder) and rename

## Block 2: UI (TBD)
- [ ] Upload entry + confirm sheet; Transfers active/completed list
- [ ] Bulk delete from multi-select; rename dialog
