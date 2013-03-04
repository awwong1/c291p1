/**
   Alexander Wong, Michelle Naylor
   CMPUT 291 Database Project 1

   Due March 11 at 5pm
   
   Tables are defined as followed:
       categories(cat, supercat)
       users(email, name, pass, last_login)
       reviews(rno, rating, text, reviewer, reviewee, rdate)
       offers(ono, ndays, price)
       ads(aid, atype, title, price, descr, location, pdate, poster, cat)
       purchases(pur_id, start_date, aid, ono)
   
   Todo:
       - Enforce character limit on user registration
       - Implement Functionalities

   Done:
       - Login/Registration
       - Display reviews between logout and currenttime after login
       - Logout
*/


import java.sql.*;
import java.util.*;
import java.util.Arrays;
import java.io.Console;

public class proj1 {

    public static Console console;
    public static Connection con;
    public static Statement stmt;
    public static ResultSet rset;
    public static String userstate = null;

    public static void main(String args[]) {	
	// NOTE: This program relies on the use of console. If console does not exist, exit.
	console = System.console();

	if (console == null){
	    System.out.println("Failed to get the console instance.");
	    System.exit(0);
	}
	
	try {
	    // Step 1: Load the appropriate jdbc driver
	    String m_drivername = "oracle.jdbc.driver.OracleDriver";
	    Class drvClass = Class.forName(m_drivername);
	    DriverManager.registerDriver((Driver) drvClass.newInstance());
	    
	    // Step 2: Establish the connection
	    String m_url = "jdbc:oracle:thin:@gwynne.cs.ualberta.ca:1521:CRS";
	    System.out.println("Connecting to '" + m_url + "'...");
	    String m_userName = console.readLine("Enter username: ");
	    char[] m_ipassword = console.readPassword("Enter password: ");
	    String m_password = new String(m_ipassword);
	    System.out.println("Connecting...");
	    con = DriverManager.getConnection(m_url, m_userName, m_password);
	    stmt = con.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				       ResultSet.CONCUR_UPDATABLE);

	    Arrays.fill(m_ipassword, ' ');
	    m_password = "";
	    
	    System.out.println("Sucessfully connected to the database.");
	    
	    // Step 3: Begin project specifications here
	    while(true){
		userstate = logchoice();
		if (userstate.equals("RETRY")){
		    // Iterate through the loop again, just calls userstate again
		    continue;
		} else {
		    // User is now logged in, allow ads search, list, post
		    // allow user search, user logout
		    // NOTE: At this point, userstate = email
		    
		    System.out.println("\nYou are logged in as: " + userstate + ".");
		    
		    // TODO: IMPLEMENT SEARCH FOR ADS : 3
		    // IMPLEMENT LIST OWN ADS : 2
		    // IMPLEMENT SEARCH FOR USERS : 4
		    // IMPLEMENT POST AN AD : 1
		    // IMPLEMENT LOGOUT : 0

		    while(true){
			// infinite loop here
			System.out.println("\nUjiji Options:");
			System.out.println("'0' for logout, '1' for post ad, '2' for list own ads,");
			System.out.println("'3' for search ads, '4' for search users");
			String raw_selection = console.readLine("Enter selection (0-4) <currently 0, 3, 4 works>:");
			int selection = 255;
			try{
			    selection = Integer.valueOf(raw_selection);
			} catch (Exception e) {
			    //e.printStackTrace();
			}
			if (selection < 0 || selection > 4) {
			    System.out.println("Invalid input '" + raw_selection + "'");
       		    continue;
			}
			if (selection == 0) {
			    // Logout user
			    logout();
			    break;
			}
			if (selection == 1) {
			    // Post an ad
			    ad_post();
			}
			if (selection == 2) {
			    // List own ads
			}
			if (selection == 3) {
			    ad_search();
			}
			if (selection == 4) {
			    // Search for users
			    user_search();
			    
			}
		    }
		    
		}
	    }
	    
	} catch (Exception e) {
	    e.printStackTrace();
	}
    }

    public static String logchoice(){
	// This function allows users to login or register as a new user
	String rvalue = "";
	System.out.println();
	int login_input = 255;
	while (login_input < 0 || login_input > 2){
	    System.out.println("Login Screen Selection");
	    System.out.println("'0' for exit, '1' for login, '2' for registration:");
	    String raw_input = console.readLine("Enter selection (0-2): ");
	    try {
		login_input = Integer.valueOf(raw_input);
	    }catch (Exception e){
		//e.printStackTrace();
	    }
	    if (login_input < 0 || login_input > 2) {
		System.out.print("Entered value '"+raw_input+"'. ");
		System.out.println("Invalid input.");
	    }
	}

	if (login_input == 0) {
	    System.out.println("\nExiting program...\n");
	    try {
		con.close();
		stmt.close();
	    } catch (SQLException e) {
		e.printStackTrace();
	    }
	    System.exit(0);
	}

	else if (login_input == 1) {
	    // Login screen here, enter email and pass
	    System.out.println("\n1) Login Screen");
	    while(true) {
		System.out.println("'0' for back, else enter email:");
		String raw_email = console.readLine("Enter email: ");
		
		if (raw_email.equals("0")){
		    System.out.println("Back...");
		    return "RETRY";
		}
		char[] raw_pass = console.readPassword("Enter pass: ");
		String pass = new String(raw_pass);
		Arrays.fill(raw_pass, ' ');
		// Query goes here
		String checkUser = "SELECT * FROM users WHERE LOWER(email) = LOWER('" +
		    raw_email + "') AND pass = '" + pass + "'";
		try {
		    rset = stmt.executeQuery(checkUser);
		    while(rset.next()){
			String email = rset.getString("email").replaceAll("\\s","");
			if (email.equals(raw_email)) {
			    String last_login = rset.getString("last_login");
			    if (last_login != null){
				last_login = last_login.substring(0, last_login.length() - 2);
			    }
			    System.out.println("Welcome back, " + rset.getString("name").trim() + 
					       ", your last login was at " + last_login +".");
			    
			    System.out.println("Displaying reviews from last login to current time:");
			    // Display all reviews between last_login and current system time
			    // where reviewee = email
			    
			    String display_reviews = "SELECT rdate, rating, reviewer, text FROM reviews WHERE " + 
				"lower(reviewee) = lower('" + email + 
				"') AND rdate BETWEEN (" + 
				"SELECT last_login FROM users WHERE LOWER(email) = LOWER('" + email +"')) AND " +
				"CURRENT_TIMESTAMP ORDER BY CURRENT_TIMESTAMP DESC";
			    rset = stmt.executeQuery(display_reviews);			    
			    Integer increment = 0;
			    String shownextreviews = null;
			    if(rset.next()){
				rset.previous();
				System.out.println("\nRating | Review Text                    |" +
						   " Reviewer             | Review Date");
				while(rset.next()){								
				    System.out.println(rset.getInt("rating") + "      | " + 
						       rset.getString("text") + " | " + 
						       rset.getString("reviewer") + " | " + 
						       rset.getString("rdate"));	   
				    increment++;				
				    if(increment%3 == 0){
					shownextreviews = "c";
					while (!shownextreviews.equals("y") && !shownextreviews.equals("n")){
					    shownextreviews = console.readLine("Show next 3 reviews (y/n): ");
					    if (!shownextreviews.equals("y") && !shownextreviews.equals("n")){
						System.out.println("Invalid input '" + shownextreviews + "'");
					    }
					}
					if (shownextreviews.equals("n")){
					    break;
					}
				    }
				}
				System.out.println("- No more reviews to show -");
			    } else {
				System.out.println("- No new reviews -");
			    }
			    return email;			   
			}
		    } 
		    System.out.println("Invalid email or pass.");
		    
		} catch (Exception e){
		    e.printStackTrace();
		}
	    }
	}

	else if (login_input == 2) {
	    // Register user screen here, ask for new email and pass
	    // NOTE: Currently, length is not restricted. Will be cut off in SQL statememnt execution
	    System.out.println("\n2) Register New User");
	    System.out.println("'0' for back, else enter new email:");

	    while(true){
		String raw_email = console.readLine("Enter email (up to 20 chars): ").trim();
		if(raw_email.equals("0")) {
		    System.out.println("Back...");
		    return "RETRY";
		}
		String raw_email2 = console.readLine("Confirm email: ").trim();
		if(!raw_email.equals(raw_email2)){
		    System.out.println("Emails do not match.");
		    continue;
		}
		char[] raw_pass = console.readPassword("Enter password (up to 4 chars): ");
		char[] raw_pass2 = console.readPassword("Confirm password: ");
		String pass = new String(raw_pass);
		String pass2 = new String(raw_pass2);
		Arrays.fill(raw_pass, ' ');
		Arrays.fill(raw_pass2, ' ');
		if (!pass.equals(pass2)){
		    System.out.println("Passwords do not match.");
		    continue;
		}
		String raw_name = console.readLine("Enter name (up to 20 chars): ").trim();
		
		// Check if the email exists in the users table
		// If the email exists make the user enter information again
		String check_email = "SELECT email FROM users WHERE LOWER(email) = LOWER('" +
		    raw_email + "')";
		String create_acc = "INSERT INTO users (email, name, pass) VALUES ('" + raw_email + "', '" 
		    + raw_name + "', '" + pass + "')";
		try {

		    rset = stmt.executeQuery(check_email);
		    if (rset.next()) {			
			System.out.println("Email already exists, please try again.");
			continue;
			
		    }
		    else{
			// Create the account
			stmt.executeUpdate(create_acc);

			// Verify the account
			String checkUser = "SELECT * FROM users WHERE LOWER(email) = LOWER('" +
			    raw_email + "') AND pass = '" + pass + "'";
			rset = stmt.executeQuery(checkUser);
			rset.next();
			String email = rset.getString("email").replaceAll("\\s","");
			if (email.equals(raw_email)) {
			    System.out.println("Welcome, " + rset.getString("name").trim() + ".");
			    return email;
			
			} else {
			    System.out.println("Invalid email or pass.");
			}
			
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}		
	    }
	}
	
	// Function should never reach here
	// my control statements should cover all cases
	return "RETRY";
    }

    public static void logout(){
	// Logs out of the system, sets current system time to last_login
 	System.out.println("\n0) Loging out...");
 	String update_lastlogin = "UPDATE users SET last_login = (SELECT CURRENT_TIMESTAMP FROM DUAL) " +
	    "WHERE lower(email) = lower('" + userstate + "')";
	
	try {
	    stmt.executeUpdate(update_lastlogin);
	    System.out.println("You have sucessfully logged out.");
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	userstate = null;
	return;    
    }

    public static void ad_search(){
	String delims = " ";
	
	// split keywords input into an array of keywords
	String keywords_str = console.readLine("Enter keywords: ");
	String[] keywords = keywords_str.split(delims);
	
	String key_search = "SELECT atype, title, price, pdate FROM ads WHERE title LIKE '%" + keywords[0] + "%' OR descr LIKE '%" + keywords[0] + "%'";
	// add keywords to the SQL query
	for (int i = 1; i < keywords.length; i++) {
	    key_search = key_search.concat(" OR title LIKE '%" + keywords[i] + "%' OR descr LIKE '%" + keywords[i] + "%'");
	}
	// add order by clause to SQL query
	
	key_search = key_search.concat(" ORDER BY pdate DESC");
	
	try {
	    // execute query
	    rset = stmt.executeQuery(key_search);
	    ad_print(rset);
	    
	} catch(SQLException ex) {
	    System.err.println("SQLException:" + ex.getMessage());
	}
    }
    public static void ad_print(ResultSet rset) {
	/** prints ads in rset in multiples of 5
        asks to print more in multiples of 5
	NOTE: if there are no more ads to display, still asks to print more
	*/
	int ad_num = 1;
	int counter = 0;
	try {
	    // print query result
	    while(rset.next() && counter < 5){
		String rs_atype = rset.getString("atype");
		String rs_title = rset.getString("title");
		Float rs_price = rset.getFloat("price");
		String rs_pdate = rset.getString("pdate");
		System.out.println((counter) + ": " + rs_atype + " " + rs_title + " " + rs_price + " " + rs_pdate);
		counter++;
	    }
	} catch(SQLException ex) {
	    System.err.println("SQLException:" + ex.getMessage());
	}
	// See more ads or more detail for specific ad
	System.out.println("To see more ads press 1, to select a specific ad press 2: ");
	String raw_selection = console.readLine("Press 0 - 2: ");
	int selection = 255;
	try{
	    selection = Integer.valueOf(raw_selection);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	// more ads
	if (selection == 1) {
	    ad_print(rset);
	}
	// specific ad
	if (selection == 2) {
	    ad_select(rset);
	}
    }

    public static void ad_select(ResultSet rset) {
	/**
	   Provides more detail for specific ads, as selected by user
	 */
	String select_title = " ";
	int counter = 5;
	String rs_poster = " ";
	// which ad to see
	String raw_selection = console.readLine("To see specific ad, press it's number: ");
	int selection = 0;
	try {
	    selection = Integer.parseInt(raw_selection);
	    System.out.println("Selection was: " + selection);
	} catch (Exception e) {
	    e.printStackTrace();
	}
	try {
	    // find specified ad
	    while(rset.previous() && counter != selection) {
		select_title = rset.getString("title");
		counter--;
	    }
	} catch(SQLException ex) {
	    System.err.println("SQLException:" + ex.getMessage());
	}

	String more_detail = "SELECT title, descr, location, cat, poster FROM ads WHERE title = '" + select_title + "'";
	try {
	    // execute query
	    rset = stmt.executeQuery(more_detail);
	    while(rset.next()){
		String rs_title = rset.getString("title");
		String rs_descr = rset.getString("descr");
		String rs_location = rset.getString("location");
		String rs_cat = rset.getString("cat");
		rs_poster = rset.getString("poster");
		System.out.println(rs_title + " " + rs_descr + " " + rs_location + " " + rs_cat + " " + rs_poster);
	    }
	} catch(SQLException ex) {
	    System.err.println("SQLException:" + ex.getMessage());
	}
	String poster_reviews = "SELECT AVG(rating) AS avg_rating FROM reviews WHERE reviewee LIKE '" + rs_poster + "'";
	
	try {
	    // execute query
	    rset = stmt.executeQuery(poster_reviews);
	    while(rset.next()){
		String rs_avg_rating = rset.getString("avg_rating");
		System.out.print(rs_avg_rating);
	    }
	} catch(SQLException ex) {
	    System.err.println("SQLException:" + ex.getMessage());
	}
    }

    public static void user_search() {
	/**
	   User should be able search by email or search by name
	   If search by name, returns a result of all users with that name; 
	   user should be able to select one from the list.
	*/
	Integer selection = 255;
	while(selection<0 || selection>2){
	    System.out.println("\n4) User Search:");
	    System.out.println("'0' for back, '1' for email search, '2' for name search");
	    String raw_selection = console.readLine("Enter selection (0-2): ");
	    try {
		selection = Integer.parseInt(raw_selection);
	    } catch (Exception e) {
		// e.printStackTrace();
	    }
	    if (selection < 0 || selection > 2) {
		System.out.println("Invalid input: '" + raw_selection + "'");
	    }
	    if (selection == 0) {
		System.out.println("Back...");
		return;
	    }	    
	    if (selection == 1) {
		// Search by email
		String searchemail = null;
		while(true){
		    System.out.println("\nSearch user email: ");
		    System.out.println("'0' for back, else enter email:");
		    searchemail = console.readLine("Enter email, '0' for back: ");
		    if (searchemail.equals("0")){
			System.out.println("Back...");
			break;
		    }
		    // Search for a user with the specified email
		    // If email exists, return name, email, number of ads, average rating
		    String searchemailquery = "SELECT users.name AS name, users.email AS email, COUNT(ads.poster) AS ads, AVG(reviews.rating) AS rating " +
			"FROM (users FULL JOIN ads ON (users.email = ads.poster)) FULL JOIN reviews ON (users.email = reviews.reviewee) " + 
			"WHERE lower(users.email) LIKE lower('%" + searchemail + "%')" + 
			"GROUP BY users.name, users.email";
		    
		    try {
			rset = stmt.executeQuery(searchemailquery);			
			if (rset.next()){
			    rset.previous();
			    selection = 1;
			    System.out.println("# | User Name            | User Email           | Ads | Avg Rating ");
			    while(rset.next()){				
				System.out.println(selection + " | " + 
						   rset.getString("name") + " | " +
						   rset.getString("email")+ " | " +
						   rset.getInt("ads") + "   | " +
						   rset.getInt("rating"));
				selection++;
			    }			    
			    Integer userselect = 0;
			    while(userselect < 1 || userselect > (selection - 1)){
				String raw_userselect = console.readLine("Select user number (1-" + (selection - 1) +", '0' for back): ");				
				try{
				    userselect = Integer.parseInt(raw_userselect);
				    if(userselect == 0){
					break;
				    }
				    // Choose the user with the email at the row specified
				    // Move the cursor back the difference of selection and userselect
				    // ie) if there are 3 people rows, and a user select 3, will move row back 0 times
				    // if the user selects 1, move the row back 3-1 = 2 times
				    Integer moveback = selection - userselect;
				    for(int i = 0; i < moveback; i++){
					rset.previous();
				    }
				    user_options(rset.getString("email").trim());
				    return;
				} catch (Exception e){
				    //e.printStackTrace();
				    System.out.println("Invalid input '"+raw_userselect+"'");
				}				
			    }

			} else {
			    System.out.println("No user by that email.");
			}
		    } catch (SQLException e) {
			e.printStackTrace();
		    }		    		    
		}
	    }	    
	    if (selection == 2) {
		// Search by name
		String searchname = null;
		while(true){
		    System.out.println("\nSearch user name: ");
		    System.out.println("'0' for back, else enter name:");
		    searchname = console.readLine("Enter name, '0' for back: ");
		    if (searchname.equals("0")){
			System.out.println("Back...");
			break;
		    }
		    // Query begins here
		    String searchnamequery = "SELECT users.name AS name, users.email AS email, COUNT(ads.poster) AS ads, AVG(reviews.rating) AS rating " +
			"FROM (users FULL JOIN ads ON (users.email = ads.poster)) FULL JOIN reviews ON (users.email = reviews.reviewee) " + 
			"WHERE lower(users.name) LIKE lower('%" + searchname + "%')" + 
			"GROUP BY users.name, users.email";
		    
		    try {
			rset = stmt.executeQuery(searchnamequery);
			if (rset.next()){
			    rset.previous();
			    selection = 1;
			    System.out.println("# | User Name            | User Email           | Ads | Avg Rating ");
			    while(rset.next()){				
				System.out.println(selection + " | " + 
						   rset.getString("name") + " | " +
						   rset.getString("email")+ " | " +
						   rset.getInt("ads") + "   | " +
						   rset.getInt("rating"));
				selection++;
			    }			    
			    Integer userselect = 0;
			    while(userselect < 1 || userselect > (selection - 1)){
				String raw_userselect = console.readLine("Select user number (1-" + (selection - 1) +", '0' for back): ");				
				try{
				    userselect = Integer.parseInt(raw_userselect);
				    if(userselect == 0){
					break;
				    }
				    Integer moveback = selection - userselect;
				    for(int i = 0; i < moveback; i++){
					rset.previous();
				    }
				    user_options(rset.getString("email").trim());
				    return;
				} catch (Exception e){
				    //e.printStackTrace();
				    System.out.println("Invalid input '"+raw_userselect+"'");
				}				
			    }			    
			    
			} else {
			    System.out.println("No user by that name.");
			}
		    } catch (SQLException e) {
			e.printStackTrace();
		    }
		    
		}
	    }	    
	}	
    }

    public static void user_options(String email){
	/**
	   This function takes the email given and allows options:
	   1) See all the reviews posted about this email
	   2) Write a review for this email
	 */
	System.out.println("You have selected '"+email+"'");
	Integer choice = null;
	while(true){
	    String raw_choice = console.readLine("'0' for back, '1' to list all reviews, '2' to write a review: ");
	    try {
		choice = Integer.parseInt(raw_choice);
		if (choice == 0){
		    break;
		}
		if (choice == 1){
		    list_reviews(email);
		}
		if (choice == 2){
		    write_review(email);
		}
	    } catch (Exception e) {
		e.printStackTrace();
		System.out.println("Invalid input '"+raw_choice+"'");
	    }
	}
	return;
    }
    
    public static void write_review(String reviewee) {
	/**
	   This function takes the class global userstate as the reviewer
	   and writes a review for the reviewee
	   This function assumes the two emails are valid.
	   This function will not allow a user to write a review for himself.
	 */
	if(reviewee.equals(userstate)){
	    System.out.println("You cannot write a review for yourself!");
	    return;
	}
	// Write the review here
	System.out.println("\nWriting review for '"+reviewee+"'...");
	Integer rating = 0;
	while(rating < 1 || rating > 5){
	    String raw_rating = console.readLine("'0' for back, Enter rating(1-5): ");
	    try{
		rating = Integer.parseInt(raw_rating);
		if (rating == 0){
		    System.out.println("Back...");
		    return;
		}
	    } catch (Exception e){
		//e.printStackTrace();
		System.out.println("Invalid input '"+raw_rating+"'");
	    }
	}
	// At this point, rating is valid. Enter text
	String rtext = null;
	while (true){
	    rtext = console.readLine("Enter review text (max 30 char): ");
	    if(rtext.length()>30){
		System.out.println("Review too long! (length: '"+rtext.length()+"'");
	    } else {
		break;
	    }
	}
	// At this point, text and rating is valid. Find new review ID
	String RNOQuery = "SELECT MAX(RNO) FROM REVIEWS";
	Integer newRNO = null;
	try{
	    rset = stmt.executeQuery(RNOQuery);
	    if(rset.next()){
		newRNO = rset.getInt(1) + 1;
	    } else {
		// This line will occur if there are no reviews in the table
		newRNO = 1;
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	    return;
	}
	// Write the query/review here
	String reviewquery = "INSERT INTO reviews (RNO, RATING, TEXT, REVIEWER, REVIEWEE, RDATE) " + 
	    "VALUES ('" + newRNO + "', '" +
	    rating + "', '" +
	    rtext + "', '" + 
	    userstate + "', '" +
	    reviewee + "', " + 
	    "CURRENT_TIMESTAMP)";
	try{
	    stmt.executeUpdate(reviewquery);
	    System.out.println("Sucessfully wrote a review for '" + reviewee + "'!");
	} catch (SQLException e) {
	    e.printStackTrace();
	    return;
	}
	return;
    }
    
    public static void list_reviews(String reviewee){
	/**
	   This function takes the reviewee and prints
	   all the reviews reviewing the reviewee
	 */
	System.out.println("\nListing reviews for '"+reviewee+"'...");
	
	String display_reviews = "SELECT rdate, rating, reviewer, text FROM reviews WHERE " + 
	    "lower(reviewee) = lower('" + reviewee + "')";
	
	try{
	    rset = stmt.executeQuery(display_reviews);
	    if(rset.next()){
		rset.previous();
		System.out.println("Review Date         | Rating | Reviewer             | Text");
		while(rset.next()){
		    System.out.println(rset.getString("rdate") + " | " +
				       rset.getInt("rating") + "      | " + 
				       rset.getString("reviewer") + " | " +
				       rset.getString("text"));
		}
	    } else {
		System.out.println("No reviews to show!");
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}	
	return;
    }
    
    public static void ad_post(){
	/**
	   Posting an ad:
	   Select an ad type (S or W):
	   Enter in the title, price, description, location, and category.
	   Have the program generate the aid and poster.
	 */
	// Select an ad type
	String adtype = null;
	while(true){
	    adtype = console.readLine("'0' for back, else select an Ad Type (s, w): ");
	    if (adtype.equals("0")){
		System.out.println("Back...");
		return;
	    }
	    if (adtype.toUpperCase().equals("S")||adtype.toUpperCase().equals("W")){
		adtype = adtype.toUpperCase();
		break;
	    } else {
		System.out.println("Invalid input '" + adtype + "'");
	    }
	}
	// HOLDING OFF HERE UNTIL PROF GETS HIS DATATYPES TOGETHER
	
	return;
    }
    
}

