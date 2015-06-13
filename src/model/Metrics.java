package model;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;

@Entity
@Table(name = "metrics")
public class Metrics implements Serializable {

    //variables
    @Id
    @SequenceGenerator(name = "seq4", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq4")
    private Integer ID;

    @Column()
    @Temporal(javax.persistence.TemporalType.DATE)
    Date scanDate;

    @Column()
    private String crawler;

    @Column()
    private int allJobs;

    @Column()
    private int jobsInEnglish;

    @Column()
    private int duplicateJobs;

    @Column()
    private int exceptions;

    //constructors
    public Metrics() {

    }

    public Metrics(String crawler) {
        this.scanDate = new Date();
        this.crawler = crawler;
        this.allJobs = 0;
        this.jobsInEnglish = 0;
        this.duplicateJobs = 0;
        this.exceptions = 0;
    }

    //methods
    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public Date getDate() {
        return scanDate;
    }

    public void setDate(Date date) {
        this.scanDate = date;
    }

    public String getCrawler() {
        return crawler;
    }

    public void setCrawler(String crawler) {
        this.crawler = crawler;
    }

    public int getExceptions() {
        return exceptions;
    }

    public void setExceptions(int exceptions) {
        this.exceptions = exceptions;
    }

    public int getJobsInEnglish() {
        return jobsInEnglish;
    }

    public void setJobsInEnglish(int jobsInEnglish) {
        this.jobsInEnglish = jobsInEnglish;
    }

    public int getAllJobs() {
        return allJobs;
    }

    public void setAllJobs(int allJobs) {
        this.allJobs = allJobs;
    }

    public int getDuplicateJobs() {
        return duplicateJobs;
    }

    public void incrementJobs() {
        this.allJobs = this.allJobs + 1;
    }

    public void incrementJobsInEnglish() {
        this.jobsInEnglish = this.jobsInEnglish + 1;
    }

    public void incrementDuplicateJobs() {
        this.duplicateJobs = this.duplicateJobs + 1;
    }

    public void incrementExceptions() {
        this.exceptions = this.exceptions + 1;
    }

    public void setDuplicateJobs(int duplicateJobs) {
        this.duplicateJobs = duplicateJobs;
    }

    public void updateMetrics(Metrics toBeAdded) {
        this.setAllJobs(this.getAllJobs() + toBeAdded.getAllJobs());
        this.setJobsInEnglish(this.getJobsInEnglish() + toBeAdded.getJobsInEnglish());
        this.setDuplicateJobs(this.getDuplicateJobs() + toBeAdded.getDuplicateJobs());
        this.setExceptions(this.getExceptions() + toBeAdded.getExceptions());
    }

    @Override
    public String toString() {
        String nl = System.lineSeparator();
        String result = getCrawler() + " METRICS:" + nl
                + "Jobs in site: " + "\t" + getAllJobs() + nl
                + "Jobs in english: " + "\t" + getJobsInEnglish() + nl
                + "Duplicates: " + "\t" + getDuplicateJobs() + nl
                + "Exceptions: " + "\t" + getExceptions() + nl;

        return result;
    }

}
