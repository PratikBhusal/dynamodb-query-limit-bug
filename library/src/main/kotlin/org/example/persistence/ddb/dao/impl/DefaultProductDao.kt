package org.example.persistence.ddb.dao.impl

import java.util.UUID
import org.example.persistence.ddb.dao.ProductDao
import org.example.persistence.ddb.model.ProductRecord
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.enhanced.dynamodb.TableSchema
import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable
import software.amazon.awssdk.enhanced.dynamodb.model.QueryConditional
import software.amazon.awssdk.enhanced.dynamodb.model.QueryEnhancedRequest

class DefaultProductDao(
    private val dynamoDbEnhancedClient: DynamoDbEnhancedClient,
) : ProductDao {

    private val table: DynamoDbTable<ProductRecord> = dynamoDbEnhancedClient.table(
        ProductRecord.TABLE_NAME,
        TableSchema.fromImmutableClass(ProductRecord::class.java),
    )

    override fun query(categoryId: UUID, limit: UInt?, isStronglyConsistentRead: Boolean) =
        table.queryWithEnhancedRequest {
            keyEqualTo {
                partitionValue(categoryId.toString())
            }
            consistentRead(isStronglyConsistentRead)
            limit(limit?.toInt())
            scanIndexForward(false)
        }
            .flatMap { it.items() }
}

private fun DynamoDbTable<ProductRecord>.queryWithEnhancedRequest(
    block: QueryEnhancedRequest.Builder.() -> Unit,
): PageIterable<ProductRecord> = query(QueryEnhancedRequest.builder().apply(block).build())

private fun QueryEnhancedRequest.Builder.keyEqualTo(block: Key.Builder.() -> Unit) = queryConditional(
    QueryConditional.keyEqualTo(
        Key
            .builder()
            .apply(block)
            .build(),
    ),
)
