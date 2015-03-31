package controllers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import play.*;
import play.mvc.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void softwingsRanking() throws SQLException, ClassNotFoundException {
        ResultSet usersScores = Services.getUsersScores("10", null, null, null);
        JsonObject top10 = new JsonObject();
        JsonArray top10User = new JsonArray();
        while (usersScores.next()) {
            JsonObject user = new JsonObject();
            user.addProperty("username", usersScores.getString("username"));
            user.addProperty("best_score", usersScores.getString("best_score"));
            user.addProperty("country", usersScores.getString("country"));
            user.addProperty("city", usersScores.getString("city"));
            user.addProperty("facebook_id", usersScores.getString("facebook_id"));

            top10User.add(user);
        }

        top10.add("top10", top10User);

        render(top10);
    }

    public static void softwingsDownloadLink () {
        render();
    }

}