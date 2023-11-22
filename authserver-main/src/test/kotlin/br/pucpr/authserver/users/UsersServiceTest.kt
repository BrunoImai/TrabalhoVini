package br.pucpr.authserver.users

import br.pucpr.authserver.events.CategoryRepository
import br.pucpr.authserver.events.EventsRepository
import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.Stubs.userStub
import br.pucpr.authserver.events.Category
import br.pucpr.authserver.events.Event
import br.pucpr.authserver.events.requests.EventRequest
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.users.requests.LoginRequest
import br.pucpr.authserver.users.requests.UserRequest
import br.pucpr.authserver.users.responses.LoginResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.throwable.shouldHaveMessage
import io.mockk.*
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.Test
import org.mockito.Mockito.verify
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.core.Authentication

internal class UsersServiceTest {
    private val usersRepositoryMock = mockk<UsersRepository>()
    private val rolesRepositoryMock = mockk<RolesRepository>()
    private val eventsRepositoryMock = mockk<EventsRepository>()
    private val categoryRepositoryMock = mockk<CategoryRepository>()
    private val requestMock = mockk<HttpServletRequest>()
    private val jwtMock = mockk<Jwt>()

    private val service = UsersService(usersRepositoryMock, rolesRepositoryMock, eventsRepositoryMock, categoryRepositoryMock, requestMock, jwtMock)

    @Test
    fun `Delete should return false if the user does not exists`() {
        every { usersRepositoryMock.findByIdOrNull(1) } returns null
        service.delete(1) shouldBe false
    }

    @Test
    fun `Delete must return true if the user is deleted`() {
        val user = userStub()
        every { usersRepositoryMock.findByIdOrNull(1) } returns user
        justRun { usersRepositoryMock.delete(user) }
        service.delete(1) shouldBe true
    }

    @Test
    fun `Delete should throw a BadRequestException if the user is the last admin`() {
        every { usersRepositoryMock.findByIdOrNull(1) } returns userStub(roles = listOf("ADMIN"))
        every {
            usersRepositoryMock.findAllByRole("ADMIN")
        } returns listOf(userStub(roles = listOf("ADMIN")))

        shouldThrow<BadRequestException> {
            service.delete(1)
        } shouldHaveMessage "Cannot delete the last system admin!"
    }

    @Test
    fun `save should create a new user`() {
        val userRequest = UserRequest("email@example.com", "password", "Name")
        val userRole = Role(name = "USER")
        val savedUser = User(1L, "email@example.com", "password", "Name", mutableSetOf(userRole), mutableListOf())

        every { rolesRepositoryMock.findByName("USER") } returns userRole
        every { usersRepositoryMock.save(any()) } returns savedUser

        val result = service.save(userRequest)

        result shouldBe savedUser
        verify(exactly = 1) { usersRepositoryMock.save(any()) }
    }

    @Test
    fun `getById should return user when user exists`() {
        val user = User(1L, "email@example.com", "password", "Name", mutableSetOf(), mutableListOf())
        every { usersRepositoryMock.findByIdOrNull(1L) } returns user

        val result = service.getById(1L)

        result shouldBe user
    }

    @Test
    fun `findAll should return all users`() {
        val users = listOf(User(1L, "email@example.com", "password", "Name", mutableSetOf(), mutableListOf()))
        every { usersRepositoryMock.findAll(Sort.by("name")) } returns users

        val result = service.findAll(null)

        result shouldBe users
    }

    @Test
    fun `login should return login response when credentials are correct`() {
        val credentials = LoginRequest("email@example.com", "password")
        val user = User(1L, credentials.email!!, credentials.password!!, "Name", mutableSetOf(), mutableListOf())
        val token = "token"
        val loginResponse = LoginResponse(token, user.toResponse())

        every { usersRepositoryMock.findByEmail(credentials.email!!) } returns user
        every { jwtMock.createToken(user) } returns token

        val result = service.login(credentials)

        result shouldBe loginResponse
    }

    @Test
    fun `saveEvent should save and return new event`() {
        val eventRequest = EventRequest("Event Name",  "Location", "18:00", "Description", 1L, 1L)
        val user = User(1L, "email@example.com", "password", "Name", mutableSetOf(), mutableListOf())
        val category = Category(1L, "Category Name", mutableListOf())

        every { usersRepositoryMock.findByIdOrNull(1L) } returns user
        every { categoryRepositoryMock.findByIdOrNull(1L) } returns category
        every { eventsRepositoryMock.save(any()) } returnsArgument 0

        val result = service.saveEvent(eventRequest)

        result.name shouldBe "Event Name"
        result.creator shouldBe user
        result.category shouldBe category
    }

    @Test
    fun `deleteEvent should delete event when conditions are met`() {
        val user = User(1L, "email@example.com", "password", "Name", mutableSetOf(Role(name = "ADMIN")), mutableListOf())
        val event = Event(1L, "Event Name", "Location", "18:00", "Description", user, mockk())

        every { eventsRepositoryMock.findByIdOrNull(1L) } returns event
        every { usersRepositoryMock.findByIdOrNull(1L) } returns user
        every { jwtMock.extract(any()) } returns mockk<Authentication>().apply {
            every { principal } returns UserToken(1L, "Name", setOf("ADMIN"))
        }
        every { eventsRepositoryMock.deleteById(1L) } just Runs

        val result = service.deleteEvent(1L)

        result shouldBe true
    }


    @Test
    fun `listAllEventsFromUser should return events created by user`() {
        val user = User(1L, "email@example.com", "password", "Name", mutableSetOf(), mutableListOf())
        val events = listOf(Event(1L, "Event Name", "Location", "18:00", "Description", user, mockk()))

        every { usersRepositoryMock.findByIdOrNull(1L) } returns user
        every { eventsRepositoryMock.findByCreatorId(1L) } returns events

        val result = service.listAllEventsFromUser(1L)

        result shouldBe events
    }


}

