import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;

/*
 * Created on Fri Sep 27 2019
 *
 * Copyright (c) 2019 - Toni Heinonen
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
public class CSVtoSQL {

    // Used for checking value types
    private static final int STRING = 0;    // Type is a string
    private static final int INT = 1;       // Type is an int
    private static final int DOUBLE = 2;    // Type is an double

    // Options provided by the user
    private static String fileToRead;   // Name of the file to convert
    private static String separator;    // Separator which separates the values
    private static String tableName;    // Name to set for the table

    // Data contains all the values from csv
    private static ArrayList<ArrayList<String>> data = new ArrayList<>();
    // Titles to use when making a table
    private static ArrayList<String> titles = new ArrayList<>();
    
    /**
     * Runs the program using 4 different methods.
     * @param args file to open
     */
    public static void main( String[] args ) {
        // File name can be passed as an argument
        checkArguments(args);

        // Get the table name from user
        getRequiredInput();

        // Read data from the csv file
        readStudentsFromCSV();

        // Create sql file from data
        convertToSQL();
    }
    
    /** 
     * Checks if player wants to open a specific file or see the help document.
     * @param args command provided by the user
     */
    private static void checkArguments(String[] args) {
        fileToRead = "File.csv";    // Default file name

        // Read first argument
        if (args.length != 0) {
            String arg = args[0];
            if (arg.equals("-h") || arg.equals("--help")) {
                printHelpMessage();     // Show help message
                System.exit(0);
            } else {
                fileToRead = args[0];   // Open provided file name
            }
        }

        // Show user that file read has begun
        System.out.println("Reading file: " + fileToRead);

        // Check if file exists
        File file = new File(fileToRead);
        if (!file.exists())
            throw new java.lang.Error("File does not exist!");
    }

    /**
     * Shows manual when user provides -h or --help in console argument.
     */
    private static void printHelpMessage() {
        String str =
        "\n-- Welcome to the CSV to SQL conversion tool! --\n" +
        "\nSelecting file:\n" +
        "   1. Place your file in this same directory\n" +
        "   2. You can select which file to open by providing it as an\n" +
        "      argument in console command 'java'\n" +
        "      (e. 'java CSVtoSQL Example.csv')\n" +
        "   3. If you don't provide any arguments, default file is opened\n" +
        "      (File.csv)\n" +
        "\nSelecting options:\n" +
        "   1. When you run the program, it asks you to give 2 arguments\n" +
        "   2. First provide the name you want for the SQL table\n" +
        "      (e. 'MyTable')\n" +
        "   3. Secondly provide the separator which separates the\n" +
        "      values in your CSV file\n" + 
        "      (e. ',')\n" +
        "\nExample CSV file structure:\n" +
        "   studentID,name,age,hometown\n" +
        "   1803234,Jim Jones,26,New York\n" +
        "   1803424,Sara Carrey,55,Chicago\n" +
        "\nNOTE: Provide arguments without ''\n";
        System.out.println(str);
    }

    /**
     * Gets input from the user.
     * 
     * User provides sql table name and separator
     * which separates the values in the csv file.
     */
    private static void getRequiredInput() {
        Scanner input = new Scanner(System.in, "utf-8");

        // Get sql table name from the user
        System.out.println("Give table name: (default = 'NewTable')");
        tableName = input.nextLine();
        // If user input nothing, use default name
        if (tableName.equals(""))
            tableName = "NewTable";

        // Get separator, which separates values in the csv file
        System.out.println("Give value separator: (default = ',')");
        separator = input.nextLine();
        // If user input nothing, use default separator
        if (separator.equals(""))
            separator = ",";

        input.close();
    }

    /**
     * Reads each line from csv file. 
     * 
     * Saves the values to the initialized variables.
     */
    private static void readStudentsFromCSV() {
        Path pathToFile = Paths.get(fileToRead);    // Get the path to the file

        // Read file with Bufferedreader
        try (BufferedReader br = Files.newBufferedReader(pathToFile,
                        StandardCharsets.UTF_8)) {

            String line = br.readLine();    // Read line from file

            // Check if there is no lines
            if (line == null) {
                throw new java.lang.Error("File is empty!");
            } else {
                line = line.replaceFirst("^\uFEFF", "");    // Remove UTF-8 BOM
            }

            int i = 0;                      // Index to add data
            boolean topicsReceived = false; // Topics have not been received

            // Loop until all the lines are read
            while (line != null) {

                // Get all the values from the line, splitting them using comma
                String[] attributes = line.split(separator);

                // First line contains the topics
                if (!topicsReceived) {

                    for (String s : attributes) {
                        titles.add(s);
                    }

                    topicsReceived = true;
                } else {
                    // Create new array list for each line
                    data.add(new ArrayList<String>());

                    // Add values to the created array list
                    for (String s : attributes) {
                        data.get(i).add(s);
                    }

                    i++;
                }

                // Read next line, if there is non then it's null
                line = br.readLine();
            }

        } catch (IOException ioe) {
            ioe.printStackTrace();  // Catch any errors
        }
    }

    /**
     * Converts attained data to a sql file. 
     */
    private static void convertToSQL() {
        try {
            // Inform user that conversion has started
            System.out.println("Converting to SQL...");

            // Create file 
            FileWriter fstream = new FileWriter(tableName + ".sql",
                                                StandardCharsets.UTF_8);
            BufferedWriter writer = new BufferedWriter(fstream);

            // Retrieve amount of lines and values
            int lineAmount = data.size();
            int valueAmount = data.get(0).size();
            // Initialize arrays which holds value's types and names
            int[] valueTypes = new int[valueAmount];
            String[] valueNames = new String[valueAmount];

            // Retrieve data types and initializes required names for sql
            for (int i = 0; i < valueAmount; i++) {
                String s = data.get(0).get(i);

                if (isInt(s)) {
                    valueTypes[i] = INT;
                    valueNames[i] = "INTEGER";
                } else if (isDouble(s)) {
                    valueTypes[i] = DOUBLE;
                    valueNames[i] = "DECIMAL(6,6)";
                } else {
                    valueTypes[i] = STRING;
                    valueNames[i] = "VARCHAR(30)";
                }
            }

            // Add comment to the start of the file
            String str = "--" + tableName + "\n" +
            // Start creating the table
            "CREATE TABLE " + tableName + " (\n";
            
            // Write all the values to the table
            for (int i = 0; i < valueAmount; i++) {
                str += "    ";
                str += titles.get(i) + " " + valueNames[i] + ",\n";
            }

            // Use the first value as an primary key
            str += "    PRIMARY KEY (" + titles.get(0) + ")\n" +
                    ");\n\n" + // Close the table creation
                    "--Data\n"; // Add comment before data values
            
            // Insert all the attained data and convert it to a style
            // which sql requires
            for (int x = 0; x < lineAmount; x++) {
                str += "INSERT INTO " + tableName + " VALUES (";
                
                for (int i = 0; i < valueAmount; i++) {
                    String value = data.get(x).get(i);
                    if (valueTypes[i] == STRING)
                        str += "'" + value + "',";
                    else 
                        str += value + ",";
                }
                
                 // Remove ',' from the last value
                str = str.substring(0, str.length()-1);
                // Close the bracket and add line break
                str += ");\n";
            }

            // Remove empty line from the end
            str = str.substring(0, str.length()-1);

            writer.write(str);  // Write the created string
            
            writer.close();     // Close the output stream

            // Inform the user that conversion is ready
            System.out.println("Conversion ready!\n" +
            "--File " + tableName + ".sql created--");
        } catch (Exception e) {//Catch exception if any
              System.err.println("Error: " + e.getMessage());
        }
    }
    
    /** 
     * Checks if the value is an integer
     * @param str value to check
     * @return boolean it is an integer
     */
    private static boolean isInt(String str) {
        // If try succeeds, it is an integer
        try {
            Integer.parseInt(str);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
    
    /** 
     * Checks if the value is an double
     * @param str value to check
     * @return boolean it is an double
     */
    private static boolean isDouble(String str) {
        // If try succeeds, it is an double
        try {
            Double.parseDouble(str);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }
}
