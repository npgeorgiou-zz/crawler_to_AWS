package dbUtils;

import model.Metrics;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import model.Job;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import model.Company;
import model.Field;

public class DatabaseUtils {

    //variables
    private static EntityManagerFactory emf;

    //construcor
    public DatabaseUtils() {
        emf = Persistence.createEntityManagerFactory("JOBINDEX_FINAL_3PU");
    }

    //methods
    public int deleteDuplicatesAndAddtoDB(ArrayList<Job> jobs) {
//        System.out.println("");
        int duplicateLinks = 0;
        if (jobs.isEmpty()) {
            return duplicateLinks;

        } else {//for each job in jobs, check for duplicate and add to db
//            System.out.println("have " + jobs.size());
            for (Iterator<Job> it = jobs.iterator(); it.hasNext();) {
                Job aJob = it.next();
                if (deleteDuplicatesTest(aJob)) {
                    it.remove();
                    duplicateLinks++;
                } else {
                    addToDatabaseTest(aJob);
                    Company c = new Company(aJob.getCompany());
                    addCompanyToDatabase(c);
                }
            }
//            System.out.println("now have " + jobs.size());
        }
        return duplicateLinks;
    }

    public boolean deleteDuplicatesTest(Job aJob) {
        boolean duplicate = false;

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        //check for duplicate urls, including urls that are the same apart from http/https:
        //https://leopharma.easycruit.com/vacancy/1286566/111414?iso=dk
        //http://leopharma.easycruit.com/vacancy/1286566/111414?iso=dk
        List<Job> result = em.createQuery("SELECT j FROM Job j WHERE j.url like :url")
                .setParameter("url", "%" + aJob.getUrl().replaceAll("https", "").replaceAll("http", "")).getResultList();
        if (!result.isEmpty()) {//if we found something
            duplicate = true;
        } else {//url check clean, check for title and company
            result = em.createQuery("SELECT j FROM Job j WHERE j.title = :title and j.company = :company")
                    .setParameter("title", aJob.getTitle()).setParameter("company", aJob.getCompany()).getResultList();
            if (!result.isEmpty()) {//if we found something
                //Some deletes will be wrong. E.g. NN or siemens have multiple positions with the same name. So:
                //Check URL format in big companies and try to get job id from url. e.g. split("id=")[1].split("&")
                //if numbers are different, then dont delete.
                duplicate = checkForSameCompanyAndTitleButDiffJobIDInURL(aJob.getUrl(), result);
            } else {
                //check one more time job title is the same with removing the -s. EG:
                //    Senior Java Developer - BESTSELLER IT
                //    Senior Java Developer BESTSELLER IT
                if (aJob.getTitle().contains("-")) {
                    String jobTitleWithoutDash = aJob.getTitle().replaceAll(" - ", "");
                    result = em.createQuery("SELECT j FROM Job j WHERE j.title = :title and j.company = :company")
                            .setParameter("title", jobTitleWithoutDash).setParameter("company", aJob.getCompany()).getResultList();

                    if (!result.isEmpty()) {//if we found something
                        duplicate = checkForSameCompanyAndTitleButDiffJobIDInURL(aJob.getUrl(), result);
                    }
                }
            }
        }
        transaction.commit();
        em.close();
        return duplicate;
    }

    private boolean checkForSameCompanyAndTitleButDiffJobIDInURL(String url, List<Job> result) {
        boolean duplicate = false;
        if (url.contains("novonordisk.com") || url.contains("oracle.taleo.net")
                || url.contains("au.dk/om/") || url.contains("saxo.easycruit.com")
                || url.contains("/find.ibm.jobs/") || url.contains("siemens.biz")
                || url.contains("www.dongenergy.com")) {

            //NovoNordisk format: http://www.novonordisk.com/careers/job_section/jp.asp?jobid=25257BR&joblng=uk&jobcnt=Denmark&type=1g&cid=JI-25257BR
            if (url.contains("novonordisk.com")) {
                //for each job that has same title and company in DB, check ID
                for (Job jobInDb : result) {
                    try {

                        String jobInDbID = jobInDb.getUrl().split("jobid=")[1].split("&")[0];
                        String jobInListID = url.split("jobid=")[1].split("&")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }

            }
            //Oracle format: https://oracle.taleo.net/careersection/2/jobdetail.ftl?job=60658
            if (url.contains("oracle.taleo.net")) {
                //for each job that has same title and company in DB, check ID
                for (Job jobInDb : result) {
                    try {

                        String jobInDbID = jobInDb.getUrl().split("job=")[1];
                        String jobInListID = url.split("job=")[1];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }
            //Aarhus University format: http://www.au.dk/om/stillinger/videnskabelige-stillinger/stillinger/Vacancy/show/695899/5285/
            if (url.contains("au.dk/om/")) {
                //for each job that has same title and company in DB, check ID
                for (Job jobInDb : result) {
                    try {

                        String jobInDbID = jobInDb.getUrl().split("/show/")[1].split("/")[0];
                        String jobInListID = url.split("/show/")[1].split("/")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }
            //Saxo Bank format: http://saxo.easycruit.com/vacancy/1271153/61376?iso=gb
            if (url.contains("saxo.easycruit.com")) {
                //for each job that has same title and company in DB, check ID
                for (Job jobInDb : result) {
                    try {

                        String jobInDbID = jobInDb.getUrl().split("/vacancy/")[1].split("/")[0];
                        String jobInListID = url.split("/vacancy/")[1].split("/")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //IBM format: https://jobs3.netmedia1.com/cp/find.ibm.jobs/M/Mobile_UI_UX_Consultant/GBS-0695978/job/
            if (url.contains("/find.ibm.jobs/")) {
                //for each job that has same title and company in DB, check ID
                for (Job jobInDb : result) {
                    try {

                        int i = jobInDb.getUrl().split("/").length - 2;
                        String jobInDbID = jobInDb.getUrl().split("/")[i];
                        int j = url.split("/").length - 2;
                        String jobInListID = url.split("/")[j];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //LEO Pharma http://leopharma.easycruit.com/vacancy/1288266/106451?iso=dk
            if (url.contains("leopharma.easycruit.com")) {
                //for each job that has same title and company in DB, check ID
                for (Job jobInDb : result) {
                    try {

                        int i = jobInDb.getUrl().split("/").length - 2;
                        String jobInDbID = jobInDb.getUrl().split("/")[i];
                        int j = url.split("/").length - 2;
                        String jobInListID = url.split("/")[j];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //some siemens format: https://jobsearch.siemens.biz/career?company=Siemens&career_job_req_id=171367&career_ns=job_listing&jobPipeline=moveon.dk&navBarLevel=JOB%5fSEARCH&rcm%5fsite%5flocale=da%5fDK&selected_lang=en_GB
            //other siemens format: https://jobsearch.siemens.biz/sfcareer/jobreqcareer?jobId=174696&company=Siemens&username=
            if (url.contains("siemens.biz")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("d=")[1].split("&")[0];
                        String jobInListID = url.split("d=")[1].split("&")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //GN format: http://careers.gn.com/vacancies/careers_vacancies_show_search_result.asp?jobad_id=2291&country=21&company=0&jobgroup=0&showinfo=0&weblayout=
            if (url.contains("careers.gn.com")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("jobad_id=")[1].split("&")[0];
                        String jobInListID = url.split("jobad_id=")[1].split("&")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //DTU format: http://www.dtu.dk/Job/job?id=2e56b550-5c0e-4aab-b9c2-9352d9a0de8d
            if (url.contains("www.dtu.dk")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("id=")[1];
                        String jobInListID = url.split("id=")[1];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //trustpilot format: https://boards.greenhouse.io/trustpilot/jobs/34504?t=tuh5cf#.VFJYLaN7yUk
            if (url.contains("boards.greenhouse.io/trustpilot")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("jobs/")[1].split("\\?t")[0];
                        String jobInListID = url.split("jobs/")[1].split("\\?t")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //LEGO format: http://www.lego.com/da-dk/careers/jobdescriptionpage?id=52104597&title=Junior%20ISA%20PCI%20Consultant
            if (url.contains("www.lego.com/")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("id=")[1].split("&title=")[0];
                        String jobInListID = url.split("id=")[1].split("&title=")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //Vestas format: https://vestas.taleo.net/careersection/global_external/jobdetail.ftl?job=211501
            if (url.contains("vestas.taleo.net")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("job=")[1];
                        String jobInListID = url.split("job=")[1];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //dong format: http://www.dongenergy.com/EN/Careers/job%20opportunities/Pages/Job_openings_detail.aspx?jobid=187770
            if (url.contains("www.dongenergy.com")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("jobid=")[1];
                        String jobInListID = url.split("jobid=")[1];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }
            //danskebank format: http://www.danskebank.com/da-dk/karriere/soeg-job/Pages/JobShow.aspx?JobPostingId=7049
            if (url.contains("www.danskebank.com")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("JobPostingId=")[1].split("&Dis=")[0];
                        String jobInListID = url.split("JobPostingId=")[1].split("&Dis=")[0];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }
            //arla format: http://www.arla.com/da/job-karriere/ledige-stillinger/senior-manager-product-packaging-aarhus/
            if (url.contains("www.arla.com")) {
                for (Job jobInDb : result) {
                    try {
                        int i = jobInDb.getUrl().split("/").length - 1;
                        String jobInDbID = jobInDb.getUrl().split("/")[i];
                        int j = url.split("/").length - 1;
                        String jobInListID = url.split("/")[j];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }
            //secunia format: http://secunia.com/company/jobs/solution-sales-specialist/
            if (url.contains("secunia.com")) {
                for (Job jobInDb : result) {
                    try {
                        int i = jobInDb.getUrl().split("/").length - 1;
                        String jobInDbID = jobInDb.getUrl().split("/")[i];
                        int j = url.split("/").length - 1;
                        String jobInListID = url.split("/")[j];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

            //Hewlett-Packard format: https://hp.taleo.net/careersection/2/jobdetail.ftl?job=3011402
            if (url.contains("hp.taleo.net")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("job=")[1];
                        String jobInListID = url.split("job=")[1];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }
            //IO format: http://www.ioi.dk/?p=1455
            if (url.contains("www.ioi.dk")) {
                for (Job jobInDb : result) {
                    try {
                        String jobInDbID = jobInDb.getUrl().split("p=")[1];
                        String jobInListID = url.split("p=")[1];
                        if (jobInDbID.equals(jobInListID)) {//then it is the same job, delete it
                            duplicate = true;
                        }
                    } catch (Exception e) {
                        //aioob ex because of url that didnt have right format. Set as dupli to remove it for safety
                        duplicate = true;
                        e.printStackTrace(System.out);
                    }
                }
            }

        } else {//its not of a company whose url format allows me to take its job ID safely, just delete it
//                    System.out.println("DUPLICATE TITLE/COMPANY: " + aJob.getTitle());
            duplicate = true;
        }

        return duplicate;
    }

    public void addToDatabaseTest(Job aJob) {

        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            //System.out.println("________________________________________________" + "\r\n");

            em.persist(aJob);
            em.flush();
            //System.out.println(">>> " + job.getID() + " " + job.getTitle());
            for (Field f : aJob.getFields()) {
                f.setJob(aJob);
                //System.out.println(">> " + f.toString());
                em.persist(f);
            }

        } catch (Exception e) {
            System.err.println(e.getMessage());
            transaction.rollback();
        }
        transaction.commit();
        em.close();
    }

    public void addCompanyToDatabase(Company aCompany) {
//        EntityManager em = emf.createEntityManager();
//        EntityTransaction transaction = em.getTransaction();
//        transaction.begin();
//
//        List<Company> result = em.createQuery("SELECT c FROM Company c WHERE c.companyName = :cn").setParameter("cn", aCompany.getCompanyName()).getResultList();
//        if (!result.isEmpty()) {//company already in DB, do nothing
//            transaction.commit();
//            em.close();
//        } else {//company not already in DB, save logo in file, and save company in DB
//            try {
//                //save logo in memory
//
//                //save company
//                try {
//                    em.persist(aCompany);
//                } catch (Exception e) {
//                    System.err.println(e.getMessage());
//                    transaction.rollback();
//                }
//                transaction.commit();
//                em.close();
//            } catch (Exception e) {
//                //somethign went wrong while saving logo, close em and transaction
//                System.err.println(e.getMessage());
//                transaction.rollback();
//                em.close();
//            }
//        }
    }

    public void addMetricToDatabase(Metrics aMetric) {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            em.persist(aMetric);
        } catch (Exception e) {
            System.err.println(e.getMessage());
            transaction.rollback();
        }
        transaction.commit();
        em.close();
    }

    public void flushDatabase() {
        EntityManager em = emf.createEntityManager();
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();

        try {
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            em.createNativeQuery("TRUNCATE jobs").executeUpdate();
            em.createNativeQuery("TRUNCATE fields").executeUpdate();
            em.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

//            em.createNativeQuery("alter table fields_test_final disable constraint fk_jobs_test_final").executeUpdate();
//            em.createNativeQuery("TRUNCATE TABLE jobs_test_final").executeUpdate();
//            em.createNativeQuery("TRUNCATE TABLE fields_test_final").executeUpdate();
//            em.createNativeQuery("alter table fields_test_final disable constraint fk_jobs_test_final").executeUpdate();
        } catch (Exception e) {
            System.err.println(e.getMessage());
            transaction.rollback();
        }
        transaction.commit();
        em.close();
    }

}
