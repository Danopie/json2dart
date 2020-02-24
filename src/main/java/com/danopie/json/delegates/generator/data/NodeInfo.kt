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
            "\t\tdata['$name'] = $name;\n"
        )

    constructor(stringRepresentation: String, name: String, mapDeserialization: String):
            this(
                    stringRepresentation,
                    null,
                    mapDeserialization,
                    "\t\tdata['$name'] = $name;\n"
            )
}