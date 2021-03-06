import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

public class Main {

	public static void main(String[] args) throws Exception {
		Connection connect;

		// Setup the connection with the DB
		connect = DriverManager.getConnection("jdbc:sqlserver://localhost\\Supermarkt:1433;user=auser;password=123456");
		// jdbc:sqlserver://[serverName[\instanceName][:portNumber]][;property=value[;property=value]]

		// Transaction Isolation Level (sonst heult er rum)
		// Serializable nie in der Realität (schlechte Performance)
		connect.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);
		connect.setAutoCommit(false);

		setUpDatabase(connect);
		synchronized (System.out) {
			new Thread(new B()).start();
			
			Statement s = connect.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			ausgabe(s, "SELECT 'A' as Thread, * " + "FROM Konto Kt");

			ResultSet rs = s.executeQuery("SELECT Name FROM Konto WHERE Saldo______ >= 50");
			ArrayList<String> leuteMitGeld;
			String list = "(";
			String sep = "";
			while (rs.next()) {
				list += sep + "'" + rs.getString("Name") + "'";
				sep = ",";
			}
			list += ")";
			System.out.println("Liste:" + list);
			
			System.out.notify();
			System.out.wait();
			
			s.execute("UPDATE Konto SET Saldo______ -= 50 WHERE Name IN " + list);
			ausgabe(s, "SELECT 'A' as Thread, * " + "FROM Konto Kt");
		}
	}

	public static void setUpDatabase(Connection c) throws Exception {
		Statement s = c.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

		try {
			s.execute("DROP TABLE Konto");
		} catch (Exception e) {
			System.out.println("dicker Patzer!");
			e.printStackTrace();
		}
		
		s.execute(
				"CREATE TABLE Konto(Kontonummer int primary key, Name VARCHAR(255), Saldo______ decimal(10,2), CHECK(Saldo______ >= 0))");
		s.execute(
				"INSERT INTO Konto(Kontonummer, Name, Saldo______) VALUES (1, 'Dividenden', 1200.00), (2, 'Enking', 200), (3, 'Peters', 3.56)");

		s.getConnection().commit();
	}

	public static void ausgabe(Statement s, String query) throws SQLException {
		synchronized (System.out) {
			System.out.println(query);
			ResultSet rs = s.executeQuery(query);

			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
				System.out.print(rs.getMetaData().getColumnName(i) + "   ");
			}

			System.out.println();

			while (rs.next()) {
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					int le = rs.getMetaData().getColumnName(i).length();
					String string;
					if (rs.getString(i).length() > le) {
						string = rs.getString(i).substring(0, le - 3) + "..." + "   ";
					} else {
						int lp = le - rs.getString(i).length();
						string = rs.getString(i) + padRight(" ", lp + 3);
					}
					System.out.print(string);
				}
				System.out.println();
			}
		}
	}

	public static String padRight(String s, int n) {
		return String.format("%1$-" + n + "s", s);
	}

}
