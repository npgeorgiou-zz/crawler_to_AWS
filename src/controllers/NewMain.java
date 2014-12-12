/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import crawlerUtils.Filter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import java.util.logging.Logger;
import model.Job;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

/**
 *
 * @author ksptsinplanet
 */
public class NewMain {

    static Filter filter = new Filter();
    static boolean duplicate = false;
    static String title1 = "Project Manager, Drug Delivery Device Development,";
    static String title2 = "https://jobsearch.siemens.biz/sfcareer/jobreqcareer?jobId=174786&company=Siemens&username=";
    static String url = "https://vestas.taleo.net/careersection/global_external/jobdetail.ftl?job=211501";
    static String cn = "Ph.D. fellowship in mastitis diagnostics and prevention at the Department of Large Animal Sciences.";

    public static void main(String[] args) {
        title1 = filter.homogeniseJobTitle(title1);
        System.out.println(title1);

    }

    static private String sanitizeUrl(String msg) throws UnsupportedEncodingException {
        String decoded = java.net.URLDecoder.decode(msg, "UTF-8");
        return decoded;
    }
}
