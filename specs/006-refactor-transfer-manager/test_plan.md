# Test Plan: TransferManager Refactor

## **Objective**
Verify that the refactoring of `TransferManager` into a decoupled architecture (Strategy/UseCase pattern) does not break existing functionality and ensures correct state management.

## **Test Scope**

### **1. Unit Tests (JVM - `app/src/test/`)**
*   **`TransferStrategy` Tests**: 
    *   Verify `SftpTransferStrategy` correctly maps JSch responses to `TransferProgress`.
    *   Verify error mapping (e.g., `SocketException` $\rightarrow$ `TransferError`).
*   **`UseCase` Tests**: 
    *   **`DownloadUseCase`**: Test offset calculation on simulated pauses.
    *   **`UploadUseCase`**: Test retry logic upon `IOException`.
    *   **Boundary Testing**: Verify behavior on 0-byte files and extremely large files.
*   **`TransferManager` Tests**:
    *   Verify `StateFlow<List<TransferItem>>` updates correctly when `TransferProgress` is received.
    *   Verify multiple simultaneous transfers are tracked independently.

### **2. Integration Tests (Android Instrumented - `app/src/androidTest/`)**
*   **`SftpTransferWorker` + `UseCase`**: 
    *   Verify the `WorkManager` correctly starts the `DownloadUseCase`.
    *   Verify that interrupted workers can restart and resume correctly using the `TransferTaskDao`.

### **3. End-to-End (E2E) Tests**
*   **Full Lifecycle**:
    *   Start a transfer $\rightarrow$ Simulate connection loss $\rightarrow$ Restart $\rightarrow$ Verify file integrity and UI state progression.

## **Test Matrix**

| Test Case | Layer | Input | Expected Output |
| :--- | :--- | :--- | :--- |
| Verify Progress Emission | Manager | `manager.notifyProgress(id, 50%)` | `manager.items` emits list with item at 50% |
| Verify Resumable Download | UseCase | `DownloadUseCase(offset=500)` | Calls `strategy.get(offset=500)` |
| Verify Protocol Agnosticism | Manager | Any `TransferStrategy` | Manager remains agnostic to protocol |
| Verify Worker Resilience | Integration | Worker interrupted at 50% | Worker resumes at 50% on next run |

## **Tools & Commands**
*   **Unit Tests**: `./gradlew testDebug`
*   **Instrumented Tests**: `./gradlew connectedDebugAndroidTest`
