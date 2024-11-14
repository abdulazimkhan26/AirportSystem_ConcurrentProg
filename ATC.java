import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ATC implements Runnable {
    private Airport airport;

    public ATC(Airport airport) {  //constructor with airportobject as parameter
        this.airport = airport;
    }

    public ATC() {
    }


    @Override
    public void run() {  //runs the startExecuting() function in the atc Main thread when started
        try {
            startExecuting();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }    
    SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm:ss.SSS");

    /* 
     * generates 6 flights at  random arrival time 
     * passes the plane_id, queue_no, airport object
     * eg planeid; AAZ 123
     * each thread is added to planeThreads arraylist
     * a plane thread is started
     */
    private void startExecuting() throws InterruptedException {   
        List<Thread> planeThreads = new ArrayList<>();
        Thread.currentThread().setName("ATC_MainThread");
        Date time = new Date();
        String startTime = timeFormat.format(time);
        System.out.println(Thread.currentThread().getName()+"  : Coordination of flight's arrival and departure started at "+startTime+"\n");
        for (int i = 1; i <= 6; i++) {
            int planeId = 100 + new Random().nextInt(900); //Generate flight ID between 100 and 999 
            Thread.sleep(new Random().nextInt(2000));    
            Flight plane = new Flight(planeId, i, airport);      
            Thread thread = new Thread(plane);
            thread.setName("AAZ " + planeId);
            planeThreads.add(thread);
            thread.start();
        }

        for (Thread thread : planeThreads) {
            thread.join();
        }
        Date time2 = new Date();
        String endTime = timeFormat.format(time2);
        System.out.println("\n"+Thread.currentThread().getName()+"  : Coordination of flight's arrival and departure ended at "+endTime);
        airport.printStatistics();
    }

    public int getEmergencyId() {
        return new Random().nextInt(7) + 1; 
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("\nTotal Runway available: 1");
        System.out.println("Total Gates available:  3");
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        Airport airport = new Airport();
        ATC atc = new ATC(airport);
        Thread atcThread = new Thread(atc);
        atcThread.start();
        atcThread.join(); 
    }
}
