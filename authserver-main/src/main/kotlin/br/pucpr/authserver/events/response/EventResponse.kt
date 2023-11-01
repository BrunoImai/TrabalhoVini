package br.pucpr.authserver.events.response

import br.pucpr.authserver.events.Category

data class EventResponse(
    var id: Long? = null,
    var name: String = "",
    var local: String = "",
    var hour: String = "",
    var description: String = "",
    var creatorId: Long,
    var categoryId: Long
)
