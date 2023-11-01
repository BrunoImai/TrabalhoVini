package br.pucpr.authserver.users

import br.pucpr.authserver.events.Event
import br.pucpr.authserver.users.responses.UserResponse
import jakarta.persistence.*
import jakarta.validation.constraints.Email

@Entity
@Table(name = "TblUser")
class User(
    @Id @GeneratedValue
    var id: Long? = null,

    @Email
    @Column(unique = true, nullable = false)
    var email: String,

    @Column(length = 50)
    var password: String,

    @Column(nullable = false)
    var name: String = "",

    @ManyToMany
    @JoinTable(
        name = "UserRole",
        joinColumns = [JoinColumn(name = "idUser")],
        inverseJoinColumns = [JoinColumn(name = "idRole")]
    )
    val roles: MutableSet<Role> = mutableSetOf(),

    @OneToMany(mappedBy = "creator", cascade = [CascadeType.ALL], orphanRemoval = true)
    val events : MutableList<Event>,
) {
    fun toResponse() = UserResponse(id!!, name, email)
}