package codes.laurence.warden.attributes

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.test.randInt
import codes.laurence.warden.test.randString
import kotlin.test.Test

internal class AttributeTypeTest {

    @Test
    fun `asHasAttributes - withAttributes`() {
        val testObj = AttributeType(type = "SomeType", typeKeyword = "customType")

        val additionalAttributes = mapOf(
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

        val hasAttributesMap = testObj.asHasAttributes(additionalAttributes).attributes()
        val attributesMap = testObj.withAttributes(additionalAttributes)

        val expectedMap = mapOf(
            "customType" to "SomeType",
            "id" to additionalAttributes["id"],
            "collectionOfHasAttributes" to (additionalAttributes["collectionOfHasAttributes"] as List<HasAttributesI>).map { it.attributes() },
            "mapOfHasAttributes" to (additionalAttributes["mapOfHasAttributes"] as Map<*, HasAttributesI>).map { it.key to it.value.attributes() }
                .toMap()
        )

        assertThat(hasAttributesMap).isEqualTo(expectedMap)
        assertThat(attributesMap).isEqualTo(expectedMap)
    }
}
