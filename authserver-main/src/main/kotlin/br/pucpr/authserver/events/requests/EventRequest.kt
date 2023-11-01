package br.pucpr.authserver.events.requests

import br.pucpr.authserver.events.Category

data class EventRequest(
    var name: String = "",
    var local: String = "",
    var hour: String = "",
    var description: String = "",
    var creatorId: Long,
    var categoryId: Long
)
