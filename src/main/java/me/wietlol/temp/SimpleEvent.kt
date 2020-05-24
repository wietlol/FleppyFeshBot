package me.wietlol.temp

// partially copied from Wietlol's internal event library
// copied to avoid having that library being public
class SimpleEvent<T> : Event<T>
{
	private val listeners: MutableList<(T) -> Unit> = ArrayList()
	
	override fun execute(eventData: T)
	{
		listeners.forEach { it(eventData) }
	}
	
	override fun register(listener: (T) -> Unit)
	{
		listeners.add(listener)
	}
}
