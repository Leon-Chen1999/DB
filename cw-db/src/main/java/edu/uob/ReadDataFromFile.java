package edu.uob;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
public class ReadDataFromFile {
        public static void readData(String fileName) {
            try {
                File file = new File(fileName);
                Scanner scanner = new Scanner(file);
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    System.out.println(line);
                }
                scanner.close();
            } catch (FileNotFoundException e) {
                System.out.println("The file " + fileName + " could not be found.");
            }
        }
}
