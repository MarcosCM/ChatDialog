/**
 * 	FICHERO: MessageSystem.java
 * 	DESCRIPCIÓN: Gestiona un proceso que envía mensajes o
 * 				recibe mensajes de otros procesos que pueden
 * 				estar en máquinas remotas.
 */
package ssdd.ms;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

public class MessageSystem {
	
	private static final String REQ_MSG = "req";
	private static final String ACK_MSG = "ack";
	private static final Serializable REQ = new MessageValue(REQ_MSG, false);
	private static final Serializable ACK = new MessageValue(ACK_MSG, false);
	
	private final int MAX_MAILBOX_SIZE = 15;
	private final int ID;
	private final boolean debug;
	
	private MailBox mailBox;
	private ArrayList<IPPort> processList;
	
	//Atributos del algoritmo Ricart & Agrawala
	private final int NUM_PROC;
	
	private int myClock = 0;
	private int auxClock = 0;
	private Semaphore acksReceived = new Semaphore(0); //bloquea hasta que se reciben todos los acks
	private boolean requestingCS = false;
	private boolean[] replyDeferred;
	
	/**
	 * 	Crea un gestor de mensajes para el proceso con id = 'src', siendo
	 * 	'src' >= 1.
	 * 	El archivo 'networkFile' contiene las ips de los procesos y los puertos
	 * 	por los que escuchan, siguiendo el siguiente patrón:
	 * 	(Línea 1) ipProceso1:puertoProceso1
	 * 		.
	 * 		.
	 * 		.
	 * 	(Línea n-ésima) ipProcesoN:puertoProcesoN
	 * 	El valor de 'debug' determina si debe mostrar mensajes de depuración por
	 * 	pantalla o no.
	 */
	public MessageSystem(int src, String networkFile, boolean debug) throws FileNotFoundException{
		//Asignación de atributos propios del proceso
		this.ID = src;
		this.debug = debug;
		this.processList = new ArrayList<IPPort>();
		
		//Guarda en un array las ips y puertos correspondientes a los procesos
		File f = new File(networkFile);
		if (f.exists()){ //El fichero existe
			Scanner s = new Scanner(f);
			s.useDelimiter("\\n");
			String line = null;
			Scanner lineScanner = null;
			
			int i=0;
			while(s.hasNext()){
				line = s.next();
				lineScanner = new Scanner(line);
				lineScanner.useDelimiter(":");
				processList.add(i, new IPPort(lineScanner.next(), Integer.parseInt(lineScanner.next().trim())));
				lineScanner.close();
				i+=1;
			}
			
			NUM_PROC = i; //número de procesos
			replyDeferred = new boolean[NUM_PROC];
			Arrays.fill(replyDeferred, false); //inicializa el array a false
			
			s.close();
		}
		else{ //El fichero no existe
			throw new FileNotFoundException();
		}
		
		//Crea un buzón para este proceso
		this.mailBox = new MailBox(MAX_MAILBOX_SIZE, processList.get(src-1).getPort());
		(new Thread(this.mailBox)).start();
	}
	
	/**
	 * 	Devuelve el semáforo que bloquea al 'acquirer' mientras
	 * 	el número de acks pendientes sea mayor que 0.
	 */
	public void waitTillReadyForCS(){
		for (int i=0; i<NUM_PROC-1; i+=1){
			try {
				acksReceived.acquire();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * 	Gestiona un mensaje REQ o ACK.
	 */
	public synchronized void reqAckManager(Envelope e){ //synchronized => sino pueden dar problemas si hay dos o más invocaciones a la vez
		MessageValue val = (MessageValue) e.getPayload();
		if (val.getValue().equals(REQ_MSG)){ //REQ
			receivedReq(e.getStamp(), e.getSource());
		}
		else{ //ACK
			receivedAck(e.getStamp(), e.getSource());
		}
	}
	
	/**
	 *	Gestiona la recepción de un mensaje de tipo REQ.
	 */
	private void receivedReq(int sourceClk, int sourcePid){
		myClock = Math.max(myClock, sourceClk) + 1;
		if (requestingCS && higherPriority(sourceClk, sourcePid)){ //si está en sección crítica y tiene más prioridad
			replyDeferred[sourcePid-1] = true;
		}
		else{
			send(sourcePid, ACK);
		}
	}
	
	/**
	 * 	Devuelve true si el proceso 'ID' tiene mayor prioridad
	 * 	que el proceso 'sourcePid' con valor de reloj 'sourceClk'.
	 */
	private boolean higherPriority(int sourceClk, int sourcePid){
		boolean res = false;
		
		if (auxClock < sourceClk){
			res = true;
		}
		else if (auxClock == sourceClk){
			if (ID < sourcePid){
				res = true;
			}
		}
		
		return res;
	}
	
	/**
	 * 	Gestiona la recepción de un mensaje de tipo ACK.
	 */
	private void receivedAck(int sourceClk, int sourcePid){
		myClock = Math.max(myClock, sourceClk) + 1;
		acksReceived.release();
	}
	
	/**
	 *	Gestiona la solicitud de entrar a la sección crítica.
	 */
	public void requestingCS(){
		requestingCS = true;
		myClock += 1;
		auxClock = myClock;
		sendToEveryone(REQ); //envía solicitudes a todos los procesos
	}
	
	/**
	 *	Gestiona el abandono de la sección crítica.
	 */
	public void leavingCS(){
		myClock+=1;
		requestingCS = false;
		for (int i=1; i<=NUM_PROC; i+=1){
			if (replyDeferred[i-1]){
				send(i, ACK);
				replyDeferred[i-1] = false;
			}
		}
	}
	
	/**
	 * 	Envía el mensaje 'msg' al proceso 'dst'.
	 */
	private void send(int dst, Serializable msg){ //private => ahora se usa sendToEveryone (dado que el sistema implementa el sendMulticast)
		Envelope env = new Envelope(this.ID, dst, msg, myClock); //Encapsula el mensaje en un objeto serializable
		IPPort ipPort = processList.get(dst-1); //La secuencia de ids de los procesos empieza por 1 en vez de 0
		try {
			//Enviar una petición de socket
			Socket s = new Socket();
			s.connect(new InetSocketAddress(ipPort.getIp(), ipPort.getPort()), 0); //Llamada bloqueante hasta que conecte
			ObjectOutputStream output = new ObjectOutputStream(s.getOutputStream());
			
			if (this.debug){ //mostrar por pantalla
				String type = "Protocolo Ricart & Agrawala";
				if (((MessageValue) msg).getIsMessage()){
					type = "Normal";
				}
				System.out.printf("ID"+ID+"\n\tEstampilla mía: "+myClock+"\n\tEnviando mensaje con parámetros\n\tTipo de mensaje: "+type+"\n\tFuente: %d; Destinatario: %d;\n\tMensaje: %s\n", this.ID, dst, env.getPayload());
			}
			output.writeObject(env);
			
			output.close();
			s.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 	Envía el mensaje 'msg' a todos los procesos conocidos.
	 */
	public void sendToEveryone(Serializable msg){
		for (int i=1; i<=NUM_PROC; i+=1){ //envía solicitudes a todos los procesos
			if (i!=ID){
				send(i, msg);
			}
		}
	}
	
	/**
	 * 	Devuelve el siguiente mensaje del buzón.
	 * 	Si no hay mensajes entonces se bloquea hasta que entre uno,
	 * 	y lo devuelve.
	 */
	public Envelope receive(){
		Envelope res = this.mailBox.getEnvelope();
		if (this.debug){ //mostrar por pantalla
			String type = "Protocolo Ricart & Agrawala";
			if (((MessageValue) res.getPayload()).getIsMessage()){
				type = "Normal";
			}
			System.out.printf("ID"+ID+"\n\tEstampilla recibida: "+res.getStamp()+"\n\tRecibido mensaje con parámetros\n\tTipo de mensaje: "+type+"\n\tFuente: %d; Destinatario: %d;\n\tMensaje: %s\n", res.getSource(), res.getDestination(), res.getPayload());
		}
		return res;
	}
	
	/**
	 * 	Cierra el buzón de manera segura, de manera que no podrá
	 * 	recibir ni recoger más mensajes.
	 */
	public void stopMailbox(){
		//Enviar petición de terminar
		this.mailBox.stopThread();
		try {
			Socket s = new Socket();
			s.connect(new InetSocketAddress("localhost", processList.get(ID-1).getPort()), 0);
			s.close();
		} catch (Exception e) {}
	}
}
