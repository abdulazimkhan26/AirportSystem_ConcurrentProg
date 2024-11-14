import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Airport {
    private Semaphore gateSemaphore;
    private Semaphore runwaySemaphore;
    private Lock refuelLock;
    private List<Gate> gates;
    private List<Long> waitingTimes;
    private int planesServed;
    private int passengersBoarded;
    ATC atc = new ATC();
    int emergencyplane = atc.getEmergencyId();
    public static final String RED = "\033[0;31m";   
    public static final String WHITE = "\033[0;37m"; 
    public static final String GREEN = "\033[0;32m"; 
    public static final String YELLOW = "\033[0;33m";
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  
    public static final String RESET = "\033[0m";
    
   

    public Airport() {
        this.gateSemaphore = new Semaphore(3, true);
        this.runwaySemaphore = new Semaphore(1, true);
        this.refuelLock = new ReentrantLock();
        this.gates = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            Gate gate = new Gate(gateSemaphore, "Gate-" + i);
            gates.add(gate);
            Thread gateThread = new Thread(gate, "Gate-" + i);
            gateThread.start();
        }
        this.waitingTimes = new ArrayList<>();
        this.planesServed = 0;
        this.passengersBoarded = 0;
    }

    public synchronized void requestLanding(Flight plane) throws InterruptedException {
        if (plane.getQueueNo() == emergencyplane) {
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+RED+" Pilot "+plane.getQueueNo()+"  : Requesting emergency landing!" + WHITE);
            while (runwaySemaphore.availablePermits() == 0) {
                wait();
            }
            runwaySemaphore.acquire();
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Permission granted. Landing now.                                 Runway Used by: " + Thread.currentThread().getName());
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Landing.");
            dock(plane);
            Thread.sleep(3000);
        } else {
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+YELLOW+" Pilot "+plane.getQueueNo()+"  : Requesting permission to land." + WHITE);
            while (runwaySemaphore.availablePermits() == 0 || gateSemaphore.availablePermits() < 2) {
                wait();
            }
            runwaySemaphore.acquire();
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Permission granted. Landing now.                                 Runway Used by: " + Thread.currentThread().getName());
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Landing.");
            dock(plane);
            Thread.sleep(3000);
        }
    }

    public void dock(Flight plane) throws InterruptedException {
        for (Gate gate : gates) {
            if (!gate.isOccupied()) {
                gate.queueDocking(plane);
                break;
            }
        }
        runwaySemaphore.release();
        synchronized (this) {
            notifyAll();
        }
    }

    public void disembarkPassengers(Flight plane) throws InterruptedException {        
        System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Disembarking passengers.");
        Thread.sleep(new Random().nextInt(4000)); 
    }

    public void refillSuppliesAndClean(Flight plane) throws InterruptedException {
        System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Refilling supplies and cleaning.");
        Thread.sleep(new Random().nextInt(4000));  
    }

    public void refuel(Flight plane) throws InterruptedException {
        refuelLock.lock();
        try {
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Refueling.");
            Thread.sleep(new Random().nextInt(200)); 
        } finally {
            refuelLock.unlock();
        }
    }

    public void embarkPassengers(Flight plane) throws InterruptedException {
        System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Embarking passengers.");
        undock(plane);
        Thread.sleep(300); 
        passengersBoarded += plane.getPassengers();
    }

    public void undock(Flight plane) throws InterruptedException {
        for (Gate gate : gates) {
            if (gate.isOccupiedBy(plane)) {
                gate.queueUndocking(plane);
                break;
            }
        }
        synchronized (this) {
            notifyAll();
        }
    }

    public synchronized void requestTakeoff(Flight plane) throws InterruptedException {        
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+YELLOW+" Pilot "+plane.getQueueNo()+"  : Requesting permission to takeoff." + WHITE);
            while (runwaySemaphore.availablePermits() == 0) {
                wait();
            }
            runwaySemaphore.acquire();
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Permission granted. Preparing to takeoff now.");
            Thread.sleep(1000);
    }
    
    public void takeoff(Flight plane) throws InterruptedException {
        try {
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+" Pilot "+plane.getQueueNo()+"  : Taking off.                                                      Runway Used by " + Thread.currentThread().getName());
            Thread.sleep(3000);
            System.out.println(BLUE_BOLD_BRIGHT+ Thread.currentThread().getName() + ":"+RESET+GREEN+" Pilot "+plane.getQueueNo()+"  : Take off successful."+WHITE+"                                             Runway released by " + Thread.currentThread().getName());
            planesServed++;
        } finally {
            runwaySemaphore.release(); // Release the runway semaphore
            synchronized (this) {
                notifyAll(); // Notify waiting threads that runway is released
            }
        }
    }

    public void recordWaitingTime(long time) {
         waitingTimes.add(time);    
    }

    public void printStatistics() throws InterruptedException {
        double maxWait = 0;
        double minWait;
        double sumWait = 0;
        double avgWait;

        for (int i = 0; i < 6; i++) {
            if (waitingTimes.get(i) > maxWait) {
                maxWait = waitingTimes.get(i);
            }
        }
        minWait = maxWait;
        for (int i = 0; i < 6; i++) {
            if (waitingTimes.get(i) < minWait) {
                minWait = waitingTimes.get(i);
            }
        }
        for (int i = 0; i < 6; i++) {
            sumWait += waitingTimes.get(i);
        }
        avgWait = (double) sumWait / planesServed;

        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
        System.out.println("Checking for Runway status...");
        Thread.sleep(1500);
        System.out.println("Runway is clear and available for landings.   Runway available: " + runwaySemaphore.availablePermits());
        System.out.println("Checking for Gate(s) status...");
        Thread.sleep(1500);
        System.out.println("All Gates clear.                              Gates available: " + gateSemaphore.availablePermits());
        System.out.println("\nMaximum waiting time : " + maxWait + " ms / " + maxWait / 1000 + " sec");
        System.out.println("Minimum waiting time : " + minWait + " ms / " + minWait / 1000 + " sec");
        System.out.println("Average waiting time : " + avgWait + " ms / " + avgWait / 1000 + " sec");
        System.out.println("Total planes served: " + planesServed);
        System.out.println("Total passengers boarded: " + passengersBoarded);
        System.out.println("---------------------------------------------------------------------------------------------------------------------------");
    }
}
