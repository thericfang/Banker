import java.util.*;
import java.util.Iterator;

import javax.annotation.Resources;
public class BankersTask {

    private boolean aborted; 
    private int computeTime;
    private int taskNumber;
    private int endCycle;
    private int waitingTime = 0;
    private Queue<Activity> activities; // The queue of activities to run for a task
    private HashMap<Integer, Integer> maxResources; // Max needed after initiate
    private HashMap<Integer, Integer> allocatedResources; // How many are currently allocated of each
    private HashMap<Integer, Integer> neededResources; // How many of each resource is needed

    // In this algorithm, there are three resource matrices to check determine safe states

    public BankersTask (int taskNumber) {
        this.taskNumber = taskNumber;
        activities = new LinkedList<Activity>();
        maxResources = new HashMap<Integer, Integer>();
        allocatedResources = new HashMap<Integer, Integer>();
        neededResources = new HashMap<Integer, Integer>();
    }

    // Add activity to the activities queue
    public void addActivity (String activity, int taskNum, int resourceNum, int units) {
        Activity a = new Activity(activity, taskNum, resourceNum, units);
        activities.add(a);
    }

    // Method to do the next activity in the activities queue
    public void doNextActivity (HashMap<Integer, Integer> resourceMap, List<BankersTask> terminatedTasks, 
        List<BankersTask> blockedTasks, List<Map.Entry> addToResourceList, int cycle, List<BankersTask> removeList) {


        // Do activities
        if (!activities.isEmpty() && !aborted && computeTime == 0) {

            Activity curActivity = activities.peek();
            String name = curActivity.getName();
            int taskNum = curActivity.getTaskNum();
            int resourceNum = curActivity.getResourceNum();
            int units = curActivity.getUnits();

            // If initiate, update max claim matrix
            if (name.equals("initiate")) { 

                initializeAllocatedResources(resourceMap);

                if (units > resourceMap.get(resourceNum)) { // Error Checking: If claim is larger than resources in system, abort task.
                    aborted = true;
                    terminatedTasks.add(this);
                    endCycle = cycle;
                }
                else { 
                    maxResources.put(resourceNum, units);
                    neededResources.put(resourceNum, units);
                    updateNeededResources();
                    activities.poll();
                }
                
            }
            else if (name.equals("request")) { 
                
                // First check if banker can grant / safe state. need <= available
                if (checkSafeState(neededResources, resourceMap)) {
                    if (blockedTasks.contains(this)) {
                        removeList.add(this);
                    }
                    // Check to see if request is greater than claim.
                    if (units > neededResources.get(resourceNum)) {
                        aborted = true;
                        terminatedTasks.add(this);
                        Map.Entry<Integer, Integer> me = new AbstractMap.SimpleEntry<Integer,Integer>(resourceNum, allocatedResources.get(resourceNum));
                        addToResourceList.add(me);


                    }
                    else {
                        // update current allocated matrix
                        int current = allocatedResources.get(resourceNum);
                        allocatedResources.put(resourceNum, current + units); // Add units to current, update allocated resources
                        resourceMap.put(resourceNum, resourceMap.get(resourceNum)-units); // Update available resources
                        activities.poll();
                    }
                    

                }
                else { // Else block the task
                    if (!blockedTasks.contains(this)) {
                        blockedTasks.add(this);
                    }
                    waitingTime++;
                    
                }
                updateNeededResources();
                
            }
            
            // Release the number of units
            else if (name.equals("release")) {

                int current = allocatedResources.get(resourceNum);
                allocatedResources.put(resourceNum, current - units);

                // Add to Resource List the amount that should be added to resource delayed after cycle
                Map.Entry<Integer, Integer> me = new AbstractMap.SimpleEntry<Integer,Integer>(resourceNum, units);
                addToResourceList.add(me);
                updateNeededResources();
                activities.poll();
            }

            // Terminate the task
            else if (name.equals("terminate")) {

                terminatedTasks.add(this);
                updateNeededResources();
                activities.poll();
                endCycle = cycle;
                
            }

            // Add compute time and wait
            else if (name.equals("compute")) {
                computeTime = resourceNum; // Compute time is given as the second number
                decreaseComputeTime();  
                activities.poll();                  
            }
        }
    }

    /* For debugging purposes
    // public void printMaxResources() {
    //     for (Map.Entry me : maxResources.entrySet()) {
    //         System.out.println("Max claim: " + me.getValue() + " units of Resource Number " + me.getKey());
    //     }
    // }

    // public void printActivities () {
    //     for (Activity a : activities) {
    //         System.out.println(a);
    //     }
    // }
    
    // public void printNeededResources() {
    //     for (Map.Entry me : neededResources.entrySet()) {
    //         System.out.println("Task " + getTaskNumber() + " Needed resources: " + me.getValue() + " units of Resource Number " + me.getKey());
    //     }
    // }

    // public void printAllocatedResources() { 
    //     for (Map.Entry me : allocatedResources.entrySet()) {
    //         System.out.println("Task " + getTaskNumber() + " Allocated resources: " + me.getValue() + " units of Resource Number " + me.getKey());
    //     }
    // }
    
    For debugging purposes */

    public void decreaseComputeTime() { // Decrements the compute time
        computeTime--;
    }
    
    public int getTaskNumber() {
        return this.taskNumber;
    }

    public int getFinishingTime() {
        return this.endCycle;
    }
 
    // Iterate through hashMap and update the needed resources. Must call every time allocated Resources is changed.
    public void updateNeededResources () { 
        Iterator it1 = maxResources.entrySet().iterator();
        Iterator it2 = allocatedResources.entrySet().iterator();
        int counter = 1;
        while (it1.hasNext()) {
            Map.Entry maxPair = (Map.Entry) it1.next();
            Map.Entry allocatedPair = (Map.Entry) it2.next();
            int difference = (Integer) maxPair.getValue() - (Integer) allocatedPair.getValue();
            neededResources.put(counter, difference);
            counter++;
        }
    }

    // Prints the output for the task
    public void printOutput(boolean aborted) {
        double timeWaiting = Math.round((double)(waitingTime)/ endCycle * 100);
        if (aborted) {
            System.out.printf("Task %-4daborted at cycle %d\n", taskNumber, endCycle);        
        }
        else {
            System.out.printf("Task %-10d%-5d%-5d%d%%\n", taskNumber, endCycle, waitingTime, (int)timeWaiting);
        }

    }

    public void initializeAllocatedResources(HashMap<Integer, Integer> resourceMap) {
        for (Map.Entry me : resourceMap.entrySet()) {
            allocatedResources.put((Integer)me.getKey(), 0);
        }
    }

    public boolean getAborted() {
        return this.aborted;
    }

    // Checks if the task is safe by comparing the needed resources matrix to the available resources
    public boolean checkSafeState (HashMap<Integer, Integer> neededResources, HashMap<Integer, Integer> resourceMap) {
        Iterator it1 = neededResources.entrySet().iterator();
        Iterator it2 = resourceMap.entrySet().iterator();
        while (it1.hasNext()) {
            Map.Entry mEntry1 = (Map.Entry)it1.next();
            Map.Entry mEntry2 = (Map.Entry)it2.next();
            if ((Integer) mEntry1.getValue() > (Integer)mEntry2.getValue()) {
                return false;
            }
        }
        return true;
    }

    public int getComputeTime() {
        return this.computeTime;
    }

    public int getWaitingTime() {
        return this.waitingTime;
    }

}