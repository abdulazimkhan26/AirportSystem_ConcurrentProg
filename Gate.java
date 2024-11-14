
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Gate implements Runnable {
    private boolean occupied;
    private Flight occupiedFlight;
    private Lock lock;
    private Semaphore gateSemaphore;
    private String gateName;
    private BlockingQueue<Flight> dockingQueue;
    private BlockingQueue<Flight> undockingQueue;    
    public static final String RED = "\033[0;31m";   
    public static final String WHITE = "\033[0;37m"; 
    public static final String GREEN = "\033[0;32m"; 
    public static final String YELLOW = "\033[0;33m";
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; 
    public static final String RESET = "\033[0m";

    public Gate(Semaphore gateSemaphore, String gateName) {
        this.occupied = false;
        this.lock = new ReentrantLock();
        this.gateSemaphore = gateSemaphore;
        this.gateName = gateName;
        this.dockingQueue = new LinkedBlockingQueue<>();
        this.undockingQueue = new LinkedBlockingQueue<>();
    }

    public boolean isOccupied() {
        return occupied;
    }

    public Lock getLock() {
        return lock;
    }

    public boolean isOccupiedBy(Flight plane) {
        return occupied && occupiedFlight.getId() == plane.getId();
    }

    public void queueDocking(Flight plane) {
        dockingQueue.offer(plane);
    }

    public void queueUndocking(Flight plane) {
        undockingQueue.offer(plane);
    }

    @Override
    public void run() {
        while (true) {
            try {
                // Process docking requests
                Flight planeToDock = dockingQueue.take();
                dockFlight(planeToDock);
                
                Flight planeToUndock = undockingQueue.take();
                undockFlight(planeToUndock);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void dockFlight(Flight plane) throws InterruptedException {
        gateSemaphore.acquire();
        occupied = true;
        occupiedFlight = plane;
        System.out.println(WHITE_BOLD_BRIGHT+Thread.currentThread().getName() +" :"+RESET+ " Plane-"+ plane.getQueueNo() +"  : Docked at " + gateName + "!                                                Runway released by AZZ:" + plane.getId() + "                  GATES REMAINING: " + gateSemaphore.availablePermits());
        Thread.sleep(new Random().nextInt(1500)); // Simulate docking time
    }

    private void undockFlight(Flight plane) {
        occupied = false;
        occupiedFlight = null;
        gateSemaphore.release();
        System.out.println(WHITE_BOLD_BRIGHT+Thread.currentThread().getName() +" :"+RESET+ " Plane-"+ plane.getQueueNo() +"  : Undocked from " + gateName + ".                                                                                        GATES REMAINING: " + gateSemaphore.availablePermits());
    }
}
