package br.pucpr.authserver.events

import br.pucpr.authserver.events.requests.CategoryRequest
import br.pucpr.authserver.security.Jwt
import br.pucpr.authserver.security.UserToken
import br.pucpr.authserver.users.UsersRepository
import br.pucpr.authserver.users.UsersService
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class EventsService(
    val eventRepository: EventsRepository,
    val categoryRepository: CategoryRepository,
    val usersRepository: UsersRepository,
    val request: HttpServletRequest,
    val jwt: Jwt
) {

    fun getUserIdFromToken(): Long {
        val authentication = jwt.extract(request)

        return authentication?.let {
            val user = it.principal as UserToken
            user.id
        } ?: throw IllegalStateException("User is not authenticated")
    }

    fun findAllEvents(): List<Event> = eventRepository.findAll()

    fun findEventById(id: Long) = eventRepository.findByIdOrNull(id)

    fun findAllEventsOrderedByName(): List<Event> {
        return eventRepository.findAllEventsSortedByName()
    }

    // Categories

    fun findEventsByCategory(categoryId: Long) = eventRepository.findByCategoryId(categoryId)

    fun createCategory(category : CategoryRequest) : Category?{
        val currentUser = usersRepository.findByIdOrNull(getUserIdFromToken()) ?: return null
        if (!currentUser.roles.any { it.name == "ADMIN" }) throw IllegalStateException("Not accepted!")
        val newCategory = Category (
            name = category.name,
            events = mutableListOf()
        )
        return categoryRepository.save(newCategory)
    }

    fun deleteCategory(id: Long) : Unit? {
        val currentUser = usersRepository.findByIdOrNull(getUserIdFromToken()) ?: return null
        if (!currentUser.roles.any { it.name == "ADMIN" }) throw IllegalStateException("Not accepted!")
        val category = categoryRepository.findByIdOrNull(id) ?: throw IllegalStateException("Category don't exists!")
        return categoryRepository.delete(category)
    }

    fun findAllCategories(): List<Category> = categoryRepository.findAll()

    companion object {
        val log = LoggerFactory.getLogger(UsersService::class.java)
    }
}