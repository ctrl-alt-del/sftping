package com.example.sftping.ui.files

import android.content.Context
import android.net.Uri
import com.example.sftping.sftp.ISftpClient
import com.example.sftping.sftp.RemoteFile
import com.example.sftping.sftp.SessionState
import com.example.sftping.sftp.SftpException
import com.example.sftping.transfer.TransferManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class FilesViewModelTest {

    private val client = mock<ISftpClient>()
    private val transferManager = mock<TransferManager>()
    private val context = mock<Context>()
    private val sessionState = SessionState()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `loadFiles sorts directories first then by name`() = runTest {
        doReturn(
            listOf(
                RemoteFile("zebra", "/zebra", 10, 0, false),
                RemoteFile("Apple", "/Apple", 0, 0, true),
                RemoteFile("beta", "/beta", 10, 0, false),
                RemoteFile("alpha", "/alpha", 10, 0, false)
            )
        ).`when`(client).listFiles("/")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")

        val files = vm.uiState.files
        assertEquals(4, files.size)
        assertTrue(files[0].isDirectory)
        assertEquals("Apple", files[0].name)
        assertTrue(files[1].isDirectory.not())
        assertEquals("alpha", files[1].name)
        assertEquals("beta", files[2].name)
        assertEquals("zebra", files[3].name)
    }

    @Test
    fun `navigateTo builds correct path and loads files`() = runTest {
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles(any())

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")
        vm.navigateTo("ops")

        assertEquals("/ops", vm.uiState.currentPath)
        verify(client).listFiles("/ops")
        assertTrue(vm.canGoBack())
    }

    @Test
    fun `navigateBack returns to previous directory`() = runTest {
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles(any())

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")
        vm.navigateTo("ops")
        vm.navigateBack()

        assertEquals("/", vm.uiState.currentPath)
        assertFalse(vm.canGoBack())
    }

    @Test
    fun `toggleSelection enters and exits multiSelectMode`() = runTest {
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles("/")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")

        vm.toggleSelection("/a.txt")
        assertTrue(vm.uiState.multiSelectMode)
        assertEquals(listOf("/a.txt"), vm.uiState.selectedPaths)

        vm.toggleSelection("/a.txt")
        assertFalse(vm.uiState.multiSelectMode)
        assertTrue(vm.uiState.selectedPaths.isEmpty())
    }

    @Test
    fun `deleteSelected calls delete and refreshes`() = runTest {
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles(any())

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")

        vm.toggleSelection("/a.txt")
        vm.toggleSelection("/b.txt")
        vm.deleteSelected()

        verify(client).delete("/a.txt")
        verify(client).delete("/b.txt")
        assertFalse(vm.uiState.multiSelectMode)
    }

    @Test
    fun `renameFile calls rename and refreshes`() = runTest {
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles(any())
        val file = RemoteFile("old.txt", "/old.txt", 100, 0, false)

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")
        vm.startRename(file)
        assertEquals("old.txt", vm.uiState.renamingFile?.name)

        vm.renameFile("new.txt")
        verify(client).rename("/old.txt", "/new.txt")
        assertNull(vm.uiState.renamingFile)
    }

    @Test
    fun `error from listFiles sets error state`() = runTest {
        doAnswer { throw SftpException("Permission denied") }
            .`when`(client).listFiles("/")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")

        assertEquals("Permission denied", vm.uiState.error)
    }

    @Test
    fun `loadFiles without argument uses sessionState initial directory`() = runTest {
        sessionState.initialDirectory = "/home/user"
        doReturn(listOf(RemoteFile("file.txt", "/home/user/file.txt", 100, 0, false)))
            .`when`(client).listFiles("/home/user")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles()

        assertEquals("/home/user", vm.uiState.currentPath)
        assertEquals("/home/user/file.txt", vm.uiState.files.first().path)
    }

    @Test
    fun `toggleShowHidden reveals dot-files without re-listing`() = runTest {
        doReturn(
            listOf(
                RemoteFile("visible.txt", "/visible.txt", 10, 0, false),
                RemoteFile(".hidden", "/.hidden", 5, 0, false)
            )
        ).`when`(client).listFiles("/")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")
        assertFalse(vm.uiState.files.any { it.name == ".hidden" })

        vm.toggleShowHidden()
        assertTrue(vm.uiState.files.any { it.name == ".hidden" })
        verify(client, times(1)).listFiles("/")
    }

    @Test
    fun `setSearchQuery filters in-memory without re-listing`() = runTest {
        doReturn(
            listOf(
                RemoteFile("alpha.txt", "/alpha.txt", 0, 0, false),
                RemoteFile("beta.log", "/beta.log", 0, 0, false)
            )
        ).`when`(client).listFiles("/")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")

        vm.setSearchQuery("log")
        assertEquals(listOf("beta.log"), vm.uiState.files.map { it.name })
        verify(client, times(1)).listFiles("/")
    }

    @Test
    fun `setSortMode reorders in-memory without re-listing`() = runTest {
        doReturn(
            listOf(
                RemoteFile("a.txt", "/a.txt", 0, 0, false),
                RemoteFile("b.txt", "/b.txt", 0, 0, false)
            )
        ).`when`(client).listFiles("/")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")
        assertEquals(listOf("a.txt", "b.txt"), vm.uiState.files.map { it.name })

        vm.setSortMode(SortMode.NAME_DESC)
        assertEquals(listOf("b.txt", "a.txt"), vm.uiState.files.map { it.name })
        verify(client, times(1)).listFiles("/")
    }

    @Test
    fun `navigateTo clears search but keeps sort and hidden`() = runTest {
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles(any())

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.loadFiles("/")
        vm.setSearchQuery("x")
        vm.setSortMode(SortMode.SIZE)
        vm.toggleShowHidden()

        vm.navigateTo("sub")

        assertEquals("", vm.uiState.searchQuery)
        assertEquals(SortMode.SIZE, vm.uiState.sortMode)
        assertTrue(vm.uiState.showHidden)
    }

    @Test
    fun `onEnterScreen loads initial directory on first entry`() = runTest {
        sessionState.epoch = 1
        sessionState.initialDirectory = "/home/u"
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles("/home/u")

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.onEnterScreen()

        assertEquals("/home/u", vm.uiState.currentPath)
        verify(client).listFiles("/home/u")
    }

    @Test
    fun `onEnterScreen reloads last path within same session`() = runTest {
        sessionState.epoch = 1
        sessionState.initialDirectory = "/home/u"
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles(any())

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.onEnterScreen()
        vm.navigateTo("logs")
        assertEquals("/home/u/logs", vm.uiState.currentPath)

        vm.onEnterScreen()
        assertEquals("/home/u/logs", vm.uiState.currentPath)
    }

    @Test
    fun `onEnterScreen resets to initial directory on new session`() = runTest {
        sessionState.epoch = 1
        sessionState.initialDirectory = "/home/u"
        doReturn(emptyList<RemoteFile>()).`when`(client).listFiles(any())

        val vm = FilesViewModel(client, transferManager, context, sessionState)
        vm.onEnterScreen()
        vm.navigateTo("logs")
        assertTrue(vm.canGoBack())

        sessionState.epoch = 2
        sessionState.initialDirectory = "/srv"
        vm.onEnterScreen()

        assertEquals("/srv", vm.uiState.currentPath)
        assertFalse(vm.canGoBack())
    }

    private fun assertNull(value: Any?) {
        org.junit.Assert.assertNull(value)
    }
}
