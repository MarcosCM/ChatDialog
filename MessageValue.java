/**
 * 	FICHERO: MessageValue.java
 * 	DESCRIPCIÓN: Gestiona el valor que toma un determinado mensaje
 * 				que será posteriormente envuelto en un sobre.
 */
package ssdd.ms;


import java.io.Serializable;

/**
 *	Clase serializable para probar/depurar el código
 */
public class MessageValue implements Serializable {

	private static final long serialVersionUID = 1L;

	private String value;
	private boolean isMessage; //true si es mensaje multicast, false si es REQ o ACK
	
	public MessageValue(String value, boolean isMessage){
		this.value = value;
		this.isMessage = isMessage;
	}
	
	/**
	 * 	Devuelve el valor del mensaje.
	 */
	public String getValue(){
		return this.value;
	}
	
	/**
	 * 	Devuelve true si es un mensaje multicast.
	 * 	Devuelve false si es un mensaje de tipo REQ o ACK.
	 */
	public boolean getIsMessage(){
		return this.isMessage;
	}
	
	public String toString(){
		return this.value;
	}
}
