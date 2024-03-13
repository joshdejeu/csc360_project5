import javax.lang.model.type.ArrayType;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.out;

import java.util.Collections;

public class Matrix {
    public int processes; // aka Rows
    public int resources; // aka Columns
    private int row_index = 0;
    private ArrayList<ArrayList<Integer>> data;

    // Constructor
    public Matrix(int rows, int columns) {
        this.processes = rows;
        this.resources = columns;
        this.data = createDynamicMatrix(this.processes, this.resources);
    }

    public static ArrayList<ArrayList<Integer>> createDynamicMatrix(int rows, int columns) {
        ArrayList<ArrayList<Integer>> matrix = new ArrayList<>(rows);

        for (int i = 0; i < rows; i++) {
            ArrayList<Integer> row = new ArrayList<>(columns);
            for (int j = 0; j < columns; j++) {
                row.add(-1); // Initialize with -1
            }
            matrix.add(row);
        }

        return matrix;
    }

    public void printRequest() {
        // ANSI escape sequence for underline
        String underlineStart = "\u001B[4m";
        String underlineEnd = "\u001B[0m";
        out.print("\n    ");
        for (int i = 'A'; i < this.resources + 'A'; i++) {
            out.print(underlineStart + (char) i + underlineEnd + " ");
        }
        out.println();
        // for every process
        for (int i = 0; i < this.data.size(); i++) {
            out.print("p" + this.data.get(i).get(0) + ": ");
            // for every resource
            for (int j = 0; j < this.resources; j++) {
                out.print(this.data.get(i).get(j + 1) + " ");
            }
            out.println();
        }
    }

    // used to create the need matrix (difference of max and allocation)
    public void differenceOfMatrices(ArrayList<ArrayList<Integer>> allocation, ArrayList<ArrayList<Integer>> max) {
        // for each process
        for (int i = 0; i < this.processes; i++) {
            // for each resource
            for (int j = 0; j < this.resources; j++) {
                int num_of_processes_needed_to_reach_max = max.get(i).get(j) - allocation.get(i).get(j);
                this.data.get(i).set(j, num_of_processes_needed_to_reach_max);
            }
        }
    }

    public void setMatrixValue(int row, int column, int value) {
        this.data.get(row).set(column, value);
    }

    public void populateMatrixFromTxt(BufferedReader br, String line) throws IOException, NumberFormatException {
        do {
            // split the string by spaces
            String[] numbers_as_string = line.split(" ");

            // parse each string element and add it to the array
            for (int i = 0; i < this.resources; i++) {
                this.setMatrixValue(this.getRowIndex(), i, Integer.parseInt(numbers_as_string[i]));
            }

            this.setRowIndex(this.getRowIndex() + 1);
        }
        // add all numbers into allocation matrix until empty line detected
        while (!(line = br.readLine()).trim().isEmpty());
    }

    public void printMatrix() {
        // ANSI escape sequence for underline
        String underlineStart = "\u001B[4m";
        String underlineEnd = "\u001B[0m";
        out.print("\n    ");
        for (int i = 'A'; i < this.resources + 'A'; i++) {
            out.print(underlineStart + (char) i + underlineEnd + " ");
        }
        out.println();
        for (int i = 0; i < this.data.size(); i++) {
            out.print("p" + (i + 1) + ": ");
            for (int j = 0; j < this.data.get(i).size(); j++) {
                out.print(this.data.get(i).get(j) + " ");
            }
            out.println();
        }
    }

    public int getRowIndex() {
        return row_index;
    }

    public void setRowIndex(int newIndex) {
        this.row_index = newIndex;
    }

    public ArrayList<ArrayList<Integer>> getData() {
        return data;
    }

    public void setData(ArrayList<ArrayList<Integer>> newData) {
        this.data = newData;
    }

    public ArrayList<ArrayList<Integer>> populateRequestsFromTxt(BufferedReader br, String line) throws IOException, NumberFormatException {
        ArrayList<ArrayList<Integer>> queue_of_requests = new ArrayList<ArrayList<Integer>>(0);
        do {
            // {process id, requested A, requested B, requested C, requested D}
            ArrayList<Integer> request = new ArrayList<>(this.resources);
            // split the string by spaces
            String[] numbers_as_string = line.split("\\s+|:");
            // E.g. "3:6 7 4 3" -> [3, 6, 7, 4, 3]
            for (int i = 0; i < numbers_as_string.length; i++) {
                request.add(Integer.parseInt(numbers_as_string[i]));
            }
            queue_of_requests.add(request);
            line = br.readLine();
        }
        // add all numbers into allocation matrix until empty line detected
        while (line != null && !line.trim().isEmpty());
        return queue_of_requests;
    }

}
//    adds data in order of proccess_id to array_of_req
//    private void addRequestToQueue(int process_id, ArrayList<Integer> data, ArrayList<Integer> array_of_requests) {
//        // Find the index where the number should be inserted
//        int index = Collections.binarySearch(array_of_requests, order_number);
////        out.println(order_number + " " + array_of_requests + " "); // Uncomment to see state of request array
//        // If index -1 make it insertion point
//        if (index < 0) {
//            index = -index - 1;
//        }
//        this.array_of_requests.add(index, order_number);
//        this.data.add(index, data); // requests will be sorted before simulation with this step
//    }
