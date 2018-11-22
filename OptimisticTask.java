import java.util.*;
import java.util.Iterator;

public class OptimisticTask {
    private boolean blocked; // Whether this 
    private boolean aborted;
    private int computeTime = 0;
    private int taskNumber;
    private int endCycle;
    private int waitingTime = 0;
    private Queue<Activity> activities;
    private HashMap<Integer, Integer> allocatedResources; // How many are currently allocated of each

    public OptimisticTask (int taskNumber) {
        this.taskNumber = taskNumber;
        activities = new LinkedList<Activity>();
        allocatedResources = new HashMap<Integer, Integer>();
    }
    public void addActivity (String activity, int taskNum, int resourceNum, int units) {
        Activity a = new Activity(activity, taskNum, resourceNum, units);
        activities.add(a);
    }
    public void printActivities () {
        for (Activity a : activities) {
            System.out.println(a);
        }
    }

    public void doNextActivity (HashMap<Integer, Integer> resourceMap1, List<OptimisticTask> terminatedTasks, 
        List<OptimisticTask> blockedTasks, List<Map.Entry> addToResourceList, int cycle, List<OptimisticTask> removeList) {
            if (!activities.isEmpty() && !aborted && computeTime == 0) {
                Activity curActivity = activities.peek();
                String name = curActivity.getName();
                int taskNum = curActivity.getTaskNum();
                int resourceNum = curActivity.getResourceNum();
                int units = curActivity.getUnits();
                if (name.equals("initiate")) { // No initiate activity for Optimistic
                    initializeAllocatedResources(resourceMap1);
                    activities.poll();
                    
                }
                else if (name.equals("request")) { 
                    // If there are enough units available, grant the request.
                    if (units <= resourceMap1.get(resourceNum)) {
                        if (blockedTasks.contains(this)) {
                            removeList.add(this);
                        }
                        // Update current allocated matrix
                        int current = allocatedResources.get(resourceNum);
                        allocatedResources.put(resourceNum, current + units); // Add units to current, update allocated resources
                        resourceMap1.put(resourceNum, resourceMap1.get(resourceNum)-units); // Update available resources
                        activities.poll();
                        
                    }
                    else { // Else block the task
                        if (!blockedTasks.contains(this)) {
                            blockedTasks.add(this);
                        }
                        blocked = true;
                        waitingTime++;
                        
                    }
                }
                else if (name.equals("release")) {
                    int current = allocatedResources.get(resourceNum);
                    allocatedResources.put(resourceNum, current - units);
                    // Add to Resource List the amount that should be added to resource delayed after cycle
                    Map.Entry<Integer, Integer> me = new AbstractMap.SimpleEntry<Integer,Integer>(resourceNum, units);
                    addToResourceList.add(me);
                    activities.poll();
                }
                else if (name.equals("terminate")) {
                    terminatedTasks.add(this);
                    activities.poll();
                    endCycle = cycle;   
                }
                else if (name.equals("compute")) {
                    computeTime = resourceNum; // Compute time is given as the second number
                    decreaseComputeTime();  
                    activities.poll();                  
                }
            }
        }

    public void decreaseComputeTime() { // Decrements the compute time
        computeTime--;
    }

    public void printAllocatedResources() { 
        for (Map.Entry me : allocatedResources.entrySet()) {
            System.out.println("Task " + getTaskNumber() + " Allocated resources: " + me.getValue() + " units of Resource Number " + me.getKey());
        }
    }

    public int getTaskNumber() {
        return this.taskNumber;
    }

    public int getFinishingTime() {
        return this.endCycle;
    }

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

    public int getComputeTime() {
        return this.computeTime;
    }

    public int getWaitingTime() {
        return this.waitingTime;
    }

    public void setAborted() {
        this.aborted = true;
    }

    public HashMap<Integer, Integer> getAllocatedResources() {
        return allocatedResources;
    }

    public boolean getBlocked() {
        return blocked;
    }

    public boolean checkDeadLock(HashMap<Integer, Integer> resourceMap1) {
        Activity curActivity = activities.peek();
        if (blocked) {
            String name = curActivity.getName();
            int taskNum = curActivity.getTaskNum();
            int resourceNum = curActivity.getResourceNum();
            int units = curActivity.getUnits();
            if (units > resourceMap1.get(resourceNum)) {
                return true;
            }
        }
        return false;
        
        
    }

    public void releaseResources(HashMap<Integer, Integer> resourceMap1) {
        for (Map.Entry mEntry : resourceMap1.entrySet()) {
            int resourceNum = (Integer)mEntry.getKey();
            int current = allocatedResources.get(resourceNum);
            resourceMap1.put(resourceNum, current + resourceMap1.get(resourceNum));
        }
    }

    public void checkAvailability(HashMap<Integer, Integer> resourceMap1, List<OptimisticTask> removeList, List<OptimisticTask> blockedTasks) {
        Activity a = activities.peek();
        int resourceNum = a.getResourceNum();
        int units = a.getUnits();
        if (units <= resourceMap1.get(resourceNum)) {
            if (blockedTasks.contains(this)) {
                removeList.add(this);
            }
        }
        
    }

    public void setEndCycle(int cycle) {
        endCycle = cycle;
    }


    
    
        
}