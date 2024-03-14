/**
 * Author: Josh Dejeu
 * Course: CSC360/660 Spring 2024
 * Project Name and Title: Project #5: Implementation of Banker's Algorithm
 * Due Date: Mar 18, 2024 11:59 PM
 * Instructor: Siming Liu
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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

import static java.lang.System.out;

public class Matrix {
    public static final String underlineStart = "\u001B[4m";
    public static final String underlineEnd = "\u001B[0m";
    public static int processes = 0; // aka Rows
    public static int resources = 0; // aka Columns
    public ArrayList<ArrayList<Integer>> data;

    // Constructor
    public Matrix(int rows, int columns) {
        processes = rows;
        resources = columns;
        this.data = createDynamicMatrix(processes, resources);
    }

    /**
     * Subtracts two matrices given by the formula C = (A - B)
     * and returns the difference. The matrices must be the same size.
     *
     * @param m_max        Maximum number of resources all process can use
     * @param m_allocation Initially allocated resources of all processes
     * @return Matrix
     * @title Difference of Matrices
     */
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

    /**
     * Creates an n x m matrix where n = rows and m = columns.
     * This matrix can change in size unlike the standard
     * java Arrays.
     *
     * @param rows    The number of rows a matrix will have
     * @param columns The number of columns a matrix will have
     * @return ArrayList<ArrayList < Integer>> A 2D Array
     * @title Create Dynamic matrix
     */
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

    /**
     * Returns a deep copied matrix of {@code original}.
     *
     * @return Matrix
     * @title Deep copy
     */
    public static Matrix deepCopy(Matrix original) {
        Matrix deepCopy = new Matrix(processes, resources);
        for (int i = 0; i < processes; i++) {
            for (int j = 0; j < resources; j++) {
                deepCopy.data.get(i).set(j, original.data.get(i).get(j));
            }
        }
        return deepCopy;
    }

    /**
     * Continues reading from wherever {@code br} is at and attempts to
     * parse each line. The instance that called this method will have
     * its resources at each process be updated.
     *
     * @param br   A BufferedReader that reads from a txt file
     * @param line The current line of text being read
     * @throws IOException           Signals some sort of I/O error
     * @throws NumberFormatException Application attempted to convert string
     * @apiNote An error will occur if the txt file is formatted
     * incorrectly such as having an extra column or row.
     * @title Populate Matrix From Text
     */
    public void populateMatrixFromTxt(BufferedReader br, String line) throws IOException, NumberFormatException {
        int row_index = 0;
        do {
            // split the string by " " and ":"
            String[] numbers_as_string = line.split(" ");
            // parse each string element and add it to the array
            for (int i = 0; i < resources; i++) {
                this.data.get(row_index).set(i, Integer.parseInt(numbers_as_string[i]));
            }
            row_index++;
            line = br.readLine();
        }
        // add all numbers into allocation matrix until empty line detected
        while (line != null && !line.trim().isEmpty());
    }


    // sets current object matrix size to whatever is currently statically stored in "resources" and "processes"

    /**
     * Prints the values of the Matrix instance that called this method.
     * Shows the values in the format where rows = processes and cols =
     * resources. Each resource is displayed with an ASCII character
     * beginning at 'A' and incrementing for every resource.
     *
     * @param title The title corresponding to the matrix displaying
     * @title Print Matrix
     */
    public void printMatrix(String title) {
        out.printf("\nThe %s Matrix is...", title);
        out.print("\n   ");
        for (int i = 'A'; i < resources + 'A'; i++) {
            out.print(underlineStart + (char) i + underlineEnd + " ");
        }
        out.println();
        for (int i = 0; i < this.data.size(); i++) {
            out.print(i + ": ");
            for (int j = 0; j < this.data.get(i).size(); j++) {
                out.print(this.data.get(i).get(j) + " ");
            }
            out.println();
        }
    }

    /**
     * Resizes current matrix instance size using the static processes
     * and resources. The instance will be a new Matrix of size n x m
     * where n = processes and m = resources. All the data within
     * the matrix will be set to -1.
     *
     * @return Matrix
     * @apiNote Can be used as a chain method
     * e.g. matrix.updateMatrixSize().somethingElse()
     * @title Update Matrix Size
     */
    public Matrix updateMatrixSize() {
        this.data = new Matrix(processes, resources).data;
        return this;
    }
}
