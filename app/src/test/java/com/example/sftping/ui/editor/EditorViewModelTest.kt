package com.example.sftping.ui.editor

import com.example.sftping.data.editor.EditorLocation
import com.example.sftping.data.editor.FakePendingEditDao
import com.example.sftping.data.editor.InMemoryEditorLocationRepository
import com.example.sftping.data.editor.PendingEdit
import com.example.sftping.data.editor.PendingEditDao
import com.example.sftping.sftp.HostKeyResult
import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.RemoteFile
import com.example.sftping.sftp.SessionState
import com.example.sftping.sftp.SftpException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class EditorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var client: FakeSftpClient
    private lateinit var repo: InMemoryEditorLocationRepository
    private lateinit var dao: PendingEditDao
    private lateinit var session: SessionState

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        client = FakeSftpClient()
        repo = InMemoryEditorLocationRepository()
        dao = FakePendingEditDao()
        session = SessionState()
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun vm() = EditorViewModel(client, repo, dao, session)

    private val loc = EditorLocation("id1", "app.conf", "/etc/app.conf", 1L)

    @Test
    fun `addLocation persists and lists`() = runTest {
        val vm = vm()
        vm.addLocation(label = "nginx", remotePath = "/etc/nginx/nginx.conf")
        advanceUntilIdle()

        assertEquals(1, vm.uiState.locations.size)
        assertEquals("nginx", vm.uiState.locations.first().label)
        assertFalse(vm.uiState.showAddSheet)
    }

    @Test
    fun `addLocation with blank path is ignored`() = runTest {
        val vm = vm()
        vm.addLocation(label = "x", remotePath = "  ")
        advanceUntilIdle()
        assertTrue(vm.uiState.locations.isEmpty())
    }

    @Test
    fun `deleteLocation removes location and its pending edit`() = runTest {
        repo.add(loc)
        dao.upsert(PendingEdit(loc.remotePath, "cached"))
        val vm = vm()
        advanceUntilIdle()

        vm.deleteLocation(loc.id)
        advanceUntilIdle()

        assertTrue(vm.uiState.locations.isEmpty())
        assertNull(dao.get(loc.remotePath))
    }

    @Test
    fun `open while connected loads remote text`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "remote-content"
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()

        vm.open(loc)
        advanceUntilIdle()

        assertEquals("remote-content", vm.uiState.content)
        assertTrue(vm.uiState.editable)
    }

    @Test
    fun `open prefers pending cached content over remote`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "remote"
        dao.upsert(PendingEdit(loc.remotePath, "cached"))
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()

        vm.open(loc)
        advanceUntilIdle()

        assertEquals("cached", vm.uiState.content)
        assertTrue(vm.uiState.saveStatus is SaveStatus.PendingSync)
    }

    @Test
    fun `open while disconnected is read-only and does not read remote`() = runTest {
        repo.add(loc)
        val vm = vm()
        advanceUntilIdle()

        vm.open(loc)
        advanceUntilIdle()

        assertFalse(vm.uiState.editable)
        assertEquals(0, client.readCount)
        assertTrue(vm.uiState.saveStatus is SaveStatus.NotConnected)
    }

    @Test
    fun `open non-existent path surfaces error`() = runTest {
        repo.add(loc)
        client.readThrows = true
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()

        vm.open(loc)
        advanceUntilIdle()

        assertNotNull(vm.uiState.error)
        assertTrue(vm.uiState.saveStatus is SaveStatus.Error)
    }

    @Test
    fun `saveNow online writes remote and clears pending`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "old"
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()

        vm.onContentChange("new-content")
        vm.saveNow()
        advanceUntilIdle()

        assertEquals("new-content", client.files[loc.remotePath])
        assertNull(dao.get(loc.remotePath))
        assertTrue(vm.uiState.saveStatus is SaveStatus.Saved)
    }

    @Test
    fun `saveNow offline caches edit and flags pending`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "old"
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()
        vm.onContentChange("edited")

        session.setConnected(false)
        advanceUntilIdle()
        vm.saveNow()
        advanceUntilIdle()

        assertEquals("edited", dao.get(loc.remotePath)?.content)
        assertTrue(vm.uiState.saveStatus is SaveStatus.PendingSync)
    }

    @Test
    fun `save failure falls back to cache`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "old"
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()
        vm.onContentChange("edited")
        client.writeThrows = true

        vm.saveNow()
        advanceUntilIdle()

        assertEquals("edited", dao.get(loc.remotePath)?.content)
        assertTrue(vm.uiState.saveStatus is SaveStatus.PendingSync)
    }

    @Test
    fun `reconnect flushes pending edit to server`() = runTest {
        repo.add(loc)
        dao.upsert(PendingEdit(loc.remotePath, "cached-offline"))
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()
        assertTrue(vm.uiState.saveStatus is SaveStatus.PendingSync)

        session.setConnected(true)
        advanceUntilIdle()

        assertEquals("cached-offline", client.files[loc.remotePath])
        assertNull(dao.get(loc.remotePath))
        assertTrue(vm.uiState.saveStatus is SaveStatus.Saved)
    }

    @Test
    fun `undo and redo drive content and flags`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "a"
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()

        vm.onContentChange("ab")
        assertTrue(vm.uiState.canUndo)

        vm.undo()
        assertEquals("a", vm.uiState.content)
        assertTrue(vm.uiState.canRedo)

        vm.redo()
        assertEquals("ab", vm.uiState.content)
        assertFalse(vm.uiState.canRedo)
    }

    @Test
    fun `onContentChange ignored when not editable`() = runTest {
        repo.add(loc)
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()

        vm.onContentChange("should be ignored")
        assertEquals("", vm.uiState.content)
    }

    @Test
    fun `consumePendingEdit opens the handed path transiently and clears it`() = runTest {
        client.files["/etc/app.conf"] = "server-content"
        session.setConnected(true)
        session.pendingEditPath = "/etc/app.conf"
        val vm = vm()
        advanceUntilIdle()

        vm.consumePendingEdit()
        advanceUntilIdle()

        assertEquals("server-content", vm.uiState.content)
        assertEquals("/etc/app.conf", vm.uiState.openLocation?.remotePath)
        assertNull(session.pendingEditPath)
        assertTrue(vm.uiState.locations.isEmpty())
    }

    @Test
    fun `consumePendingEdit with no pending path does nothing`() = runTest {
        val vm = vm()
        advanceUntilIdle()

        vm.consumePendingEdit()
        advanceUntilIdle()

        assertNull(vm.uiState.openLocation)
    }

    @Test
    fun `permission-denied save surfaces error and does not cache`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "old"
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()
        vm.onContentChange("edited")
        // Structured status flag (JSch id == 3), message intentionally lacks the word
        // "permission" to prove detection doesn't rely on string matching.
        client.writeError = SftpException("Failed to write ${loc.remotePath}", permissionDenied = true)

        vm.saveNow()
        advanceUntilIdle()

        assertTrue(vm.uiState.saveStatus is SaveStatus.Error)
        assertNull(dao.get(loc.remotePath))
    }

    @Test
    fun `permission-denied detected via message fallback also surfaces error`() = runTest {
        repo.add(loc)
        client.files[loc.remotePath] = "old"
        session.setConnected(true)
        val vm = vm()
        advanceUntilIdle()
        vm.open(loc)
        advanceUntilIdle()
        vm.onContentChange("edited")
        client.writeError = SftpException("Failed to write ${loc.remotePath}: Permission denied")

        vm.saveNow()
        advanceUntilIdle()

        assertTrue(vm.uiState.saveStatus is SaveStatus.Error)
        assertNull(dao.get(loc.remotePath))
    }
}

private class FakeSftpClient : ISftpClient {
    val files = mutableMapOf<String, String>()
    var readThrows = false
    var writeThrows = false
    var writeError: SftpException? = null
    var readCount = 0

    override suspend fun readText(path: String): String {
        readCount++
        if (readThrows) throw SftpException("read failed")
        return files[path] ?: throw SftpException("no such file")
    }

    override suspend fun writeText(path: String, content: String) {
        writeError?.let { throw it }
        if (writeThrows) throw SftpException("write failed")
        files[path] = content
    }

    override suspend fun connect(host: String, port: Int, user: String, password: String?): HostKeyResult =
        HostKeyResult.Trusted
    override suspend fun trustAndProceed(host: String) {}
    override suspend fun homeDirectory(): String = "/"
    override suspend fun listFiles(path: String): List<RemoteFile> = emptyList()
    override suspend fun disconnect() {}
    override suspend fun delete(path: String) {}
    override suspend fun rename(oldPath: String, newPath: String) {}
    override suspend fun download(remotePath: String, destFilePath: String, onProgress: (Long, Long) -> Unit) {}
    override suspend fun upload(srcFilePath: String, remotePath: String, onProgress: (Long, Long) -> Unit) {}
    override suspend fun downloadWithResume(remotePath: String, destFilePath: String, skip: Long, onProgress: (Long, Long) -> Unit) {}
    override suspend fun uploadWithResume(srcFilePath: String, remotePath: String, skip: Long, onProgress: (Long, Long) -> Unit) {}
}
