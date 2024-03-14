/**
 * @author Josh Dejeu
 */

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList; //vector equivalent
import java.util.Collections;

public class Main {
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";
    public static final String underlineStart = "\u001B[4m";
    public static final String underlineEnd = "\u001B[0m";

    public static void main(String[] args) {
        String filePath = "s1.txt";
        //        String filePath = args[1];
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
                        case 3 -> matrix_allocation.resizeMatrix().populateMatrixFromTxt(br, line);
                        // An n x m max matrix
                        case 4 -> matrix_max.resizeMatrix().populateMatrixFromTxt(br, line);
                        // A 1 x m available vector
                        case 5 -> populateVectorFromTxt(vector_available, line);
                        //  A i : 1 x m request vector
                        case 6 -> populateVectorFromTxt(vector_request, line);
                        default -> out.printf("Something went wrong: %s\n", line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Matrix matrix_need = Matrix.differenceOfMatrices(matrix_max, matrix_allocation); // n x m need matrix

        // Display all data gathered from txt
        out.println("\t-- The data this simulation will be running --\n");
        out.printf("There are %d processes in the system.\n", Matrix.processes);
        out.printf("There are %d resource types.\n", Matrix.resources);
        matrix_allocation.printMatrix("Allocation");
        matrix_max.printMatrix("Max");
        matrix_need.printMatrix("Need");
        printVector("Available", vector_available);

        // SAFETY CHECK
        Matrix copyAllocation = matrix_allocation.deepCopy();
        checkSystemSafety(copyAllocation, matrix_need, vector_available);
        if (systemIsSafe(copyAllocation, matrix_need, vector_available)) {
            out.println(GREEN + "\nTHE SYSTEM IS IN A SAFE STATE!" + RESET);
        } else {
            out.println(RED + "\nTHE SYSTEM IS NOT IN A SAFE STATE!" + RESET);
        }

        // Echo the request vector.  Label the process making the request and resource types
        printRequest(vector_request);

        // Compute if the request can be granted.
        try {
            boolean areAllRequestsWithinClaim = checkRequestAgainstNeed(vector_request, matrix_need);
            out.printf("THE REQUEST CAN %s BE GRANTED!\n", (areAllRequestsWithinClaim ? "" : RED + "NOT" + RESET));
        } catch (IllegalArgumentException e) {
            out.println(e.getMessage());
        }

        // Compute the new available vector
        printVector("New Available", vector_available);
    }

    // The maximum number of loops to check if remaining resources-
    // are in a safe state is proportional to the number of processes
    // E.g: 1st loop calculating need matrix and you skip over p2, you have to loop again-
    // E.g. contd: to see if you have enough resources for p2 now that other processes have finished
    public static void checkSystemSafety(Matrix allocation, Matrix need, ArrayList<Integer> available) {
        for (int p = 0; p < Matrix.processes; p++) {
            if (!noResourcesRemainInAllocation(allocation)) {
                // see if resources can be retrieved from processes
                systemIsSafe(allocation, need, available);
            } else {
                break;
            }
        }
    }

    // TRUE if a number != 0 exists in Allocation Matrix
    public static boolean noResourcesRemainInAllocation(Matrix allocation) {
        boolean noResourcesRemaining = true;
        for (int i = 0; i < Matrix.processes; i++) {
            for (int j = 0; j < Matrix.resources; j++) {
                int resource_x_count = allocation.data.get(i).get(j);
                if (resource_x_count != 0) {
                    noResourcesRemaining = false;
                    return false;
                }
            }
        }
        return noResourcesRemaining;
    }

    // returns TRUE if Allocation Matrix is all 0
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

    public static void printVector(String title, ArrayList<Integer> vector) {
        out.printf("\nThe %s Vector is...\n", title);
        // ANSI escape sequence for underline
        String underlineStart = "\u001B[4m";
        String underlineEnd = "\u001B[0m";
        for (int i = 'A'; i < vector.size() + 'A'; i++) {
            out.printf("%s%-2c%s ", underlineStart, ((char) i), underlineEnd);
        }
        out.print("\n");
        vector.forEach(value -> out.printf("%-2s ", value));
        out.println();
    }

    // TRUE if request of Process i is <= to Need of Process i
    // assume only 1 request (For loop not required for 1 iteration)
    public static boolean checkRequestAgainstNeed(ArrayList<Integer> request, Matrix need) {
        boolean allRequestsGood = true;
        // [3:1 5 6 3] -> id = 3
        int request_process_id = request.getFirst();
        if (request_process_id > Matrix.processes) {
            throw new IllegalArgumentException(String.format("\n%sProcess ID %d out of range of Matrix need.processes\n%s%s", RED, request_process_id, request, RESET));
        }
        ArrayList<Integer> need_x = need.data.get(request_process_id - 1);
        ArrayList<Boolean> result = new ArrayList<>();
        for (int i = 0; i < need_x.size(); i++) {
            result.add(request.get(i + 1) <= need_x.get(i));
        }
        for (Boolean value : result) {
            if (!value) {
                allRequestsGood = false;
                break;
            }
        }
        return allRequestsGood;
    }

    // populates vector with data from txt
    // @Pre - vector must be size 0
    public static void populateVectorFromTxt(ArrayList<Integer> vector, String line) {
        String[] numbers_as_string = line.split("\\s+|:");
        // {process id, requested A, requested B, requested C, requested D}
        // split the string by spaces
        // E.g. "3:6 7 4 3" -> [3, 6, 7, 4, 3]
        for (String s : numbers_as_string) {
            vector.add(Integer.parseInt(s));
        }
    }

    // @Pre - assumes only 1 request
    public static void printRequest(ArrayList<Integer> vector) {
        out.print("\nThe Request Vector is...\n  ");
        for (int i = 'A'; i < vector.size() - 1 + 'A'; i++, out.print(underlineStart + (char) (i - 1) + underlineEnd + " "))
            ;
        out.printf("\n%d:", vector.getFirst());
        // for every resource
        for (int j = 1; j < vector.size(); j++) {
            out.print(vector.get(j) + " ");
        }
        out.println("\n");
    }
}