/* 
This uses Jsoup to parse and write the html file.

Better would be to follow the format of the file;
already one has to do some processing specific to the file type,
but absent a clear specification, we just made assumptions about
the format of the html file (maybe I was supposed to ask).

The fact that we're parsing the html file and writing it out via
a dom means that some aspects of the formatting will change
with the first use.

This uses file locking to assure that no other user on the same
machine starts this program and does conflicting operations that
would screw up the balance or file.

There is still the possibility that one runs out of disk space while
writing the file; real transaction systems have to worry about such things.
We just assume that the lock is good, and there is sufficient
space to write the whole file out again.

This is not how I normally structure programs or code;
everything is a static method just so I can have a simple thing to ship
(all in one file).  I hope this is ok for demonstration purposes.
A real form of this might have a class that represents one of these
"accounts", locks the file, and provides nice object-oriented methods
to perform transactions on the account.
I didn't do this, I spent long enough (in my view) just getting this
working, and to show some of my coding style.
We can talk more about good software engineering habits in an interview,
hopefully this is sufficient to get us to the next steps.

Steve Handerson
*/

import org.skhanderson.Account;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

class bank {

    /**
     * A function that repeatedly asks the user for a valid amount.
     * <p>
     * A valid amount is positive, numeric, and has at most one
     * decimal point and at most two digits representing the cents.
     * @param in The BufferedReader for the user input stream
     * @param prompt The string to prompt the user with.
     * @return A float representing the amount.
     * @throws IOException possibly from the readline.
     */
    public static float askAmount(BufferedReader in, String prompt) throws IOException {
	float amount = (float)-1.0;
	String line = "";
	while (amount <= 0.0) {
	    System.out.print(prompt);
	    line = in.readLine();
	    if (!line.matches("^[0-9]+(\\.[0-9]{2})?$")) {
		System.out.println("Unrecognized amount.");
		amount = (float)-1.0;
		continue;
	    }
	    try {
		amount = Float.parseFloat(line);
	    } catch (Exception e) {
		System.out.println("Unrecognized amount.");
	    }
	}
	return amount;
    }



    /* 
     * Main program for the account application.
     * <p>
     * We first load and lock the file, and terminate if we cannot
     * obtain a lock on the file.
     * We obtain the RandomAccessFile that represents the account.
     * We then loop, reading commands, and doing the appropriate
     * operation.
     * On exit, we simply call System.exit(0).
     *
     * @param args The program arguments.  The optional argument is the 
     * name of the file.
     */
    public static void main(String[] args) {
	if (args.length > 2) {
	    System.err.println("Usage: <this> balance_log_file.html");
	    System.exit(1);
	}
	String htmlfilename = "log.html";
	if (args.length > 0) {
	    htmlfilename = args[0];
	}
	BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
	try {
	    Account account = new Account(htmlfilename);
	    while (true) {
		System.out.print("Please enter in a command (Deposit, Withdraw, Balance, Exit) :");
		String line = "";
		try {
		    line = in.readLine();
		} catch (Exception e) {
		    System.exit(1);
		}
		line = line.toUpperCase();
		line = line.trim();
		if (line.equals("EXIT")) {
		    System.exit(0);
		}
		else if (line.equals("BALANCE")) {
		    float bal = account.balance();
		    System.out.format("The current balance is: $%.2f\n", bal);
		}
		else if (line.equals("WITHDRAW")) {
		    float amount = askAmount(in, "Please enter an amount to withdraw:");
		    float balance = account.balance();
		    if (amount > balance) {
			System.out.println("Insufficient balance.");
		    } else {
			account.appendRecord(-amount);
		    }
		}
		else if (line.equals("DEPOSIT")) {
		    float amount = askAmount(in, "Please enter an amount to deposit:");
		    account.appendRecord(amount);
		} else {
		    System.out.println("Unrecognized command.");
		}

	    }
	} catch (IOException e) {
	    System.err.println("IO Exception on some file -- perhaps the balance!");
	    System.err.println("Terminating for safety.");
	}
    }
}
