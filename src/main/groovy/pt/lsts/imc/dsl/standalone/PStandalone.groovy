package pt.lsts.imc.dsl.standalone

import pt.lsts.imc.dsl.*
import pt.lsts.imc.*
import pt.lsts.imc.CompassCalibration.DIRECTION
import pt.lsts.neptus.messages.InvalidMessageException
import pt.lsts.imc.net.*

class PlanStandalone  {
	
	
	private IMCProtocol imc
	private DSLPlan dslPlan
	PlanStandalone(DSLPlan plan){
		dslPlan = plan
		imc = null
		
	}
	
	static PlanStandalone plan(Closure cl) {
		def plan = new DSLPlan()
  
		def code = cl.rehydrate(plan, cl.getOwner(), cl.getThisObject())
		code.resolveStrategy = Closure.DELEGATE_FIRST
		code()
		new PlanStandalone(plan)
	}
	
	public IMCProtocol getIMCProtocol() {
		if(imc==null)
			imc= new IMCProtocol()
		imc
	}
	
	void closeConnection(){
		getIMCProtocol().stop();
		imc=null
	}

	void stopCurrentPlan(String vehicle){
		if(send(new PlanControl(opStr:'STOP'),vehicle))
			println "Stop plan"
	}
/**
 * Send message to vehicles in argument
 * @param vehicles
 * @return a list of vehicles witch sending failed
 */
	boolean sendTo(String vehicle) {
		def plan_spec = dslPlan.asPlanSpecification()
		PlanControl plan = 	new PlanControl(
			opStr: 'LOAD', //'START'
			planId: dslPlan.plan_id,
			arg: plan_spec
			)
		println "Sending plan to "+vehicle
		send(plan,vehicle)

	}
	
	void startPlan(String vehicle,boolean isLoaded=false){
		stopCurrentPlan vehicle
		PlanControl plan = 	new PlanControl(
			opStr: 'START', //'START'
			planId: dslPlan.plan_id,
			arg: isLoaded? null : dslPlan.asPlanSpecification()
			)
		if(send(plan,vehicle)){
			println ("$dslPlan.plan_id commanded to $vehicle")
			closeConnection()
		}
		else return
	}
	
	boolean send(PlanControl p, String vehicle,long milis=30000){
		//IMCProtocol protocol=new IMCProtocol()
		println "Tryig to stablish connection to "+ vehicle+". . ."
		getIMCProtocol().connect vehicle 
		def connect = getIMCProtocol().waitFor(vehicle, milis)
		if (connect){// select != null &&  //IMCSendMessageUtils.sendMessage(plan,false,vehicle)
			println "Connection stablished"
			if(getIMCProtocol().sendMessage(vehicle, p))
				return true
			else
				println "Error sending plan to $vehicle"
				closeConnection()
				return false
		}
		else{
			println "Error communicating with $vehicle"
			closeConnection()
			return false
		}
		
	
	}
	
}
// Plan builder

//PlanStandalone.plan {
//	planName "Bananas-Plan"
//	def home = new Location(41.185242, -8.704803)
//	
//	// set current plan location
//	locate home
//	
//	// set speed units to use
//	speed.units = Speed.Units.METERS_PS
//	speed.value = 1.2
//	
//	// set z reference to use
//	z.value = 5
//	z.units = Z.Units.ALTITUDE
//}











