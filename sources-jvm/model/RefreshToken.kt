package io.fluidsonic.server

import io.fluidsonic.json.*


@Json
inline class RefreshToken(val value: String) {

	override fun toString() = value
}
