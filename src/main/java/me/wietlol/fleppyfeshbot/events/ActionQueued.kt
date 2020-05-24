package me.wietlol.fleppyfeshbot.events

import me.wietlol.fleppyfeshbot.data.GameAction

data class ActionQueued(
	override val timestamp: Long,
	val action: GameAction,
	val targetFrame: Long
) : GameEvent
