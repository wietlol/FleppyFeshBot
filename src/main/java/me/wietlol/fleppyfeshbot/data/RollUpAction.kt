package me.wietlol.fleppyfeshbot.data

import java.awt.event.KeyEvent

class RollUpAction(
	override val targetLane: Lane
) : RollAction
{
	override val name: String
		get() = "roll up"
	override val key: Int
		get() = KeyEvent.VK_UP
}
