/**
 * 	FICHERO: MailBox.java
 * 	DESCRIPCI�N: Gestiona un hilo de ejecuci�n que escucha
 * 				por un puerto determinado para crear conexiones
 * 				con otro proceso que quiere enviarle un mensaje.
 * 				El hilo tiene una cola con un m�ximo de tama�o
 * 				a la que se a�aden todos los mensajes.
 * 				El proceso al que van destinados estos mensajes
 * 				recoger� los mensajes en el orden en que fueron
 * 				enviados.
 */
package ssdd.ms;

import java.io.ObjectInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class MailBox implements Runnable {

	private final int PORT;
	
	private LinkedBlockingQueue<Envelope> queue;
	private boolean run;
	
	public MailBox(int size, int port){
		this.PORT = port;
		this.queue = new LinkedBlockingQueue<Envelope>(size);
		this.run = true;
	}
	
	public void stopThread(){
		this.run = false;
	}
	
	/**
	 * 	Devuelve el siguiente mensaje del buz�n.
	 * 	Si no hay mensajes entonces se bloquea hasta que entre uno,
	 * 	y lo devuelve.
	 */
	public Envelope getEnvelope(){
		Envelope res = null;
		
		try {
			res = this.queue.take(); //Se bloquea hasta que haya un sobre en el buz�n
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		return res;
	}
	
	/**
	 * 	Lanza un hilo de ejecuci�n que a�ade mensajes entrantes
	 * 	al buz�n (una cola).
	 */
	public void run(){
		try {
			ServerSocket serverSocket = new ServerSocket(this.PORT);
			while (run){
				Socket s = serverSocket.accept(); //Llamada bloqueante hasta que conecte con un nuevo proceso
				if (run){ //Si no es petici�n de terminar
					ObjectInputStream input = new ObjectInputStream(s.getInputStream());
					Envelope env = (Envelope) input.readObject();
					
					this.queue.offer(env); //Si hay hueco en la cola inserta el sobre, sino lo desecha
					
					input.close();
				}
				
				s.close();
			}
			
			serverSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
