public class Activity {
    private String name;
    private int taskNum; // Should be redundant because we already have the taskNum within a Task, and Activity will only be part of Task.
    private int resourceNum;
    private int units;
    public Activity (String name, int taskNum, int resourceNum, int units) {
        this.name = name;
        this.taskNum = taskNum;
        this.resourceNum = resourceNum;
        this.units = units;
    }
    @Override
    public String toString() {
        String s = String.format("%10s %2d %2d %2d", name, taskNum, resourceNum, units);
        return s;
    }
    public String getName() {
        return this.name;
    }
    public int getTaskNum() {
        return this.taskNum;
    }
    public int getResourceNum() {
        return this.resourceNum;
    }
    public int getUnits() {
        return this.units;
    }
    


}