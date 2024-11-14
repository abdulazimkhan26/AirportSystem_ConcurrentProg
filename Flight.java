import java.util.Random;

public class Flight implements Runnable {
    private int id;
    private int queueNo;
    private int passengers;
    private Airport airport;

    public Flight(int id, int queueNo, Airport airport) {
        this.id = id;
        this.queueNo = queueNo;
        this.passengers = new Random().nextInt(11) + 40; // Maximum passengers of 50
        this.airport = airport;
    }

    public int getId() {
        return id;
    }

    public int getQueueNo() {
        return queueNo;
    }

    public int getPassengers() {
        return passengers;
    }
    
    //executes these functions of airport class concurrently for all six thread    
    @Override
    public void run() {  
        try {
            airport.requestLanding(this);
            long startTime = System.currentTimeMillis();
            airport.disembarkPassengers(this);
            airport.refuel(this);
            airport.refillSuppliesAndClean(this);
            airport.embarkPassengers(this);
            airport.undock(this);
            airport.requestTakeoff(this);
            long endTime = System.currentTimeMillis();
            airport.takeoff(this);
            airport.recordWaitingTime(endTime - startTime);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
