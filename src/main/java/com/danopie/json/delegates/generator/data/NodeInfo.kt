package com.danopie.json.delegates.generator.data

data class NodeInfo(
        val stringRepresentation: String,
        val node: NodeWrapper?,
        val mapDeserialization: String?,
        val mapSerialization: String?
) {
    constructor(stringRepresentation: String, name: String):
        this(
            stringRepresentation,
            null,
            "map[\"$name\"],\n",
            "\t\tmap['$name'] = $name;\n"
        )

    constructor(stringRepresentation: String, name: String, mapDeserialization: String):
            this(
                    stringRepresentation,
                    null,
                    mapDeserialization,
                    "\t\tmap['$name'] = $name;\n"
            )
    constructor(stringRepresentation: String, name: String, mapDeserialization: String, mapSerialization: String):
            this(
                    stringRepresentation,
                    null,
                    mapDeserialization,
                    mapSerialization
            )
}