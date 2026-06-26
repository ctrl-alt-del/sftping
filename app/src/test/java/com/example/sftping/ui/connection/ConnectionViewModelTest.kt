package com.example.sftping.ui.connection

import com.example.sftping.data.connection.ConnectionProfile
import com.example.sftping.data.connection.ConnectionRepository
import com.example.sftping.security.SecretStore
import com.example.sftping.sftp.HostKeyResult
import com.example.sftping.sftp.ISftpClient
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify

@OptIn(ExperimentalCoroutinesApi::class)
class ConnectionViewModelTest {

    private val client = mock<ISftpClient>()
    private val repo = mock<ConnectionRepository>()
    private val secretStore = mock<SecretStore>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `form fields update state`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()

        val vm = ConnectionViewModel(client, repo, secretStore)

        vm.updateHost("10.0.0.1")
        assertEquals("10.0.0.1", vm.uiState.host)

        vm.updatePort("2222")
        assertEquals("2222", vm.uiState.port)

        vm.updateUsername("admin")
        assertEquals("admin", vm.uiState.username)

        vm.updatePassword("secret")
        assertEquals("secret", vm.uiState.password)
    }

    @Test
    fun `connect with trusted key succeeds and clears error`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doReturn(HostKeyResult.Trusted).`when`(client).connect(any(), any(), any(), any())

        val vm = ConnectionViewModel(client, repo, secretStore)
        vm.updateHost("10.0.0.1")
        vm.updatePort("22")
        vm.updateUsername("admin")
        vm.updatePassword("pw")
        vm.connect()
        advanceUntilIdle()

        verify(client).connect("10.0.0.1", 22, "admin", "pw")
        assertNull(vm.uiState.error)
        assertTrue(vm.uiState.connecting.not())
    }

    @Test
    fun `connect sets hostKeyResult to Unknown on first connection`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doReturn(HostKeyResult.Unknown("10.0.0.1", "SHA256:abc", "ssh-ed25519"))
            .`when`(client).connect(any(), any(), any(), any())

        val vm = ConnectionViewModel(client, repo, secretStore)
        vm.updateHost("10.0.0.1")
        vm.updatePort("22")
        vm.updateUsername("admin")
        vm.updatePassword("pw")
        vm.connect()

        assertTrue(vm.uiState.hostKeyResult is HostKeyResult.Unknown)
    }

    @Test
    fun `connect sets error on SftpException`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doAnswer { throw SftpException("Connection refused") }
            .`when`(client).connect(any(), any(), any(), any())

        val vm = ConnectionViewModel(client, repo, secretStore)
        vm.updateHost("10.0.0.1")
        vm.updatePort("22")
        vm.updateUsername("admin")
        vm.updatePassword("pw")
        vm.connect()

        assertEquals("Connection refused", vm.uiState.error)
    }

    @Test
    fun `selectRecent populates fields and loads password`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doReturn("decrypted").`when`(secretStore).unseal("10.0.0.1:22:admin")

        val vm = ConnectionViewModel(client, repo, secretStore)
        vm.selectRecent(ConnectionProfile("10.0.0.1", 22, "admin"))

        assertEquals("10.0.0.1", vm.uiState.host)
        assertEquals("22", vm.uiState.port)
        assertEquals("admin", vm.uiState.username)
        assertEquals("decrypted", vm.uiState.password)
    }
}
