/**
 * 	FICHERO: Sender.java
 * 	DESCRIPCI�N: Gestiona un hilo de ejecuci�n para enviar
 * 				mensajes los dem�s procesos.
 */
package ssdd.ms;

import java.io.Serializable;
import java.util.concurrent.LinkedBlockingQueue;

public class Sender implements Runnable {
	
	private TotalOrderMulticast t;
	private LinkedBlockingQueue<Serializable> msgs;
	
	public Sender(MessageSystem ms){
		this.t = new TotalOrderMulticast(ms);
		msgs = new LinkedBlockingQueue<Serializable>();
	}
	
	/**
	 * 	Asigna al hilo el siguiente mensaje a enviar
	 * 	y lo desbloquea para que lo env�e.
	 */
	public void sendMessage(Serializable msg){
		msgs.offer(msg); //la cola ya implementa un monitor => s�lo un offer a la vez => no hace falta synchronized en el m�todo
	}
	
	/**
	 * 	Se bloquea constantemente hasta que tiene un nuevo
	 * 	mensaje que enviar a los dem�s procesos.
	 */
	public void run() {
		while(true){
			try {
				t.sendMulticast(msgs.take()); //se bloquea hasta que entra un nuevo mensaje a la cola (para enviarlo)
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
