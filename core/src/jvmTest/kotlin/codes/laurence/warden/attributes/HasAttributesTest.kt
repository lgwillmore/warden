package codes.laurence.warden.attributes

import assertk.assertThat
import assertk.assertions.isEqualTo
import codes.laurence.warden.test.randString
import kotlin.test.Test

internal class HasAttributesTest {

    @Test
    fun toAttributes() {
        val source = ParentThingWithAtts(
            aThing = ThingWithoutAtts(
                noAtts = randString()
            ),
            customTypeThing = CustomTypeThing(
                anAtt = randString()
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
            "type" to "ParentThing",
            "aThing" to source.aThing,
            "customTypeThing" to mapOf(
                "customType" to "CustomTypeThing",
                "anAtt" to source.customTypeThing.anAtt
            ),
            "childrenList" to source.childrenList.map {
                mapOf(
                    "type" to "NestedThing",
                    "nestedAtt" to it.nestedAtt
                )
            },
            "childrenMap" to source.childrenMap.map {
                it.key to mapOf(
                    "type" to "NestedThing",
                    "nestedAtt" to it.value.nestedAtt
                )
            }.toMap(),
            "childrenSet" to source.childrenSet.map {
                mapOf(
                    "type" to "NestedThing",
                    "nestedAtt" to it.nestedAtt
                )
            },
            "primeChild" to mapOf(
                "type" to "NestedThing",
                "nestedAtt" to source.primeChild.nestedAtt
            ),
        )
        val actual = source.toAttributes()
        assertThat(actual).isEqualTo(expected)
    }
}

internal data class ParentThingWithAtts(
    val aThing: ThingWithoutAtts,
    val customTypeThing: CustomTypeThing,
    val primeChild: NestedThingWithAtts,
    val childrenList: List<NestedThingWithAtts>,
    val childrenSet: Set<NestedThingWithAtts>,
    val childrenMap: Map<String, NestedThingWithAtts>,
) : HasAttributes(type = "ParentThing")

internal data class NestedThingWithAtts(
    val nestedAtt: String,
    private val aPrivateAtt: String = randString()
) : HasAttributes(type = "NestedThing")

internal data class ThingWithoutAtts(
    val noAtts: String
)

internal data class CustomTypeThing(
    val anAtt: String
) : HasAttributes(type = "CustomTypeThing", typeKeyword = "customType")
