package org.example.persistence.ddb.dao

import arrow.core.NonEmptyList
import java.util.UUID
import org.example.persistence.ddb.model.ProductRecord
import org.example.persistence.util.model.PositiveInt

interface ProductDao {

    fun query(
        categoryId: UUID,
        itemsPerPage: PositiveInt? = null,
        isStronglyConsistentRead: Boolean = false,
    ): Sequence<NonEmptyList<ProductRecord>>
}
