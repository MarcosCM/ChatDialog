/**
 * 	FICHERO: TotalOrderMulticast.java
 * 	DESCRIPCI�N: Gestiona el env�o de mensajes mediante el
 * 				algoritmo de Ricart & Agrawala
 */
package ssdd.ms;

import java.io.Serializable;

public class TotalOrderMulticast {

	private MessageSystem ms;
	
	public TotalOrderMulticast(MessageSystem ms){
		this.ms = ms;
	}
	
	/**
	 * 	Env�a el mensaje 'msg' a todos los procesos que conoce.
	 */
	public synchronized void sendMulticast(Serializable msg){ //synchronized => s�lo un mensaje a la vez
		ms.requestingCS();
		ms.waitTillReadyForCS();
		ms.sendToEveryone(msg);
		ms.leavingCS();
	}
	
	/**
	 * 	Atiende los mensajes REQ o ACK de la manera correspondiente
	 * 	y devuelve el siguiente mensaje multicast que se reciba.
	 */
	public Envelope receiveMulticast(){
		while (true){
			Envelope e = ms.receive();
			MessageValue val = (MessageValue) e.getPayload();
			if (val.getIsMessage()){
				return e;
			}
			else{
				ms.reqAckManager(e);
			}
		}
	}
}
