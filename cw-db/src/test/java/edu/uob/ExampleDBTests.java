package edu.uob;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.swing.plaf.PanelUI;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

public class ExampleDBTests {

    private DBServer server;

    // Create a new server _before_ every @Test
    @BeforeEach
    public void setup() {
        server = new DBServer();
    }

    // Random name generator - useful for testing "bare earth" queries (i.e. where tables don't previously exist)
    private String generateRandomName()
    {
        String randomName = "";
        for(int i=0; i<10 ;i++) randomName += (char)( 97 + (Math.random() * 25.0));
        return randomName;
    }

    private String sendCommandToServer(String command) {
        // Try to send a command to the server - this call will timeout if it takes too long (in case the server enters an infinite loop)
        return assertTimeoutPreemptively(Duration.ofMillis(1000), () -> { return server.handleCommand(command);},
        "Server took too long to respond (probably stuck in an infinite loop)");
    }

    // A basic test that creates a database, creates a table, inserts some test data, then queries it.
    // It then checks the response to see that a couple of the entries in the table are returned as expected
    @Test
    public void testBasicCreateAndQuery() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Dave', 55, TRUE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Bob', 35, FALSE);");
        sendCommandToServer("INSERT INTO marks VALUES ('Clive', 20, FALSE);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "A valid query was made, however an [OK] tag was not returned");
        assertFalse(response.contains("[ERROR]"), "A valid query was made, however an [ERROR] tag was returned");
        assertTrue(response.contains("Steve"), "An attempt was made to add Steve to the table, but they were not returned by SELECT *");
        assertTrue(response.contains("Clive"), "An attempt was made to add Clive to the table, but they were not returned by SELECT *");
    }

    // A test to make sure that querying returns a valid ID (this test also implicitly checks the "==" condition)
    // (these IDs are used to create relations between tables, so it is essential that they work !)
    @Test
    public void testQueryID() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT id FROM marks WHERE name == 'Steve';");
        // Convert multi-lined responses into just a single line
        String singleLine = response.replace("\n"," ").trim();
        // Split the line on the space character
        String[] tokens = singleLine.split(" ");
        // Check that the very last token is a number (which should be the ID of the entry)
        String lastToken = tokens[tokens.length-1];
        try {
            Integer.parseInt(lastToken);
        } catch (NumberFormatException nfe) {
            fail("The last token returned by `SELECT id FROM marks WHERE name == 'Steve';` should have been an integer ID, but was " + lastToken);
        }
    }

    // A test to make sure that databases can be reopened after server restart
    @Test
    public void testTablePersistsAfterRestart() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        // Create a new server object
        server = new DBServer();
        sendCommandToServer("USE " + randomName + ";");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("Steve"), "Steve was added to a table and the server restarted - but Steve was not returned by SELECT *");
    }

    // Test to make sure that the [ERROR] tag is returned in the case of an error (and NOT the [OK] tag)
    @Test
    public void testForErrorTag() {
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("SELECT * FROM libraryfines;");
        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");
    }
    @Test
    public void testForCreateTag() {
        String randomName1 = generateRandomName();
        String randomName2 = generateRandomName();
        String randomName3 = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName1 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE " + randomName2 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE " + randomName3 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        String randomName4 = generateRandomName();
        response = sendCommandToServer("CREATE TABLE " + randomName4 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was returned");
        response = sendCommandToServer("USE " + randomName2 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was returned");
        response = sendCommandToServer("CREATE TABLE " + randomName4 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was returned");
//        response = sendCommandToServer("SELECT * FROM libraryfines;");
//        sendCommandToServer("USE " + randomName + ";");
//        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
//        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
////        String response = sendCommandToServer("SELECT * FROM libraryfines;");
//        assertTrue(response.contains("[ERROR]"), "An attempt was made to access a non-existent table, however an [ERROR] tag was not returned");
//        assertFalse(response.contains("[OK]"), "An attempt was made to access a non-existent table, however an [OK] tag was returned");
    }

    @Test
    public void testForCreateTableTag(){
        String randomName1 = generateRandomName();
        String randomName2 = generateRandomName();
        String randomName4 = generateRandomName();
        String randomName5 = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName1 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName2 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag not was returned");
        response = sendCommandToServer("USE " + randomName1 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag not was returned");
        response = sendCommandToServer("CREATE TABLE TEST;");
        assertTrue(response.contains("[OK]"), "[OK] tag not was returned");
        response = sendCommandToServer("SELECT * FROM test;");
        assertTrue(response.contains("[OK]"), "[OK] tag not was returned");
        response = sendCommandToServer("CREATE TABLE " + randomName4 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag not was returned");
        response = sendCommandToServer("CREATE TABLE " + randomName5 + "( name, sex, age)" + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag not was returned");
        response = sendCommandToServer("SELECT * FROM " + randomName5 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag not was returned");
        assertTrue(response.contains("ID"), "should print the title of the table");
        assertTrue(response.contains("name"), "should print the title of the table");
        assertTrue(response.contains("sex"), "should print the title of the table");
        assertTrue(response.contains("age"), "should print the title of the table");
    }

    @Test
    public void testCreateFailTag(){
        String randomName1 = generateRandomName();
        String randomName2 = generateRandomName();
        String randomName3 = generateRandomName();
        String randomName4 = generateRandomName();
        String randomName5 = generateRandomName();
        String randomName6 = generateRandomName();
        String randomName7 = generateRandomName();
        String randomName8 = generateRandomName();
        String response = sendCommandToServer("CREATE DATABASE " + randomName1);
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName5 + "(name, sex, age)" + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABAS db ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE db ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE " + randomName3 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE DATABASE " + randomName3 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName5 + "name, sex, age" + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("USE " + randomName3 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName2 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName2 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName2);
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLEE " + randomName2 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName4 + "( name, sex, age)" + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName4 + "( name, sex, age)" + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName5 + "name, sex, age" + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName6 + "name, )" + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName7 + "(name, )" + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("CREATE TABLE " + randomName8 + "()" + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

    }

    @Test
    public void testDropTag(){
        String randomName = generateRandomName();
        String randomName1 = generateRandomName();
        String randomName2 = generateRandomName();
        String randomName3 = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
//        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("DROP " );
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP TABLE marks " );
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP TABLE ;" );
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP TABLE marks ;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("DROP TABLE marks ;" );
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("CREATE TABLE "  + randomName1 + " (name, mark, pass);");
        sendCommandToServer("CREATE TABLE "  + randomName2 + " (name, mark, pass);");
        sendCommandToServer("CREATE TABLE "  + randomName3 + " (name, mark, pass);");
        response = sendCommandToServer("DROP DATABASE " + randomName);
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE " +  ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE " + randomName + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("USE " + randomName + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
    }

    @Test
    public void testDropDatabaseTag(){
        String randomName = generateRandomName();
        String randomName1 = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("CREATE DATABASE " + randomName1 + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        String response = sendCommandToServer("DROP DATABASE " + randomName1 + ";");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("USE " + randomName1 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
//        String response = sendCommandToServer("SELECT * FROM marks;");
//        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
//        sendCommandToServer("DROP TABLE marks ;");
//        response = sendCommandToServer("SELECT * FROM marks;");
//        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
    }

    @Test
    public void testDropErrorTag(){
        String randomName = generateRandomName();
        String randomName1 = generateRandomName();
        String randomName2 = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        String response = sendCommandToServer("DROP ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP SJI");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP TABLE");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE " + randomName);
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DROP DATABASE " + randomName + " s");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        //database is not exist
        response = sendCommandToServer("DROP DATABASE " + randomName1 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("DROP TABLE " + randomName1 + ";");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("USE " + randomName + ";");

//        sendCommandToServer("CREATE DATABASE " + randomName1 + ";");
//        sendCommandToServer("USE " + randomName + ";");
//        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
//        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
//        response = sendCommandToServer("DROP DATABASE " + randomName1 + ";");
//        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
//        response = sendCommandToServer("USE " + randomName1 + ";");
//        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
    }

    @Test
    public void testUseTag(){
        String randomName = generateRandomName();
        String randomName1 = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("CREATE DATABASE " + randomName1 + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("CREATE TABLE temp (name, mark, pass);");
        String response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("SELECT * FROM temp;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        sendCommandToServer("INSERT INTO marks VALUES ('Steve', 65, TRUE);");
        response = sendCommandToServer("USE " + randomName1);
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("USE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("USE " + randomName1 + ";");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM temp;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        response = sendCommandToServer("SELECT * FROM marks;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
    }

    @Test
    public void testAlterAddTag(){
        String randomName = generateRandomName();
        String response = sendCommandToServer("ALTER TABLE marks ADD sex;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        response = sendCommandToServer("ALTER TABLE marks ADD sex");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER AA;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("ALTER TABLE marks ADD sex;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE, 'male');");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ADD address;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");

    }

    @Test
    public void testAlterDropTag(){
        String randomName = generateRandomName();
        String randomName1 = generateRandomName();
        String randomName2 = generateRandomName();
        String randomName3 = generateRandomName();
        String response = sendCommandToServer("ALTER TABLE marks ADD sex;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        response = sendCommandToServer("ALTER TABLE marks DROP pass");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks DROP ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks DROP ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER AA;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("ALTER TABLE marks DROP ID;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks DROP pass;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks DROP pass;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("insert into marks values ('bob', 20);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("insert into marks values ('sam', 10);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("ALTER TABLE marks DROP mark;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");

    }

    @Test
    public void testInsertTag(){
        String randomName = generateRandomName();
        String response = sendCommandToServer("ALTER TABLE marks ADD sex;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE)");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE, 12);");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ();");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into marks values");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into marks values;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into marks ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into marks ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT into ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("INSERT ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");

    }

    @Test
    public void testSelectErrorTag(){
        String randomName = generateRandomName();
        String response = sendCommandToServer("ALTER TABLE marks ADD sex;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Clive'");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("SELECT * FROM marks WHERE name == ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WHERE name ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WHERE name ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WHERE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WHERE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WEREER ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("SELECT * FROM marks ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROM ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROMM ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * FROMM ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT @ ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT * ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT id ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("SELECT ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("SELECT * FROM marks WHERE name == Clive! ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

        response = sendCommandToServer("SELECT * FROM marks WHERE name == 'Clive';");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks ;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("SELECT ID FROM marks ;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WHERE (mark > 40 OR mark < 30);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WHERE mark > 40 OR mark < 30;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("SELECT * FROM marks WHERE (mark > 40 OR mark == 35 AND ID == 1);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");


    }

    @Test
    public void testSelect(){
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");

        sendCommandToServer("INSERT into marks values ('bob', 20, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob', 45, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob', 50, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob1', 55, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob1', 56, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob1', 57, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob2', 60, TRUE);");
        sendCommandToServer("INSERT into marks values ('bob3', 80, FALSE);");

        String response = sendCommandToServer("select * from marks where mark > 33.5;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        sendCommandToServer("select * from marks where mark >= 20.0;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        sendCommandToServer("select * from marks where mark != 39.5;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        sendCommandToServer("select * from marks where mark < 75;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        sendCommandToServer("select * from marks where mark <= 60;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        sendCommandToServer("select * from marks where pass LIKE FALSE;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        sendCommandToServer("select * from marks where name LIKE 'bob';");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");

    }

    @Test
    public void testDeleteErrorTag(){
        String randomName = generateRandomName();
        String response = sendCommandToServer("ALTER TABLE marks ADD sex;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob1', 55, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob2', 22, TRUE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob3', 23, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");

        response = sendCommandToServer("DELETE FROM marks WHERE mark == 55");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark == ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark == ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks123 ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");

    }

    @Test
    public void testDelete(){
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        String response = sendCommandToServer("INSERT into marks values ('bob', 20, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob', 45, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob', 50, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob1', 55, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('sally', 55, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('sally1', 55, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob1', 56, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob1', 57, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob2', 60, TRUE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("INSERT into marks values ('bob3', 80, FALSE);");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");

        response = sendCommandToServer("DELETE FROM marks WHERE mark < 40;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark > 79;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark >= 60;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark <= 50;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark == 55;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark != 56;");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark - 56;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("DELETE FROM marks WHERE mark LIKE 'sally';");
        assertTrue(response.contains("[OK]"), "[OK] tag was not returned");
//        assertTrue(response.contains("57"), "57 should contain");
        assertFalse(response.contains("56"), "56 does not contain");
    }

    @Test
    public void testJoin(){
        String randomName = generateRandomName();
        sendCommandToServer("CREATE DATABASE " + randomName + ";");
        sendCommandToServer("USE " + randomName + ";");
        sendCommandToServer("CREATE TABLE marks (name, mark, pass);");
        sendCommandToServer("INSERT into marks values ('bob', 20, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob', 45, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob', 50, FALSE);");
        sendCommandToServer("INSERT into marks values ('bob1', 55, FALSE);");
        sendCommandToServer("INSERT into marks values ('sally', 55, FALSE);");

        sendCommandToServer("CREATE TABLE coursework (task, submission);");
        sendCommandToServer("INSERT INTO coursework VALUES ('DB', 3);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 1);");
        sendCommandToServer("INSERT INTO coursework VALUES ('OXO', 4);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 2);");
        sendCommandToServer("INSERT INTO coursework VALUES ('STAG', 5);");

        String response = sendCommandToServer("JOIN coursework213 AND marks ON submission AND id;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ON submission AND id");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ON submission AND ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ON submission AND ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ON submission ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ON submission ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ON ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ON ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND marks ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework AND ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN coursework ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN ;");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");
        response = sendCommandToServer("JOIN ");
        assertTrue(response.contains("[ERROR]"), "[ERROR] tag was not returned");


        String response1 = sendCommandToServer("JOIN coursework AD marks ON submission AND id;");
        assertTrue(response1.contains("[ERROR]"), "[ERROR] tag was not returned");
        String response2 = sendCommandToServer("JOIN coursework AND marks or submission AND id;");
        assertTrue(response2.contains("[ERROR]"), "[ERROR] tag was not returned");
        String response3 = sendCommandToServer("JOIN coursework AND marks ON submission or id;");
        assertTrue(response3.contains("[ERROR]"), "[ERROR] tag was not returned");
        String response4 = sendCommandToServer("JOIN coursework AND marks ON submission AND id");
        assertTrue(response4.contains("[ERROR]"), "[ERROR] tag was not returned");
        String response5 = sendCommandToServer("JOIN coursework AND marks ON submission AND id;");
        assertTrue(response5.contains("[OK]"), "[OK] tag was not returned");

        String response11 = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'bob'");
        assertTrue(response11.contains("[ERROR]"), "not right sign");
        response11 = sendCommandToServer("UPDATE ");
        assertTrue(response11.contains("[ERROR]"), "not right sign");
        response11 = sendCommandToServer("UPDATE marks12");
        assertTrue(response11.contains("[ERROR]"), "not right sign");
        response11 = sendCommandToServer("UPDATE marks#");
        assertTrue(response11.contains("[ERROR]"), "not right sign");
        response11 = sendCommandToServer("UPDATE marks ");
        assertTrue(response11.contains("[ERROR]"), "not right sign");
        response11 = sendCommandToServer("UPDATE marks SET ");
        assertTrue(response11.contains("[ERROR]"), "not right sign");


        String response6 = sendCommandToServer("UPDATE marks SE mark = 38 WHERE name == 'bob';");
        assertTrue(response6.contains("[ERROR]"), "not SET");
        String response7 = sendCommandToServer("UPDATE marks SET mark , 38 WHERE name == 'bob';");
        assertTrue(response7.contains("[ERROR]"), "not Assign");
        String response8 = sendCommandToServer("UPDATE marks SET mark = * WHERE name == 'bob';");
        assertTrue(response8.contains("[ERROR]"), "not right sign");
        String response9 = sendCommandToServer("UPDATE marks SET mark = 38 WHE name == 'bob';");
        assertTrue(response9.contains("[ERROR]"), "not where");
        String response10 = sendCommandToServer("UPDATE marks SET mark = 38 WHERE name == 'bob';");
        assertTrue(response10.contains("[ERROR]"), "not right sign");

    }



}
