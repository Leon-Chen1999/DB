package edu.uob;

import java.io.IOException;
import java.util.ArrayList;

public class SelectParser extends Parser{


    public SelectParser(String currentDatabase, String currentTable, ArrayList<ArrayList<String>> table, ArrayList<ArrayList<String>> table2) {
        super(currentDatabase, currentTable, table, table2);
    }

    public void selectTable(ArrayList<String> tokens, Parser parser) throws IOException {
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after SELECT";
            return;
        }
        //TODO create a small function from this
        int fromNum = 0;
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).toUpperCase().equals("FROM")){
                fromNum = i;
                break;
            }
        }
        ArrayList<String> tmp = new ArrayList<>();
        int j = 0;
        for (int i = 0; i < fromNum; i++) {
            tmp.add(j++, tokens.get(i));
        }
        //TODO create a small function to this
        if(!isWildAttribList(tmp)){
            clientMessage = "[ERROR] wrong WildAttribList";
            return;
        }
        for (int i = 0; i < fromNum; i++) {
            super.getRestToken(tokens);
        }
        //[TableName]
        //TODO TableName 后面都没写
        super.getRestToken(tokens);
        if(tokens.size() == 0){
            clientMessage = "[ERROR] need something after FROM ";
            return;
        }
        if(!checkTableExist(currentDatabase,tokens.get(0))){
            clientMessage = "[ERROR] table doesn't exist";
            return;
        }
        if (tokens.size() == 2 && !tokens.get(1).toUpperCase().equals("WHERE")){
                super.currentTable = tokens.get(0);
                selectQuery(tmp);
        } else {//" WHERE " <Condition>
            super.currentTable = tokens.get(0);
            super.getRestToken(tokens);
            if(tokens.size() == 0){
                clientMessage = "[ERROR] need something after FROM ";
                return;
            }
            //WHERE
            if (!tokens.get(0).toUpperCase().equals("WHERE")) {
                clientMessage = "[ERROR] wrong syntax after table name";
                return;
            }
            super.getRestToken(tokens);
            if(tokens.size() == 0){
                clientMessage = "[ERROR] need something after WHERE ";
                return;
            }
            if (checkLastToken(tokens)){
                tokens.remove(tokens.size()-1);
                //TODO
                selectQueryWhere(tmp,tokens);
            }else {
                clientMessage = "[ERROR] last is not ;";
            }
        }
    }

    public void selectQuery(ArrayList<String> tmp) throws IOException {
        QueryTable queryTable = readFile1(currentDatabase,currentTable,table);
        if (tmp.size() == 1 && tmp.get(0).equals("*")){
            System.out.println("122");
            System.out.println(clientMessage);
            clientMessage =  queryTable.printToClient();
            System.out.println("125");
            System.out.println(clientMessage);
        }else {
            System.out.println("128");
            queryTable.printToClientSelect(tmp);

        }
    }
    public boolean selectQueryWhere(ArrayList<String> tmp, ArrayList<String> tokens) throws IOException {
        System.out.println("136");
        QueryTable queryTable = readFile1(currentDatabase,currentTable,table);
        QueryTable queryTable2 = readFile1(currentDatabase,currentTable,table2);
        queryTable2.doNothing();
//        for (int i = 0; i < table.size(); i++) {
//            table2.add(table.get(i));
//        }
        System.out.println("98");
        System.out.println(table);
        System.out.println(table2);
        if (tmp.size() == 1 && tmp.get(0).equals("*")){
            if ((isCondition(tokens))){
                System.out.println("122");
                System.out.println(clientMessage);
                clientMessage =  queryTable.printToClient();
                System.out.println("125");
                System.out.println(clientMessage);
            }else {
                clientMessage = "[ERROR] wrong condition";
                return false;
            }

        }else {
            if ((isCondition(tokens))){
                System.out.println("151");
                System.out.println(clientMessage);
                System.out.println("153");
                clientMessage = queryTable.printToClientSelect(tmp);
//                clientMessage =  queryTable.printToClient();
                System.out.println("154");
                System.out.println(clientMessage);
            }
//            else {
//                clientMessage = "[ERROR] wrong condition";
//                return false;
//            }



        }
        return false;
    }
}
