import java.io.IOException;
import java.util.*;
import java.util.Scanner;
import java.io.FileInputStream;

public class Tester {
    public static void main (String args[]) throws IOException {
        String fileName = "";
        Scanner input = null;
        try {
            fileName = args[0];
            input = new Scanner(new FileInputStream(fileName));

        }
        catch (Exception ex) {
            System.out.println("Please check the name and path of the input.");
            System.exit(0);
        }
        ArrayList<Integer> resourceUnits = new ArrayList<Integer>();

        int numOfTasks = 0, numOfResources = 0;

        HashMap<Integer, BankersTask> bankersTaskMap = new HashMap<Integer, BankersTask>(); // HashMap of tasks used for Banker's Algorithm -> Using a hashmap to retrieve tasks because the task number is given
        HashMap<Integer, Integer> resourceMap = new HashMap<Integer, Integer>();  // HashMap of available resources -> Using a hashmap to retrieve because resource number is also given.

        HashMap<Integer, OptimisticTask> optimisticTaskMap = new HashMap<Integer, OptimisticTask>(); // HashMap of tasks used for optimistic resource manager
        HashMap<Integer, Integer> resourceMap1 = new HashMap<Integer, Integer>(); // HashMap of available resources for optimistic resource manager
        
        int line = 1;
        while (input.hasNext()) { //
            String curLine = input.nextLine();
            if (line == 1) {
                String[] strings = curLine.split(" ");
                numOfTasks = Integer.parseInt(strings[0]);
                numOfResources = Integer.parseInt(strings[1]);
                for (int i = 1; i <= numOfTasks; i++) {
                    BankersTask t = new BankersTask(i);
                    bankersTaskMap.put(i, t);
                    OptimisticTask ot = new OptimisticTask(i);
                    optimisticTaskMap.put(i, ot);
                    
                }
                for (int i = 2; i < strings.length; i++) {
                    resourceMap.put(i-1, Integer.parseInt(strings[i]));
                    resourceMap1.put(i-1, Integer.parseInt(strings[i]));
                }
            }
            else {
                if (curLine.matches("(.*)(\\d)(\\s+)(\\d)(\\s+)(\\d)")) {
                    String[] strings = curLine.split("\\s+"); // Split by using whitespace delimiter
                    int curTaskNumber = Integer.parseInt(strings[1]);
                    bankersTaskMap.get(curTaskNumber).addActivity(strings[0], Integer.parseInt(strings[1]), // Adds activity to task's queue
                        Integer.parseInt(strings[2]), Integer.parseInt(strings[3]));
                    optimisticTaskMap.get(curTaskNumber).addActivity(strings[0], Integer.parseInt(strings[1]), // Adds activity to task's queue
                        Integer.parseInt(strings[2]), Integer.parseInt(strings[3]));
                }
                
                
            }
            line++;

        }
        doOptimisticAlgorithm(optimisticTaskMap, resourceMap1);
        doBankersAlgorithm(bankersTaskMap, resourceMap);

        
    }

    public static void doBankersAlgorithm (HashMap<Integer, BankersTask> bankersTaskMap, HashMap<Integer, Integer> resourceMap) {
        int cycle = 0;
        List<BankersTask> terminatedTasks = new ArrayList<BankersTask>(); // List for terminated tasks   
        List<BankersTask> blockedTasks = new ArrayList<BankersTask>(); // List for the blocked tasks
        List<Map.Entry> addToResourceList = new ArrayList<Map.Entry>(); // List to add released resources AFTER cycle
        while (terminatedTasks.size() != bankersTaskMap.size()) { // Do until each Task terminates
            // System.out.println("CYCLE " + cycle + "------------------------------------"); // For debugging
            Iterator it = bankersTaskMap.entrySet().iterator();
            ArrayList<Integer> visitedTasks = new ArrayList<Integer>(); // To verify which are visited
            if (!blockedTasks.isEmpty()) { // First check blocked tasks
                List<BankersTask> removeList = new ArrayList<BankersTask>();
                for (BankersTask t : blockedTasks) {
                    // printAvailableResources(resourceMap); // For debugging
                    visitedTasks.add(t.getTaskNumber());
                    t.doNextActivity(resourceMap, terminatedTasks, blockedTasks, addToResourceList, cycle, removeList);
                
                }
                for (BankersTask t: removeList) {
                    blockedTasks.remove(t);
                }
            }
            while (it.hasNext()) { // Do next activity for each Task
                // printAvailableResources(resourceMap); For debugging
                Map.Entry me = (Map.Entry) it.next();
                BankersTask t = (BankersTask)me.getValue();
                    if (t.getComputeTime() != 0) { // If compute time incomplete
                        t.decreaseComputeTime(); // If default compute time is 0, do nothing. Else, decrement compute time
                    }
                    else {
                        if (!visitedTasks.contains(t.getTaskNumber())) { // If blocked task was already checked, skip and only do tasks not checked.
                        t.doNextActivity(resourceMap, terminatedTasks, blockedTasks, addToResourceList, cycle, new ArrayList<BankersTask>()); 
                        }
                    }
                
               
            }
            while (!addToResourceList.isEmpty()) { // Update the available resources after a cycle, so those resources released during a cycle aren't available during the same cycle.
                for (Map.Entry mEntry : addToResourceList) {
                    Integer CurrentKey = (Integer)mEntry.getKey();
                    Integer CurrentValue = (Integer)mEntry.getValue();
                    resourceMap.put(CurrentKey, CurrentValue+resourceMap.get(CurrentKey));
                }
                addToResourceList.clear();

            }
            cycle++;
           
        }
        printTasksBankers(terminatedTasks);
        
    }

    public static void printAvailableResources(HashMap<Integer, Integer> resourceMap) {
        Iterator it = resourceMap.entrySet().iterator();
        System.out.print("Available Resources: (");
        while (it.hasNext()) {
            Map.Entry mEntry = (Map.Entry) it.next();
            if (it.hasNext()) {
                System.out.print(mEntry.getValue() + ", ");
            }
            else {
                System.out.print(mEntry.getValue());
            }
        }
        System.out.println(")");
        
    }

    public static void printTasksBankers(List<BankersTask> terminatedTasks) {
        Collections.sort(terminatedTasks, new Comparator<BankersTask>() { // sort by taskNum
			@Override
			public int compare(BankersTask t1, BankersTask t2) {
				return t1.getTaskNumber() - t2.getTaskNumber();
			}
        });
        System.out.println("          BANKER'S");
        int totalRunningTime = 0;
        int totalWaitTime = 0;
        for (BankersTask t : terminatedTasks) {
            if (t.getAborted()) {
                t.printOutput(true);
            }
            else {
                t.printOutput(false);
                totalRunningTime += t.getFinishingTime();
                totalWaitTime += t.getWaitingTime();
            }
            
        }
        double totalPercentOfWaitingTime = Math.round((double)(totalWaitTime)/(totalRunningTime)* 100);
        System.out.printf("%-15s%-5d%-5d%d%%\n", "Total", totalRunningTime, totalWaitTime, (int)totalPercentOfWaitingTime);
    }

    public static void doOptimisticAlgorithm (HashMap<Integer, OptimisticTask> optimisticTaskMap, HashMap<Integer, Integer> resourceMap1) {
        int cycle = 0;
        List<OptimisticTask> terminatedTasks = new ArrayList<OptimisticTask>(); // List for terminated tasks   
        List<OptimisticTask> blockedTasks = new ArrayList<OptimisticTask>(); // List for the blocked tasks
        List<Map.Entry> addToResourceList = new ArrayList<Map.Entry>(); // List to add released resources AFTER cycle

        while (terminatedTasks.size() != optimisticTaskMap.size()) {
            Iterator it = optimisticTaskMap.entrySet().iterator();
            ArrayList<Integer> visitedTasks = new ArrayList<Integer>(); // To verify which are visited
            if (!blockedTasks.isEmpty()) { // First check blocked tasks
                List<OptimisticTask> removeList = new ArrayList<OptimisticTask>();
                for (OptimisticTask t : blockedTasks) {
                    visitedTasks.add(t.getTaskNumber());
                    t.doNextActivity(resourceMap1, terminatedTasks, blockedTasks, addToResourceList, cycle, removeList);
                }
                for (OptimisticTask t : removeList) {
                    blockedTasks.remove(t);
                }
            }
            while (it.hasNext()) { // Do next activity for each Task
                Map.Entry me = (Map.Entry) it.next();
                OptimisticTask t = (OptimisticTask)me.getValue(); // Get Task
                if (t.getComputeTime() != 0) { 
                    t.decreaseComputeTime(); // If default compute time is 0, do nothing. Else, decrement compute time
                }
                else {
                    if (!visitedTasks.contains(t.getTaskNumber())) { // If blocked task was already checked, skip and only do tasks not checked.
                        t.doNextActivity(resourceMap1, terminatedTasks, blockedTasks, addToResourceList, cycle, new ArrayList<OptimisticTask>());
                    } 
                }
               
            }
            resolveDeadLock(optimisticTaskMap, resourceMap1, blockedTasks, terminatedTasks, visitedTasks, addToResourceList, cycle);
            // If at the end of a cycle all tasks are blocked, then there is a deadlock. Release the resources of the first task and make them available at the start of the next cycle.
            // while (isDeadLock(optimisticTaskMap, resourceMap1)) {
                // // Get lowest task number
                // Collections.sort(blockedTasks, new Comparator<OptimisticTask>() { // sort by taskNum
                //     @Override
                //     public int compare(OptimisticTask t1, OptimisticTask t2) {
                //         return t1.getTaskNumber() - t2.getTaskNumber();
                //     }
                // });
                
            //     OptimisticTask lowestTask = blockedTasks.remove(0);
            //     // Remove lowest task's currently allocated resources and abort
            //     for (Map.Entry mEntry : lowestTask.getAllocatedResources().entrySet()) {
            //         int resourceNum = (Integer)mEntry.getKey();
            //         int units = (Integer)mEntry.getValue();
            //         // don't need to update allocated resources, just need to update available resources
            //         addToResourceList.add(mEntry);
            //     }
            //     // Map.Entry<Integer, Integer> me = new AbstractMap.SimpleEntry<Integer,Integer>(resourceNum, units);
            //     // addToResourceList.add(me);
            //     lowestTask.setAborted();
            //     terminatedTasks.add(lowestTask);
            //     System.out.println("Task " + lowestTask.getTaskNumber()+ " ABORTED");
            // }
            while (!addToResourceList.isEmpty()) { // Update the available resources after a cycle, so those resources released during a cycle aren't available during the same cycle.
                for (Map.Entry mEntry : addToResourceList) {
                    Integer CurrentKey = (Integer)mEntry.getKey();
                    Integer CurrentValue = (Integer)mEntry.getValue();
                    resourceMap1.put(CurrentKey, CurrentValue+resourceMap1.get(CurrentKey));
                }
                addToResourceList.clear();

            }
            cycle++;
        }
        // for (Map.Entry mEntry : optimisticTaskMap.entrySet()) { // 
        //     OptimisticTask ot = (OptimisticTask)mEntry.getValue();
        //     ot.printActivities();
        // }

        printTasksOptimistic(terminatedTasks);
        

    }

    public static void printTasksOptimistic(List<OptimisticTask> terminatedTasks) {
        Collections.sort(terminatedTasks, new Comparator<OptimisticTask>() { // sort by taskNum
			@Override
			public int compare(OptimisticTask t1, OptimisticTask t2) {
				return t1.getTaskNumber() - t2.getTaskNumber();
			}
        });
        System.out.println("          FIFO");
        int totalRunningTime = 0;
        int totalWaitTime = 0;
        for (OptimisticTask t : terminatedTasks) {
            if (t.getAborted()) {
                
                t.printOutput(true);
            }
            else {
                t.printOutput(false);
                totalRunningTime += t.getFinishingTime();
                totalWaitTime += t.getWaitingTime();
            }
            
        }
        double totalPercentOfWaitingTime = Math.round((double)(totalWaitTime)/(totalRunningTime)* 100);
        System.out.printf("%-15s%-5d%-5d%d%%\n", "Total", totalRunningTime, totalWaitTime, (int)totalPercentOfWaitingTime);
    }

    public static void resolveDeadLock(HashMap<Integer, OptimisticTask> optimisticTaskMap, HashMap<Integer, Integer> resourceMap1,  
    List<OptimisticTask> blockedTasks, List<OptimisticTask> terminatedTasks, ArrayList<Integer> visitedTasks, List<Map.Entry> addToResourceList, int cycle) {
        while (blockedTasks.size() == optimisticTaskMap.size()-terminatedTasks.size() && !blockedTasks.isEmpty()) { // if there are just as many non-terminated tasks as there are blocked, we have a deadlock
            // Get lowest task number
            Collections.sort(blockedTasks, new Comparator<OptimisticTask>() { // sort by taskNum
                @Override
                public int compare(OptimisticTask t1, OptimisticTask t2) {
                    return t1.getTaskNumber() - t2.getTaskNumber();
                }
            });
            OptimisticTask t = blockedTasks.remove(0); // Remove lowest and release its resources.
            t.releaseResources(resourceMap1);
            t.setEndCycle(cycle);
            t.setAborted();
            terminatedTasks.add(t);
            
            List<OptimisticTask> removeList = new ArrayList<OptimisticTask>(); // List to remove the tasks in blocked tasks
            for (OptimisticTask t1 : blockedTasks) {
                visitedTasks.add(t1.getTaskNumber());
                t1.checkAvailability(resourceMap1, removeList, blockedTasks);
            }
            for (OptimisticTask t1 : removeList) {
                blockedTasks.remove(t1);
            }

        }        
        // if (!blockedTasks.isEmpty()) { // Check blocked tasks
            
        // }
    }



   
}