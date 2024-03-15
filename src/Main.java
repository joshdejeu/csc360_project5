/**
 * @author Josh Dejeu
 * @course CSC360/660 Spring 2024
 * @project Project #5 - Implementation of Banker's Algorithm
 * @due Date Mar 18, 2024 11:59 PM
 * @instructor: Siming Liu
 * <p>
 * -----Project Environment Details-----
 * <ul>
 * <li>Operating System: Windows 11 10.0</li>
 * <li>Java Development Kit (JDK) version: 21.0.2</li>
 * <li>Java Runtime Environment (JRE) version: 21.0.2+13-58</li>
 * <li>Java Virtual Machine (JVM) version: 21.0.2+13-58</li>
 * <li>Java Vendor: Oracle Corporation</li>
 * </ul>
 */

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList; // Vector equivalent
import java.util.Arrays;
import java.util.Collections;

public class Main {
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String underlineStart = "\u001B[4m";
    public static final String underlineEnd = "\u001B[0m";
    public static String filePath;

    /**
     * Populates matrices and vectors with data from a txt file, prints
     * the data that was parsed, and then runs simulations to test
     * whether the system is in a safe state and whether it can process
     * an incoming request and remain in a safe state.
     *
     * @param args String array of command line arguments
     * @title Main
     */
    public static void main(String[] args) {
        if (Arrays.stream(args).findAny().isEmpty()) {
            out.println("Not enough arguments");
            System.exit(1);
        } else {
            filePath = args[0];
        }

        int data_section = 1; // a "section" is in-between empty lines in the txt file

        // (initially empty while we find n and m)
        Matrix m_allocation = new Matrix(0, 0); // n x m allocation matrix
        Matrix m_max = new Matrix(0, 0); // n x m max matrix

        ArrayList<Integer> v_available = new ArrayList<>(0); // A 1 x m available vector
        ArrayList<Integer> v_request = new ArrayList<>(0); // A i : 1 x m request vector

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Ignore empty lines
                if (!line.trim().isEmpty()) {
                    switch (data_section++) {
                        //  Number of processes: n
                        case 1 -> Matrix.processes = Integer.parseInt(line);
                        //  Number of resource types: m
                        case 2 -> Matrix.resources = Integer.parseInt(line);
                        // Resize matrices because we now know n AND m
                        // An n x m allocation matrix
                        case 3 -> m_allocation.updateSize().populateMatrixFromTxt(br, line);
                        // An n x m max matrix
                        case 4 -> m_max.updateSize().populateMatrixFromTxt(br, line);
                        // A 1 x m available vector
                        case 5 -> populateVectorFromString(v_available, line);
                        //  A i : 1 x m request vector
                        case 6 -> populateVectorFromString(v_request, line);
                        default -> out.printf("Something went wrong: %s\n", line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Subtract allocation from max
        Matrix m_need = Matrix.differenceOfMatrices(m_max, m_allocation); // n x m need matrix

        // Display all data gathered from txt
        out.printf("There are %d processes in the system.\n\n", Matrix.processes);
        out.printf("There are %d resource types.\n", Matrix.resources);
        m_allocation.printMatrix("Allocation");
        m_max.printMatrix("Max");
        m_need.printMatrix("Need");
        printVector("Available", v_available);

        // SAFETY CHECK
        Matrix deepCopyAllocation = Matrix.deepCopy(m_allocation);
        ArrayList<Integer> deepCopyAvailableResources = new ArrayList<>(v_available);
        checkSystemSafety(deepCopyAllocation, m_need, deepCopyAvailableResources);
        boolean systemIsSafe = systemIsSafe(deepCopyAllocation, m_need, deepCopyAvailableResources);
        out.printf("\n%sTHE SYSTEM IS %sIN A SAFE STATE!%s\n", (systemIsSafe ? GREEN : RED), (systemIsSafe ? "" : "NOT "), RESET);

        // Echo the request vector.  Label the process making the request and resource types
        printRequest(v_request);

        // Compute if the request can be granted.
        // Using Resource-Request Algorithm for Process Pi
        // Slide 28 of 43 ch07_v01 Deadlocks
//      Step 1
        boolean step1 = false;
        try {
            step1 = requestLessOrEqualToNeed(v_request, m_need);
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
            System.exit(1);
        }
//      Step2
        boolean step2 = awaitResources(v_request, v_available, m_allocation, m_need);

        out.printf("%sTHE REQUEST CAN %sBE GRANTED!%s\n", ((step1 && step2) ? GREEN : RED), ((step1 && step2) ? "" : "NOT "), RESET);

        // Vector has been altered in previous steps
        printVector("Available", v_available);

//        m_allocation.printMatrix("New allocation"); // uncomment to see the new Allocation matrix
//        m_need.printMatrix("New Need"); // uncomment to see the new Need matrix
    }

    /**
     * Checks whether a process requested within its maximum allowed
     * processes. This is the first step in the Resource-Request
     * Algorithm for a Process Pi.
     *
     * @param request Vector of process requester and resources
     * @param need    Matrix of process needed to reach max
     * @return boolean TRUE if request &#8804 need
     * @throws IllegalArgumentException Process requested more resources than its max
     * @title Request Less or Equal To Need
     */
    public static boolean requestLessOrEqualToNeed(ArrayList<Integer> request, Matrix need) throws IllegalArgumentException {
        if (allValuesLessThanOrEqual(request, need.data.get(request.getFirst()))) {
            return true;
        } else {
            throw new IllegalArgumentException(String.format("Process [%s] exceeds the initial claimed maximum amount of resources.", request.getFirst()));
        }
    }

    /**
     * Allocates resources to a process and updates the count of
     * available resources.
     *
     * @param request    Vector of process requester and resources
     * @param available  Vector of available resources
     * @param allocation Matrix of resources allocated to a process
     * @param need       Matrix of process needed to reach max
     * @title Allocate Process Resources
     */
    public static void allocateProcessResources(ArrayList<Integer> request, ArrayList<Integer> available, Matrix allocation, Matrix need) {
        for (int i = 0; i < Matrix.resources; i++) {
            int allocation_resource = allocation.data.get(request.getFirst()).get(i);
            int need_resource = need.data.get(request.getFirst()).get(i);
            int requested_resource = request.get(i + 1);
            int available_resource = available.get(i);

            available.set(i, (available_resource - requested_resource));
            allocation.data.get(request.getFirst()).set(i, (allocation_resource + requested_resource));
            need.data.get(request.getFirst()).set(i, (need_resource - requested_resource));
        }
    }

    /**
     * Checks whether there are enough resources for the request, if not
     * it must wait, and it goes through a simulation seeing which
     * processes can release their resources. After each process releases
     * its resources it checks whether there are enough resources now.
     * Finally, it returns the status of the allocation of resources to
     * requester.
     *
     * @param request    Vector of process requester and resources
     * @param available  Vector of available resources
     * @param allocation Matrix of resources allocated to a process
     * @param need       Matrix of resources needed to reach max
     * @return boolean TRUE if resources were allocated to requester
     * @title Await Resources
     */
    public static boolean awaitResources(ArrayList<Integer> request, ArrayList<Integer> available, Matrix allocation, Matrix need) {
        boolean step2 = allValuesLessThanOrEqual(request, available);
        if (!step2) {
            // Wait for resources to be free
            // Set a timeout to prevent infinite looping
            for (int i = 0; i < sumUntilZero(Matrix.processes); i++) {
                // a boolean vector for every resource - TRUE if all resources can be returned
                ArrayList<Boolean> noRemainingResourcesInProcess = new ArrayList<>(Collections.nCopies(Matrix.processes, false));
                for (int j = 0; j < sumUntilZero(Matrix.processes); j++) {
                    // Trying to return a processes resources back to available vector
                    noRemainingResourcesInProcess.set(j % (Matrix.processes), willProcessReleaseResources((j % (Matrix.processes)), allocation, need, available));
                }
                // After finishing each process check if now there are enough resources for the request
                step2 = allValuesLessThanOrEqual(request, available);
            }

//                step2 = awaitResources(request, available, allocation, max, need);
        } else {
            // If resources have been freed, allocate the process its resources
            allocateProcessResources(request, available, allocation, need);
        }

        return step2;
    }

    /**
     * Calculates the sum of a number added to itself and subtracting
     * 1 until that number is 0. This is used to calculate the maximum
     * number of loops required to give each process time to either
     * wait/release its resources. In the worst case scenario this
     * is how many loops would be required to release all resources.
     *
     * @param number The number to sum until 0
     * @return int
     * @title Sum Until Zero
     */
    public static int sumUntilZero(int number) {
        int sum = 0;
        for (int i = number; i > 0; i--) {
            sum += i;
        }
        return sum;
    }

    /**
     * Checks if there are enough available resources for a process to
     * return all of its resources back to the available vector. Returns
     * the status of whether it was successful or not.
     *
     * @param process_index The process attempting to release its resources
     * @param allocation    Matrix of resources allocated to a process
     * @param need          Matrix of resources needed to reach max
     * @param available     Vector of available resources
     * @return boolean TRUE if process released its resources
     * @title Will Process Release Resources
     */
    public static boolean willProcessReleaseResources(int process_index, Matrix allocation, Matrix need, ArrayList<Integer> available) {
        // Trying to return a processes resources back to available
        ArrayList<Boolean> availableResourcesExist = new ArrayList<>(Collections.nCopies(Matrix.resources, false));
        // ALL need resource X must be <= ALL available resource X
        for (int j = 0; j < Matrix.resources; j++) {
            int need_resource = need.data.get(process_index).get(j);
            int available_resource = available.get(j);
            // If enough available resources exist for this process to reach its max resources
            // then it can use the available resources, so we set the vector index to TRUE
            // The boolean vector has an element for each resource, for all resources to
            // be returned every vector element must be true
            availableResourcesExist.set(j, (need_resource <= available_resource));
        }

        boolean sufficientAvailableResources = availableResourcesExist.stream().allMatch(b -> b.equals(true));
        // If every resource for this process can be returned then return to available vector
        if (sufficientAvailableResources) {
            for (int j = 0; j < Matrix.resources; j++) {
                int resources_returned = allocation.data.get(process_index).get(j);
                int available_resource = available.get(j);
                allocation.data.get(process_index).set(j, 0);
                available.set(j, (available_resource + resources_returned));
            }
        }
        // Return whether this process released its resources
        return sufficientAvailableResources;
    }

    /**
     * Check system safety by seeing if all resources can be
     * released, sometimes a process may not have enough resources, so it
     * will have to wait for resources to be free, this will need to loop.
     * The maximum number of loops needed to check if resources can be
     * released. Max number of loops will be = to the number of process
     * in the Matrix.
     *
     * @param allocation n x m Matrix of allocation
     * @param need       n x m Matrix of need
     * @param available  1 x m Vector of available processes
     * @apiNote The {@code available} vector will be changed
     * @title Check System Safety
     */
    public static void checkSystemSafety(Matrix allocation, Matrix need, ArrayList<Integer> available) {
        for (int p = 0; p < Matrix.processes; p++) {
            if (resourcesRemainInMatrix(allocation)) {
                // see if resources can be retrieved from processes
                systemIsSafe(allocation, need, available);
            } else {
                break;
            }
        }
    }

    /**
     * Return TRUE if matrix is not 0 for every resource.
     *
     * @param matrix A process x resource Matrix
     * @return boolean
     * @title Resources Remain In Matrix
     */
    public static boolean resourcesRemainInMatrix(Matrix matrix) {
        boolean resourcesRemain = false;
        for (int i = 0; i < Matrix.processes; i++) {
            for (int j = 0; j < Matrix.resources; j++) {
                int resource_x_count = matrix.data.get(i).get(j);
                if (resource_x_count != 0) {
                    return (resourcesRemain = true);
                }
            }
        }
        return resourcesRemain;
    }

    /**
     * Return TRUE if all resources from allocation can be
     * returned to available resource vector.
     *
     * @param allocation An n x m Matrix of allocation
     * @param need       An n x m Matrix of need
     * @param available  A 1 x m Vector of available processes
     * @return boolean
     * @title System Is Safe
     */
    public static boolean systemIsSafe(Matrix allocation, Matrix need, ArrayList<Integer> available) {
        ArrayList<Boolean> noRemainingResourcesInProcess = new ArrayList<>(Collections.nCopies(Matrix.processes, false));
        // for every need, difference with available to see if system is stuck or not
        for (int i = 0; i < Matrix.processes; i++) {
            ArrayList<Boolean> availableResourcesExist = new ArrayList<>(Collections.nCopies(Matrix.resources, false));

            // ALL need resource X must be <= ALL available resource X
            for (int j = 0; j < Matrix.resources; j++) {
                int need_resource = need.data.get(i).get(j);
                int available_resource = available.get(j);
                availableResourcesExist.set(j, (need_resource <= available_resource));
            }

            // if all needs met on process Y then return its resources to available
            boolean sufficientAvailableResources = availableResourcesExist.stream().allMatch(b -> b.equals(true));
            if (sufficientAvailableResources) {
                for (int j = 0; j < Matrix.resources; j++) {
                    int resources_returned = allocation.data.get(i).get(j);
                    allocation.data.get(i).set(j, 0);
                    int available_resource = available.get(j);
                    available.set(j, (available_resource + resources_returned));
                }
            }
            noRemainingResourcesInProcess.set(i, sufficientAvailableResources);
        }
        return noRemainingResourcesInProcess.stream().allMatch(b -> b.equals(true));
    }

    /**
     * Outputs to console the Integers stored in the {@code vector}
     * parameter.
     *
     * @param title  A title to signify the name of the vector
     * @param vector An ArrayList of integers length of Matrix.resources
     * @title Print Vector
     */
    public static void printVector(String title, ArrayList<Integer> vector) {
        out.printf("\nThe %s Vector is...\n", title);
        // ANSI escape sequence for underline
        String underlineStart = "\u001B[4m";
        String underlineEnd = "\u001B[0m";
        for (int i = 'A'; i < vector.size() + 'A'; i++) {
            out.printf("%s%c%s ", underlineStart, ((char) i), underlineEnd);
        }
        out.print("\n");
        vector.forEach(value -> out.printf("%s ", value));
        out.println();
    }

    /**
     * Return TRUE if all values in {@code list1} &lt;= {@code need}.
     * This is used for Request <= Need, AND, Request <= Available.
     * Refer to Step 1 on Slide 28 of 43 in ch07_v01 Deadlocks.
     *
     * @param list1 The count of each process requested to use
     * @param list2 ector of initially claimed resources to reach max
     *              for process that requested
     * @return boolean
     * @title All Values Less Than Or Equal
     */
    public static boolean allValuesLessThanOrEqual(ArrayList<Integer> list1, ArrayList<Integer> list2) {
        boolean allRequestsGood = true;
        try {
            // [3:1 5 6 3] -> id = 3
            int request_process_id = list1.getFirst();
            if (request_process_id > Matrix.processes) {
                throw new IllegalArgumentException(String.format("\n%sProcess ID %d out of range of Matrix need.processes\n%s%s", RED, request_process_id, list1, RESET));
            }
            ArrayList<Boolean> result = new ArrayList<>();
            for (int i = 0; i < Matrix.resources; i++) {
                int request_resource_x = list1.get(i + 1);
                int need_resource_x = list2.get(i);
                result.add(request_resource_x <= need_resource_x);
            }
            for (Boolean value : result) {
                if (!value) {
                    allRequestsGood = false;
                    break;
                }
            }
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
            System.exit(1);
        }
        return allRequestsGood;
    }

    /**
     * Populate a vector with values from a txt file
     * that are seperated by either ":" or " ".
     *
     * @param vector The vector to populate with integers
     * @param line   A string containing numbers
     * @apiNote {@code vector} must initially have a size 0
     * @title Populate Vector From String
     */
    public static void populateVectorFromString(ArrayList<Integer> vector, String line) {
        String[] numbers_as_string = line.split("\\s+|:");
        // {process id, requested A, requested B, requested C, requested D}
        // split the string by spaces or colons
        // E.g. "3:6 7 4 3" -> [3, 6, 7, 4, 3]
        for (String s : numbers_as_string) {
            vector.add(Integer.parseInt(s));
        }
    }


    /**
     * Output the contents of a 1D vector
     *
     * @param vector The vector to be displayed in console
     * @title Print Request
     */
    public static void printRequest(ArrayList<Integer> vector) {
        out.print("\nThe Request Vector is...\n  ");
        for (int i = 'A'; i < Matrix.resources + 'A'; i++, out.print(underlineStart + (char) (i - 1) + underlineEnd + " "))
            ;
        out.printf("\n%d:", vector.getFirst());
        // for every resource
        for (int j = 1; j < vector.size(); j++) {
            out.print(vector.get(j) + " ");
        }
        out.println("\n");
    }
}