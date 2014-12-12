package model;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.SequenceGenerator;

@Entity
@Table(name = "companies")

public class Company implements Serializable {
    //variables

    @Id
    @SequenceGenerator(name = "seq3", initialValue = 1, allocationSize = 1)
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "seq3")
    private Integer ID;

    @Column()
    String companyName;
    
//    @Column()
//    String logoPath;
    
    //constructors
    public Company(String companyName) {
        this.companyName = companyName;
//        this.logoPath = logoPath;
    }

    public Company() {

    }

    //methods

    public Integer getID() {
        return ID;
    }

    public void setID(Integer ID) {
        this.ID = ID;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

//    public String getLogoPath() {
//        return logoPath;
//    }
//
//    public void setLogoPath(String logoPath) {
//        this.logoPath = logoPath;
//    }


    @Override
    public String toString() {
        return "Name: " + getID();
    }

}
