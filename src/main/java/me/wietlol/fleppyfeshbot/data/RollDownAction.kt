package me.wietlol.fleppyfeshbot.data

import java.awt.event.KeyEvent

class RollDownAction(
	override val targetLane: Lane
) : RollAction
{
	override val name: String
		get() = "roll down"
	override val key: Int
		get() = KeyEvent.VK_DOWN
}
