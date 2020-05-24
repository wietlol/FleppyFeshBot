package me.wietlol.fleppyfeshbot.events

import me.wietlol.fleppyfeshbot.data.Lane

data class FlamerAppeared(
	override val timestamp: Long,
	val lane: Lane
) : GameEvent
