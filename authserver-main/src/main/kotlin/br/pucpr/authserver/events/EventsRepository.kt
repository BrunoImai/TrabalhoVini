package br.pucpr.authserver.events

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface EventsRepository : JpaRepository<Event, Long> {
    fun findByCreatorId(creatorId: Long): List<Event>

    fun findByCategoryId(categoryId: Long): List<Event>
}