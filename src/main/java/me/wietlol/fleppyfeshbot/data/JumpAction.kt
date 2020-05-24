package me.wietlol.fleppyfeshbot.data

import java.awt.event.KeyEvent

object JumpAction : GameAction
{
	override val name: String
		get() = "jump"
	override val durationToJump: Long
		// 753_M too low
		// 754_M high enough
		get() = 754_000_000
	override val durationToRoll: Long
		// 850_M too low
		// 855_M high enough
		get() = 855_000_000
	override val key: Int
		get() = KeyEvent.VK_SPACE
}
