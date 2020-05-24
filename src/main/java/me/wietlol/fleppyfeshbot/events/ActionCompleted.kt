package me.wietlol.fleppyfeshbot.events

import me.wietlol.fleppyfeshbot.data.GameAction

data class ActionCompleted(
	override val timestamp: Long,
	val action: GameAction
) : GameEvent
