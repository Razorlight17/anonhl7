package pt.healthysystems.anonhl7;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.json.JSONException;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;

import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.EncodingNotSupportedException;
import ca.uhn.hl7v2.parser.Parser;
import ca.uhn.hl7v2.parser.PipeParser;
import ca.uhn.hl7v2.util.Terser;
//import ca.uhn.hl7v2.Location;
//import ca.uhn.hl7v2.util.SegmentFinder;
//import ca.uhn.hl7v2.model.Segment;
//import ca.uhn.hl7v2.model.v23.message.ADT_A04;
//import ca.uhn.hl7v2.model.v23.segment.MSH;

@RestController
public class Controller {

	@RequestMapping(value = "anonhl7", method = RequestMethod.GET)
	public String form() {
		
		return "<h3>Healthy Systems - Testing page</h3><p>>>>Anonimize HL7data<br>Status: ok</br></p><form method='post' action='anonhl7'>"
				+ "<textarea name='in'></textarea><input type='submit'></form>";

	}

	@RequestMapping(value = "anonhl7", method = RequestMethod.POST, produces = "text/plain")
	public String message(@RequestBody String body) throws MalformedURLException, IOException, JSONException, HL7Exception {

		System.out.println("\n \n 1 >>>>>>>>>>>>> Original Msg: \n" + body);

		int x = body.length();
		String sb2 = new String(body);
		String bodysub_2 = sb2.replace("result|message_1|", "");
		String editedBody = bodysub_2.toString();

		System.out.println("\n \n 2 >>>>>>>>>>>>> TESTE body CONTAGEM: \n" + x + "\n \n 3 >>>>>>>>>>>>> TESTE EDITEDBODY - eliminar o result|message_1: \n" + editedBody);

		HapiContext context = new DefaultHapiContext();
		Parser p = context.getGenericParser();
		Message hapiMsg = null;
		try {
			hapiMsg = p.parse(editedBody);
		} catch (EncodingNotSupportedException e) {
			e.printStackTrace();
		} catch (HL7Exception e) {
			e.printStackTrace();
		}

		// dependendo do tipo de mensagens do sistema, será preciso adaptar.
		//ADT_A04 adtMsg = (ADT_A04) hapiMsg; // adt_a04 é msg de registo de paciente
		//MSH mshAdtMsg = adtMsg.getMSH();
		
		// ORU_R01 obsMsg = (ORU_R01)hapiMsg; //ORU_R01 é msg de observação
		// MSH msObsMsg = obsMsg.getMSH();

		// Para ADT_A04
		//CM_MSG msgType = mshAdtMsg.getMsh9_MessageType();
		//ST msgTrigger = mshAdtMsg.getMessageControlID();
		//CX mensagemTeste = adtMsg.getPID().getPid4_AlternatePatientID();

		//System.out.println("\n 4----------- tipo de mensagem: " + msgType + "\n 5----------- trigger de mensagem: "
		//		+ msgTrigger + "\n 6----------- PID alternativo: admin" + mensagemTeste);

		StringBuilder sb = new StringBuilder();
		String[] lines = editedBody.split("[\n\r]");
		for (String line : lines)
			if (!line.startsWith("PID"))
				sb.append(line + " \n");

		String editedBody1 = sb.toString();
		System.out.println("\n 7 >>>>>>>>>>>>> Filtered Msg a eliminar linha pid: \n" + editedBody1);		
		
	
		try {
			Message hapiMsg1;
			hapiMsg1 = p.parse(editedBody);
			//ADT_A04 adtMsg1 = (ADT_A04) hapiMsg1;
			
			//criar divisor por pipes e terser para identificar campos do segmento
			PipeParser pipeParser = new PipeParser();
			Message messageParser = pipeParser.parse(editedBody);
			Terser terser = new Terser(messageParser);
			//Segment segment = terser.set("/.PID", "-");
			//System.out.println(segment);
			
			for(int i=1; i<31; i++){
				String conc = "/.PID-" + i;
				//System.out.println(i + ": " + segment.getField(i).length);
				terser.set(conc, "");
				for(int j=1; j<50 ; j++){
					String conc2 = conc + "-" + j;
					terser.set(conc2, "");
//					for (int r=1; r<5; r++){ //caso da repeticao de campos com () -- ~|~~~~~~~~~|~
//						String rep = conc + "(" + r + ")";
//						terser.set(rep , "");
//					}
					for(int k=1; k<20 ; k++){
						String conc3 = conc2 + "-" + k;
						terser.set(conc3, "");
					}
				}
				String f= "/.PID-1";
				terser.set(f, "999999");
			}
			

			
			String editedMessage = messageParser.encode();
			System.out.println("\n mensagem modificada com o parser \n" + editedMessage);
			
			//CX mensagemTeste1 = adtMsg.getPID().getPid4_AlternatePatientID();
			//System.out.println("\n 9 >>>>>>>>>>>>> PID alt: \n" + hapiMsg1);
			//System.out.println("\n 10 >>>>>>>>>>>>> PID alternativo depois de Filtered Msg: \n" + mensagemTeste1);
			HttpJSON.post(new URL("http://localhost:6650"), editedMessage);
		} catch (EncodingNotSupportedException e) {
			e.printStackTrace();
		} catch (HL7Exception e) {
			e.printStackTrace();
		}

		
				
		//HttpJSON.post(new URL("http://localhost:6650"), editedMessage);
		return editedBody1;
	}
}
