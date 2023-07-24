package edu.uob;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;

public class UseParser extends Parser{


    public UseParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }

    public boolean useDatabase(ArrayList<String> tokens, Parser parser){
        super.getRestToken(tokens);
        if (tokens.size() != 0 && checkLastToken(tokens)) {
            //TODO use database
            if (setDatabaseName(tokens.get(0))){
                clientMessage = "[OK] databaseName: " + getDatabaseName();
                System.out.println("rrrrrrr");
                System.out.println(currentDatabase);
                System.out.println(super.currentDatabase);
                parser.currentDatabase = getDatabaseName();
                System.out.println(currentDatabase);
                System.out.println(super.currentDatabase);
                return true;
            }else {
                return false;
            }
//            setDatabaseName(tokens.get(0));

//            System.out.println("databaseName : " + getDatabaseName());
        }else {
            //FIXME show the information on the client not in the server! how?
            clientMessage = "[ERROR] wrong syntax";
            System.out.println("wrong syntax");
            return false;
        }
    }

    public String getDatabaseName(){
        System.out.println("ggggggggggggggggg");
        System.out.println(currentDatabase);
        return currentDatabase;
    }
    public boolean setDatabaseName(String databaseName){
        storageFolderPath = Paths.get("databases", databaseName).toAbsolutePath().toString();
        File databaseDrop = new File(storageFolderPath);
        if (databaseDrop.exists()) {
            System.out.println("pppppppppppppp");
            System.out.println(super.currentDatabase);
            super.currentDatabase = databaseName;
            System.out.println("qqqqqqqqqq");
            System.out.println(super.currentDatabase);
            System.out.println(currentDatabase);
            return true;
        }else {
            clientMessage = "[ERROR] wrong database or database is not exist";
            return false;
        }

    }
}
