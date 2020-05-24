package me.wietlol.fleppyfeshbot

import me.wietlol.fleppyfeshbot.data.JumpAction
import me.wietlol.fleppyfeshbot.data.Lane.Bottom
import me.wietlol.fleppyfeshbot.data.Lane.Middle
import me.wietlol.fleppyfeshbot.data.Lane.Top
import me.wietlol.fleppyfeshbot.data.RollAction
import me.wietlol.fleppyfeshbot.data.RollDownAction
import me.wietlol.fleppyfeshbot.data.RollUpAction
import java.lang.Long.max

data class FleppyFeshBot(
	val game: FleppyFeshGame
)
{
	// known issues
	// a Sequence<Knife> produces Result<Sushi>, need to either lower the jump action duration or increase game speed
	
	// 430_M too low to land before knife has passed
	// 450_M high enough to land before knife has passed
	private val knifeHitDelay = 480_000_000L
	
	init
	{
		with(game) {
			onFlamerAppeared.register { (_, lane) ->
				if (lane == currentFeshPosition)
				{
					val top = flamersActiveUntil[0]
					val middle = flamersActiveUntil[1]
					val bottom = flamersActiveUntil[2]
					when (lane)
					{
						Top -> queueAction(max(currentFrame, middle), RollDownAction(Middle))
						Middle ->
						{
							if (top == 0L || top < bottom)
								queueAction(max(currentFrame, top), RollUpAction(Top))
							else
								queueAction(max(currentFrame, bottom), RollDownAction(Bottom))
						}
						Bottom -> queueAction(max(currentFrame, middle), RollUpAction(Middle))
					}
				}
			}
			onKnifeAppeared.register { queueAction(currentFrame + (knifeHitDelay / gameSpeed).toLong(), JumpAction) }
			onActionCompleted.register { (_, action) ->
				if (action is RollAction && action.targetLane == Middle)
				{
					if (flamersActiveUntil[1] > currentFrame)
					{
						val top = flamersActiveUntil[0]
						val bottom = flamersActiveUntil[2]
						if (top == 0L || top < bottom)
							queueAction(max(currentFrame, top), RollUpAction(Top))
						else
							queueAction(max(currentFrame, bottom), RollDownAction(Bottom))
					}
				}
			}
			
//			generateSequence(0L) { it + 2 }
//				.takeWhile { it < 200 }
//				.forEach {
//					queueAction(it, RollUpAction(Top))
//					queueAction(it + 1, RollDownAction(Bottom))
//				}
//			generateSequence(0L) { it + 1 }
//				.takeWhile { it < 100 }
//				.forEach {
//					queueAction(it, JumpAction)
//				}
//			generateSequence(0L) { it + 3 }
//				.takeWhile { it < 300 }
//				.forEach {
//					queueAction(it, RollUpAction(Top))
//					queueAction(it + 1, JumpAction)
//					queueAction(it + 1, RollDownAction(Bottom))
//				}
		}
	}
	
	fun start()
	{
		game.start()
	}
}
