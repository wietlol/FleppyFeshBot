package me.wietlol.fleppyfeshbot.data

import me.wietlol.fleppyfeshbot.FleppyFeshGame

interface GameAction
{
	val name: String
	val durationToRoll: Long
	val durationToJump: Long
	val key: Int
	
	fun applyWith(bot: FleppyFeshGame)
	{
		val robot = bot.robot
		robot.keyPress(key)
		robot.keyRelease(key)
	}
}
