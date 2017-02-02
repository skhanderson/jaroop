package org.skhanderson;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.RandomAccessFile;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.channels.FileChannel;
import java.nio.channels.Channels;
import java.nio.channels.FileLock;
import java.io.IOException;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Account {
    private String htmlfile;
    private RandomAccessFile htmlrandom;
    private FileChannel htmlchannel;
    private FileLock lock;
    /*
     * Creates an account using the passed html filename.
     * @param filename A string of the filename.
     * @throws IOException Throws this if there is a problem with the file or can't lock it.
     */
    public Account(String filename) throws IOException {
	htmlfile = filename;
	htmlrandom = new RandomAccessFile(new File(filename), "rw");
	htmlchannel = htmlrandom.getChannel();
	lock = htmlchannel.tryLock();
	if (lock == null) {
	    throw new IOException();
	}
    }

    /**
     * Gets the contents of the file.
     * @returns String of the contents.
     */
    private String instream_to_contents() throws IOException {
	htmlrandom.seek(0);
	InputStream instream = Channels.newInputStream(htmlrandom.getChannel());
	StringBuilder result = new StringBuilder();
	int c;
	while (-1 != (c = instream.read()))
	    result.append((char)c);
	return result.toString();
    }

    /**
     * Parses the RandomAccessFile into a Jsoup Document.
     * @returns A Jsoup Document.
     * @throws IOException if there is an exception on the file.
     */
    private Document parseFile() throws IOException {
	String contents = instream_to_contents();
	Document doc = Jsoup.parse(contents);
	return doc;
    }

    /**
     * Gets the balance from the RandomAccessFile representing the account.
     * @returns A float representing the current balance.
     * @throws IOException if there is an exception on the file.
     */
    public float balance() throws IOException {
	float balance = (float)0.0;
	Document doc = parseFile();
	Elements transactions = doc.select("table[id=transactions]");
	for (Element sub: transactions.select("td")) {
	    float amount = Float.parseFloat(sub.text().trim());
	    balance += amount;
	}
	return balance;
    }
    /**
     * Function that appends a numeric transaction to the account file.
     * <p>
     * No checking is done whether the amount is valid.
     * It is up to the user to determine whether the transaction 
     * (for instance, a withdrawal) is valid, and there is enough 
     * money in the account.
     * 
     * @param file The RandomAccessFile representing the html account file.
     * @param num The amount of the transaction as a float.
     */
    public void appendRecord(float num) throws IOException {
	Document doc = parseFile();
	Elements transactions = doc.select("table[id=transactions]");
	Element last = null;
	for (Element el: transactions) {
	    last = el;
	}
	Elements tbody = last.select("tbody");
	for (Element el: tbody) {
	    last = el;
	}
	String text = String.format("%.2f", num);
	last.appendElement("tr").appendElement("td").text(text);
	String newcontents = doc.html();
	if (newcontents != null) {
	    htmlrandom.seek(0);
	    BufferedWriter out = new BufferedWriter(new OutputStreamWriter(Channels.newOutputStream(htmlrandom.getChannel())));
	    out.write(newcontents);
	    out.flush();
	    htmlrandom.setLength(newcontents.length());
	}
    }
}	
