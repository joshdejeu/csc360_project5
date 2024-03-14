import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.out;

public class Matrix {
    public static final String underlineStart = "\u001B[4m";
    public static final String underlineEnd = "\u001B[0m";
    public static int processes; // aka Rows
    public static int resources; // aka Columns
    public int row_index = 0;
    private ArrayList<ArrayList<Integer>> data;

    // Default Constructor
    public Matrix() {
        this.data = createDynamicMatrix(processes, resources);
    }

    // Constructor
    public Matrix(int rows, int columns) {
        processes = rows;
        resources = columns;
        this.data = createDynamicMatrix(processes, resources);
    }

    // used to create the need matrix (difference of max and allocation)
    public static Matrix differenceOfMatrices(Matrix m_max, Matrix m_allocation) {
        // for each process
        Matrix m_difference = new Matrix(processes, resources);
        for (int i = 0; i < processes; i++) {
            // for each resource
            for (int j = 0; j < resources; j++) {
                int num_of_processes_needed_to_reach_max = m_max.getData().get(i).get(j) - m_allocation.getData().get(i).get(j);
                m_difference.data.get(i).set(j, num_of_processes_needed_to_reach_max);
            }
        }
        return m_difference;
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

    public void populateMatrixFromTxt(BufferedReader br, String line, boolean isRequest) throws IOException, NumberFormatException {
        ArrayList<ArrayList<Integer>> queue_of_requests = new ArrayList<>(0);
        do {
            // split the string by " " and ":"
            String[] numbers_as_string = line.split("\\s+|:");
            if (isRequest) {
                // {process id, requested A, requested B, requested C, requested D}
                ArrayList<Integer> request = new ArrayList<>(resources);
                // split the string by spaces
                // E.g. "3:6 7 4 3" -> [3, 6, 7, 4, 3]
                for (String s : numbers_as_string) {
                    request.add(Integer.parseInt(s));
                }
                queue_of_requests.add(request);
                this.data = queue_of_requests;
            } else {
                // parse each string element and add it to the array
                for (int i = 0; i < resources; i++) {
                    this.data.get(this.row_index).set(i, Integer.parseInt(numbers_as_string[i]));
                }
                this.row_index++;
            }
            line = br.readLine();
        }
        // add all numbers into allocation matrix until empty line detected
        while (line != null && !line.trim().isEmpty());
    }

    public void printRequest() {
        out.print("\nRequest Matrix:\n\t");
        for (int i = 'A'; i < resources + 'A'; i++, out.print(underlineStart + (char) (i - 1) + underlineEnd + " ")) ;
        out.println();
        // for every process
        for (ArrayList<Integer> datum : this.data) {
            out.print("p" + datum.getFirst() + ": ");
            // for every resource
            for (int j = 0; j < resources; j++) {
                out.print(datum.get(j + 1) + " ");
            }
            out.println();
        }
    }

    public void printMatrix() {
        // ANSI escape sequence for underline
        out.print("\n    ");
        for (int i = 'A'; i < resources + 'A'; i++) {
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

    // deep copy
    public Matrix clone() {
        Matrix deepCopy = new Matrix(processes, resources);
        for (int i = 0; i < processes; i++) {
            for (int j = 0; j < resources; j++) {
                deepCopy.getData().get(i).set(j, this.data.get(i).get(j));
            }
        }
        return deepCopy;
    }

    public ArrayList<ArrayList<Integer>> getData() {
        return data;
    }
}
