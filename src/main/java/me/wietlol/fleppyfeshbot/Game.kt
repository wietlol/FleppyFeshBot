package me.wietlol.fleppyfeshbot

interface Game
{
	val begin: Long
	val desiredFps: Int
	var currentFrame: Long
	var currentDelta: Long
	
	fun runGame(update: (TimeFrame) -> Unit): Nothing
	{
		var previous = System.nanoTime() - begin
		val minimumNanoTime = 1_000_000_000L / desiredFps
		
		while (true)
		{
			val current = System.nanoTime() - begin
			
			val delta = current - previous
			if (delta >= minimumNanoTime)
			{
				currentFrame = current
				currentDelta = delta
				update(TimeFrame(current, delta))
				previous = current
			}
		}
	}
	
	fun waitFor(update: (TimeFrame) -> Boolean)
	{
		var previous = System.nanoTime()
		val minimumNanoTime = 1_000_000_000L / desiredFps
		
		while (true)
		{
			val current = System.nanoTime()
			
			val delta = current - previous
			if (delta >= minimumNanoTime)
			{
				currentFrame = current
				currentDelta = delta
				if (update(TimeFrame(current, delta)))
					break
				previous = current
			}
		}
	}
}
