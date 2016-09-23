/**
 * 	FICHERO: Receiver.java
 * 	DESCRIPCI�N: Gestiona un hilo de ejecuci�n para mostrar
 * 				los mensajes recibidos en la ventana de chat
 * 				correspondiente al proceso.
 */
package ssdd.ms;


public class Receiver implements Runnable {

	private TotalOrderMulticast t;
	private ChatDialog chat;
	
	public Receiver(MessageSystem ms){
		this.t = new TotalOrderMulticast(ms);
	}
	
	/**
	 * 	Asigna una ventana de chat al hilo receptor.
	 */
	public void setChat(ChatDialog chat){
		this.chat = chat;
	}
	
	/**
	 * 	Constantemente espera a recibir mensajes multicast
	 * 	y los a�ade al chat del proceso.
	 */
	public void run() {
		while(true){
			Envelope e = t.receiveMulticast();
			chat.addMessage("ID"+e.getSource()+": "+e.getPayload());
		}
	}
}
