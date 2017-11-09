package pt.lsts.imc.dsl

import java.util.HashMap
import java.util.List
import groovy.xml.*
import pt.lsts.imc.EntityParameter
import pt.lsts.imc.Maneuver
import pt.lsts.imc.SetEntityParameters
import java.util.LinkedList;
import java.util.List;
import java.util.Vector
import pt.lsts.imc.IMCMessage
import pt.lsts.imc.IMCUtil;;

class Payload {
	
	HashMap <String,String[]> available_params //TODO verify entityParameters settings
	String name
	List<EntityParameter> params
	SetEntityParameters setEntityParam
	public Payload(String nombre) {
		name = normalizeParameterName(nombre)
		params = new ArrayList<>()
		setEntityParam = new  SetEntityParameters()
		setEntityParam.setName name
		params.add(new EntityParameter("Active","true"))
		
	  }
	
   String normalizeParameterName (String parameter){
	  String result=""
	  int length = parameter.split().length
	  parameter.split().eachWithIndex{ p, index ->
	      String aux =  p.capitalize().substring(0,1)
	      p.substring(1).each{
	          aux+=it.toLowerCase()
	      }
		  if(index >0)
		  	result+=" "+aux
	  	 else
		  result+=aux
	  }
        result
        
     }
   
   public String getName(){
	   name
	   }
   
	public void property (String prop,int value){
		params.add(new EntityParameter(normalizeParameterName(prop),value.toString()))
	}
	public void property (String prop,String value){
		params.add(new EntityParameter(normalizeParameterName(prop),value))
	}
	
	public static Vector<IMCMessage> addPayload(List<Payload> payloads) {

		List<SetEntityParameters> setEntities = new ArrayList<>()
		payloads.each{
			it.setEntityParam.setParams it.params
			setEntities.add it.setEntityParam
			}
//		Vector<IMCMessage> msg = new Vector<>()
//		msg.addAll setEntities
//		Collections collection = new Collections();
//		Collections.addAll(collection,setEntities)
		setEntities
		
	}

}
