package br.pucpr.authserver.events

import br.pucpr.authserver.events.requests.CategoryRequest
import br.pucpr.authserver.events.response.CategoryResponse
import br.pucpr.authserver.events.response.EventResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*


@RestController
@RequestMapping("/events")
class EventController(val service: EventsService) {

    @GetMapping
    fun listEvents(): ResponseEntity<List<EventResponse>> = ResponseEntity.ok(service.findAllEvents().map { it.toResponse() })

    @GetMapping("/category/{id}")
    fun getEventsByCategory(@PathVariable id: Long) =
        service.findEventsByCategory(id)
            .map { ResponseEntity.ok(it.toResponse()) }

    @GetMapping("/{id}")
    fun getEventById(@PathVariable id: Long) =
        service.findEventById(id)
            ?.let { ResponseEntity.ok(it.toResponse()) }
            ?: ResponseEntity.notFound().build()

    @DeleteMapping("/category/{id}")
    fun deleteEvent(@PathVariable id: Long): ResponseEntity<Void> {
        service.deleteCategory(id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/sorted")
    fun listEventsSortedByName(): ResponseEntity<List<EventResponse>> {
        val sortedEvents = service.findAllEventsOrderedByName()
        return ResponseEntity.ok(sortedEvents.map { it.toResponse() })
    }


    // Categories
    @PostMapping("/category")
    fun createCategory(@Valid @RequestBody req: CategoryRequest) =
        service.createCategory(req)!!
            .toResponse()
            .let { ResponseEntity.status(HttpStatus.CREATED).body(it) }


    @GetMapping("/category")
    fun listCategories(): ResponseEntity<List<CategoryResponse>> = ResponseEntity.ok(service.findAllCategories().map { it.toResponse() })
}