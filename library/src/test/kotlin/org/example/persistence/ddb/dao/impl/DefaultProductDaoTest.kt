package org.example.persistence.ddb.dao.impl

import io.kotest.assertions.withClue
import io.kotest.matchers.collections.shouldBeSmallerThan
import io.kotest.matchers.collections.shouldContainAll
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.ints.shouldBeLessThanOrEqual
import java.util.UUID
import org.example.persistence.ddb.Constants.DYNAMODB_MAX_ITEMS_PER_WRITEBATCH
import org.example.persistence.ddb.Constants.DYNAMODB_PARTITION_KEY_ATTRIBUTE_SHORTNAME
import org.example.persistence.ddb.Constants.DYNAMODB_SORT_KEY_ATTRIBUTE_SHORTNAME
import org.example.persistence.ddb.dao.ProductDao
import org.example.persistence.ddb.model.ProductRecord
import org.example.persistence.util.model.PositiveInt
import org.example.testing.extension.DynamoDbLocalClientExtension
import org.example.testing.extension.DynamoDbLocalClientExtension.Companion.createTable
import org.example.testing.extension.DynamoDbLocalClientExtension.Companion.deleteTable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.services.dynamodb.model.BillingMode
import software.amazon.awssdk.services.dynamodb.model.ScalarAttributeType
import org.example.persistence.ddb.util.dsl.buildAttributeDefinition as attributeDefinition
import org.example.persistence.ddb.util.dsl.buildCreateTableRequest as createTableRequest
import org.example.persistence.ddb.util.dsl.buildKeySchemaElement as keySchemaElement

@ExtendWith(DynamoDbLocalClientExtension::class)
class DefaultProductDaoTest {

    companion object {
        val DEFAULT_CATEGORY_ID: UUID = UUID.randomUUID()
    }

    private val createTableRequest = createTableRequest {
        tableName(ProductRecord.TABLE_NAME)
        billingMode(BillingMode.PAY_PER_REQUEST)

        keySchema(
            keySchemaElement {
                attributeName(DYNAMODB_PARTITION_KEY_ATTRIBUTE_SHORTNAME)
                keyType(software.amazon.awssdk.services.dynamodb.model.KeyType.HASH)
            },
            keySchemaElement {
                attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_SHORTNAME)
                keyType(software.amazon.awssdk.services.dynamodb.model.KeyType.RANGE)
            },
        )

        attributeDefinitions(
            attributeDefinition {
                attributeName(DYNAMODB_PARTITION_KEY_ATTRIBUTE_SHORTNAME)
                attributeType(ScalarAttributeType.S)
            },
            attributeDefinition {
                attributeName(DYNAMODB_SORT_KEY_ATTRIBUTE_SHORTNAME)
                attributeType(ScalarAttributeType.S)
            },
        )
    }

    private lateinit var table: DynamoDbTable<ProductRecord>
    private lateinit var dao: ProductDao

    @BeforeEach
    fun beforeEach() {
        println("root before each")
        table = createTable(
            ProductRecord::class.java,
            ProductRecord.TABLE_NAME,
            createTableRequest,
        )
        dao = DefaultProductDao(DynamoDbLocalClientExtension.dynamoDbEnhancedClient)
    }

    @AfterEach
    fun afterEach() {
        println("root after each")
        deleteTable(ProductRecord.TABLE_NAME)
    }

    @Test
    fun `query returns at most 12 items per page`() {
        table.scan().flatMap { it.items() }.shouldHaveSize(0)

        val limit = PositiveInt(DYNAMODB_MAX_ITEMS_PER_WRITEBATCH / 2u)

        val records = (0..<(DYNAMODB_MAX_ITEMS_PER_WRITEBATCH * 2u).toInt()).map {
            ProductRecord.build {
                categoryId(DEFAULT_CATEGORY_ID)
                name("product$it")
            }
        }.onEach {
            table.putItem(it)
        }

        withClue("All items should have been put into the table") {
            with(table.scan().flatMap { it.items() }) {
                shouldContainExactlyInAnyOrder(records)
            }
        }

        val actual = dao.query(
            DEFAULT_CATEGORY_ID,
            limit,
        )

        actual.forEach {
            it.size.shouldBeLessThanOrEqual(limit.toInt())
            it.shouldBeSmallerThan(records)
            records.shouldContainAll(it)
        }
    }
}
