package codes.laurence.warden.atts

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

internal class AttributeTypeTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `asHasAttributes - withAttributes`() {
        val testObj = AttributeType(type = "SomeType", typeKeyword = "customType")

        val additionalAttributesVararg = listOf(
            "id" to randInt(),
            "collectionOfHasAttributes" to listOf(
                NestedThingWithAtts(
                    nestedAtt = randString(),
                    aPrivateAtt = randString()
                )
            ),
            "mapOfHasAttributes" to mapOf(
                "foo" to NestedThingWithAtts(nestedAtt = randString(), aPrivateAtt = randString())
            )
        )
        val additionalAttributes = mapOf(
            *additionalAttributesVararg.toTypedArray()
        )

        val hasAttributesMap = testObj.asHasAttributes(additionalAttributes).attributes()
        val attributesMap = testObj.withAttributes(additionalAttributes)
        val attributesVarargs = testObj.withAttributes(*additionalAttributesVararg.toTypedArray())

        val expectedMap = mapOf(
            "customType" to "SomeType",
            "id" to additionalAttributes["id"],
            "collectionOfHasAttributes" to (additionalAttributes["collectionOfHasAttributes"] as List<HasAttributesI>).map { it.attributes() },
            "mapOfHasAttributes" to (additionalAttributes["mapOfHasAttributes"] as Map<*, HasAttributesI>).map { it.key to it.value.attributes() }
                .toMap()
        )

        assertThat(hasAttributesMap).isEqualTo(expectedMap)
        assertThat(attributesMap).isEqualTo(expectedMap)
        assertThat(attributesVarargs).isEqualTo(expectedMap)
    }

    @Test
    fun attributes() {
        val testObj = AttributeType(type = "SomeType", typeKeyword = "customType")
        assertThat(testObj.attributes()).isEqualTo(
            mapOf(
                "customType" to "SomeType"
            )
        )
    }
}
