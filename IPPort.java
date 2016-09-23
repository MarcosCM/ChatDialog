/**
 * 	FICHERO: IPPort.java
 * 	DESCRIPCIÓN: Encapsula una ip y puerto determinados.
 */
package ssdd.ms;

public class IPPort {

	private String ip;
	private int port;
	
	/**
	 * 	Crea un objeto con una ip y un puerto determinados.
	 */
	public IPPort(String ip, int port){
		this.ip = ip;
		this.port = port;
	}
	
	/**
	 * 	Devuelve la ip.
	 */
	public String getIp(){
		return this.ip;
	}
	
	/**
	 * 	Devuelve el puerto.
	 */
	public int getPort(){
		return this.port;
	}
}
