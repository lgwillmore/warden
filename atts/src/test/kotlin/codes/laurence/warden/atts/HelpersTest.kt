package codes.laurence.warden.atts

import assertk.assertThat
import assertk.assertions.isEqualTo
import kotlin.test.Test

class HelpersTest {

    @Test
    fun attributesOf() {
        val source = ParentThingWithAtts(
            aThing = ThingWithoutAtts(
                noAtts = randString(),
            ),
            customTypeThing = CustomTypeThing(
                anAtt = randString()
            ),
            customAttsThing = CustomAttsThing(
                shouldNotSeeMe = randString(),
            ),
            primeChild = NestedThingWithAtts(
                nestedAtt = randString(),
            ),
            childrenList = listOf(
                NestedThingWithAtts(
                    nestedAtt = randString()
                )
            ),
            childrenSet = setOf(
                NestedThingWithAtts(
                    nestedAtt = randString()
                )
            ),
            childrenMap = mapOf(
                randString() to NestedThingWithAtts(
                    nestedAtt = randString()
                )
            ),
        )
        val expected = mapOf(
            "aThing" to source.aThing,
            "customTypeThing" to mapOf(
                "customType" to "CustomTypeThing",
                "anAtt" to source.customTypeThing.anAtt
            ),
            "customAttsThing" to mapOf(
                "shouldSeeMe" to source.customAttsThing.shouldNotSeeMe
            ),
            "childrenList" to source.childrenList.map {
                mapOf(
                    "attributeType" to "NestedThing",
                    "nestedAtt" to it.nestedAtt
                )
            },
            "childrenMap" to source.childrenMap.map {
                it.key to mapOf(
                    "attributeType" to "NestedThing",
                    "nestedAtt" to it.value.nestedAtt
                )
            }.toMap(),
            "childrenSet" to source.childrenSet.map {
                mapOf(
                    "attributeType" to "NestedThing",
                    "nestedAtt" to it.nestedAtt
                )
            },
            "primeChild" to mapOf(
                "attributeType" to "NestedThing",
                "nestedAtt" to source.primeChild.nestedAtt
            ),
        )
        val actual = attsOf(source)
        assertThat(actual).isEqualTo(expected)
    }
}
