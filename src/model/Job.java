package model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import static javax.persistence.CascadeType.ALL;

import javax.persistence.Column;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Temporal;
import javax.persistence.Transient;

@Entity
@Table(name = "jobs")
//@NamedQueries({
//    @NamedQuery(name = "Job.findAll", query = "SELECT j FROM Job j")
//})
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
    String area;

    @Column()
    String foundAt;

    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL)
    private List<Field> fields;

    //constructors
    public Job(String title, String company, URL url, Date postDate, String area,
            String foundAt, ArrayList<Field> fields) {
        this.title = title;
        this.company = company;
        this.url = url.toString();
        this.postDate = postDate;
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
        // add field if its not already there
        boolean alreadycontains = false;
        for (Field field : fields) {
            if (field.getField().equals(newField.getField())) {
                alreadycontains = true;
            }
        }
        if (alreadycontains == false) {
            fields.add(newField);
            //System.out.println("added: " + newField.getField());
        }

    }

    public void removeField(String fieldName) {
        // remove field if you find it there
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getField().equals(fieldName)) {
                fields.remove(i);
                //System.out.println("removed: " + fieldName);
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
