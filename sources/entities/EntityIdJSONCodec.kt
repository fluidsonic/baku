package com.github.fluidsonic.baku

import com.github.fluidsonic.fluid.json.JSONCodec
import com.github.fluidsonic.fluid.json.JSONCodingContext
import com.github.fluidsonic.fluid.json.JSONCodingType
import com.github.fluidsonic.fluid.json.JSONDecoder
import com.github.fluidsonic.fluid.json.JSONEncoder
import com.github.fluidsonic.fluid.json.JSONException
import com.github.fluidsonic.fluid.json.jsonCodingType


internal class EntityIdJSONCodec<Id : EntityId>(
	private val factory: EntityId.Factory<Id>
) : JSONCodec<Id, JSONCodingContext> {

	override val decodableType = jsonCodingType(factory.idClass)


	override fun decode(valueType: JSONCodingType<in Id>, decoder: JSONDecoder<JSONCodingContext>) =
		decoder.readString().let { string ->
			factory.parse(string) ?: throw JSONException("Invalid '${factory.type}' ID: $string")
		}


	override fun encode(value: Id, encoder: JSONEncoder<JSONCodingContext>) {
		encoder.writeString(factory.serialize(value))
	}
}
