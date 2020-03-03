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
package org.imt.atlantique;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

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

/**
 * @author Issam
 *
 */
public class IoTClient implements CallBack, AccessPointListener {

	private DeviceAccessPoint sensorDeviceAccessPoint;
	private DeviceAccessPoint actuatorDeviceAccessPoint;

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

	private void changeActuator() {
		Map<String, Object> dataValue = new ConcurrentSkipListMap<String, Object>();
		dataValue.put("elementName", "");
		dataValue.put("command", "");
		this.actuatorDeviceAccessPoint.callAction("CommandService", "ExecuteCommand", dataValue);
	}

	@Override
	public ArrayList<DataElement> receiveEventNotification(EventSubscription source, ParameterValue parameterValue) {
		System.out.println("-----Youpi the sensor send data -----");
		viewParameterValue(parameterValue);
		return null;
	}

	public static void viewParameterValue(ParameterValue parameterValue) {
		System.out.println("Name: " + parameterValue.getName() + " valeur :"
				+ ParameterValueManagement.getString(parameterValue, null));

		for (Iterator attributeIterator = parameterValue.attributes(); attributeIterator.hasNext();) {
			ParameterAttribute attribut = (ParameterAttribute) attributeIterator.next();
			System.out.println("ATTRIBUT: " + attribut.getName() + " : " + attribut.getValue());
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
