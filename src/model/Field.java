package model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

@Entity
@Table(name = "fields")

public class Field implements Serializable {//maybe serializable is not necessary?
    //variables

    @Id
    @SequenceGenerator(name = "seq2", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq2")
    private Integer ID;

    @ManyToOne()
    @JoinColumn(name = "jobID")
    private Job job;

    @Column()
    String field;

    //constructors
    public Field(String fieldName) {
        this.field = fieldName;
    }

    public Field() {

    }

    //methods
    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getField() {
        return field;
    }

    public void setField(String fieldName) {
        this.field = fieldName;
    }

    public Job getJob() {
        return job;
    }

    public void setJob(Job job) {
        this.job = job;
    }

    @Override
    public String toString() {
        return "JobID: " + getJob() + ", " + "Category name: " + getField();
    }

}
