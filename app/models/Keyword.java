package models;

import play.db.jpa.Model;

import javax.persistence.Entity;

/**
 * Created by TuAnh on 2/26/14.
 */

@Entity
public class Keyword extends Model {
    public String keyword;
    public String shortCode;
}
