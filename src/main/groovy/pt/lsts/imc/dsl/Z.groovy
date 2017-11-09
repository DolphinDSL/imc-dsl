package pt.lsts.imc.dsl

import pt.lsts.imc.dsl.Speed.Units;

class Z {
    
    public enum Units {
        DEPTH,
        ALTITUDE,
        HEIGHT,
        NONE
    }
    
    double value;
    Units units;
    public Z(double value,Units units) {
        this.value=value;
        this.units=units;
    }

    String  getUnits(){
        return this.units.toString();
        }
	
	static Units fromString(String unit){
		if(unit.equalsIgnoreCase(Units.DEPTH.toString()))
			return Units.DEPTH
		if(unit.equalsIgnoreCase(Units.ALTITUDE.toString()))
			return Units.ALTITUDE
		if(unit.equalsIgnoreCase(Units.HEIGHT.toString()))
			return Units.HEIGHT
		if(unit.equalsIgnoreCase(Units.NONE.toString()))
			return Units.NONE
	}
}
