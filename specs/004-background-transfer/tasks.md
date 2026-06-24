# Background Transfer — Tasks

> To be detailed when this feature is started (depends on 003).
> One task = one commit; build + tests must pass per task.

## Block 0
- [ ] Notification + queue mockups; spec + plan approved; test_plan detailed

## Block 1: Worker (TBD)
- [ ] Add WorkManager + Hilt-Work; `SftpTransferWorker` (@HiltWorker)
- [ ] Foreground service type + permissions; progress notification

## Block 2: Queue (TBD)
- [ ] Enqueue/constraints; pause = stop + re-enqueue from offset
- [ ] Queue UI (speed, ETA, pause/cancel)
