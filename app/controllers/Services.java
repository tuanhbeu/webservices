package controllers;

import models.Keyword;
import play.Play;
import play.db.jpa.JPA;
import play.mvc.Controller;

import java.sql.*;
import java.util.List;

/**
 * Created by TuAnh on 2/25/14.
 */
public class Services extends Controller {

    // Get Clicks by Content
    // Username & Password
    // Content = Keyword
    // Day = yyyy-MM-dd
    public static void getClicksbyContent(String username, String password, String frmDate, String endDate) throws SQLException, ClassNotFoundException {
        if (username.equalsIgnoreCase("VioletHill") && password.equalsIgnoreCase("vi0letH1ll")) {
            List<Keyword> keywords = Keyword.all().fetch();
            if (keywords == null) {
                return;
            }
            String contents = "";
            for (Keyword keyword:keywords) {
                contents += ",'" + keyword.keyword + "'";
            }
            contents = contents.substring(1);
            String sqlQuery = "select * from sms_logs where content in (" + contents + ") and inserted_date between '" + frmDate + " 00:00:00'  and '" + endDate + " 23:59:59'" ;

            Connection conn = null;
            Statement statement = null;
            ResultSet resultSet = null;

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(Play.configuration.getProperty("dbsrv104"));
            statement = conn.createStatement();

            resultSet = statement.executeQuery(sqlQuery);
            String result = "";
            while (resultSet.next()) {
                result += resultSet.getString("sender") + "|" + resultSet.getString("receiver") + "|" + resultSet.getString("content") + "|" + resultSet.getString("inserted_date") + "\n";
            }
            renderText(result);
        } else {
            renderText("May lua bo a???");
        }
    }
}
