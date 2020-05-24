package me.wietlol.fleppyfeshbot

import me.wietlol.fleppyfeshbot.gui.Display

object Main
{
	@JvmStatic
	fun main(args: Array<String>)
	{
		val game = FleppyFeshGame()
		val display = Display(game)
		val bot = FleppyFeshBot(game)
		
		bot.start()
	}
}
