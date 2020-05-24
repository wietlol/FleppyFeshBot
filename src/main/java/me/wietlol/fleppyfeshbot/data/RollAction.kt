package me.wietlol.fleppyfeshbot.data

import me.wietlol.fleppyfeshbot.FleppyFeshGame

interface RollAction : GameAction
{
	val targetLane: Lane
	override val durationToJump: Long
		// 370_M too low
		// 375_M high enough
		get() = 375_000_000
	override val durationToRoll: Long
		// 370_M too low // 375 appears to be too low too
		// 380_M high enough
		get() = 390_000_000
	
	override fun applyWith(bot: FleppyFeshGame)
	{
		super.applyWith(bot)
		
		bot.currentFeshPosition = targetLane
	}
}
