package org.example.persistence.ddb.model

import java.time.Instant
import java.util.UUID
import org.example.persistence.ddb.Constants.DYNAMODB_PARTITION_KEY_ATTRIBUTE_SHORTNAME
import org.example.persistence.ddb.Constants.DYNAMODB_SORT_KEY_ATTRIBUTE_SHORTNAME
import org.example.persistence.ddb.Constants.DYNAMODB_TIME_TO_LIVE_ATTRIBUTE_SHORTNAME
import org.example.persistence.ddb.converter.InstantAsEpochSecondAttributeConverter
import org.example.persistence.util.model.UUIDv7
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbAttribute
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbConvertedBy
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbIgnore
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbImmutable
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbSortKey

@Suppress("detekt:complexity:LongParameterList")
@DynamoDbImmutable(builder = ProductRecord.Builder::class)
class ProductRecord private constructor(
    @get:DynamoDbPartitionKey
    @get:DynamoDbAttribute(DYNAMODB_PARTITION_KEY_ATTRIBUTE_SHORTNAME)
    val categoryId: UUID = UUID.randomUUID(),

    @get:DynamoDbSortKey
    @get:DynamoDbAttribute(DYNAMODB_SORT_KEY_ATTRIBUTE_SHORTNAME)
    @get:JvmName("getProductId")
    val productId: UUIDv7,

    val name: String,

    @get:DynamoDbAttribute(DYNAMODB_TIME_TO_LIVE_ATTRIBUTE_SHORTNAME)
    @get:DynamoDbConvertedBy(InstantAsEpochSecondAttributeConverter::class)
    val timeToLive: Instant? = null,
) {
    companion object {
        const val TABLE_NAME = "product_v1"

        @JvmStatic
        fun builder() = Builder()

        inline fun build(block: Builder.() -> Unit) = Builder().apply(block).build()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ProductRecord

        if (categoryId != other.categoryId) return false
        if (productId != other.productId) return false
        if (name != other.name) return false
        if (timeToLive != other.timeToLive) return false

        return true
    }

    override fun hashCode(): Int {
        var result = categoryId.hashCode()
        result = 31 * result + productId.hashCode()
        result = 31 * result + name.hashCode()
        result = 31 * result + (timeToLive?.hashCode() ?: 0)
        return result
    }

    override fun toString() = buildString {
        append("ProductRecord(")
        append("categoryId=$categoryId, ")
        append("productId=$productId, ")
        append("name='$name'")
        if (timeToLive != null) {
            append(", ")
            append("timeToLive=$timeToLive")
        }
        append(")")
    }

    class Builder {
        private var categoryId: UUID = UUID.randomUUID()
        private var productId: UUIDv7 = UUIDv7.randomUUIDv7()
        private lateinit var name: String
        private var timeToLive: Instant? = null

        fun categoryId(categoryId: UUID) = apply {
            this.categoryId = categoryId
        }

        @JvmName("productId")
        fun productId(productId: UUIDv7) = apply {
            this.productId = productId
        }

        fun name(name: String) = apply {
            this.name = name
        }

        fun timeToLive(timeToLive: Instant?) = apply {
            this.timeToLive = timeToLive
        }

        @DynamoDbIgnore
        fun from(record: ProductRecord) = apply {
            categoryId = record.categoryId
            productId = record.productId
            timeToLive = record.timeToLive
            timeToLive = record.timeToLive
        }

        fun build() = ProductRecord(categoryId, productId, name, timeToLive)
    }
}
