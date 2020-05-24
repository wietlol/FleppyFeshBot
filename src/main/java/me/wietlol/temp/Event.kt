package me.wietlol.temp

// partially copied from Wietlol's internal event library
// copied to avoid having that library being public
interface Event<T>
{
	fun execute(eventData: T)
	
	fun register(listener: (T) -> Unit)
}
