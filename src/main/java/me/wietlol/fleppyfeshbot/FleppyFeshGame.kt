package me.wietlol.fleppyfeshbot

import me.wietlol.fleppyfeshbot.data.GameAction
import me.wietlol.fleppyfeshbot.data.GameState
import me.wietlol.fleppyfeshbot.data.GameState.Menu
import me.wietlol.fleppyfeshbot.data.GameState.StartingSequence
import me.wietlol.fleppyfeshbot.data.JumpAction
import me.wietlol.fleppyfeshbot.data.Lane
import me.wietlol.fleppyfeshbot.data.Lane.Bottom
import me.wietlol.fleppyfeshbot.data.Lane.Middle
import me.wietlol.fleppyfeshbot.data.Lane.Top
import me.wietlol.fleppyfeshbot.data.RollAction
import me.wietlol.fleppyfeshbot.data.RollDownAction
import me.wietlol.fleppyfeshbot.data.RollUpAction
import me.wietlol.fleppyfeshbot.events.ActionCompleted
import me.wietlol.fleppyfeshbot.events.ActionQueued
import me.wietlol.fleppyfeshbot.events.FlamerAppeared
import me.wietlol.fleppyfeshbot.events.GameStateChanged
import me.wietlol.fleppyfeshbot.events.GameUpdated
import me.wietlol.fleppyfeshbot.events.KnifeAppeared
import me.wietlol.temp.Event
import me.wietlol.temp.SimpleEvent
import java.awt.Color
import java.awt.Rectangle
import java.awt.Robot
import java.awt.image.BufferedImage
import java.util.*

class FleppyFeshGame(
	internal val robot: Robot = Robot()
) : Game
{
	// known issues
	// - none so far
	
	// idea... sorta...
	// if we add a native key listener, we could detect the game events that are not controlled programmatically as well
	// (the rolling and jumping... I think)
	// that way, we can play the game and watch if the detection works correctly... while spamming actions...
	// could be a nice way to get more accurate numbers especially past the 150 or 200 mark... but we need a player to do that :D
	// would probably introduce a bit of re-designing and refactoring
	
	// 1_060_M too low
	// 1_070_M high enough
	private val knifeDuration = 1_080_000_000L
	
	// 1_200_M high enough
	private val flamerDuration = 1_200_000_000L
	
	// positions are based on a [1920x1080] resolution. if shared, perhaps these need to be changed
	private val dangerDetectionArea = Rectangle(1250, 400, 1, 446)
	private val startingSequenceDetectionArea = Rectangle(1250, 400, 1, 201)
	
	val onKnifeAppeared: Event<KnifeAppeared> = SimpleEvent()
	val onFlamerAppeared: Event<FlamerAppeared> = SimpleEvent()
	val onActionCompleted: Event<ActionCompleted> = SimpleEvent()
	val onActionQueued: Event<ActionQueued> = SimpleEvent()
	val onUpdate: Event<GameUpdated> = SimpleEvent()
	val onGameStateChanged: Event<GameStateChanged> = SimpleEvent()
	
	// the robot api hard limits to 60 fps based on the operating system...
	// this works well, except for when shit gets real
	// recording at 30 fps using Action! causes the game to detect at 45 fps because of complex reasons
	// maybe if we use JNA GDI32, it would work faster... definitely a nice choice if we want to detect the game window position and size
	override val desiredFps = 60
	override var currentFrame = 0L
	override var currentDelta = 0L
	
	override var begin = 0L
		private set
	
	var currentFeshPosition = Middle
		internal set
	var currentFeshState = "idle"
		private set
	
	var level = 0
		private set
	
	private var rollCooldownUntil = 0L
	private var jumpCooldownUntil = 0L
	private var knifeActiveUntil = 0L
	val flamersActiveUntil = Array(3) { 0L }
	
	@Suppress("RemoveExplicitTypeArguments") // editor inconsistent with the compiler, explicit type arguments are apparently required
	private val actions: Queue<ActionRequest> = PriorityQueue(Comparator.comparing<ActionRequest, Long> { it.requestedFrame })
	
	val gameSpeed
		get() = when (level)
		{
			in 0..150 -> 1.0
			in 153..200 -> 1.42 // 1.4 too low?
			in 200..Int.MAX_VALUE -> 2.0 // 1.9 too low? // not sure if the speed will ever change after 200
			else -> 1.0
		}
	
	fun start()
	{
		onGameStateChanged.execute(GameStateChanged(Menu))
		waitFor {
			onUpdate.execute(GameUpdated)
			startingSequenceStarted()
		}
		onGameStateChanged.execute(GameStateChanged(StartingSequence))
		waitFor {
			onUpdate.execute(GameUpdated)
			startingSequenceEnded()
		}
		onGameStateChanged.execute(GameStateChanged(GameState.Game))
		startGame()
	}
	
	private fun startingSequenceStarted(): Boolean =
		getStartingSequencePixels()
  			.all { it.red == 252 && it.green == 103 && it.blue == 103 }
	
	private fun startingSequenceEnded(): Boolean =
		getStartingSequencePixels()
			.all { it.red == 255 && it.green == 190 && it.blue == 132 }
	
	private fun startGame()
	{
		begin = System.nanoTime()
		runGame { (frame, _) ->
			detectGameEvents()
			onUpdate.execute(GameUpdated)
			
			val head = actions.peek()
			if (head != null && head.requestedFrame <= frame && canFeshDoAction(head.action))
			{
				val action = actions.poll().action
				currentFeshState = action.name
				action.applyWith(this)
				
				rollCooldownUntil = currentFrame + (action.durationToRoll.toDouble() / gameSpeed).toLong()
				jumpCooldownUntil = currentFrame + (action.durationToJump.toDouble() / gameSpeed).toLong()
				
				onActionCompleted.execute(ActionCompleted(currentFrame, action))
			}
			else if (rollCooldownUntil <= currentFrame && jumpCooldownUntil <= currentFrame)
			{
				currentFeshState = "idle"
				
				@Suppress("NON_EXHAUSTIVE_WHEN")
				if (isKnifeActive().not() && isAnyFlamerActive().not())
					when (currentFeshPosition)
					{
						Top -> queueAction(currentFrame, RollDownAction(Middle))
						Bottom -> queueAction(currentFrame, RollUpAction(Middle))
					}
			}
		}
	}
	
	fun queueAction(frame: Long, action: GameAction)
	{
		onActionQueued.execute(ActionQueued(currentFrame, action, frame))
		actions.add(ActionRequest(action, frame))
	}
	
	fun isKnifeActive(): Boolean =
		knifeActiveUntil > currentFrame
	
	@Suppress("MemberVisibilityCanBePrivate")
	fun isAnyFlamerActive() =
		isFlamerActive(Top)
			|| isFlamerActive(Middle)
			|| isFlamerActive(Bottom)
	
	fun isFlamerActive(lane: Lane) =
		flamersActiveUntil[lane.index] > currentFrame
	
	@Suppress("MemberVisibilityCanBePrivate")
	fun canFeshDoAction(action: GameAction): Boolean =
		when (action)
		{
			is JumpAction -> jumpCooldownUntil <= currentFrame
			is RollAction -> rollCooldownUntil <= currentFrame
			else -> throw NotImplementedError("function `canFeshDoAction` needs an implementation for '${action.javaClass.name}'.")
		}
	
	fun getLongestActionCooldownUntil(): Long =
		rollCooldownUntil // assumed to be the longest
	
	private fun detectGameEvents()
	{
		val dangerPixels = getDangerPixels()
		detectKnife(dangerPixels.first())
		detectFlamers(dangerPixels.drop(1))
	}
	
	private fun detectKnife(knifePixel: Color)
	{
		if (isKnifeActive().not() && knifePixel.red < 254)
		{
			knifeActiveUntil = currentFrame + (knifeDuration / gameSpeed).toLong()
			level++
			onKnifeAppeared.execute(KnifeAppeared(currentFrame))
		}
	}
	
	private fun detectFlamers(flamerPixels: List<Color>)
	{
		detectFlamer(Top, flamerPixels[0])
		detectFlamer(Middle, flamerPixels[1])
		detectFlamer(Bottom, flamerPixels[2])
	}
	
	// positions are based on a [1920x1080] resolution. if shared, perhaps these need to be changed
	private fun getStartingSequencePixels(): List<Color> =
		generateScreenshot(startingSequenceDetectionArea)
			.let {
				listOf(
					it.getRGB(0, 0),
					it.getRGB(0, 100),
					it.getRGB(0, 200)
				)
			}
			.map { Color(it, true) }
	
	// positions are based on a [1920x1080] resolution. if shared, perhaps these need to be changed
	private fun getDangerPixels(): List<Color> =
		generateScreenshot(dangerDetectionArea)
			.let {
				listOf(
					it.getRGB(0, 445),
					it.getRGB(0, 0),
					it.getRGB(0, 175),
					it.getRGB(0, 365)
				)
			}
			.map { Color(it, true) }
	
	private fun detectFlamer(lane: Lane, color: Color)
	{
		if (isFlamerActive(lane).not())
		{
			if (color.green < 189 && color.red == 255)
			{
				flamersActiveUntil[lane.index] = currentFrame + (flamerDuration / gameSpeed).toLong()
				level++
				onFlamerAppeared.execute(FlamerAppeared(currentFrame, lane))
			}
		}
	}
	
	private fun generateScreenshot(rectangle: Rectangle): BufferedImage =
		robot.createScreenCapture(rectangle)
	
	private data class ActionRequest(
		val action: GameAction,
		val requestedFrame: Long
	)
}
