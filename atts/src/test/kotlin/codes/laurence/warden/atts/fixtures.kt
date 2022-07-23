package codes.laurence.warden.atts

internal data class ParentThingWithAtts(
    val aThing: ThingWithoutAtts,
    val customTypeThing: CustomTypeThing,
    val customAttsThing: CustomAttsThing,
    val primeChild: NestedThingWithAtts,
    val childrenList: List<NestedThingWithAtts>,
    val childrenSet: Set<NestedThingWithAtts>,
    val childrenMap: Map<String, NestedThingWithAtts>,
) : HasAtts(attType = AttType("ParentThing"))

internal data class NestedThingWithAtts(
    val nestedAtt: String,
    private val aPrivateAtt: String = randString()
) : HasAtts(attType = AttType("NestedThing"))

internal data class ThingWithoutAtts(
    val noAtts: String
)

internal data class CustomTypeThing(
    val anAtt: String
) : HasAtts(attType = AttType("CustomTypeThing", typeKeyword = "customType"))

internal data class CustomAttsThing(
    val shouldNotSeeMe: String
) : HasAttsI {

    override fun atts(): Atts {
        return mapOf("shouldSeeMe" to shouldNotSeeMe)
    }
}
