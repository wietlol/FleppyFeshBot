package me.wietlol.fleppyfeshbot.gui

import java.awt.Graphics

class TextContent(
	val text: String
) : UiContent
{
	override fun drawAt(graphics: Graphics, x: Int, y: Int)
	{
		graphics.drawString(text, x, y)
	}
}
