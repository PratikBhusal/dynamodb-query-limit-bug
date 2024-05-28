package org.example.persistence.ddb.converter

import java.time.Instant
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter
import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.EnhancedAttributeValue
import software.amazon.awssdk.enhanced.dynamodb.internal.converter.attribute.InstantAsStringAttributeConverter
import software.amazon.awssdk.services.dynamodb.model.AttributeValue

/**
 * @see InstantAsStringAttributeConverter
 */
class InstantAsEpochSecondAttributeConverter : AttributeConverter<Instant> {
    override fun transformFrom(instant: Instant): AttributeValue = EnhancedAttributeValue
        .fromNumber(instant.epochSecond.toString())
        .toAttributeValue()

    override fun transformTo(attributeValue: AttributeValue): Instant = Instant.ofEpochSecond(
        attributeValue.n().toLong(),
    )

    override fun type(): EnhancedType<Instant> = EnhancedType.of(Instant::class.java)

    override fun attributeValueType() = AttributeValueType.N
}
