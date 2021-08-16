package codes.laurence.warden.attributes

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.test.randString
import org.junit.Test

class ExtensionsTest {

    @Test
    fun `HasAttributesI - withAttributes`() {
        val thingWithAtts = NestedThingWithAtts(
            nestedAtt = randString(),
            aPrivateAtt = randString()
        )
        val additionalThngWithAttributes = NestedThingWithAtts(
            nestedAtt = randString(),
            aPrivateAtt = randString()
        )

        val actual = thingWithAtts.withAttributes(
            "additional" to additionalThngWithAttributes
        )

        assertThat(actual).isEqualTo(
            mapOf(
                "attributeType" to "NestedThing",
                "nestedAtt" to thingWithAtts.nestedAtt,
                "additional" to additionalThngWithAttributes.attributes()
            )
        )
    }

}