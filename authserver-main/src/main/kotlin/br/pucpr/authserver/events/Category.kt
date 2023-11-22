package br.pucpr.authserver.events

import br.pucpr.authserver.events.response.CategoryResponse
import jakarta.persistence.*

@Entity
@Table(name = "Category")
class Category (
    @Id
    @GeneratedValue
    var id: Long? = null,

    @Column(nullable = false)
    var name: String = "",

    @OneToMany(mappedBy = "category", cascade = [CascadeType.ALL], orphanRemoval = true)
    val events : MutableList<Event>,
    )
{
    fun toResponse() = CategoryResponse(id, name)
}