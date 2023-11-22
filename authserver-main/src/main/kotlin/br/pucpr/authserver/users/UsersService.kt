package br.pucpr.authserver.users

import br.pucpr.authserver.events.Category
import br.pucpr.authserver.events.CategoryRepository
import br.pucpr.authserver.events.Event
import br.pucpr.authserver.events.EventsRepository
import br.pucpr.authserver.events.requests.EventRequest
import br.pucpr.authserver.exception.BadRequestException
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.users.requests.LoginRequest
import br.pucpr.authserver.users.requests.UserRequest
import br.pucpr.authserver.users.responses.LoginResponse
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Sort
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class UsersService(
    val repository: UsersRepository,
    val rolesRepository: RolesRepository,
    val eventRepository: EventsRepository,
    val categoryRepository: CategoryRepository,
    val request: HttpServletRequest,
    val jwt: Jwt
) {

    private fun getUserIdFromToken(): Long {
        val authentication = jwt.extract(request)

        return authentication?.let {
            val user = it.principal as UserToken
            user.id
        } ?: throw IllegalStateException("User is not authenticated")
    }

    fun save(req: UserRequest): User {
        val user = User(
            email = req.email!!,
            password = req.password!!,
            name = req.name!!,
            events = mutableListOf()
        )
        val userRole = rolesRepository.findByName("USER")
            ?: throw IllegalStateException("Role 'USER' not found!")

        user.roles.add(userRole)
        return repository.save(user)
    }

    fun getById(id: Long) = repository.findByIdOrNull(id)

    fun findAll(role: String?): List<User> =
        if (role == null) repository.findAll(Sort.by("name"))
        else repository.findAllByRole(role)

    fun login(credentials: LoginRequest): LoginResponse? {
        val user = repository.findByEmail(credentials.email!!) ?: return null
        if (user.password != credentials.password) return null
        log.info("User logged in. id={} name={}", user.id, user.name)
        return LoginResponse(
            token = jwt.createToken(user),
            user.toResponse()
        )
    }

    fun delete(id: Long): Boolean {
        val user = repository.findByIdOrNull(id) ?: return false
        if (user.roles.any { it.name == "ADMIN" }) {
            val count = repository.findAllByRole("ADMIN").size
            if (count == 1)  throw BadRequestException("Cannot delete the last system admin!")
        }
        log.warn("User deleted. id={} name={}", user.id, user.name)
        repository.delete(user)
        return true
    }


    // EVENTS

    fun findEventById(id: Long) = eventRepository.findByIdOrNull(id)

    fun saveEvent(eventRequest: EventRequest): Event {
        val user = getById(eventRequest.creatorId) ?: throw IllegalStateException("User don't exist!")
        val category = getCategoryById(eventRequest.categoryId) ?: throw IllegalStateException("Category don't exist!")
        val event = Event(
            name = eventRequest.name,
            creator = user,
            description = eventRequest.description,
            hour = eventRequest.hour,
            local = eventRequest.local,
            category = category
        )
        return eventRepository.save(event)
    }

    fun updateEvent(id: Long, updatedEvent: EventRequest): Event? {
        eventRepository.findByIdOrNull(id) ?: return null
        val user = getById(updatedEvent.creatorId) ?: throw IllegalStateException("User don't exist!")
        val category = getCategoryById(updatedEvent.categoryId) ?: throw IllegalStateException("Category don't exist!")
        if (user.id != getUserIdFromToken()) throw IllegalStateException("User isn't the creator!")
        val newEvent = Event(
            name = updatedEvent.name,
            local = updatedEvent.local,
            hour = updatedEvent.hour,
            description = updatedEvent.description,
            creator = user,
            category = category
        )
        return eventRepository.save(newEvent)
    }

    fun deleteEvent(id: Long) : Boolean? {
        val event = findEventById(id) ?: return null
        val currentUser = getById(getUserIdFromToken()) ?: throw IllegalStateException("User don't exist!")
        if ((event.creator.id != currentUser.id) || !currentUser.roles.any { it.name == "ADMIN" }) return null
        eventRepository.deleteById(id)
        return true
    }

    fun listAllEventsFromUser(id: Long) : List<Event>? {
        getById(id) ?: throw IllegalStateException("User don't exist!")
        val test = eventRepository.findByCreatorId(id)
        return eventRepository.findByCreatorId(id)
    }

    // Category

    fun getCategoryById(id: Long) = categoryRepository.findByIdOrNull(id)

    companion object {
        val log = LoggerFactory.getLogger(UsersService::class.java)
    }
}

