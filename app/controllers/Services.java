package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import models.Keyword;
import play.Logger;
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

    public static void getCdr (String day) throws SQLException, ClassNotFoundException {
        String month = day.substring(0, 6);

        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(Play.configuration.getProperty("dbsrv92"));
        statement = conn.createStatement();

        String sqlQuery = "SELECT a.TimeStamp, a.ChargeResult, a.MSISDN, b.keyword, a.Cost \n" +
                "FROM \n" +
                "\t(\n" +
                "\t\tselect TimeStamp, ChargeResult, MSISDN, ProductID, Cost\n" +
                "\t\tfrom cdr_sync" + month + "\n" +
                "\t\twhere TimeStamp like '" + day +"%' and ChargeResult = 1\n" +
                "\t) as a\n" +
                "INNER JOIN\n" +
                "\t( \n" +
                "\t\tselect keyword, sdp_product_id\n" +
                "\t\tfrom sdp_keywords_matching\n" +
                "\t\twhere type = 'SUB' and keyword in ('DK VIO','DK VIO7', 'XSMB')\n" +
                "\t) as b\n" +
                "ON a.ProductID = right(b.sdp_product_id, 10);";

        resultSet = statement.executeQuery(sqlQuery);
        JsonObject result = new JsonObject();
        JsonArray resultArray = new JsonArray();
        while (resultSet.next()) {
            JsonObject row = new JsonObject();
            row.addProperty("TimeStamp", resultSet.getString("TimeStamp"));
            row.addProperty("ChargeResult", resultSet.getString("ChargeResult"));
            row.addProperty("MSISDN", resultSet.getString("MSISDN"));
            row.addProperty("keyword", resultSet.getString("keyword"));
            row.addProperty("Cost", resultSet.getString("Cost"));
            resultArray.add(row);
        }
        result.addProperty("errorCode", "0");
        result.add("result", resultArray);
        renderJSON(result.toString());
    }

    public static void softwingsInsertScore (String username, int score) throws SQLException, ClassNotFoundException {
        Logger.info("Insert Score: username = %s & score = %s", username, score);
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(Play.configuration.getProperty("vio1"));
        statement = conn.createStatement();


        String checkUsernameQuery = "select * from user where username = '" + username + "'";
        resultSet = statement.executeQuery(checkUsernameQuery);

        JsonObject result = new JsonObject();

        if (resultSet.next()) {
            String updateScoreQuery = "update user set best_score = " + score + " where username = '" + username + "'";
            statement.executeUpdate(updateScoreQuery);
            result.addProperty("result", "0");
            Logger.info("Username %s: does exist and updated best_score: %s", username, score);
        } else {
            String insertUserQuery = "insert into user (username, best_score) values ('" + username + "', " + score + ")";
            statement.executeUpdate(insertUserQuery);
            result.addProperty("result", "1");
            Logger.info("Username %s: is created and updated best_score: %s", username, score);
        }


        statement.close();
        resultSet.close();
        conn.close();

        // return JSON
        renderJSON(result);

    }

    public static void softwingsCheckUsername (String username) throws SQLException, ClassNotFoundException {
        Logger.info("Checking username: username = %s ", username);

        JsonObject result = new JsonObject();

        if (usernameDoesExist(username)) {
            result.addProperty("result", "1");
            Logger.info("Username %s: does exist", username);
        } else {
            result.addProperty("result", "0");
            Logger.info("Username %s: does not exist", username);
        }


        // return JSON
        // 0: user doesn't exist
        // 1: user exists
        renderJSON(result);

    }

    public static void softwingsInsertProfile (String username, String city, String country, String facebook_id) throws ClassNotFoundException, SQLException {
        Logger.info("Update Profile for username: username = %s , city = %s, country = %s, facebook_id = %s", username, city, country, facebook_id);
        JsonObject result = new JsonObject();

        if (usernameDoesExist(username)) {
            Connection conn = null;
            Statement statement = null;
            ResultSet resultSet = null;

            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection(Play.configuration.getProperty("vio1"));
            statement = conn.createStatement();

            String updateProfileQuery = "update  user set " +
                    "city = '" + city + "', " +
                    "country = '" + country + ", " +
                    "facebook_id = '" + facebook_id + " " +
                    "where username = '" + username + "'";
            statement.executeUpdate(updateProfileQuery);
            statement.close();
            resultSet.close();
            conn.close();
            result.addProperty("result", "0");
        } else  {
            result.addProperty("result", "200");
        }

        renderJSON(result);
    }


    private static boolean usernameDoesExist (String username) throws ClassNotFoundException, SQLException {
        Connection conn = null;
        Statement statement = null;
        ResultSet resultSet = null;

        Class.forName("com.mysql.jdbc.Driver");
        conn = DriverManager.getConnection(Play.configuration.getProperty("vio1"));
        statement = conn.createStatement();

        String checkUsernameQuery = "select * from user where username = '" + username + "'";
        resultSet = statement.executeQuery(checkUsernameQuery);

        if (resultSet.next()) {
            statement.close();
            resultSet.close();
            conn.close();
            return  true;
        } else {
            statement.close();
            resultSet.close();
            conn.close();
            return false;
        }
    }
}
