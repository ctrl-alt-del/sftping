# Resumable Transfers — Tasks

> To be detailed when this feature is started (depends on 002).
> One task = one commit; build + tests must pass per task.

## Block 0
- [ ] Mockups (transfer card pause/resume states); spec + plan approved; test_plan detailed

## Block 1: Persistence (TBD)
- [ ] Add Room + KSP; `TransferTask` entity + DAO + database

## Block 2: Resume engine (TBD)
- [ ] Resumable download (get + RESUME + skip → cache)
- [ ] Resumable upload (stat + put RESUME; fallback to overwrite)
- [ ] Large-file tuning (buffer, keep-alive, no compression)
- [ ] Pause = persist offset; resume = continue from offset
