package io.fluidsonic.server


class Payload private constructor(val rawValue: Any) { // TODO refactor

	companion object {

		val empty = Payload(Unit)


		fun raw(value: Any) =
			Payload(value)
	}
}
