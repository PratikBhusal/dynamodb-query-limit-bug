package org.example.persistence.ddb.dao

import java.util.UUID
import org.example.persistence.ddb.model.ProductRecord

interface ProductDao {

    fun query(categoryId: UUID, limit: UInt?, isStronglyConsistentRead: Boolean = false): List<ProductRecord>
}
