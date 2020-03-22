/*
 * Project : demoDeviceClient
 * File  : DemoActuator.java
 * Package : org.imt.atlantique.sss.upas.client
 * Description : 
 * Date : 14 févr. 2020
 * Copyright : © 02 2020 - All rights reserved - IMT Atlantique
 * @version 1.0
 * @auteur Issam
 */
package hygrometric;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;

import org.upec.lissi.ubistruct.dpws.client.CallBack;
import org.upec.lissi.ubistruct.dpws.client.DataElement;
import org.upec.lissi.ubistruct.dpws.client.DeviceAccessPoint;
import org.upec.lissi.ubistruct.dpws.client.EventSubscription;
import org.upec.lissi.ubistruct.dpws.client.UbiStructFactory;
import org.upec.lissi.ubistruct.dpws.client.event.AccessPointEvent;
import org.upec.lissi.ubistruct.dpws.client.event.AccessPointListener;
import org.upec.lissi.ubistruct.dpws.client.event.AccessPointStatus;
import org.ws4d.java.communication.CommunicationException;
import org.ws4d.java.service.parameter.ParameterAttribute;
import org.ws4d.java.service.parameter.ParameterValue;
import org.ws4d.java.service.parameter.ParameterValueManagement;
import org.ws4d.java.structures.Iterator;

import com.complexible.stardog.api.Connection;
import com.complexible.stardog.api.ConnectionConfiguration;
import com.complexible.stardog.api.ConnectionPool;


import com.stardog.stark.IRI;
import com.stardog.stark.Values;
import com.stardog.stark.query.SelectQueryResult;
import com.stardog.stark.query.io.QueryResultWriters;
import com.stardog.stark.vocabs.FOAF;
import com.stardog.stark.vocabs.RDF;
import static com.stardog.stark.Values.literal;

/**
 * @author Issam
 *
 */
public class IoTClient implements CallBack, AccessPointListener {

	private DeviceAccessPoint sensorDeviceAccessPoint;
	private DeviceAccessPoint actuatorDeviceAccessPoint;
	
	private Connection connection;
	private static Integer idObs = 0;
	/**
	 * 
	 */
	public IoTClient() {
		// create the factory
		UbiStructFactory ubiStructFactory = UbiStructFactory.getInstance();
		// Save the current class as a listener to receive a notification when the
		// access points it manages change state
		ubiStructFactory.getDPWSFacadeInstance().addAccessPointListener(this);
		// create access point to communicate with device (receive notification or send
		// call action)
		/*
		 * The names of the service and the nameSpace are given by the provider of the
		 * device or service.
		 */
		this.sensorDeviceAccessPoint = ubiStructFactory.newDeviceAccessPoint("Sensor",
				"http://www.imt-atlantique.fr/upas");
		this.actuatorDeviceAccessPoint = ubiStructFactory.newDeviceAccessPoint("actuator",
				"http://www.imt-atlantique.fr/upas");
		//Instanciation de l'objet de connection à Stardog
		this.connection = ClientStardog.getConnection();
		
	}

	@Override
	public void accessPointStatusChanged(AccessPointEvent e) {
		// We receive here the notifications on the change of AccessPoint created
		System.out.println("Attention notification: " + e.getComment());
		// Upon receipt of the Device connection we will subscribe
		if (e.getStatus() == AccessPointStatus.CONNECTED)// check if the notification concerns a connection
		{
			if (e.getAccessPoint() == this.sensorDeviceAccessPoint) {
				System.out.println("Found the sensor");
				if (this.sensorDeviceAccessPoint.isReady()) {
					try {
						EventSubscription eventSubscription = sensorDeviceAccessPoint.createEventSubscription(
								"http://www.enstb.org/upas/Notification", "NotificationService",
								"DataNotificationEvent", true);
						eventSubscription.setCallback(this);
					} catch (NullPointerException | IllegalArgumentException | CommunicationException e1) {
						e1.printStackTrace();
					}
				}
			}
			
			if (e.getAccessPoint() == this.actuatorDeviceAccessPoint) {
				System.out.println("Found the actuator");
			}

		}
	}
	

	//Apparemment command = ON / OFF
	private void changeActuator(String elementName, String command) {
		Map<String, Object> dataValue = new ConcurrentSkipListMap<String, Object>();
		dataValue.put("elementName",  elementName);
		dataValue.put("command", command);
		this.actuatorDeviceAccessPoint.callAction("CommandService", "ExecuteCommand", dataValue);
	}

	@Override
	public ArrayList<DataElement> receiveEventNotification(EventSubscription source, ParameterValue parameterValue) {
		System.out.println("-----Youpi the sensor sends data -----");
		connection.begin();
		IRI iriObs = Values.iri("http://api.stardog.com/observation", idObs.toString());
		connection.add()
			.statement(iriObs, RDF.TYPE, Values.iri("http://www.w3.org/ns/sosa/Observation"));
		viewParameterValue(parameterValue);
//		IRI iri = Values.iri("http://api.stardog.com/observation", idObs.toString());
		idObs++;
		connection.commit();
		//doActuation();
		return null;
	}

	public void viewParameterValue(ParameterValue parameterValue) {
		System.out.println("Name: " + parameterValue.getName() + " valeur :"
				+ ParameterValueManagement.getString(parameterValue, null));

		for (Iterator attributeIterator = parameterValue.attributes(); attributeIterator.hasNext();) {
			ParameterAttribute attribut = (ParameterAttribute) attributeIterator.next();
			System.out.println("ATTRIBUT: " + attribut.getName() + " : " + attribut.getValue());
			IRI iriObs = Values.iri("http://api.stardog.com/observation/", idObs.toString());
			switch (attribut.getName().toString()){
				case "{http://www.enstb.org/upas/Notification}timesStamp":
					System.out.println("!!!TIMESTAMP!!!");
					connection.add()
						.statement(iriObs, Values.iri("http://www.w3.org/ns/sosa/resultTime"), literal(attribut.getValue()));
					break;
/*				case "{http://www.enstb.org/upas/Notification}unit":
					System.out.println("!!!UNIT!!!");
					connection.add()
						.statement(iriObs, Values.iri("http://www.w3.org/ns/sosa/resultTime"), literal(attribut.getValue()));
					break;
*/				case "{http://www.enstb.org/upas/Notification}value":
					System.out.println("!!!VALUE!!!");
					connection.add()
						.statement(iriObs, Values.iri("http://www.w3.org/ns/sosa/hasSimpleResult"), literal(attribut.getValue()));
					break;
				case "{http://www.enstb.org/upas/Notification}id":
					System.out.println("!!!ID!!!");
					connection.add()
						.statement(iriObs, Values.iri("http://www.w3.org/ns/sosa/madeBySensor"), Values.iri("http://api.stardog.com/sensor/", attribut.getValue()));
					break;
				case "{http://www.enstb.org/upas/Notification}name":
					System.out.println("!!!NAME!!!");
					connection.add()
						.statement(iriObs, Values.iri("http://www.w3.org/ns/sosa/observedProperty"), Values.iri("http://api.stardog.com/", attribut.getValue()));
					break;	
				default:
					System.out.println("Pas de donnée ajoutée :( Switch sur :" + attribut.getName().toString());
					break;
			}
				
		}
		if (parameterValue.hasChildren())
			for (Iterator iterator = parameterValue.children(); iterator.hasNext();)
				viewParameterValue((ParameterValue) iterator.next());

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		@SuppressWarnings("unused")
		IoTClient iotClient = new IoTClient();
	}

}
