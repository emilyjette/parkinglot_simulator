import java.io.File;
import java.util.Scanner;

/**
 * @author Mehrdad Sabetzadeh, University of Ottawa, Professor
 * @author Emily Jette, University of Ottawa, Student 
 */

public class ParkingLot {
	/**
	 * The delimiter that separates values
	 */
	private static final String SEPARATOR = ",";

	/**
	 * The delimiter that separates the parking lot design section from the parked
	 * car data section
	 */
	private static final String SECTIONER = "###";

	/**
	 * Instance variable for storing the number of rows in a parking lot
	 */
	private int numRows;

	/**
	 * Instance variable for storing the number of spaces per row in a parking lot
	 */
	private int numSpotsPerRow;

	/**
	 * Instance variable (two-dimensional array) for storing the lot design
	 */
	private CarType[][] lotDesign;

	/**
	 * Instance variable (two-dimensional array) for storing occupancy information
	 * for the spots in the lot
	 */
	private Car[][] occupancy;

	/**
	 * Constructs a parking lot by loading a file
	 * 
	 * @param strFilename is the name of the file
	 */
	public ParkingLot(String strFilename) throws Exception {

		if (strFilename == null) {
			System.out.println("File name cannot be null.");
			return;
		}

		// determine numRows and numSpotsPerRow
		calculateLotDimensions(strFilename);
		// null == N 
		lotDesign = new CarType [numRows][numSpotsPerRow];// will be filled with what kind of car
		occupancy = new Car [numRows][numSpotsPerRow];//will be filled with where cars are 

	
		// populate lotDesign and occupancy
		populateFromFile(strFilename);
	}

	/**
	 * Parks a car (c) at a give location (i, j) within the parking lot.
	 * 
	 * @param i is the parking row index
	 * @param j is the index of the spot within row i
	 * @param c is the car to be parked
	 */
	public void park(int i, int j, Car c) {
		occupancy[i][j] = c;
	}

	/**
	 * Removes the car parked at a given location (i, j) in the parking lot
	 * 
	 * @param i is the parking row index
	 * @param j is the index of the spot within row i
	 * @return the car removed; the method returns null when either i or j are out
	 *         of range, or when there is no car parked at (i, j)
	 */
	public Car remove(int i, int j) {
		Car tmp; 
		if(i >= numRows || j >= numSpotsPerRow || occupancy[i][j] == null){
			return null;
		}
		tmp = occupancy[i][j];
		occupancy[i][j] = null;
		tmp = null;
		return tmp; 

	}

	/**
	 * Checks whether a car (which has a certain type) is allowed to park at
	 * location (i, j)
	 * 
	 * @param i is the parking row index
	 * @param j is the index of the spot within row i
	 * @return true if car c can park at (i, j) and false otherwise
	 */
	public boolean canParkAt(int i, int j, Car c) {
		CarType type = c.getType();
		if(i >= numRows || j >= numSpotsPerRow){
			return false;
		}
		if( occupancy[i][j] != null){
			return false;
		}
		CarType space = lotDesign[i][j];
		if (space != CarType.NA){
			if(space == CarType.ELECTRIC && type == CarType.ELECTRIC){
				return true;
			}
			if(space == CarType.SMALL && (type == CarType.ELECTRIC || type == CarType.SMALL)){
				return true;
			}
			if(space == CarType.REGULAR && (type == CarType.ELECTRIC || type == CarType.SMALL || type == CarType.REGULAR)){
				return true;
			}
			if(space == CarType.LARGE && (type == CarType.ELECTRIC || type == CarType.SMALL || type == CarType.REGULAR || type == CarType.LARGE)){
				return true; 
			}	
		}
		/*
		E can park in E, S, R, L
		S can park in S , R, L
		R can park in R,L
		L can park in L
		*/
		return false; 
	}

	/**
	 * @return the total capacity of the parking lot excluding spots that cannot be
	 *         used for parking (i.e., excluding spots that point to CarType.NA)
	 */
	public int getTotalCapacity() {
		int capacity = numRows * numSpotsPerRow;
		String car;
		for(int i = 0; i < numRows; i ++){
			for(int j = 0; j < numSpotsPerRow; j++){
				car = Util.getLabelByCarType(lotDesign[i][j]);
				if(car.equals("N")){
					capacity --;
				}
			}
		}
		return capacity; 

	}

	/**
	 * @return the total occupancy of the parking lot (i.e., the total number of
	 *         cars parked in the lot)
	 */
	public int getTotalOccupancy() {
		int occupied = 0;
		for(int i = 0; i < numRows; i++){
			for(int j = 0; j < numSpotsPerRow; j++){
				if(occupancy[i][j] != null){
					occupied ++;
				}

			}
		}
		return occupied; 		
	}

	private void calculateLotDimensions(String strFilename) throws Exception {

		Scanner scanner = new Scanner(new File(strFilename));
		numSpotsPerRow = 0; // colums
		numRows = 0;
		while (scanner.hasNext()) {
			// this is going over the lines
			String str = scanner.nextLine();
			String line = str.replaceAll("\\s","").replaceAll(",","").trim();
			if(line.equals("")) {
				continue;
			}
			if(line.equals(SECTIONER)) {
				// SECTIONER = "###"
				break;
			}
            numRows ++; 
            numSpotsPerRow =line.length();
		}
		
		scanner.close();
	}

	private void populateFromFile(String strFilename) throws Exception {

		Scanner scanner = new Scanner(new File(strFilename));

		String [] carSize; 
		CarType size;
		String [] location;
		int row = 0;

		// while loop for reading the lot design
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			//before SECTIONER
			if(str.equals("")) {
				continue;
			}
			if(str.equals(SECTIONER)) {
				// SECTIONER = "###"
				break;
			}
			carSize = str.split(",");
			for(int i = 0; i < numSpotsPerRow; i++){
				carSize[i] = carSize[i].trim();// gets "S", "N", ect
				size = Util.getCarTypeByLabel(carSize[i]);
				lotDesign[row][i] = size; 
			}
			carSize = null;
			row ++; 
			
		}
		Car newCar;
		int whichRow = 0; 
		int whichColumn = 0;
		CarType type;
		String plate;
		String [] infoOnCar;

		// while loop for reading occupancy data
		while (scanner.hasNext()) {
			String str = scanner.nextLine();
			//After SECTIONER
			if(str.equals("")) {
				continue;
			}
			infoOnCar = str.split(",");


			whichRow = Integer.parseInt(infoOnCar[0].trim());
			whichColumn = Integer.parseInt(infoOnCar[1].trim());
			type = Util.getCarTypeByLabel(infoOnCar[2].trim()); 
			plate = infoOnCar[3].trim();
			newCar = new Car(type, plate);
			
			if(canParkAt(whichRow, whichColumn, newCar)){
				park(whichRow, whichColumn, newCar);
			}
			else{
				System.out.println("Car " + newCar + " cannot be parked at (" + whichRow + ", " + whichColumn + ")");
			}
		}
		scanner.close();

	}

	/**
	 * Produce string representation of the parking lot
	 * 
	 * @return String containing the parking lot information
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("==== Lot Design ====").append(System.lineSeparator());

		for (int i = 0; i < lotDesign.length; i++) {
			for (int j = 0; j < lotDesign[0].length; j++) {
				buffer.append((lotDesign[i][j] != null) ? Util.getLabelByCarType(lotDesign[i][j])
						: Util.getLabelByCarType(CarType.NA));
				if (j < numSpotsPerRow - 1) {
					buffer.append(", ");
				}
			}
			buffer.append(System.lineSeparator());
		}

		buffer.append(System.lineSeparator()).append("==== Parking Occupancy ====").append(System.lineSeparator());

		for (int i = 0; i < occupancy.length; i++) {
			for (int j = 0; j < occupancy[0].length; j++) {
				buffer.append(
						"(" + i + ", " + j + "): " + ((occupancy[i][j] != null) ? occupancy[i][j] : "Unoccupied"));
				buffer.append(System.lineSeparator());
			}

		}
		return buffer.toString();
	}

	/**
	 * <b>main</b> of the application. The method first reads from the standard
	 * input the name of the file to process. Next, it creates an instance of
	 * ParkingLot. Finally, it prints to the standard output information about the
	 * instance of the ParkingLot just created.
	 * 
	 * @param args command lines parameters (not used in the body of the method)
	 * @throws Exception
	 */

	public static void main(String args[]) throws Exception {

		System.out.print("Please enter the name of the file to process: ");

		Scanner scanner = new Scanner(System.in);

		String strFilename = scanner.nextLine();

		ParkingLot lot = new ParkingLot(strFilename);

		System.out.println("Total number of parkable spots (capacity): " + lot.getTotalCapacity());

		System.out.println("Number of cars currently parked in the lot: " + lot.getTotalOccupancy());

		System.out.print(lot);

	}
}
