package codes.laurence.warden.atts

internal data class ParentThingWithAtts(
    val aThing: ThingWithoutAtts,
    val customTypeThing: CustomTypeThing,
    val customAttsThing: CustomAttsThing,
    val primeChild: NestedThingWithAtts,
    val childrenList: List<NestedThingWithAtts>,
    val childrenSet: Set<NestedThingWithAtts>,
    val childrenMap: Map<String, NestedThingWithAtts>,
) : HasAttributes(attributeType = AttributeType("ParentThing"))

internal data class NestedThingWithAtts(
    val nestedAtt: String,
    private val aPrivateAtt: String = randString()
) : HasAttributes(attributeType = AttributeType("NestedThing"))

internal data class ThingWithoutAtts(
    val noAtts: String
)

internal data class CustomTypeThing(
    val anAtt: String
) : HasAttributes(attributeType = AttributeType("CustomTypeThing", typeKeyword = "customType"))

internal data class CustomAttsThing(
    val shouldNotSeeMe: String
) : HasAttributesI {

    override fun attributes(): Attributes {
        return mapOf("shouldSeeMe" to shouldNotSeeMe)
    }
}
