package me.wietlol.fleppyfeshbot.gui

import java.awt.Graphics

interface UiContent
{
	fun drawAt(graphics: Graphics, x: Int, y: Int)
}
