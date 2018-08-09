package pt.lsts.imc.dsl

import pt.lsts.imc.dsl.Speed.Units
import pt.lsts.imc.*
import pt.lsts.imc.CompassCalibration.DIRECTION
import pt.lsts.neptus.messages.InvalidMessageException
import pt.lsts.imc.net.*

/**
 * DSL to generate IMC plans (maneuvers)
 * ported to groovy from  https://github.com/zepinto/imc-kotlin 
 * @author lsts
 *
 */
class DSLPlan {
	
	String plan_id
	String description
	Speed speed
	Z z
	Location location
	List<PlanManeuver> mans = new ArrayList <>()
	List<String> payload    = new ArrayList <>()
	int count
    String [] vehicles_id
    //String [] otherPayloads //Communications means and Acoustics requirements //TODO
	PlanSpecification planSpec = null
	DSLPlan(String id){
		plan_id = id
		count = 1
		location = Location.APDL
		speed = new Speed(900.0,Speed.Units.RPM) //default from Neptus
		z     = new Z(2.0,Z.Units.DEPTH)         //default from Neptus
//		speed = new Speed(1.0, Speed.Units.METERS_PS)
//		z = new Z(0.0,Z.Units.DEPTH)
	}
	
	public void setInitialLocation(Location loc){
		locate loc
	}
	
	public void vehicles(String... vs){
		vehicles_id = vs
	}
	
	public void planName(String id){
		plan_id = id
	}
	public String getPlan_id(){
		plan_id
	}
	public void speed(double value,String units){
		Speed.Units unit = Speed.fromString(units) //case insensitive
		speed = new Speed(value,unit)
		}
	public void z(double value,String units){
		Z.Units unit = Z.fromString(units)
		z = new Z(value,unit)
		}
	public void speed(double value,Speed.Units unit){
		speed = new Speed(value,unit)
		}
	public void z(double value,Z.Units unit){
		z = new Z(value,unit)
		}

	/**
	 * @return the mans
	 */
	public List<PlanManeuver> getMans() {
		return mans;
	}

	/**
	 * @param mans the mans to set
	 */
	public void setMans(List<PlanManeuver> mans) {
		this.mans = mans;
	}
	
	public List<String> getPayloadRequirements(){
		return payload;
		}
	
	 def Maneuver maneuver(String id, Class maneuver,Vector<IMCMessage> payloads) {
		def man = maneuver.newInstance()
			man.setLat location.latitude
			man.setLon location.longitude
			man.setSpeed speed.value
			man.setZ     z.value
			man.setZUnitsStr z.getUnits()
			man.setSpeedUnitsStr speed.getUnits()
							
		mans.add(planFromMan(id,man,payloads))
		man
	}
    
     def PlanManeuver planFromMan(String id,Maneuver man,Vector<IMCMessage> payloads) {
         
         def planFromMan = new PlanManeuver (
             maneuverId: id,
             data: man
           )
		 if(!payloads.empty)
		 	planFromMan.setStartActions(payloads)
         planFromMan
     }


	 //default values on args to current fields -> Named Parameters are Converted to Map
	 // see -> http://stackoverflow.com/questions/15393962/groovy-named-parameters-cause-parameter-assignments-to-switch-any-way-around-th

	 def Maneuver goTo (LinkedHashMap params){
		 def id = "$count"+".Goto"
         count++
		 List<Payload> payload = new ArrayList<>()
		 if(params!=null){
		 params.with{
			 if(params['speed']!= null)
		 		this.speed = params.speed
			 if(params['z']!= null)
				 this.z = params.z
			 if(params['location']!= null)
				 this.location = params.location
			 if(params['payload'] != null){
				 def payloadComponent
				 params['payload'].each{
					 if(it['name'] != null){
						 def payload_name = it['name']
						 payloadComponent = new Payload(payload_name)
						 
							 it.each {
							 key,value ->
							 if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
								 payloadComponent.property key, value}
							  }

						 payload.add payloadComponent
				 }
				 else
					 println "The name of the payload required must be provided."              }
		 }
			
		 }
	}
	 	def man = maneuver(id,Goto,Payload.addPayload(payload))
		addPayloadRequirements(payload)
        man
	}
	 
	 
	 def Maneuver followPath (LinkedHashMap params){
		 def id = "$count"+".FollowPath"
		 Vector<PathPoint> pts = new Vector<>()
		 count++
		 List<Payload> payload = new ArrayList<>()
		 if(params!=null){
		 params.with{
			 if(params['speed']!= null)
		 		this.speed = params.speed
			 if(params['z']!= null)
				 this.z = params.z
			 if(params['location']!= null)
				 this.location = params.location
			 if(params['payload'] != null){
				 def payloadComponent
				 params['payload'].each{
					 if(it['name'] != null){
						 def payload_name = it['name']
						 payloadComponent = new Payload(payload_name)
						 
							 it.each {
							 key,value ->
							 if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
								 payloadComponent.property key, value}
							  }

						 payload.add payloadComponent
				 }
				 else
					 println "The name of the payload required must be provided."              }
		 }
			if(params['points'] != null){
				params.points.each {
					pts.add(convertToPP(this.location,it))		
				}
			}
		 
		 }
	}
		FollowPath man = maneuver(id,FollowPath,Payload.addPayload(payload))
		man.setPoints(pts)
		addPayloadRequirements(payload)
		man
	}
	 
def PathPoint convertToPP(Location l1,Location l2){
	double[] offsets = l1.offsets(l2)
	float ze =0.0
	PathPoint pp = new PathPoint(((float)offsets[0]),((float)offsets[1]),ze)
	pp
}
	  
def addPayloadRequirements(List<Payload> ps){
	ps.each {
		if(!payload.contains(it.getName()))
			payload.add it.getName()
	}
}

def Maneuver loiter(LinkedHashMap params){
         List<Payload> payload = new ArrayList<>()
		double radius=20.0
		int duration = 60
		def id = "$count"+".Loiter"
        count++
		if (params != null){
		 if(params['duration']!=null)
		 	 duration= params.duration
	     if(params['radius']!=null)
			  radius= params.radius
		 if(params['speed']!= null)
		 	this.speed = params.speed
		 if(params['z']!= null)
			 this.z = params.z
		 if(params['location']!= null)
			 this.location = params.location
		 if(params['id'] != null)
			 id = params.id
                         if(params['payload'] != null){ 
                 def payloadComponent
                 params['payload'].each{
                     if(it['name'] != null){
                         def payload_name = it['name']
                         payloadComponent = new Payload(payload_name)
                         
                             it.each {
                             key,value -> 
                             if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
                                 payloadComponent.property key, value}
                              }

                         payload.add payloadComponent
                 }
                 else
                     println "The name of the payload required must be provided."              }
         }
		}
		Loiter man   = maneuver(id,Loiter,Payload.addPayload(payload))
		man.duration = duration 
		man.radius   = radius
        man.setType(Loiter.TYPE.CIRCULAR)
		addPayloadRequirements(payload)
		man
		
	}

	//def yoyo(double max_depth=20.0,double min_depth=2.0,Speed speed = this.speed, Z z = this.z,Location loc= this.location,String id="${count++}"+".YoYo"){
	def Maneuver yoyo(LinkedHashMap params){
         List<Payload> payload = new ArrayList<>()
		double max_depth=20.0,min_depth=2.0
		double  pitch = Math.toRadians(15)// Pitch Angle (rad) -> 15 degrees
		def id = "$count"+".YoYo"
        count++
		if (params != null){
		  if(params['pitch']!=null) //defined in degrees
			pitch= Math.toRadians(params.pitch)
		 if(params['max_depth']!=null)
			  max_depth= params.max_depth
		 if(params['min_depth']!=null)
			  min_depth= params.min_depth
		 if(params['speed']!= null)
			 this.speed = params.speed
		 if(params['z']!= null)
			 this.z = params.z
		 if(params['location']!= null)
			 this.location = params.location
		 if(params['id'] != null)
			 id = params.id
             if(params['payload'] != null){ 
                 def payloadComponent
                 params['payload'].each{
                     if(it['name'] != null){
                         def payload_name = it['name']
                         payloadComponent = new Payload(payload_name)
                         
                             it.each {
                             key,value -> 
                             if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
                                 payloadComponent.property key, value}
                              }

                         payload.add payloadComponent
                 }
                 else
                     println "The name of the payload required must be provided."              }
         }
		}
		
		YoYo man = maneuver(id, YoYo,Payload.addPayload(payload))
		
		man.amplitude = (max_depth - min_depth)
		man.z         = (max_depth + min_depth) / 2.0
		man.setPitch(pitch)
		addPayloadRequirements(payload)
		man
		
	}


	//def popup(double duration=180.0,boolean currPos=true,Speed speed = this.speed, Z z = this.z,Location loc= this.location,String id="{count++}"+".Popup"){
	def Maneuver popup(LinkedHashMap params) {
         List<Payload> payload = new ArrayList<>()
		int duration = 180
		def currPos  = true
		def wts      = false
		def sk       = false
		def id = "$count"+".PopUp"
        count++
		if (params != null){
		 if(params['duration']!=null)
			  duration= params.duration
		 if(params['currPos']!=null)
			  currPos= params.currPos
	     if(params['waitAtSurface']!=null)
			  wts= params.waitAtSurface
		 if(params['stationKeep']!=null)
			  sk= params.stationKeep
		 if(params['speed']!= null)
			 this.speed = params.speed
		 if(params['z']!= null)
			 this.z = params.z
		 if(params['location']!= null){
			 this.location = params.location
			 currPos = false
		 }
		 if(params['id'] != null)
			 id = params.id
             if(params['payload'] != null){ 
                 def payloadComponent
                 params['payload'].each{
                     if(it['name'] != null){
                         def payload_name = it['name']
                         payloadComponent = new Payload(payload_name)
                         
                             it.each {
                             key,value -> 
                             if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
                                 payloadComponent.property key, value}
                              }

                         payload.add payloadComponent
                 }
                 else
                     println "The name of the payload required must be provided."              }
         }
		}
		 

		PopUp man = maneuver(id, PopUp,Payload.addPayload(payload))
		
		man.duration = duration 
		man.flags    = ((currPos ? PopUp.FLG_CURR_POS : 0) + (wts ? PopUp.FLG_WAIT_AT_SURFACE :0) + (sk ? PopUp.FLG_STATION_KEEP: 0 ))
		addPayloadRequirements(payload)
		man
		
	}
	
	//def skeeping(double radius=20.0,double duration=0 ,Speed speed = this.speed, Z z = this.z,Location loc= this.location,String id="{count++}"+".StationKeeping") {
	def Maneuver skeeping(LinkedHashMap params){
         List<Payload> payload = new ArrayList<>()
		double radius= 20.0
		int duration = 60
		def id = "$count"+".StationKeeping"
        count++
		if (params != null){
		 if(params['duration']!=null)
			  duration= params.duration
		 if(params['radius']!=null)
			  radius= params.radius
		 if(params['speed']!= null)
			 this.speed = params.speed
		 if(params['z']!= null)
			 this.z = params.z
		 if(params['location']!= null)
			 this.location = params.location
		 if(params['id'] != null)
			 id = params.id
                                 if(params['payload'] != null){ 
                 def payloadComponent
                 params['payload'].each{
                     if(it['name'] != null){
                         def payload_name = it['name']
                         payloadComponent = new Payload(payload_name)
                         
                             it.each {
                             key,value -> 
                             if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
                                 payloadComponent.property key, value}
                              }

                         payload.add payloadComponent
                 }
                 else
                     println "The name of the payload required must be provided."              }
         }
		}
		 
		StationKeeping man = maneuver(id, StationKeeping,Payload.addPayload(payload))
		man.duration = duration
		man.radius   = radius
		addPayloadRequirements(payload)
		man
		}
	
	def DIRECTION compassDir(String dir){
		switch(dir){
			case "VDEP": return DIRECTION.VDEP;
			             break;

			case "CCLOCKW": return DIRECTION.CCLOCKW;
						    break;
			case "IWINDCURR": return DIRECTION.IWINDCURR;
						    break;
			
			case "CLOCKW":
			default:       return DIRECTION.CLOCKW;
			               break;
		}
	}
	
	def Maneuver compassCalibration(LinkedHashMap params){
		
		double amplitude=1,radius=5,pitch=15
		int duration = 300
		DIRECTION direction = DIRECTION.CLOCKW //VDEP(0),CLOCKW(1),CCLOCKW(2),IWINDCURR(3);
         List<Payload> payload = new ArrayList<>()
		def id = "$count"+".CompassCalibration"
        count++
		if (params != null){
		 if(params['duration']!=null)
			  duration= params.duration
	     if(params['pitch']!=null)
			  pitch= params.pitch
		if(params['direction']!=null)
			  direction= compassDir(params.direction.toUpperCase())
		 if(params['radius']!=null)
			  radius= params.radius
		if(params['amplitude']!=null)
			amplitude= params.amplitude
		if(params['speed']!= null)
			 this.speed = params.speed
		 if(params['z']!= null)
			 this.z = params.z
		 if(params['location']!= null)
			 this.location = params.location
		 if(params['id'] != null)
			 id = params.id
             if(params['payload'] != null){ 
                 def payloadComponent
                 params['payload'].each{
                     if(it['name'] != null){
                         def payload_name = it['name']
                         payloadComponent = new Payload(payload_name)
                         
                             it.each {
                             key,value -> 
                             if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
                                 payloadComponent.property key, value}
                              }

                         payload.add payloadComponent
                 }
                 else
                     println "The name of the payload required must be provided."              }
         }
		}
		 
		CompassCalibration man = maneuver(id, CompassCalibration,Payload.addPayload(payload))
		
		man.duration  = duration
		man.direction = direction
		man.radius    = radius
		man.amplitude = amplitude
		man.pitch     = Math.toRadians(pitch)
		addPayloadRequirements(payload)
		man
		}
	
	

	def Maneuver launch(LinkedHashMap params)  {
		def id="$count"+".Launch"
        count++
         List<Payload> payload = new ArrayList<>()
		if (params != null){
			if(params['speed']!= null)
				this.speed = params.speed
			if(params['z']!= null)
				this.z = params.z
			if(params['location']!= null)
				this.location = params.location
			if(params['id'] != null)
				id = params.id
             if(params['payload'] != null){ 
                 def payloadComponent
                 params['payload'].each{
                     if(it['name'] != null){
                         def payload_name = it['name']
                         payloadComponent = new Payload(payload_name)
                         
                             it.each {
                             key,value -> 
                             if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
                                 payloadComponent.property key, value}
                              }

                         payload.add payloadComponent
                 }
                 else
                     println "The name of the payload required must be provided."              }
         }
		   }
        
        def man = maneuver(id,Launch,Payload.addPayload(payload))
		addPayloadRequirements(payload)
        man
        
	}
	
	def Maneuver rows(LinkedHashMap params){
		double bearing=0.0,cross_angle=0.0,width=100,length=200,hstep=27.0
		short coff=15,flags
		//float alternation = 1.0f //percentage
	    short alternation = 100
		boolean squareCurve=true,firstCurveRight=false
        List<Payload> payload = new ArrayList<>()
        def id = "$count"+".Rows"
        count++
		if (params != null){
			if(params['bearing']!= null)
				bearing = Math.toRadians(params.bearing)
			if(params['alternation']!= null)
				alternation = params.alternation
			if(params['width']!= null)
				width = params.width			
			if(params['length']!= null)
				length = params.length
			if(params['hstep']!= null)
				hstep = params.hstep
			if(params['cross_angle']!= null)
				cross_angle = Math.toRadians(params.cross_angle)
			if(params['curvOff']!= null)
				coff = params.curvOff
			if(params['coff']!= null)
				coff = params.coff
			if(params['squareCurve']!= null)
				squareCurve = params.squareCurve
			if(params['firstCurveRight']!= null)
				firstCurveRight = params.firstCurveRight
			if(params['id'] != null)
				id = params.id
			if(params['speed']!= null)
				this.speed = params.speed
			if(params['z']!= null)
				this.z = params.z
			if(params['location']!= null)
				this.location = params.location
			if(params['id'] != null)
				id = params.id
             if(params['payload'] != null){ 
                 def payloadComponent
                 params['payload'].each{
                     if(it['name'] != null){
                         def payload_name = it['name']
                         payloadComponent = new Payload(payload_name)
                         
                             it.each {
                             key,value -> 
                             if(!key.equals("name")){ //verify(payload,key) <-- verify it parameter exists in payload to avoid runtime exceptions!"!!
                                 payloadComponent.property key, value}
                              }

                         payload.add payloadComponent
                 }
                 else
                     println "The name of the payload required must be provided."              }
         }
		   }
		Rows man = maneuver(id,Rows,Payload.addPayload(payload))
		//Rows man = new Rows(timeout, location.latitude, location.longitude, z.value, z.units, speed.value, speed.units, bearing, cross_angle, width, length, hstep, coff, alternation, flags, null)
		man.width          = width
		man.length         = length
		man.hstep          = hstep
		man.coff           = coff
		man.setAlternation(alternation)//(short)(alternation*100f));
		man.setBearing(bearing)
		man.setCrossAngle(cross_angle)
        flags = ((squareCurve ? Rows.FLG_SQUARE_CURVE : 0) + (firstCurveRight ? Rows.FLG_CURVE_RIGHT : 0))
	    man.setFlags flags
		
		mans.add(planFromMan(id,man,Payload.addPayload(payload)))
		addPayloadRequirements(payload)

		man

	}
	
	def locate(double latitude, double longitude) {
		def loc = new Location(latitude, longitude)
		this.location = loc
	}
	def locate(Angle latitude, Angle longitude) {
		def loc = new Location(latitude, longitude)
		this.location = loc
	}
	def locate(Location loc) {
		this.location = loc

		
	}
	
	def move(double northing, double easting){
		this.location= location.translateBy(northing,easting)
	}

private def List<PlanTransition> maneuver_transitions(){
		
		def trans = new ArrayList<PlanTransition>()
		PlanManeuver previous = null
		mans.each {
			if (previous != null) {
			
			def transition = new PlanTransition(
				sourceMan: previous.maneuverId,
				destMan: it.maneuverId,
				conditions: "maneuverIsDone"
			)
		trans += transition
	   }
		previous = it
	}
		trans
} 
		
		
	def PlanSpecification asPlanSpecification() {
		if(planSpec!=null)
		return planSpec
		else{
			if(vehicles_id==null)
				vehicles_id = {"lauv-xplore-1"}
			PlanSpecification ps = new PlanSpecification()
			ps.description = description
			ps.planId = plan_id
			ps.startManId = getMans()[0].getManeuverId()
			ps.setManeuvers getMans()
			ps.setTransitions maneuver_transitions()
			try{
				ps.validate()
				
				
			}
			catch(InvalidMessageException e){
				ps = null
			}
			planSpec = ps
			planSpec
		}
	}
}







