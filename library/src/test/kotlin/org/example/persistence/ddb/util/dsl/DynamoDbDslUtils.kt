@file:JvmName("DynamoDbDslUtils")

package org.example.persistence.ddb.util.dsl

import software.amazon.awssdk.enhanced.dynamodb.DynamoDbTable
import software.amazon.awssdk.enhanced.dynamodb.Key
import software.amazon.awssdk.services.dynamodb.model.AttributeDefinition
import software.amazon.awssdk.services.dynamodb.model.CreateTableRequest
import software.amazon.awssdk.services.dynamodb.model.GlobalSecondaryIndex
import software.amazon.awssdk.services.dynamodb.model.KeySchemaElement

fun buildCreateTableRequest(block: CreateTableRequest.Builder.() -> Unit): CreateTableRequest =
    CreateTableRequest.builder().apply(block).build()

fun buildKeySchemaElement(block: KeySchemaElement.Builder.() -> Unit): KeySchemaElement =
    KeySchemaElement.builder().apply(block).build()

fun buildGlobalSecondaryIndex(block: GlobalSecondaryIndex.Builder.() -> Unit): GlobalSecondaryIndex =
    GlobalSecondaryIndex.builder().apply(block).build()

fun buildAttributeDefinition(block: AttributeDefinition.Builder.() -> Unit): AttributeDefinition =
    AttributeDefinition.builder().apply(block).build()

fun <T> DynamoDbTable<T>.getItem(block: Key.Builder.() -> Unit): T = getItem(Key.builder().apply(block).build())
