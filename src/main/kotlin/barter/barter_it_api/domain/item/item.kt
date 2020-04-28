package barter.barter_it_api.domain.item

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import java.time.LocalDateTime
import javax.validation.constraints.*

@Document
data class Item(@Id val id: String? = null,
                val userId: String?,
                val name: String?,
                val description: String?,
                val category: Categories?,
                val condition: Conditions?,
                val mark: String?,
                val createdAt: LocalDateTime? = LocalDateTime.now(),
                val count: Int? = 1,
                val imageIds: List<String>?,
                val proposals: MutableList<Proposal>?,
                val idsOfItemsProposedInOtherItems: List<String>?
)

data class ItemRequest(
        @get:NotBlank(message = "notBlank")
        val name: String?,

        val userId: String?,

        val description: String?,

        @get:NotNull(message = "required")
        val category: Categories?,

        @get:NotNull(message = "required")
        val condition: Conditions?,

        val mark: String?,

        @get:Min(value = 1, message = "tooSmall")
        val count: Int? = 1,

        val imageIds: List<String>?,

        val proposals: MutableList<Proposal>?,

        val proposedInOthersList: List<String>?
)

data class Proposal(
        @get:NotNull(message = "required")
        var status: Status,

        @get:NotNull(message = "required")
        val item: Item
)

enum class Categories {
    AUTOMOTIVE,
    JEWELRY_AND_WATCHES,
    HOUSEHOLD,
    CLOTHES,
    FURNITURE
}

enum class Conditions {
    VERY_GOOD,
    GOOD,
    DAMAGED
}

enum class Status {
    PENDING,
    REJECTED,
    ACCEPTED
}

fun ItemRequest.toItem() = Item(
        name = this.name,
        userId = this.userId,
        description = this.description,
        category = this.category,
        condition = this.condition,
        mark = this.mark,
        count = this.count,
        imageIds = this.imageIds,
        proposals = this.proposals,
        idsOfItemsProposedInOtherItems = this.proposedInOthersList
)