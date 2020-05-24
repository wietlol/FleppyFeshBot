package me.wietlol.fleppyfeshbot.gui

import me.wietlol.fleppyfeshbot.FleppyFeshGame
import me.wietlol.fleppyfeshbot.data.GameState.Game
import me.wietlol.fleppyfeshbot.data.GameState.Menu
import me.wietlol.fleppyfeshbot.data.GameState.StartingSequence
import me.wietlol.fleppyfeshbot.data.Lane.Bottom
import me.wietlol.fleppyfeshbot.data.Lane.Middle
import me.wietlol.fleppyfeshbot.data.Lane.Top
import java.awt.Color
import java.awt.Frame
import java.awt.Graphics
import java.awt.Image
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.WindowEvent
import java.awt.image.BufferedImage
import java.text.DecimalFormat
import java.util.*
import javax.imageio.ImageIO
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants
import kotlin.collections.HashMap

class Display(
	private val game: FleppyFeshGame
)
{
	private val frame = JFrame()
	private val resources: MutableMap<String, BufferedImage> = HashMap()
	
	private val logs: MutableList<TimedUiContent> = LinkedList()
	
	private val nanoTime: Long
		get() = System.nanoTime()
	
	init
	{
		// preload resource(s)
		getImage("/images/danger.png")
		
		frame.contentPane = object : JPanel()
		{
			override fun paintComponent(graphics: Graphics)
			{
				render(graphics)
			}
		}
		
		frame.isUndecorated = true
		frame.isAlwaysOnTop = true
		frame.extendedState = Frame.MAXIMIZED_BOTH
		frame.defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE
		frame.background = Color(0, 0, 0, 0)
		frame.focusableWindowState = false
		
		frame.addMouseListener(object : MouseAdapter()
		{
			override fun mouseClicked(e: MouseEvent)
			{
				if (e.button == MouseEvent.BUTTON2)
					frame.dispatchEvent(WindowEvent(frame, WindowEvent.WINDOW_CLOSING))
			}
		})
		frame.isVisible = true
		
		with(game) {
			onKnifeAppeared.register { (timestamp) ->
				addTimedText("knife appeared at ${decimalFormat.format(timestamp / 1.b)}")
			}
			onFlamerAppeared.register { (timestamp, lane) ->
				addTimedText("flamer appeared at ${decimalFormat.format(timestamp / 1.b)} on ${lane.laneName}")
			}
			onActionQueued.register { (timestamp, action, frame) ->
				addTimedText("queued action at ${decimalFormat.format(timestamp / 1.b)} to ${action.name} at ${decimalFormat.format(frame / 1.b)}s")
			}
			onUpdate.register {
				update()
			}
			onGameStateChanged.register { (gameState) ->
				when (gameState)
				{
					Menu -> addTimedText("waiting for game to start")
					StartingSequence -> addTimedText("game starting...")
					Game -> addTimedText("game started")
				}
			}
		}
	}
	
	private fun update()
	{
		frame.repaint()
	}
	
	private fun addTimedText(text: String, duration: Long = 2_500_000_000)
	{
		logs.add(TimedUiContent(TextContent(text), nanoTime + duration))
	}
	
	private val decimalFormat = DecimalFormat("0.00")
	
	private fun render(graphics: Graphics)
	{
		// positions are based on a [1920x1080] resolution. if shared, perhaps these need to be changed
		with(game) {
			graphics.color = Color.darkGray
			graphics.fillRect(65, 8, 290, 67 + logs.size * 10)
			graphics.color = Color.red
			graphics.drawString("score: $level", 70, 20)
			graphics.drawString("lane: ${currentFeshPosition.laneName}", 70, 30)
			val longestCooldownUntil = getLongestActionCooldownUntil()
			if (longestCooldownUntil <= currentFrame)
				graphics.drawString("state: $currentFeshState", 70, 40)
			else
				graphics.drawString("state: $currentFeshState (${decimalFormat.format((longestCooldownUntil - currentFrame) / 1.b)}s)", 70, 40)
			graphics.drawString("time: ${decimalFormat.format((currentFrame) / 1.b)}s", 70, 50)
			graphics.drawString("fps: ${decimalFormat.format(1.b / currentDelta)}", 70, 60)
			graphics.drawString("speed: ${decimalFormat.format(gameSpeed)}", 70, 70)
			
			if (isKnifeActive())
				graphics.drawImage(getImage("/images/danger.png"), 1585, 935, null)
			
			if (isFlamerActive(Top))
				graphics.drawImage(getImage("/images/danger.png"), 1700, 300, null)
			if (isFlamerActive(Middle))
				graphics.drawImage(getImage("/images/danger.png"), 1700, 500, null)
			if (isFlamerActive(Bottom))
				graphics.drawImage(getImage("/images/danger.png"), 1700, 700, null)
			
			// game dangers
//			renderTarget(graphics, 1250, 400)
//			renderTarget(graphics, 1250, 575)
//			renderTarget(graphics, 1250, 765)
//			renderTarget(graphics, 1250, 845)
			
			// starting sequence
//			renderTarget(graphics, 1250, 400)
//			renderTarget(graphics, 1250, 500)
//			renderTarget(graphics, 1250, 600)
			
			var index = 0
			val nanoTime = nanoTime
			logs.listIterator().let { iterator ->
				while (iterator.hasNext())
				{
					val item = iterator.next()
					item.content.drawAt(graphics, 70, 80 + 10 * index)
					
					if (item.untilFrame < nanoTime)
						iterator.remove()
					
					index++
				}
			}
		}
	}
	
	@Suppress("unused")
	private fun renderTarget(graphics: Graphics, x: Int, y: Int)
	{
		graphics.drawOval(x - 8, y - 8, 16, 16)
	}
	
	@Suppress("SameParameterValue")
	private fun getImage(imagePath: String): Image =
		resources.computeIfAbsent(imagePath) { ImageIO.read(javaClass.getResourceAsStream(imagePath)) }
	
	companion object
	{
		// not really clean, but whatever
		private val Int.b: Double
			get() = this * 1_000_000_000.0
	}
}
