package br.pucpr.authserver

import br.pucpr.authserver.events.Category
import kotlin.random.Random
import br.pucpr.authserver.events.Event
import br.pucpr.authserver.users.Role
import br.pucpr.authserver.users.User

fun randomString(
    length: Int = 10, allowedChars: List<Char> =
        ('A'..'Z') + ('a'..'z') + ('0'..'9')
) = (1..length)
    .map { allowedChars.random() }
    .joinToString()

object Stubs {
    fun userStub(
        id: Long? = Random.nextLong(1, 1000),
        roles: List<String> = listOf("USER"),
        eventList: List<Event> = emptyList()
    ): User {
        val name = "user-${id ?: "new"}"
        return User(
            id = id,
            name = name,
            email = "$name@email.com",
            password = randomString(),
            roles = roles
                .mapIndexed { i, it -> Role(i.toLong(), it) }
                .toMutableSet(),
            events = eventList.toMutableList()
        )
    }

    fun eventStub(
        id: Long? = Random.nextLong(1, 1000),
        creator: User,
        category: Category
    ): Event {
        val name = "event-${id ?: "new"}"
        return Event(
            id = id,
            name = name,
            local = "Event Location",
            hour = "Event Hour",
            description = "Event Description",
            creator = creator,
            category = category
        )
    }

    fun categoryStub(
        id: Long? = Random.nextLong(1, 1000),
        eventList: List<Event> = emptyList()
    ): Category {
        val name = "category-${id ?: "new"}"
        return Category(
            id = id,
            name = name,
            events = eventList.toMutableList()
        )
    }
}