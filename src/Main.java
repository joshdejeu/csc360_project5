/**
 * @author Josh Dejeu
 */

import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.util.ArrayList; //vector equivalent
import java.util.Collections;
//2:0 4 2 0
//3:0 4 2 0
//3:0 4 2 0
//3:0 4 2 0
//1:0 4 2 0
//1:0 4 2 0
//4:0 4 2 0
//5:0 4 2 0
//9:0 4 2 0

public class Main {
    public static final String RESET = "\u001B[0m";
    public static final String GREEN = "\u001B[32m";
    public static final String RED = "\u001B[31m";

    public static void main(String[] args) {
        String filePath = "s1.txt";
        //        String filePath = args[1];
        int data_section = 1; // a "section" is in-between empty lines in the txt file

        int number_of_processes = 0;
        int number_of_resources = 0;

        // (initially empty while we find n and m)
        Matrix matrix_allocation = new Matrix(0, 0); // n x m allocation matrix
        Matrix matrix_max = new Matrix(0, 0); // n x m max matrix
        Matrix matrix_need = new Matrix(0, 0); // n x m need matrix

        ArrayList<Integer> vector_available = new ArrayList<>(0); // A 1 x m available vector
        Matrix matrix_request = new Matrix(0, 0); // A i : 1 x m request vector

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Ignore empty lines
                if (!line.trim().isEmpty()) {
                    switch (data_section) {
                        //  Number of processes: n
                        case 1 -> number_of_processes = Integer.parseInt(line);
                        //  Number of resource types: m
                        case 2 -> {
                            number_of_resources = Integer.parseInt(line);
                            Matrix.processes = number_of_processes;
                            Matrix.resources = number_of_resources;
                            // Resize matrices because we now know n AND m
                            matrix_allocation = new Matrix();
                            matrix_max = new Matrix();
                        }
                        // An n x m allocation matrix
                        case 3 -> matrix_allocation.populateMatrixFromTxt(br, line, false);
                        // An n x m max matrix+
                        case 4 -> {
                            matrix_max.populateMatrixFromTxt(br, line, false);
                            matrix_need = Matrix.differenceOfMatrices(matrix_max, matrix_allocation);
                        }
                        // A 1 x m available vector
                        case 5 -> {
                            vector_available = new ArrayList<>(number_of_resources);
                            String[] numbers_as_string = line.split(" "); // split the string by spaces
                            for (int i = 0; i < number_of_resources; i++) { // parse each string element and add it to the array
                                vector_available.add(i, Integer.parseInt(numbers_as_string[i]));
                            }
                        }
                        //  A i : 1 x m request vector
                        case 6 -> matrix_request.populateMatrixFromTxt(br, line, true);
                        default -> out.printf("Something went wrong: %s\n", line);
                    }
                    data_section++;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Display all data gathered from txt
        out.println("\t-- The data this simulation will be running --\n");
        out.printf("Number of Processes: %d\n", number_of_processes);
        out.printf("Number of Resources: %d\n", number_of_resources);
        printMatrix("Allocation", matrix_allocation);
        printMatrix("Max", matrix_max);
        printMatrix("Need", matrix_need);
        printVector("Available", vector_available);

        // SAFETY CHECK
        Matrix copyAllocation = matrix_allocation.clone();
        checkSystemSafety(copyAllocation, matrix_need, vector_available);
        if (systemIsSafe(copyAllocation, matrix_need, vector_available)) {
            out.println(GREEN + "\nThe system is safe" + RESET);
        } else {
            out.println(RED + "\nThe system is NOT safe" + RESET);
        }
        copyAllocation = null; // Set the reference to null we no longer need this

        // Echo the request vector.  Label the process making the request and resource types
        matrix_request.printRequest();

        // Compute if the request can be granted.


        // Compute the new available vector
        printVector("*NEW* Available", vector_available);
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
                int resource_x_count = allocation.getData().get(i).get(j);
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
                int resource_x = need.getData().get(i).get(j);
                int available_x = available.get(j);
                availableResourcesExist.set(j, (resource_x <= available_x));
            }

            // if all needs met on process Y then return its resources to available
            boolean sufficientAvailableResources = availableResourcesExist.stream().allMatch(b -> b.equals(true));
            if (sufficientAvailableResources) {
                for (int l = 0; l < Matrix.resources; l++) {
                    int resources_returned = allocation.getData().get(i).get(l);
                    allocation.getData().get(i).set(l, 0);
                    int available_x = available.get(l);
                    available.set(l, (available_x + resources_returned));
                }
            }
            noRemainingResourcesInProcess.set(i, sufficientAvailableResources);
        }
        return noRemainingResourcesInProcess.stream().allMatch(b -> b.equals(true));
    }

    public static void printVector(String title, ArrayList<Integer> vector) {
        out.printf("\n%s Vector:\n", title);
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

    public static void printMatrix(String title, Matrix matrix) {
        out.printf("\n%s Matrix:", title);
        matrix.printMatrix();
    }

}