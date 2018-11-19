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
        HashMap<Integer, Task> taskMap = new HashMap<Integer, Task>(); // HashMap of tasks -> Using a hashmap to retrieve tasks because the task number is given
        HashMap<Integer, Integer> resourceMap = new HashMap<Integer, Integer>();  // HashMap of available resources -> Using a hashmap to retrieve because resource number is also given.
        int line = 1;
        while (input.hasNext()) { //
            String curLine = input.nextLine();
            if (line == 1) {
                String[] strings = curLine.split(" ");
                numOfTasks = Integer.parseInt(strings[0]);
                numOfResources = Integer.parseInt(strings[1]);
                for (int i = 1; i <= numOfTasks; i++) {
                    Task t = new Task(i);
                    taskMap.put(i, t);
                }
                for (int i = 2; i < strings.length; i++) {
                    // Resource resource = new Resource(i-1, Integer.parseInt(strings[i]));
                    resourceMap.put(i-1, Integer.parseInt(strings[i]));
                }
            }
            else {
                if (curLine.matches("(.*)(\\d)(\\s+)(\\d)(\\s+)(\\d)")) {
                    String[] strings = curLine.split("\\s+"); // Split by using whitespace delimiter
                    int curTaskNumber = Integer.parseInt(strings[1]);
                    taskMap.get(curTaskNumber).addActivity(strings[0], Integer.parseInt(strings[1]), // Adds activity to task's queue
                        Integer.parseInt(strings[2]), Integer.parseInt(strings[3]));
                }
                
                
            }
            line++;

        }
        // System.out.println("Activities: ");
        // for (Map.Entry me : taskMap.entrySet() ) {
        //     Task t = (Task)me.getValue();
        //     t.printActivities();
        // }
        // System.out.println("Number of tasks: " + numOfTasks);
        // for (int i = 0; i < resourceUnits.size(); i++) {
        //     System.out.println(resourceUnits.get(i));
        // }

        doBankersAlgorithm(taskMap, resourceMap);
    }

    public static void doBankersAlgorithm (HashMap<Integer, Task> taskMap, HashMap<Integer, Integer> resourceMap) {
        int cycle = 0;
        List<Task> terminatedTasks = new ArrayList<Task>(); // List for terminated tasks   
        List<Task> blockedTasks = new ArrayList<Task>(); // List for the blocked tasks
        List<Map.Entry> addToResourceList = new ArrayList<Map.Entry>(); // List to add released resources AFTER cycle
        while (terminatedTasks.size() != taskMap.size()) { // Do until each Task terminates
            System.out.println("CYCLE " + cycle + "--------------------------------uwu");
            Iterator it = taskMap.entrySet().iterator();
            int counter = 1;
            ArrayList<Integer> visitedTasks = new ArrayList<Integer>(); // To verify which are visited
            int tempNum = -1;
            if (!blockedTasks.isEmpty()) { // First check blocked tasks
                for (Task t : blockedTasks) {
                    printAvailableResources(resourceMap);
                    t.printNeededResources();
                    visitedTasks.add(t.getTaskNumber());
                    t.doNextActivity(resourceMap, terminatedTasks, blockedTasks, addToResourceList, cycle);
                }
            }
            while (it.hasNext()) { // Do next activity for each Task
                printAvailableResources(resourceMap);
                Map.Entry me = (Map.Entry) it.next();
                Task t = (Task)me.getValue();
                t.printNeededResources();
                if (!visitedTasks.contains(t.getTaskNumber())) { // If blocked task was already checked, skip and only do tasks not checked.
                    t.doNextActivity(resourceMap, terminatedTasks, blockedTasks, addToResourceList, cycle);
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
        Collections.sort(terminatedTasks, new Comparator<Task>() {
			@Override
			public int compare(Task t1, Task t2) {
				return t1.getTaskNumber() - t2.getTaskNumber();
			}
		});
	
        for (Task t : terminatedTasks) {
            if (t.getAborted()) {
                t.printOutput(true);
            }
            else {
                t.printOutput(false);
            }
            
        }
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
        
        // for (Map.Entry me : resourceMap.entrySet()) {
        //     System.out.print(me.getValue() + " ");
        // }
        // System.out.println(")");
    }

   
}