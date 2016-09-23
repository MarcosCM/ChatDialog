/**
 * 	FICHERO: Sender.java
 * 	DESCRIPCIÓN: Gestiona un hilo de ejecución para enviar
 * 				mensajes los demás procesos.
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
	 * 	y lo desbloquea para que lo envíe.
	 */
	public void sendMessage(Serializable msg){
		msgs.offer(msg); //la cola ya implementa un monitor => sólo un offer a la vez => no hace falta synchronized en el método
	}
	
	/**
	 * 	Se bloquea constantemente hasta que tiene un nuevo
	 * 	mensaje que enviar a los demás procesos.
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
