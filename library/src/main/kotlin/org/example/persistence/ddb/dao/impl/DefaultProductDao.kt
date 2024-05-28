package org.example.persistence.ddb.dao.impl

import arrow.core.NonEmptyList
import java.util.UUID
import kotlinx.collections.immutable.toImmutableList
import org.example.persistence.ddb.dao.ProductDao
import org.example.persistence.ddb.model.ProductRecord
import org.example.persistence.util.model.PositiveInt
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

    /**
     * @see <a href="https://github.com/aws/aws-sdk-java-v2/issues/1951">aws-sdk-java-v2/issues/1951</a>
     * @see <a href="https://github.com/aws/aws-sdk-java-v2/issues/3226#issuecomment-1151398233">aws-sdk-java-v2/issues/3226</a>
     */
    @Suppress("detekt:style:MaxLineLength")
    override fun query(categoryId: UUID, itemsPerPage: PositiveInt?, isStronglyConsistentRead: Boolean) =
        table.queryWithEnhancedRequest {
            keyEqualTo {
                partitionValue(categoryId.toString())
            }
            consistentRead(isStronglyConsistentRead)
            limit(itemsPerPage?.toInt())
            scanIndexForward(false)
        }
            .asSequence()
            .filter { it.count() > 0 }
            .map { it.items().toNonEmptyListOrThrow() }
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

private fun <A> Iterable<A>.toNonEmptyListOrThrow(): NonEmptyList<A> {
    val iter = iterator()
    check(iter.hasNext()) {
        "List should have at least 1 item"
    }

    return NonEmptyList(iter.next(), Iterable { iter }.toImmutableList())
}
