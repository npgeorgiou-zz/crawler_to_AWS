package model;

import di.Config;
import di.DI;
import di.Filter;
import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;

@Entity
@Table(name = "jobs")

public class Job implements Serializable {

    @Id
    @SequenceGenerator(name = "seq", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq")
    private Integer ID;

    @Column()
    String title;

    @Column()
    String company;

    @Column()
    String url;

    @Column()
    @Temporal(javax.persistence.TemporalType.DATE)
    Date postDate;

    @Column()
    String text;

    @Column()
    int paidJob;

    @Column()
    String area;

    @Column()
    String foundAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<Field> fields;

    //constructors
    public Job(DI di, String title, String company, String url, Date postDate, String text, int paidJob, String area,
            String foundAt, ArrayList<Field> fields) {
        Config c = di.getConfig();
        Filter filter = di.getFilter();

        this.title = (c.getProp("filter_title").equals("true")) ? filter.homogeniseJobTitle(title) : title;
        this.company = (c.getProp("filter_company").equals("true")) ? filter.homogeniseCompanyName(company) : company;
        this.url = (c.getProp("filter_url").equals("true")) ? filter.homogeniseURL(url) : url;

        this.postDate = postDate;
        this.text = (c.getProp("full_job_text").equals("true")) ? text : "";
        this.paidJob = paidJob;
        this.area = area;
        this.foundAt = foundAt;
        this.fields = fields;
    }

    public Job() {
    }

    //methods
    //----------------------------------
    public List<Field> getFields() {
        return fields;
    }

    public void addField(Field newField) {
        // Add field if its not already there
        boolean alreadycontains = false;
        for (Field field : fields) {
            if (field.getField().equals(newField.getField())) {
                alreadycontains = true;
            }
        }
        if (alreadycontains == false) {
            fields.add(newField);
            // System.out.println("added: " + newField.getField());
        }

    }

    public void removeField(String fieldName) {
        // Remove field if you find it there
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getField().equals(fieldName)) {
                fields.remove(i);
            }
        }

    }

    //----------------------------------
    public int getID() {
        return ID;
    }

    public void setID(int id) {
        this.ID = id;
    }

    public String getFoundAt() {
        return foundAt;
    }

    public void setFoundAt(String foundAt) {
        this.foundAt = foundAt;
    }

    public String getJobCity() {
        return area;
    }

    public void setJobCity(String jobCity) {
        this.area = jobCity;
    }

    public int getPaidJob() {
        return paidJob;
    }

    public void setPaidJob(int paidJob) {
        this.paidJob = paidJob;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Date getPostDate() {
        return postDate;
    }

    public void setPostDate(Date postDate) {
        this.postDate = postDate;
    }

    @Override
    public String toString() {

        return "Job title: " + getTitle() + "\nAnnouncer: " + getCompany()
                + "\nLink: " + getUrl() + "\nPost Date: "
                + getPostDate() + "\nFields: " + getFields().toString();
    }

}
