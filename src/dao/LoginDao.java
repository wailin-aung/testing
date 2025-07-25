package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import util.DatabaseConnection;

public class LoginDao {
	public static boolean check_credential(String user, String psw) throws SQLException {
		Connection connection = null;
		connection = DatabaseConnection.getConnection();
		if (connection != null) {
			System.out.println("Connection Successful!");
			System.out.println(user);
			String sql = "select * from users where name like ? and psw like ?";
			PreparedStatement stmt = connection.prepareStatement(sql);
			stmt.setString(1, user);
			stmt.setString(2, psw);
			ResultSet rs = stmt.executeQuery();
			if (rs.next()) {
				return true;
			}
		}
		return false;
		
	}
}

