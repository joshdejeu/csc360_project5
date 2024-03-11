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
    private ArrayList<Integer> array_of_requests;

    // Constructor
    public Matrix(int rows, int columns) {
        this.processes = rows;
        this.resources = columns;
        this.data = createDynamicMatrix(this.processes, this.resources);
        this.array_of_requests = new ArrayList<>(0);
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

    public void populateRequestsFromTxt(BufferedReader br, String line) throws IOException, NumberFormatException {
        do {
            ArrayList<Integer> request_numbers = new ArrayList<>(this.resources);
            // split the string by spaces
            String[] numbers_as_string = line.split("\\s+|:");
            int order_number = Integer.parseInt(numbers_as_string[0]);
            // E.g. "3:6 7 4 3" -> [6, 7, 4, 3]
            for (int i = 1; i < numbers_as_string.length; i++) {
                request_numbers.add(Integer.parseInt(numbers_as_string[i]));
            }
            this.addRequestInOrder(order_number, request_numbers, array_of_requests);
            line = br.readLine();
        }
        // add all numbers into allocation matrix until empty line detected
        while (line != null && !line.trim().isEmpty());
    }

    private void addRequestInOrder(int order_number, ArrayList<Integer> data, ArrayList<Integer> array_of_requests) {
        // Find the index where the number should be inserted
        int index = Collections.binarySearch(array_of_requests, order_number);
//        out.println(order_number + " " + array_of_requests + " "); // Uncomment to see state of request array
        // If index -1 make it insertion point
        if (index < 0) {
            index = -index - 1;
        }
        this.array_of_requests.add(index, order_number);
        this.data.add(index, data); // requests will be sorted before simulation with this step
    }

}
