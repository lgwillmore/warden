package codes.laurence.warden.atts

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

internal class AttTypeTest {

    @Suppress("UNCHECKED_CAST")
    @Test
    fun `asHasAttributes - withAttributes`() {
        val testObj = AttType(type = "SomeType", typeKeyword = "customType")

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

        val hasAttributesMap = testObj.asHasAtts(additionalAttributes).atts()
        val attributesMap = testObj.withAtts(additionalAttributes)
        val attributesVarargs = testObj.withAtts(*additionalAttributesVararg.toTypedArray())

        val expectedMap = mapOf(
            "customType" to "SomeType",
            "id" to additionalAttributes["id"],
            "collectionOfHasAttributes" to (additionalAttributes["collectionOfHasAttributes"] as List<HasAttsI>).map { it.atts() },
            "mapOfHasAttributes" to (additionalAttributes["mapOfHasAttributes"] as Map<*, HasAttsI>).map { it.key to it.value.atts() }
                .toMap()
        )

        assertThat(hasAttributesMap).isEqualTo(expectedMap)
        assertThat(attributesMap).isEqualTo(expectedMap)
        assertThat(attributesVarargs).isEqualTo(expectedMap)
    }

    @Test
    fun attributes() {
        val testObj = AttType(type = "SomeType", typeKeyword = "customType")
        assertThat(testObj.atts()).isEqualTo(
            mapOf(
                "customType" to "SomeType"
            )
        )
    }
}
