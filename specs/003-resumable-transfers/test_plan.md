# Resumable Transfers — Test Plan

> To be detailed when this feature is started.

## Unit
- [ ] Offset bookkeeping; resume-vs-restart decision (size/mtime changed)
- [ ] Server-without-resume fallback logic

## Integration / Instrumented
- [ ] Pause/resume download → byte-identical result (checksum)
- [ ] Network-drop recovery; app-kill recovery from Room

## Edge Cases
- [ ] >1 GB file; remote changed mid-transfer; disk pressure
