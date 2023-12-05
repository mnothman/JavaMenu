import java.sql.*;
import java.util.Scanner;

class DatabaseManager {
    private static Connection connectToDatabase() throws SQLException {
        DriverManager.registerDriver(new oracle.jdbc.driver.OracleDriver());
        return DriverManager.getConnection( ""); //put url, user, password from oracle
    }

    public static void main(String[] args) {
        try (Scanner scanner = new Scanner(System.in);
             Connection db = connectToDatabase()) {
            int choice;
            do {
                printMenu();
                System.out.print("Enter choice: ");
                choice = scanner.nextInt();
                executeChoice(choice, db);
            } while (choice != 5);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void printMenu() {
        System.out.println("1) Insert");
        System.out.println("2) Delete");
        System.out.println("3) Update");
        System.out.println("4) View");
        System.out.println("5) Quit");
    }

    private static void executeChoice(int choice, Connection db) {
        try {
            switch (choice) {
                case 1:
                    insert(db);
                    break;
                case 2:
                    delete(db);
                    break;
                case 3:
                    update(db);
                    break;
                case 4:
                    view(db);
                    break;
                case 5:
                    System.out.println("Exiting");
                    break;
                default:
                    System.out.println("Please choose 1 through 5.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("An error occurred: " + e.getMessage());
        }
    }

    private static void insert(Connection db) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try (Statement st = db.createStatement()) {
            System.out.print("Enter First Name: ");
            String name_tableone = scanner.nextLine().toUpperCase();
            System.out.print("Enter Second Name: ");
            String name_tabletwo = scanner.nextLine().toUpperCase();
            System.out.print("Enter key: ");
            int key = scanner.nextInt();
            ResultSet rsCheck = st.executeQuery(String.format("SELECT * FROM declaration1001 WHERE name_tableone = '%s'", name_tableone));
            if (!rsCheck.next()) {
                st.executeUpdate(String.format("INSERT INTO declaration1001 values (rand_rand_seq.nextval, '%s')", name_tableone));
                st.executeUpdate(String.format("INSERT INTO declaration2002 values ('%s', (SELECT id_tableone FROM declaration1001 WHERE name_tableone = '%s'), %d)", name_tabletwo, name_tableone, key));
            } else {
                st.executeUpdate(String.format("INSERT INTO declaration2002 values ('%s', (SELECT id_tableone FROM declaration1001 WHERE name_tableone = '%s'), %d)", name_tabletwo, name_tableone, key));
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private static void delete(Connection db) {
        Scanner scanner = new Scanner(System.in);
        try (Statement st = db.createStatement()) {
            System.out.println("Key to delete: ");
            int key = scanner.nextInt();
            ResultSet foreignSet = st.executeQuery("SELECT id_tableone FROM declaration2002 WHERE key = " + key);
            if (foreignSet.next()) {
                int foreignKey = foreignSet.getInt(1);
                String lineRecord = "DELETE FROM declaration2002 WHERE key = " + key;
                st.execute(lineRecord);
                ResultSet rsCheck = st.executeQuery("SELECT id_tableone FROM declaration2002 WHERE id_tableone = " + foreignKey);
                if (!rsCheck.next()) {
                    st.execute("DELETE FROM declaration1001 WHERE id_tableone = " + foreignKey);
                }
            } else {
                System.out.println("Record not found.");
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private static void update(Connection db) throws Exception {
        Scanner scanner = new Scanner(System.in);
        try (Statement st = db.createStatement()) {
            System.out.println("WHICH NAME_TABLEONE WOULD YOU LIKE TO CHANGE: ");
            String id_tableone = scanner.nextLine();
            System.out.println("UPDATE TO: ");
            String newName = scanner.nextLine();
            String line = "UPDATE declaration1001 SET name_tableone = ? WHERE id_tableone = (SELECT id_tableone FROM declaration1001 WHERE name_tableone = ?)";
            try (PreparedStatement preparedStatement = db.prepareStatement(line)) {
                preparedStatement.setString(1, newName.toUpperCase());
                preparedStatement.setString(2, id_tableone.toUpperCase());
                preparedStatement.executeUpdate();
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }

    private static void view(Connection db) throws Exception {
        try (Statement st = db.createStatement()) {
            ResultSet rs = st.executeQuery("SELECT name_tabletwo, name_tableone, key FROM declaration2002 INNER JOIN declaration1001 ON declaration2002.id_tableone = declaration1001.id_tableone");
            ResultSetMetaData metaData = rs.getMetaData();
            while (rs.next()) {
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    System.out.println(columnName + ": " + rs.getString(columnName));
                }
                System.out.println();
            }
        } catch (SQLException s) {
            s.printStackTrace();
        }
    }
}


