package br.pucpr.authserver.events

import br.pucpr.authserver.events.response.EventResponse
import br.pucpr.authserver.users.User
import jakarta.persistence.*

@Entity
@Table(name = "Events")
class Event (
    @Id @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @Column(nullable = false)
    var local: String = "",

    var hour: String = "",

    var description: String = "",

    @ManyToOne
    @JoinColumn(name = "creator_id")
    val creator: User,

    @ManyToOne
    @JoinColumn(name = "category")
    val category: Category,

    )
{
    fun toResponse() = EventResponse(id, name,local,hour,description, creator.id!!, category.id!!)
}