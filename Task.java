import java.util.*;
import java.util.Iterator;

import javax.annotation.Resources;
public class Task {
    private boolean aborted;
    private int taskNumber;
    private int curCycle;
    private int endCycle;
    private int begCycle = 0;
    private int waitingTime = 0;
    private Queue<Activity> activities;
    private HashMap<Integer, Integer> maxResources; // Max needed after initiate
    private HashMap<Integer, Integer> allocatedResources; // How many are currently allocated of each
    private HashMap<Integer, Integer> neededResources; // How many of each resource is needed
    public Task (int taskNumber) {
        this.taskNumber = taskNumber;
        activities = new LinkedList<Activity>();
        maxResources = new HashMap<Integer, Integer>();
        allocatedResources = new HashMap<Integer, Integer>();
        neededResources = new HashMap<Integer, Integer>();
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
    public void doNextActivity (HashMap<Integer, Integer> resourceMap, List<Task> terminatedTasks, 
        List<Task> blockedTasks, List<Map.Entry> addToResourceList, int cycle) {
            if (!activities.isEmpty() && !aborted) {
                Activity curActivity = activities.peek();
                String name = curActivity.getName();
                int taskNum = curActivity.getTaskNum();
                int resourceNum = curActivity.getResourceNum();
                int units = curActivity.getUnits();
                if (name.equals("initiate")) { // Should be fine
                    initializeAllocatedResources(resourceMap);
                    if (units > resourceMap.get(resourceNum)) { // Error Checking: If claim is larger than resources in system, abort task.
                        aborted = true;
                        terminatedTasks.add(this);
                        System.out.println("Task " + taskNum + " ABORTED");
                    }
                    else {
                        maxResources.put(resourceNum, units);
                        neededResources.put(resourceNum, units);
                        updateNeededResources();
                        activities.poll();
                        System.out.println("Finished Activity: " + curActivity);
                    }
                    
                }
                else if (name.equals("request")) { 
                    
                    // First check if banker can grant / safe state. need <= available
                    if (neededResources.get(resourceNum) <= resourceMap.get(resourceNum)) {

                        // update current allocated matrix
                        int current = allocatedResources.get(resourceNum);
                        allocatedResources.put(resourceNum, current + units); // Add units to current, update allocated resources
                        resourceMap.put(resourceNum, resourceMap.get(resourceNum)-units); // Update available resources
                        activities.poll();
                        System.out.println("Finished Activity: " + curActivity);

                    }
                    else { // Else block the task
                        if (!blockedTasks.contains(this)) {
                            blockedTasks.add(this);
                        }
                        waitingTime++;
                        System.out.println("Task " + taskNumber + " Blocked request of " + units + " units");
                        
                    }
                    updateNeededResources();
                    
                    printAllocatedResources();
                }
                else if (name.equals("release")) {
                    int current = allocatedResources.get(resourceNum);
                    System.out.print("Before relase: ");
                    printAllocatedResources();
                    allocatedResources.put(resourceNum, current - units);
                    Map.Entry<Integer, Integer> me = new AbstractMap.SimpleEntry<Integer,Integer>(resourceNum, units);
                    addToResourceList.add(me);
                    // resourceMap.put(resourceNum, resourceMap.get(resourceNum) + units);
                    System.out.println("Finished Activity: " + curActivity);
                    printAllocatedResources();
                    updateNeededResources();
                    activities.poll();
                }
                else if (name.equals("terminate")) {
                    terminatedTasks.add(this);
                    System.out.println("Finished Activity: " + curActivity);
                    printAllocatedResources();
                    updateNeededResources();
                    activities.poll();
                    endCycle = cycle;
                    
                }
            }
            curCycle = cycle;
        }
    public void printMaxResources() {
        for (Map.Entry me : maxResources.entrySet()) {
            System.out.println("Max claim: " + me.getValue() + " units of Resource Number " + me.getKey());
        }
    }
    
    public void printNeededResources() {
        for (Map.Entry me : neededResources.entrySet()) {
            System.out.println("Task " + getTaskNumber() + " Needed resources: " + me.getValue() + " units of Resource Number " + me.getKey());
        }
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

    
    
    public void updateNeededResources () { // Iterate through hashMap and update the needed resources. Must call every time allocated Resources is changed.
        Iterator it1 = maxResources.entrySet().iterator();
        Iterator it2 = allocatedResources.entrySet().iterator();
        int counter = 1;
        while (it1.hasNext()) {
            Map.Entry maxPair = (Map.Entry) it1.next();
            Map.Entry allocatedPair = (Map.Entry) it2.next();
            int difference = (Integer) maxPair.getValue() - (Integer) allocatedPair.getValue();
            neededResources.put(counter, difference);
        }
    }

    public void printOutput(boolean aborted) {
        if (aborted) {
            System.out.printf("Task %-10daborted\n", taskNumber);
        }
        else {
            System.out.printf("Task %-10d%-5d%-5d\n", taskNumber, endCycle, waitingTime);
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








}