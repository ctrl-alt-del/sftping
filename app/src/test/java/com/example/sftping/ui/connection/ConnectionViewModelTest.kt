package com.example.sftping.ui.connection

import com.example.sftping.data.connection.ConnectionProfile
import com.example.sftping.data.connection.ConnectionRepository
import com.example.sftping.security.KnownHostsStore
import com.example.sftping.security.SecretStore
import com.example.sftping.security.TrustedHost
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
    private val knownHostsStore = mock<KnownHostsStore>()
    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() { Dispatchers.setMain(testDispatcher) }

    @After
    fun tearDown() { Dispatchers.resetMain() }

    @Test
    fun `form fields update state`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)

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
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doReturn(HostKeyResult.Trusted).`when`(client).connect(any(), any(), any(), any())

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)
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
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doReturn(HostKeyResult.Unknown("10.0.0.1", "SHA256:abc", "ssh-ed25519"))
            .`when`(client).connect(any(), any(), any(), any())

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)
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
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doAnswer { throw SftpException("Connection refused") }
            .`when`(client).connect(any(), any(), any(), any())

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)
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
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doReturn("decrypted").`when`(secretStore).unseal("10.0.0.1:22:admin")

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)
        vm.selectRecent(ConnectionProfile("10.0.0.1", 22, "admin"))

        assertEquals("10.0.0.1", vm.uiState.host)
        assertEquals("22", vm.uiState.port)
        assertEquals("admin", vm.uiState.username)
        assertEquals("decrypted", vm.uiState.password)
    }

    @Test
    fun `loadTrustedHosts populates state on init`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doReturn(
            listOf(
                TrustedHost("a", "SHA256:a", "ssh-ed25519", 1L),
                TrustedHost("b", "SHA256:b", "ssh-rsa", 2L)
            )
        ).`when`(knownHostsStore).all()

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)
        advanceUntilIdle()

        assertEquals(2, vm.uiState.trustedHosts.size)
    }

    @Test
    fun `revokeTrustedHost removes and refreshes`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)
        vm.revokeTrustedHost("example.com")
        advanceUntilIdle()

        verify(knownHostsStore).remove("example.com")
    }

    @Test
    fun `revokeAndReverify removes stored key and re-verifies as Unknown`() = runTest {
        doReturn(emptyList<ConnectionProfile>()).`when`(repo).loadRecent()
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doReturn(emptyList<TrustedHost>()).`when`(knownHostsStore).all()
        doReturn(
            HostKeyResult.Changed("10.0.0.1", "SHA256:old", "SHA256:new"),
            HostKeyResult.Unknown("10.0.0.1", "SHA256:new", "ssh-rsa")
        ).`when`(client).connect(any(), any(), any(), any())

        val vm = ConnectionViewModel(client, repo, secretStore, knownHostsStore)
        vm.updateHost("10.0.0.1")
        vm.updatePort("22")
        vm.updateUsername("admin")
        vm.updatePassword("pw")
        vm.connect()
        advanceUntilIdle()
        assertTrue(vm.uiState.hostKeyResult is HostKeyResult.Changed)

        vm.revokeAndReverify()
        advanceUntilIdle()

        verify(knownHostsStore).remove("10.0.0.1")
        assertTrue(vm.uiState.hostKeyResult is HostKeyResult.Unknown)
    }
}
