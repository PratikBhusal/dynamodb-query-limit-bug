package org.example.persistence.ddb

object Constants {
    const val DYNAMODB_COMPOSITE_ATTRIBUTE_DELIMITER = '#'
    const val DYNAMODB_MAX_ITEMS_PER_WRITEBATCH = 25u
    const val DYNAMODB_PARTITION_KEY_ATTRIBUTE_SHORTNAME = "pk"
    const val DYNAMODB_SORT_KEY_ATTRIBUTE_SHORTNAME = "sk"
    const val DYNAMODB_TIME_TO_LIVE_ATTRIBUTE_SHORTNAME = "ttl"
}
