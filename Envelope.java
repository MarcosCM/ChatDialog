/**
 * 	FICHERO: Envelope.java
 * 	DESCRIPCIÓN: Gestiona un objeto serializable que contiene un
 * 				mensaje serializable, un id de proceso fuente y
 * 				otro id de proceso destino, siendo la id >= 1.
 */
package ssdd.ms;

import java.io.Serializable;

public class Envelope implements Serializable {

	private static final long serialVersionUID = 1L;

	private int source;
	private int destination;
	private Serializable payload;
	//Atributo del algoritmo Ricart & Agrawala => stamp = clk+1 en cada mensaje
	private int stamp;
	
	public Envelope(int source, int destination, Serializable payload, int stamp){
		this.source = source;
		this.destination = destination;
		this.payload = payload;
		this.stamp = stamp;
	}
	
	public int getSource(){
		return this.source;
	}
	
	public int getDestination(){
		return this.destination;
	}
	
	public Serializable getPayload(){
		return this.payload;
	}
	
	public int getStamp(){
		return this.stamp;
	}
}
