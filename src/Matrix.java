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
    public ArrayList<ArrayList<Integer>> data;

    // Constructor
    public Matrix(int rows, int columns) {
        processes = rows;
        resources = columns;
        this.data = createDynamicMatrix(processes, resources);
    }

    // used to create the need matrix (difference of max and allocation)
    // returns C = (A - B)
    public static Matrix differenceOfMatrices(Matrix m_max, Matrix m_allocation) {
        // for each process
        Matrix m_difference = new Matrix(processes, resources);
        for (int i = 0; i < processes; i++) {
            // for each resource
            for (int j = 0; j < resources; j++) {
                int num_of_processes_needed_to_reach_max = m_max.data.get(i).get(j) - m_allocation.data.get(i).get(j);
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

    // sets current object matrix size to whatever is currently statically stored in "resources" and "processes"
    public Matrix resizeMatrix() {
        this.data = new Matrix(processes, resources).data;
        return this;
    }

    public void populateMatrixFromTxt(BufferedReader br, String line) throws IOException, NumberFormatException {
        do {
            // split the string by " " and ":"
            String[] numbers_as_string = line.split(" ");
            // parse each string element and add it to the array
            for (int i = 0; i < resources; i++) {
                this.data.get(this.row_index).set(i, Integer.parseInt(numbers_as_string[i]));
            }
            this.row_index++;
            line = br.readLine();
        }
        // add all numbers into allocation matrix until empty line detected
        while (line != null && !line.trim().isEmpty());
    }

    public void printMatrix(String title) {
        out.printf("\nThe %s Matrix is...", title);
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
    public Matrix deepCopy() {
        Matrix deepCopy = new Matrix(processes, resources);
        for (int i = 0; i < processes; i++) {
            for (int j = 0; j < resources; j++) {
                deepCopy.data.get(i).set(j, this.data.get(i).get(j));
            }
        }
        return deepCopy;
    }

}
