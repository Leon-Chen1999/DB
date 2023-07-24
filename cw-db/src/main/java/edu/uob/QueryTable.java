package edu.uob;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Array;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class QueryTable extends Parser{
    public QueryTable(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }


//    public void readFile(String currentDatabase, String tableName){
//        String suffixPath = ".tab";
//        String storageFolderPath = "";
//        storageFolderPath = Paths.get("databases",currentDatabase,tableName+suffixPath).toAbsolutePath().toString();
////        File table = new File(storageFolderPath);
//        try {
//            File table = new File(storageFolderPath);
//            Scanner scanner = new Scanner(table);
//            while (scanner.hasNextLine()) {
//                String line = scanner.nextLine();
//                System.out.println(line);
//            }
//            scanner.close();
//        } catch (FileNotFoundException e) {
//            System.out.println("The file " + tableName + " could not be found.");
//        }
//    }

//    public void addCol(String currentDatabase, String currentTable, String[] tokens){
//        for (int i = 0; i < tokens.length; i++) {
////            Object ArrayList;
////            ArrayList<String> tmp = new ArrayList<String>();
//            table.add(new ArrayList<>());
//            table.get(i).add(tokens[i]);
//        }
//        System.out.println("42");
//        for (int i = 0; i < tokens.length; i++) {
//            System.out.println(tokens[i]);
//        }
//        System.out.println("123");
//        for (int i = 0; i < table.size(); i++) {
//            for (int j = 0; j < table.get(i).size(); j++) {
//                System.out.print(table.get(i).get(j));
//            }
//            System.out.println();
//
//        }
//    }

    public void doNothing(){
        ArrayList<String> tmp = new ArrayList<>();
        table.add(tmp);
        table.remove(table.size() - 1);
    }
    public void addRow(ArrayList<String> tokens){
        System.out.println("addrow : "+ table.size());
        System.out.println(tokens);
        System.out.println(table);
        ArrayList<String> tmp = new ArrayList<String>();
        int num = table.size();
        System.out.println(String.valueOf(num));
        tmp.add(String.valueOf(num));
        for (int i = 0; i < tokens.size(); i++) {
            tmp.add(tokens.get(i));
        }
//        table.get(table.size() - 2).add("\n");
        table.add(tmp);
        System.out.println("oooooo");
        System.out.println(table);
//        for (int i = 0; i < tokens.size(); i++) {
//            table.get(table.size() - 1).add(tokens.get(i) + "\t");
//        }
    }
    public void addColSingle(String tokens){
        //FIXME old
//        super.table.add(new ArrayList<String>());
//        super.table.get(table.size() - 1).add(tokens);
        //FIXME new
        table.get(0).add(tokens);
        System.out.println("59");
    }
    public boolean removeCol(String tokens){
        int num = -1;
//        table.get(0).contains(tokens);
        System.out.println(table.get(0).size());
//        System.out.println(table.get(0).get(0));
//        System.out.println(table.get(0).get(1));
//        System.out.println(table.get(0).get(2));
//        System.out.println(table.get(0).get(3));
//        System.out.println(table.get(0).get(3).equals("weather"));

        System.out.println("33333333333"+tokens);
//        System.out.println(table.get(0).get(0));
        for (int i = 0; i < table.size(); i++) {
            ArrayList<String> subArr = table.get(0);
            for (int j = 0; j < subArr.size(); j++) {
                String element = subArr.get(j);
                System.out.println("element: " + element);
                if (element.equals(tokens)) {
                    num = j;
//                    col = j;
//                    break;
                }
            }
            if (num != -1) {
                break;
            }
        }
        if (num == -1){
            return false;
        }
        System.out.println("num: " + num);
        System.out.println();
        for (int i = 0; i < table.size(); i++) {
            ArrayList<String> subArr = table.get(i);
            if (subArr.size() > num) {
                subArr.remove(num); // Remove the second element in each row
            }
        }
        return true;
    }
    
    public void printTable(){
        for (int i = 0; i < table.size(); i++) {
            for (int j = 0; j < table.get(0).size(); j++) {
                System.out.print(table.get(i).get(j) + "\t");
            }
            System.out.println();
        }
    }

    public String printToClient(){
        ArrayList<String > message = new ArrayList<String>();
        System.out.println("131");
        for (int i = 0; i < table.size(); i++) {
            String tmp = "";
            System.out.println("133");
            for (int j = 0; j < table.get(0).size(); j++) {
//                System.out.println("134");
//                System.out.println(table.get(i).get(j));
                tmp = tmp + table.get(i).get(j) + "\t";
//                System.out.println("tmp139: " + tmp);
//                System.out.println(table.get(i).get(j) + "\t");
            }
//            System.out.println("137tmp: " + tmp);
            message.add(tmp);
//            System.out.println();
        }
        clientMessage = "[OK]\n";
        System.out.println("141");
        for (int i = 0; i < message.size(); i++) {
            System.out.println(message.get(i));
        }
        for (int i = 0; i < message.size(); i++) {
            if (i < message.size() - 1){
                clientMessage = clientMessage + message.get(i) + "\n";
            }else {
                clientMessage = clientMessage + message.get(i);
            }

        }
        table.clear();
        return clientMessage;
    }

    public String printToClientSelect(ArrayList<String> tokens){
        clientMessage = "";
        ArrayList<String> message = new ArrayList<String>();
        ArrayList<ArrayList<String>> selectedColumns = new ArrayList<>();
        message.clear();
        // Create a new 2D ArrayList to store the selected columns
//        ArrayList<ArrayList<String>> selectedColumns = new ArrayList<>();
        for (ArrayList<String> row : table) {
            ArrayList<String> selectedRow = new ArrayList<>();
            for (int i = 0; i < tokens.size(); i++) {
                for (int j = 0; j < table.get(0).size(); j++) {
                    if (table.get(0).get(j).toLowerCase().equals(tokens.get(i).toLowerCase())){//存j
                        selectedRow.add(row.get(j));
                    }
                }
            }
            selectedColumns.add(selectedRow);
        }
        // give the message to message [] and can print it
        for (int i = 0; i < selectedColumns.size(); i++) {
            String tmp = "";
            System.out.println("174");
            for (int j = 0; j < selectedColumns.get(0).size(); j++) {
                tmp = tmp + selectedColumns.get(i).get(j) + "\t";
            }
            message.add(tmp);
        }
        clientMessage = "[OK]\n";
        for (int i = 0; i < table.size(); i++) {
            System.out.println(message.get(i));
        }
        System.out.println("247");
        for (int i = 0; i < message.size(); i++) {
            if (i < message.size() - 1){
                clientMessage = clientMessage + message.get(i) + "\n";
            }else {
                clientMessage = clientMessage + message.get(i);
            }

        }
        message.clear();
        for (ArrayList<String> innerList : selectedColumns) {
            innerList.clear(); // clear each inner list using the clear() method
        }
        table.clear();
        return clientMessage;
    }


    public String selectCondition(ArrayList<String> tokens){
        clientMessage = "";
        ArrayList<String> message = new ArrayList<String>();
        ArrayList<ArrayList<String>> selectedColumns = new ArrayList<>();
        message.clear();
        System.out.println("175");
        System.out.println(message);


        // Create a new 2D ArrayList to store the selected columns
//        ArrayList<ArrayList<String>> selectedColumns = new ArrayList<>();
        for (ArrayList<String> row : table) {
            ArrayList<String> selectedRow = new ArrayList<>();
            for (int i = 0; i < tokens.size(); i++) {
                for (int j = 0; j < table.get(0).size(); j++) {
                    if (table.get(0).get(j).equals(tokens.get(i))){//存j
                        selectedRow.add(row.get(j));
                    }
                }
            }
            selectedColumns.add(selectedRow);
        }

        ArrayList<ArrayList<String>> selectedRows = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            for (int j = 0; j < selectedColumns.get(0).size(); j++) {
                if (selectedColumns.get(0).get(j).equals(tokens.get(i))){//存行数
                    for (ArrayList<String> row : selectedColumns) {
                        if (row.contains(tokens.get(i))) {
                            selectedRows.add(row);
                        }
                    }
                }
            }
        }


        // give the message to message [] and can print it
        for (int i = 0; i < selectedRows.size(); i++) {
            String tmp = "";
            System.out.println("174");
            for (int j = 0; j < selectedRows.get(0).size(); j++) {
//                System.out.println("134");
//                System.out.println(table.get(i).get(j));
                tmp = tmp + selectedRows.get(i).get(j) + "\t";
//                System.out.println("tmp139: " + tmp);
//                System.out.println(table.get(i).get(j) + "\t");
            }
//            System.out.println("137tmp: " + tmp);
            message.add(tmp);
//            System.out.println();
        }
        clientMessage = "[OK]\n";
        System.out.println("141");
        System.out.println(message.size());
        System.out.println(table.size());
        for (int i = 0; i < table.size(); i++) {
            System.out.println(message.get(i));
        }
        System.out.println("247");
        for (int i = 0; i < message.size(); i++) {
            if (i < message.size() - 1){
                clientMessage = clientMessage + message.get(i) + "\n";
            }else {
                clientMessage = clientMessage + message.get(i);
            }

        }
        message.clear();
        for (ArrayList<String> innerList : selectedRows) {
            innerList.clear(); // clear each inner list using the clear() method
        }
        table.clear();
        return clientMessage;
    }


}
