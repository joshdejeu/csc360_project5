import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

import java.lang.reflect.Array;
import java.util.ArrayList; //vector equivalent
import java.util.Collections;

/**
 * @author Josh Dejeu
 */

public class Main {
    public static void main(String[] args) {
        String filePath = "s1.txt";
//        String filePath = args[1];
        int data_section = 1; // a "section" is in-between empty lines in the txt file

        int number_of_processes = 0;
        int number_of_resources = 0;

        // (initially empty while we find n and m)
        Matrix matrix_allocation = new Matrix(0, 0);                // n x m allocation matrix
        Matrix matrix_max = new Matrix(0, 0);                       // n x m max matrix
        Matrix matrix_need = new Matrix(0, 0);                      // n x m need matrix

        ArrayList<Integer> vector_available = new ArrayList<>(0);     // A 1 x m available vector
        Matrix matrix_request = new Matrix(0, 0);                   // A i : 1 x m request vector


        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                // Ignore empty lines
                if (!line.trim().isEmpty()) {
                    switch (data_section) {
                        case 1: //  Number of processes: n
                            number_of_processes = Integer.parseInt(line);
                            break;
                        case 2: //  Number of resource types: m
                            number_of_resources = Integer.parseInt(line);
                            // Resize matrices because we now know n AND m
                            matrix_allocation = new Matrix(number_of_processes, number_of_resources);
                            matrix_max = new Matrix(number_of_processes, number_of_resources);
                            matrix_need = new Matrix(number_of_processes, number_of_resources);
                            matrix_request.processes = number_of_processes;
                            matrix_request.resources = number_of_resources;
                            break;
                        case 3: // An n x m allocation matrix
                            matrix_allocation.populateMatrixFromTxt(br, line);
//                            matrix_allocation.printMatrix(); // uncomment to print Allocation matrix
                            break;
                        case 4: // An n x m max matrix+
                            matrix_max.populateMatrixFromTxt(br, line);
//                            matrix_max.printMatrix(); // uncomment to print Allocation matrix
                            matrix_need.differenceOfMatrices(matrix_allocation.getData(), matrix_max.getData());
                            break;
                        case 5: // A 1 x m available vector
                            vector_available = new ArrayList<>(number_of_resources);
                            String[] numbers_as_string = line.split(" "); // split the string by spaces
                            for (int i = 0; i < number_of_resources; i++) { // parse each string element and add it to the array
                                vector_available.add(i, Integer.parseInt(numbers_as_string[i]));
                            }
                            break;
                        case 6: //  A i : 1 x m request vector
                            matrix_request.setData(matrix_request.populateRequestsFromTxt(br, line));
                            break;
                        default:
                            out.println(String.format("Something went wrong: %s", line));
                    }
                    data_section++;
                }
            }
        } catch (IOException e) {
            out.println(e);
        }

        // Display all data gathered from txt
        out.println("\t-- The data this simulation will be running --\n");

        out.printf("Number of Processes: %d\n", number_of_processes);
        out.printf("Number of Resources: %d\n", number_of_resources);

        out.printf("\nAllocation Matrix:");
        matrix_allocation.printMatrix();

        out.printf("\nMax Matrix:");
        matrix_max.printMatrix();

        out.printf("\nNeed Matrix:");
        matrix_need.printMatrix();

        out.println("\nAvailable Vector:");
        // ANSI escape sequence for underline
        String underlineStart = "\u001B[4m";
        String underlineEnd = "\u001B[0m";
        for (int i = 'A'; i < number_of_resources + 'A'; i++) {
            out.print(underlineStart + (char) i + underlineEnd + " ");
        }
        out.println();
        vector_available.forEach(value -> out.print(value + " "));
        out.println();

        // is system safe
        determineIfSystemSafe(matrix_allocation, matrix_max, matrix_need, vector_available);

        out.printf("\nRequest Matrix:");
        matrix_request.printRequest();

        // can request be granted

        //new available vec


        out.printf("\nAllocation Matrix After:");
        matrix_allocation.printMatrix();

    }

    public static void determineIfSystemSafe(Matrix allocation, Matrix max, Matrix need, ArrayList<Integer> available) {
        // TODO: check if any processes remain and loop until either no possible solution or all resources returned

        // for every need, difference with available to see if system is stuck or not
        for (int i = 0; i < need.processes; i++) {
            ArrayList<Boolean> resource_need_possible = new ArrayList<>(Collections.nCopies(4, false));

            // for each resource
            // ALL resource X must be <= ALL available X
            for (int j = 0; j < need.resources; j++) {
                int resource_x = need.getData().get(i).get(j);
                int available_x = available.get(j);
                resource_need_possible.set(j, (resource_x <= available_x));
            }

            // if all needs met on process Y then return its resources to available
            boolean allTrue = resource_need_possible.stream().allMatch(b -> b.equals(true));
            if (allTrue) {
                for (int l = 0; l < allocation.resources; l++) {
                    int resources_returned = allocation.getData().get(i).get(l);
                    allocation.getData().get(i).set(l, 0);
                    int available_x = available.get(l);
                    available.set(l, (available_x + resources_returned));
                }
            }

            out.print("Available: ");
            available.forEach(value -> out.print(value + " "));
            out.println();
        }
        out.println();
    }

}
//1 5 2 0
//        1 5 3 2
//        1 5 3 2
//        2 8 8 6
//        2 14 11 8
//        2 14 12 12