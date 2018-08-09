package pt.lsts.imc.dsl

import pt.lsts.util.WGS84Utilities

class Angle {
	double rad
	public Angle(double r) {
		rad = r
	}
	def asDegrees() {Math.toDegrees rad}
	def asRadians() { rad}
	def minus (Angle a) {return new Angle(a.asRadians() - this.asRadians())}
	def plus (Angle a) {return new Angle(a.asRadians() + this.asRadians())}
	def times (double n) {return new Angle(this.asRadians() * n)}
	def div (double n) {return new Angle(this.asRadians() / n)}
	def unaryMinus () {return new Angle (-this.asRadians())}
	
	@Override
	String toString(){
		String.format("%.1fº", asDegrees())
	}
}
/**
 * 
 * @author keila
 * Latitude and Longitude in Radians 
 */
class Location {
	
	def final static FEUP = new Location(toRadians(41.1781918), toRadians(-8.5954308))
	def final static APDL = new Location(toRadians(41.185242), toRadians(-8.704803))
	def static toRadians(double value) {Math.toRadians value} //make it a Category of double/Integer
	def static toDeg(double value){Math.toDegrees value}
	//test 	println assert deg(90) == Math.PI.rad() / 2
	double latitude
	double longitude
	public Location (double la, double lo) {
		latitude = la
		longitude = lo
	}
	public Location (Angle la, Angle lo) {
		latitude = la.rad
		longitude = lo.rad
	}

	def angle (Location l) {
		def offset = offsets(l)
		Math.atan2(offset[1], offset[0])
	}



	def offsets(Location l) {
		return WGS84Utilities.WGS84displacement(
				toDeg(latitude), toDeg(longitude), 0.0, toDeg(l.latitude), toDeg(l.longitude), 0.0)
		// new Tuple2(offset[0],offset[1])
	}

	def distance(Location l) {
		def offset = offsets(l)
		return Math.hypot(offset[0], offset[1])
	}


	def translateBy(double n,double e )  {
		def coords =  WGS84Utilities.WGS84displace(toDeg(latitude), toDeg(longitude), 0.0, n, e, 0.0)
		return new Location(toRadians(coords[0]),toRadians(coords[1]))
	}
	@Override
	public String toString(){return latitude+" "+longitude}
}
