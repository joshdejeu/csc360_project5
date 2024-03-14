/**
 * @author Josh Dejeu
 * @course: CSC360/660 Spring 2024
 * @project Name and Title: Project #5: Implementation of Banker's Algorithm
 * @due Date: Mar 18, 2024 11:59 PM
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
        Matrix matrix_allocation = new Matrix(0, 0); // n x m allocation matrix
        Matrix matrix_max = new Matrix(0, 0); // n x m max matrix

        ArrayList<Integer> vector_available = new ArrayList<>(0); // A 1 x m available vector
        ArrayList<Integer> vector_request = new ArrayList<>(0); // A i : 1 x m request vector

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
                        case 3 -> matrix_allocation.updateMatrixSize().populateMatrixFromTxt(br, line);
                        // An n x m max matrix
                        case 4 -> matrix_max.updateMatrixSize().populateMatrixFromTxt(br, line);
                        // A 1 x m available vector
                        case 5 -> populateVectorFromString(vector_available, line);
                        //  A i : 1 x m request vector
                        case 6 -> populateVectorFromString(vector_request, line);
                        default -> out.printf("Something went wrong: %s\n", line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Subtract allocation from max
        Matrix matrix_need = Matrix.differenceOfMatrices(matrix_max, matrix_allocation); // n x m need matrix

        // Display all data gathered from txt
        out.printf("There are %d processes in the system.\n\n", Matrix.processes);
        out.printf("There are %d resource types.\n", Matrix.resources);
        matrix_allocation.printMatrix("Allocation");
        matrix_max.printMatrix("Max");
        matrix_need.printMatrix("Need");
        printVector("Available", vector_available);

        // SAFETY CHECK
        Matrix deepCopyAllocation = Matrix.deepCopy(matrix_allocation);
        ArrayList<Integer> deepCopyAvailableResources = new ArrayList<>(vector_available);
        checkSystemSafety(deepCopyAllocation, matrix_need, deepCopyAvailableResources);
        boolean systemIsSafe = systemIsSafe(deepCopyAllocation, matrix_need, deepCopyAvailableResources);
        out.printf("\n%sTHE SYSTEM IS %sIN A SAFE STATE!%s\n", (systemIsSafe ? GREEN : RED), (systemIsSafe ? "" : "NOT "), RESET);

        // Echo the request vector.  Label the process making the request and resource types
        printRequest(vector_request);

        // Compute if the request can be granted.
        // Slide 28 of 43 ch07_v01 Deadlocks
        boolean step1 = isRequestWithinClaim(vector_request, matrix_need);
//        boolean step2 = false;
//        boolean step3 = false;
        out.printf("THE REQUEST CAN %sBE GRANTED!\n", (step1 ? "" : RED + "NOT " + RESET));

        // Compute the new available vector
        printVector("Available", vector_available);
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
     * @param matrix process x resource Matrix
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
     * @param allocation n x m Matrix of allocation
     * @param need       n x m Matrix of need
     * @param available  1 x m Vector of available processes
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
                int resource_x = need.data.get(i).get(j);
                int available_x = available.get(j);
                availableResourcesExist.set(j, (resource_x <= available_x));
            }

            // if all needs met on process Y then return its resources to available
            boolean sufficientAvailableResources = availableResourcesExist.stream().allMatch(b -> b.equals(true));
            if (sufficientAvailableResources) {
                for (int l = 0; l < Matrix.resources; l++) {
                    int resources_returned = allocation.data.get(i).get(l);
                    allocation.data.get(i).set(l, 0);
                    int available_x = available.get(l);
                    available.set(l, (available_x + resources_returned));
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
     * Return TRUE if the {@code request} < {@code need} that was
     * initially claimed. Refer to Step 1 on Slide 28 of 43 in ch07_v01
     * Deadlocks.
     *
     * @param request The count of each process requested to use
     * @param need    Matrix of initially claimed resources to reach max
     * @return boolean
     * @title Is Request Within Claim
     */
    public static boolean isRequestWithinClaim(ArrayList<Integer> request, Matrix need) {
        boolean allRequestsGood = true;
        try {
            // [3:1 5 6 3] -> id = 3
            int request_process_id = request.getFirst();
            if (request_process_id > Matrix.processes) {
                throw new IllegalArgumentException(String.format("\n%sProcess ID %d out of range of Matrix need.processes\n%s%s", RED, request_process_id, request, RESET));
            }
            ArrayList<Boolean> result = new ArrayList<>();
            for (int i = 0; i < Matrix.resources; i++) {
                int request_resource_x = request.get(i + 1);
                int need_resource_x = need.data.get(request.getFirst()).get(i);
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