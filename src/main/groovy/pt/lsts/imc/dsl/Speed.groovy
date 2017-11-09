package pt.lsts.imc.dsl

class Speed {
    
    public enum Units{
        METERS_PS,
        PERCENTAGE,
        RPM
    }
    
    double value;
    Units units;
    public Speed(double value,Units units) {
        this.value=value;
        this.units=units;
    }
    
    String  getUnits(){
        return units.toString();
        }
	static Units fromString(String unit){
		if(unit.equalsIgnoreCase(Units.METERS_PS.toString()))
			return Units.METERS_PS
		if(unit.equalsIgnoreCase(Units.PERCENTAGE.toString()))
			return Units.PERCENTAGE
		if(unit.equalsIgnoreCase(Units.RPM.toString()))
			return Units.RPM
	}
}