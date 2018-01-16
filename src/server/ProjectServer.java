// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 
package server;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.sql.Blob;
import java.sql.Connection;
//import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import entities.CardEntity;
import entities.ComplaintEntity;
import entities.CustomerEntity;
import entities.DeliveryEntity;
import entities.OrderEntity;
import entities.ProductEntity;
import entities.StoreEntity;
import gui.GeneralMessageController;
import logic.ConnectedClients;
import logic.FilesConverter;
import logic.MessageToSend;
import ocsf.server.AbstractServer;
import ocsf.server.ConnectionToClient;
/**
 * This class overrides some of the methods in the abstract 
 * superclass in order to give more functionality to the server.
 */
public class ProjectServer extends AbstractServer 
{
	//do not touch//
	private static Connection con;
	private static String driver="com.mysql.jdbc.Driver";
	private int incomingFileName;
  final public static int DEFAULT_PORT = 5555;
  
  
  /// * Constructs an instance of the echo server.
  /**
   * Constructor for the server
   * @param port the port of the server
   */
  public ProjectServer(int port) 
  {
    super(port);
  }
  
  /**
   * method for connecting to DB
   * @return	the Connection
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  protected static  Connection connectToDB() throws SQLException, ClassNotFoundException
  {
	// Class.forName("com.mysql.jdbc.Driver");
 	 return DriverManager.getConnection("jdbc:mysql://localhost/projectx","root","Braude");	//connect to the sql database
  }
  
  
  
  /**
   * This method removes the connection of the user from the connected client list
   * @param username
   */
  private void terminateConnection(String username)
  {
	  System.out.println(username + " logged out");
	  ConnectedClients.removeConnectedClient(username);
  }

  /**
   * This method get the products matching the self defined products from the product table in the DB
 * @throws ClassNotFoundException  problem connecting
 * @throws SQLException for the sql query
 * @throws IOException for the files converter
   */
  private ArrayList<ProductEntity> getSelfDefinedProducts(ArrayList<String> requests) throws ClassNotFoundException, SQLException, IOException
  {
	  			//the arrayList of String in form of {minPrice,maxPrice,type,dominantColor(if chosen)}
	 ArrayList<ProductEntity> listOfProducts = new ArrayList<ProductEntity>();
	 ProductEntity product;
	 Blob productImage;
	 Statement stmt;
	 ResultSet rs;
	 try
		{
			con = connectToDB(); //call method to connect to DB
			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		} catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
		}
	 Double minPrice,maxPrice;
	 minPrice= Double.parseDouble(requests.get(0)); 				//parse the minimum price
	 maxPrice= Double.parseDouble(requests.get(1)); 				//parse the maximum price
	 
	 String type = "";
	 type=requests.get(2); 											//get the product type
	 
	 String dominantColor ="";
	 if(!requests.get(3).isEmpty()) 								//if dominant color was chosen
	 {
		dominantColor = "AND ProductDominantColor = '"+ requests.get(4)+"'";
	 }
	 stmt=con.createStatement();
	 rs = stmt.executeQuery("Select * FROM projectx.product WHERE ProductPrice BETWEEN "+minPrice+"AND"+maxPrice+"AND ProductType = '"+type+"'"+dominantColor+"");
	 while(rs.next())
	 {
		 product = new ProductEntity();									//create new product
		 product.setProductID(rs.getInt(1));
		 product.setProductName(rs.getString(2));
		 product.setProductType(rs.getString(3));
		 product.setProductPrice(rs.getDouble(4));
		 product.setProductDescription(rs.getString(5));
		 				//**get the blob for the image from the DB**//
		 productImage =con.createBlob();
		 productImage = rs.getBlob(6);
		 InputStream is = productImage.getBinaryStream();
		 
		 product.setProductImage(FilesConverter.convertInputStreamToByteArray(is)); 		//set the input stream to a byte array
		 product.setProductDominantColor(rs.getString(7));
		 listOfProducts.add(product);									//add the product to the list
	 }
	 return listOfProducts;												//return the found products
	 
  }
  
  /**
   * This method cancel an order and sets its status to canceled in the DB
   * @param OrderID	the order to cancel
   * @return
 * @throws ClassNotFoundException 
 * @throws SQLException 
   */
  private String cancelOrder(Integer OrderID) throws ClassNotFoundException, SQLException
  {
	  Statement stmt;
	  String retMsg="";
	  try
		{
			con = connectToDB(); //call method to connect to DB
			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		} catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
		}
	  stmt = con.createStatement();
	  try {
	  stmt.executeUpdate("UPDATE projectx.order SET OrderStatus = 'cancelled' WHERE OrderID = " + OrderID+ "");
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  return retMsg = "faild to cancel the order.";
	  }
	  return retMsg = "Order cancelled";
  }
  
  
  /**
   * This method calculates time difference between two Timestamp objects
   * @param t1	Later Timestamp
   * @param t2	sooner Timestamp
   * @return the time difference in miliseconds
   */
  private Long calculateTimeDifference(Timestamp t1, Timestamp t2)
  {
		Long t3 = t1.getTime()-t2.getTime();
		return t3;
  }
  
  
  
  /**
   * This method sets the customer's order status to "cancel_request" 
   * @param UserID the customer's id
   * @throws SQLException
 * @throws ClassNotFoundException 
   */
  private String cancelRequest(Integer OrderID) throws SQLException, ClassNotFoundException
  {
	  Statement stmt;
	  String retMsg = "";
	  try
		{
			con = connectToDB(); //call method to connect to DB
			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		} catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
		}
	  try {
	  stmt = con.createStatement();
	  Timestamp timestamp = new Timestamp(System.currentTimeMillis());
//	   Date date = new Date(System.currentTimeMillis());
//	    Time time = new Time(System.currentTimeMillis());
//	    time.setTime(System.currentTimeMillis());
//		DateFormat formatter = new SimpleDateFormat("HH:mm");		//format the time
//		DateFormat formatter2 = new SimpleDateFormat("dd/MM/yy");
	//  stmt.executeUpdate("UPDATE projectx.order SET OrderStatus = 'cancel_requested' ,CancelRequestDate = '"+ formatter.format(date.toString()) + "',CancelRequestTime = '"+ formatter2.format(date.toString())+"' WHERE Ordernum = " + OrderID + "");
		PreparedStatement ps = con.prepareStatement("UPDATE projectx.order SET OrderStatus = ? , CancelRequestTime = ? WHERE Ordernum = ?"); //prepare a statement
		ps.setString(1, OrderEntity.OrderStatus.cancel_requested.toString());
		ps.setTimestamp(2, timestamp);
		ps.setInt(3, OrderID);
		//ps.setDate(2, new Date(System.currentTimeMillis()));
		//ps.setTime(3, time);
		ps.executeUpdate();
	  }
	  catch(Exception e)
	  {
		  e.printStackTrace();
		  return retMsg="cancel request failed.";
	  }
	  
	  return retMsg = "cancel request sent.";
  }
  
  /**
   * This method gets all the cancel requests from the DB
   * @return	an arrayList of Orders
 * @throws SQLException 
 * @throws ClassNotFoundException 
   */
  private ArrayList<OrderEntity> getCancelRequests() throws SQLException, ClassNotFoundException
  {
	  ArrayList<OrderEntity> listOfOrdersFromDB = new ArrayList<OrderEntity>();
		OrderEntity order;
		StoreEntity store;
		DeliveryEntity delivery = null;
		ProductEntity product;
		Statement stmt,stmt2,stmt3,stmt4,stmt5;
		ResultSet rs,rs2,rs3,rs4,rs5; 				//for the (order,delivery,list of products,store)
		try
		{
			con = connectToDB(); //call method to connect to DB
			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		} catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
		}
		stmt = con.createStatement();
		stmt2 = con.createStatement();
		stmt3 = con.createStatement();
		stmt4 = con.createStatement();
		stmt5 = con.createStatement();
	    rs = stmt.executeQuery("SELECT * FROM projectx.order WHERE OrderStatus = 'cancel_requested'"); //get all the stores (ID,Name,managerID) in the stores table from the data base
		
		while (rs.next())
		{
			order = new OrderEntity();
			
					//** get the delivery details **//
			rs2 = stmt2.executeQuery("SELECT * FROM projectx.delivery WHERE OrderID ="+rs.getInt(1)+"" );	//get the delivery data for the order
			while(rs2.next())
				delivery = new DeliveryEntity(rs2.getString(2), rs2.getString(3), rs2.getString(4), rs2.getTimestamp(5));		//create a new delivery using the data
			order.setDeliveryDetails(delivery);
			
					//**get the products in the order **//
			rs3 = stmt3.executeQuery("SELECT ProductID FROM projectx.productsinorder WHERE Ordernum = " +rs.getInt(1)+ "");
			while(rs3.next())
			{
				rs4 = stmt4.executeQuery("SELECT * FROM projectx.product WHERE ProductID = " +rs3.getInt(1)+""); 	//get each product's details from DB
				while(rs4.next())
				{
					product = new ProductEntity(rs4.getInt(1), rs4.getString(2), rs4.getString(3), rs4.getDouble(4), rs4.getString(5), rs4.getString(7)); //**NO IMAGE YET//
					order.addProductToCart(product); 			//add the product to the order
				}
			}
			order.setOrderID(rs.getInt(1));
			order.setUserName(rs.getString(2));
			order.setOrderTime(rs.getTimestamp(3));
//			order.setOrderTime(rs.getTime(3));
//			order.setOrderDate(rs.getDate(4));
			if(rs.getString(4) != null)
				order.setCard(new CardEntity(rs.getString(4)));
			if(rs.getString(5).equals(OrderEntity.SelfOrDelivery.selfPickup.toString()))  //set self pickup or delivery
				order.setOrderPickup(OrderEntity.SelfOrDelivery.selfPickup);
			else
				order.setOrderPickup(OrderEntity.SelfOrDelivery.delivery);
			
			if(rs.getString(6).equals(OrderEntity.OrderStatus.active.toString()))		//check order status
				order.setStatus(OrderEntity.OrderStatus.active);
			else if(rs.getString(6).equals(OrderEntity.OrderStatus.cancel_requested.toString()))
				order.setStatus(OrderEntity.OrderStatus.cancel_requested);
			else
				order.setStatus(OrderEntity.OrderStatus.cancelled);
			
			if(rs.getInt(7) == 0)				//check if order was paid for
				order.setPaid(false);
			else
				order.setPaid(true);
			
			order.setTotalPrice(rs.getDouble(8));
			order.setReceivingTimestamp(rs.getTimestamp(9));
//			order.setReceivingDate(rs.getDate(10));
//			order.setReceivingTime(rs.getTime(11));
						//** get the store **//
			rs5 = stmt5.executeQuery("SELECT * FROM projectx.store WHERE BranchID = "+rs.getInt(12)+"");
			while(rs5.next())
			{
				store = new StoreEntity(rs5.getInt(1), rs5.getString(2), rs5.getInt(3));
				order.setStore(store);
			}
			
			if(rs.getString(7).equals(OrderEntity.CashOrCredit.cash.toString()))
				order.setPaymendMethod(OrderEntity.CashOrCredit.cash);
			else
				order.setPaymendMethod(OrderEntity.CashOrCredit.credit);
	
			listOfOrdersFromDB.add(order); //add the product from the data base to the list
		}
		
		return listOfOrdersFromDB;
  }
  
  
  /**
   * This method returns an ArrayList of the customer's orders
   * @param userID	the user id
   * @return ArrayList  of orders
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  private ArrayList<OrderEntity> getCustomerOrders(String userID) throws SQLException, ClassNotFoundException
  {
	  ArrayList<OrderEntity> listOfOrdersFromDB = new ArrayList<OrderEntity>();
		OrderEntity order;
		StoreEntity store;
		DeliveryEntity delivery = null;
		ProductEntity product;
		Statement stmt,stmt2,stmt3,stmt4,stmt5;
		ResultSet rs,rs2,rs3,rs4,rs5; 				//for the (order,delivery,list of products,store)
		try
		{
			con = connectToDB(); //call method to connect to DB
			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		} catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
		}
		stmt = con.createStatement();
		stmt2 = con.createStatement();
		stmt3 = con.createStatement();
		stmt4 = con.createStatement();
		stmt5 = con.createStatement();
	    rs = stmt.executeQuery("SELECT * FROM projectx.order WHERE UserID ='"+userID+"'"); //get all the stores (ID,Name,managerID) in the stores table from the data base
		
		while (rs.next())
		{
			order = new OrderEntity();
			
					//** get the delivery details **//
			rs2 = stmt2.executeQuery("SELECT * FROM projectx.delivery WHERE OrderID ="+rs.getInt(1)+"" );	//get the delivery data for the order
			while(rs2.next())
				delivery = new DeliveryEntity(rs2.getString(2), rs2.getString(3), rs2.getString(4), rs2.getTimestamp(5));		//create a new delivery using the data
			order.setDeliveryDetails(delivery);
			
					//**get the products in the order **//
			rs3 = stmt3.executeQuery("SELECT ProductID FROM projectx.productsinorder WHERE Ordernum = " +rs.getInt(1)+ "");
			while(rs3.next())
			{
				rs4 = stmt4.executeQuery("SELECT * FROM projectx.product WHERE ProductID = " +rs3.getInt(1)+""); 	//get each product's details from DB
				while(rs4.next())
				{
					product = new ProductEntity(rs4.getInt(1), rs4.getString(2), rs4.getString(3), rs4.getDouble(4), rs4.getString(5), rs4.getString(7)); //**NO IMAGE YET//
					order.addProductToCart(product); 			//add the product to the order
				}
			}
			order.setOrderID(rs.getInt(1));
			order.setUserName(userID);
			order.setOrderTime(rs.getTimestamp(3));
//			order.setOrderTime(rs.getTime(3));
//			order.setOrderDate(rs.getDate(4));
			if(rs.getString(4) != null)
				order.setCard(new CardEntity(rs.getString(4)));
			if(rs.getString(5).equals(OrderEntity.SelfOrDelivery.selfPickup.toString()))  //set self pickup or delivery
				order.setOrderPickup(OrderEntity.SelfOrDelivery.selfPickup);
			else
				order.setOrderPickup(OrderEntity.SelfOrDelivery.delivery);
			
			if(rs.getString(6).equals(OrderEntity.OrderStatus.active.toString()))		//check order status
				order.setStatus(OrderEntity.OrderStatus.active);
			else if(rs.getString(6).equals(OrderEntity.OrderStatus.cancel_requested.toString()))
				order.setStatus(OrderEntity.OrderStatus.cancel_requested);
			else
				order.setStatus(OrderEntity.OrderStatus.cancelled);
			
			if(rs.getInt(7) == 0)				//check if order was paid for
				order.setPaid(false);
			else
				order.setPaid(true);
			
			order.setTotalPrice(rs.getDouble(8));
			order.setReceivingTimestamp(rs.getTimestamp(9));
//			order.setReceivingTime(rs.getTime(11));
						//** get the store **//
			rs5 = stmt5.executeQuery("SELECT * FROM projectx.store WHERE BranchID = "+rs.getInt(10)+"");
			while(rs5.next())
			{
				store = new StoreEntity(rs5.getInt(1), rs5.getString(2), rs5.getInt(3));
				order.setStore(store);
			}
			
			if(rs.getString(7).equals(OrderEntity.CashOrCredit.cash.toString()))
				order.setPaymendMethod(OrderEntity.CashOrCredit.cash);
			else
				order.setPaymendMethod(OrderEntity.CashOrCredit.credit);
	
			listOfOrdersFromDB.add(order); //add the product from the data base to the list
		}
		
		return listOfOrdersFromDB;
	  
  }

  /**
   * This method add an order to the data base
   * @param newOrder	the new order
   * @return	messages
   * @throws SQLException
   * @throws ClassNotFoundException
   */
	private ArrayList<String> createNewOrder(OrderEntity newOrder) throws SQLException, ClassNotFoundException {
		ArrayList<String> returnMessage = new ArrayList<String>();
		Statement stmt;
		int orderCounter = 0;
		try
		{
			con = connectToDB(); //call method to connect to DB

			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		}

		catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
			e.printStackTrace();
		}

		stmt = con.createStatement();
//		ResultSet rs= stmt.executeQuery("SELECT OrdersID FROM projectx.counters");			//get the current latest order id
//		
//		while(rs.next())
//			orderCounter = rs.getInt(1);
		
			PreparedStatement ps = con.prepareStatement("INSERT INTO projectx.order (UserID,OrderTime,OrderCard,PickupMethod,OrderStatus,OrderPaid,TotalPrice,ReceiveTimestamp,BranchID) VALUES (?,?,?,?,?,?,?,?,?)"); //prepare a statement
			//ps.setInt(1, orderCounter+1);  				//insert new order id into the statement
			ps.setString(1, newOrder.getUserName());
			
			Timestamp time = new Timestamp(System.currentTimeMillis()); 	//get the time and date the order was placed on
			ps.setTimestamp(2, time);
			
			if(newOrder.getCard()!=null)						//if there is a card
				ps.setString(3,newOrder.getCard().getText());
			else
				ps.setString(3, null);
			ps.setString(4,newOrder.getOrderPickup().toString());
			ps.setString(5,newOrder.getStatus().toString());
			
			if(newOrder.getPaid())			//if paid
				ps.setInt(6, 1);
			else
				ps.setInt(6, 0);
			
			ps.setDouble(7, newOrder.getTotalPrice());
			ps.setTimestamp(8, newOrder.getReceivingTimestamp());
//			ps.setDate(10, newOrder.getReceivingDate());
//			ps.setTime(11, newOrder.getReceivingTime());
			ps.setInt(9, newOrder.getStore().getBranchID());
			ps.executeUpdate();

//			ps =con.prepareStatement("UPDATE projectx.counters SET OrdersID= ? + 1");	//increment the orders ID counter
//			ps.setInt(1, orderCounter);
//			ps.executeUpdate();
			
					//** now insert all the products in order to the productsInOrder table **//
			PreparedStatement ps3;
			for(ProductEntity product : newOrder.getProductsInOrder())
			{
				ps3 = con.prepareStatement("INSERT INTO projectx.productsinorder (OrderNum,ProductID) VALUES (?,?)"); //prepare a statement
				ps3.setInt(1, orderCounter+1);
				ps3.setInt(2, product.getProductID());
				ps3.executeUpdate();
			}
			
			if(newOrder.getOrderPickup().equals(OrderEntity.SelfOrDelivery.delivery))		//if the order has a delivery
			{
						//** insert all the order's delivery details in to the delivery table **//
				newOrder.getDeliveryDetails().setOrderID(orderCounter+1);	//set the orderID for the delivery
				PreparedStatement ps2 = con.prepareStatement("INSERT INTO projectx.delivery (OrderID,DeliveryAddress,RecipientName,PhoneNumber,DeliveryTimestamp) VALUES (?,?,?,?,?)"); //prepare a statement
				ps2.setInt(1, newOrder.getDeliveryDetails().getOrderID());
				ps2.setString(2, newOrder.getDeliveryDetails().getDeliveryAddress());
				ps2.setString(3, newOrder.getDeliveryDetails().getRecipientName());
				ps2.setString(4,	 newOrder.getDeliveryDetails().getPhoneNumber());
				ps2.setTimestamp(5, newOrder.getReceivingTimestamp());
//				ps2.setDate(5, newOrder.getDeliveryDetails().getDeliveryDate());
//				ps2.setTime(6, newOrder.getDeliveryDetails().getDeliveryTime());
				ps2.executeUpdate();
			}
	  
	  
	  returnMessage.add("Your order was placed successfully.\nWe hope to see you again.");
	  return returnMessage;
  }
  
  /** This method handles any login attempt messages received from the client.
  *
  * @param msg The message received from the client (the userName and the password)
  * @param client The connection from which the message originated.
 * @throws ClassNotFoundException 
 * @throws SQLException 
  */
	public ArrayList<String> login(ConnectionToClient client, String loginInfo)
			throws ClassNotFoundException, SQLException {
		ArrayList<String> returnMessage = new ArrayList<String>();
		String[] data = loginInfo.split("~");	//split the userName and the password
		int attempts;
		Statement stmt;
		try
		{
			con = connectToDB(); //call method to connect to DB
			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		} catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
			e.printStackTrace();
		}
		stmt = con.createStatement();

		ResultSet rs = stmt.executeQuery("SELECT * FROM projectx.users WHERE Username = '" + data[0] + "'"); //query to check if such a user exists
		if (!(rs.next())) //if user does not exists
		{
			//failed - ArrayList<String> to return in form of ["failed",reason of failure]
			returnMessage.add("failed"); //state failed to log in
			returnMessage.add("user does not exists"); //reason for failure
			return returnMessage;
		} else //if user name was found
		{
			if (ConnectedClients.isConnected(data[0]))	//if the user is already logged in to the system
			{
					//failed - ArrayList<String> to return in form of ["failed",reason of failure]
				returnMessage.add("failed"); 							//state failed to log in
				returnMessage.add("user is already logged in"); 		//reason for failure
				System.out.println("connected user tried to login again - blocked");
				return returnMessage;
			} else if (data[1].equals(rs.getString(2))) 				//if password received matches the data base 
			{
				if (rs.getInt(4) == 3)	//if user is already blocked from too many login attempts
				{ 
						//failed - ArrayList<String> to return in form of ["failed",reason of failure]
					returnMessage.add("failed");						//state failed to log in
					returnMessage.add("user is blocked"); 				//reason for failure
					return returnMessage;
				} else
				{
										//----------success - ArrayList<String> to return in form of ["success",user's type]----------//
					returnMessage.add("success"); //state succeeded to login
					returnMessage.add(rs.getString(3)); //add the type of user (customer,worker...)
					PreparedStatement ps = con.prepareStatement("UPDATE users SET LoginAttempts = 0  WHERE Username = ?"); //prepare a statement
					ps.setString(1, data[0]); //reset the user's login attempts to 0
					ps.executeUpdate();

					ConnectedClients.insertNewConnection(data[0]);	//insert the userName to the connected list of users
					return returnMessage;
				}
			} else if (!(data[1].equals(rs.getString(2)))) //if password received does not match the data base 
			{
				if (rs.getInt(4) == 3)
				{ //if user is already blocked from too many login attempts
					//failed - ArrayList<String> to return in form of ["failed",reason of failure]
					returnMessage.add("failed"); //state failed to log in
					returnMessage.add("user is blocked"); //reason for failure
					return returnMessage;
				} else
				{
						//failed - ArrayList<String> to return in form of ["failed",reason of failure,number of attempts made]
					returnMessage.add("failed"); 							//state failed to log in
					returnMessage.add("password does not match"); 			//reason for failure
					attempts = rs.getInt(4) + 1; 							//increment number of attempts made
					PreparedStatement ps = con.prepareStatement("UPDATE users SET LoginAttempts = ? WHERE Username = ?"); //prepare a statement
					ps.setString(2, data[0]);
					ps.setInt(1, attempts); 								//update the number of attempts made to log in 
					ps.executeUpdate();
					returnMessage.add(Integer.toString(attempts));			//add the number of attempts left
					return returnMessage;
				}
			}
		}
		return returnMessage;

	}
  
  
   /** This method handles any product creation messages received from the client.
   *
   * @param msg The message received from the client.
   * @param client The connection from which the message originated.
   */
  public String insertProduct(String msg, ConnectionToClient client) throws SQLException, ClassNotFoundException
  {
	  String [] data= msg.split("~");	//split the data
	  Statement stmt;
	  try
	    {
	    con = connectToDB();	//call method to connect to DB
	      
	    }
	    catch( SQLException e)	//catch exception
	    {
	      System.out.println("SQLException: " + e.getMessage() );
	     
	    }
	    System.out.println("Message received: " + msg + " from " + client);
	    								//first check if the ID already exists in the DB
	    stmt = con.createStatement();
	    ResultSet rs = stmt.executeQuery("SELECT * FROM product WHERE ProductID = '" +data[0]+"'");	//prepare a statement
	    if(!(rs.next()))	//if no such ID exists in the DB, Insert the new data
	    {
		    PreparedStatement ps = con.prepareStatement("INSERT INTO product (ProductID,ProductName,ProductType) VALUES (?,?,?)");	//prepare a statement
		    ps.setString(1,data[0]);	//insert parameters into the statement
		    ps.setString(2, data[1]);
		    ps.setString(3, data[2]);
		    ps.executeUpdate();
		    
		    return "Success";
	    }
	    
	    else	//if such Id already exists return failed String
	    	return "Failed";
	    
	  }
  
 
  	
  	/**
  	 * converts array of bytes (byte[]) into a InputStream in order to enter it to the database as blob
  	 * 
  	 * 
  	 * @param byteArray - the byte[] to convert
  	 */
/*  	public InputStream convertByteArrayToInputStream(byte [] byteArray) {
  		
  		InputStream retInputStream = new ByteArrayInputStream(byteArray);
  		
  		return retInputStream;
  	}				*/
  	
  	
  	/**
  	 * this method gets a Blob item from the data base and converts it into InputStream 
  	 * 
  	 * @param orderNum - primary key of the table we want to get the data from
  	 * @return
  	 * @throws SQLException
  	 */
  	public InputStream getInputStreamFromDB(String orderNum) throws SQLException{
  		
  		InputStream is = null;
  		Statement stmt;
  		Blob b = con.createBlob();							//Object to contain the data from the data base
  		
  		try {
  		con=connectToDB();									//Achieve connection to the data base
  		}
  		catch(Exception e) {
  			System.out.println("failed connecting to db");	
  		}
  		
  		try {
  			stmt=con.createStatement();
  		    ResultSet rs = stmt.executeQuery("SELECT * FROM projectx.complaints WHERE Ordernum = '" +orderNum+"'");	//Statement to execute
  		    if (rs.next())						//if such ID exists in the DB, get the new data
  		    	b = rs.getBlob("File");			//getting the wanted blob form the column File
  		    is=b.getBinaryStream();				//convert blob to InputStream
  		    
  		}catch(Exception e) {
  			e.printStackTrace();
  		}
  		
  		return is;							//returned value
  	}
  	
  	
  	/**
  	 * this method converts InputStream to a File
  	 * 
  	 * @param is - InputStream to convert
  	 * @throws IOException
  	 */
/*  	public void convertInputStreamToFile(InputStream is) throws IOException {
  		
  	OutputStream outputStream = new FileOutputStream(new File("/home/mdhttr/Documents/converted/img.jpg"));		//new file's output 

	int read = 0;
	byte[] bytes = new byte[1024];

	while ((read = is.read(bytes)) != -1) {				//convertion proccess
		outputStream.write(bytes, 0, read);
		}
  	}				*/
  	
  	
  	/**
  	 * this method converts InputStream object into array of bytes(byte[])
  	 * 
  	 * 
  	 * @param inStrm - InputStream to convert
  	 * @return  - array of bytes
  	 * @throws IOException 
  	 */
  /*	public byte[] convertInputStreamToByteArray(InputStream inStrm) throws IOException {
  		
  		byte [] retByteArray=null;
  		byte[] buff = new byte[4096];
  		int bytesRead = 0;

        ByteArrayOutputStream bao = new ByteArrayOutputStream();
        
        while((bytesRead = inStrm.read(buff)) != -1) {							//read the entire stream
            bao.write(buff, 0, bytesRead);
         }

         retByteArray = bao.toByteArray();
  
  		return retByteArray;
  	}								*/
    	
  	
  	
  	
  	/**
  	 * This method gets the list of stores from the DB
  	 * @return	arrayList of StoreEntity
  	 * @throws SQLException		thrown if there was an SQL exception
  	 * @throws ClassNotFoundException	ClassNotFoundException	thrown if connecting to DB failed
  	 */
	private ArrayList<StoreEntity> getAllstoresFromDB() throws SQLException, ClassNotFoundException {
		ArrayList<StoreEntity> listOfStoresFromDB = new ArrayList<StoreEntity>();
		StoreEntity store;
		Statement stmt;
		try
		{
			con = connectToDB(); //call method to connect to DB
			if (con != null)
				System.out.println("Connection to Data Base succeeded");
		} catch (SQLException e) //catch exception
		{
			System.out.println("SQLException: " + e.getMessage());
		}
		stmt = con.createStatement();
		ResultSet rs1 = stmt.executeQuery("SELECT BranchID,BranchName,BranchManager FROM projectx.store"); //get all the stores (ID,Name,managerID) in the stores table from the data base

		while (rs1.next())
		{
			store = new StoreEntity(rs1.getInt(1), rs1.getString(2), rs1.getInt(3)); //create a new instance of a store
			listOfStoresFromDB.add(store); //add the product from the data base to the list
		}

		stmt = con.createStatement();
		ResultSet rs2;
		Map<String,Double> storeDiscoutsSales; 	//holds all the discounts of the store sale
		for (StoreEntity store2 : listOfStoresFromDB)
		{
			rs2 = stmt.executeQuery("SELECT ProductID,ProductPrice FROM projectx.discount WHERE BranchID = "+ store2.getBranchID()); //get all the discounts for each store
			if(!(rs2.next()))						//if the store has no discounts
			{
				store2.setStoreDiscoutsSales(null);
			} else
			{
				storeDiscoutsSales = new HashMap<String, Double>();			//create  a new hashMap for discounts
				while (rs2.next())
				{
					storeDiscoutsSales.put(rs2.getString(1), rs2.getDouble(2)); //insert each store's discounts to a hashMap
				}
				store2.setStoreDiscoutsSales(storeDiscoutsSales); //insert the hashMap of discounts to the storeEntity
			}
		}
		
		stmt = con.createStatement();
		ResultSet rs3;
		ArrayList<Integer> listOfStoreWorkers;			//holds the list of store workers for the store entity
		for (StoreEntity store2 : listOfStoresFromDB)
		{
			rs3 = stmt.executeQuery("SELECT WorkerID FROM projectx.storeemployee WHERE BranchID = "+ store2.getBranchID()); //get all the discounts for each store
			if(!(rs3.next()))						//if the store has no workers
			{
				store2.setStoreWorkers(null);;
			} else
			{
				listOfStoreWorkers = new ArrayList<Integer>();			//create  a new arrayList for workers
				while (rs3.next())
				{
					if(rs3.getInt(1) == store2.getStoreManagerWorkerID()) {		//if the worker is the manager 
						store2.setStoreManagerWorkerID(rs3.getInt(1));			//set manager to store
					}else													//if a simple store worker
						listOfStoreWorkers.add(rs3.getInt(1)); 				//insert each store worker's worker id to the list
				}
				store2.setStoreWorkers(listOfStoreWorkers);				//set the list of workers for the store
			}
		}

		return listOfStoresFromDB;
	}
	
	
  /**
   * This method gets all of the products from the catalog
   * @return returns an ArrayList of all of the products in the catalog
   * @throws ClassNotFoundException	thrown if connecting to DB failed
   * @throws SQLException	thrown if there was an SQL exception
   */
  private ArrayList<ProductEntity> getCatalog() throws ClassNotFoundException, SQLException
  {
	  ArrayList<ProductEntity> listOfProducts = new ArrayList<ProductEntity>();
	  ProductEntity product;
	  Statement stmt;
	  try
	    {
	    con = connectToDB();	//call method to connect to DB
	    if(con!=null)
	    System.out.println("Connection to Data Base succeeded");  
	    }
	    catch( SQLException e)	//catch exception
	    {
	      System.out.println("SQLException: " + e.getMessage() );
	    }
	  stmt = con.createStatement();
	  ResultSet rs = stmt.executeQuery("SELECT * FROM projectx.catalog");	//get all the products in the catalog table from the data base
	  
	  while(rs.next())
	  {
		 //product = new ProductEntity(rs.getString(1),rs.getString(2),rs.getString(3),rs.getDouble(4),rs.getString(5),rs.getString(6));	//create a new instance of a product
		 //listOfProducts.add(product);	//add the product from the data base to the list
	  }
	  return listOfProducts;
  }
  
  
  /**
   * this method handles the creation of new customers complaint
   * 
   * 
   * @param details	- String of the complaint info ("complaint!"complaintnumber|complaintDescription)
   * @return
   * @throws SQLException
   * @throws ClassNotFoundException
   */
  	public String complaint(ComplaintEntity details) throws SQLException, ClassNotFoundException {
	
	  Statement stmt;
	  
	  try
	    {
	    con = connectToDB();	//call method to connect to DB
	    
	    if(con!=null)
	    System.out.println("Connection to Data Base succeeded");  
	    }
	  
	    catch( SQLException e)	//catch exception
	    {
	      System.out.println("SQLException: " + e.getMessage() );
	    }
	  
	  stmt = con.createStatement();
	  
	  ResultSet rs = stmt.executeQuery("SELECT * FROM projectx.order WHERE Ordernum = '" +details.getOrderID()+"'");	//prepare a statement
	    if((rs.next()))																						//if such ID exists in the DB, Insert the new data
	    {
	    	InputStream inStrm=FilesConverter.convertByteArrayToInputStream(details.getFile());
		    PreparedStatement ps = con.prepareStatement("INSERT INTO projectx.complaints (OrderNum,Description,Status,File) VALUES (?,?,?,?)");	//prepare a statement
		    ps.setInt(1, details.getOrderID());																			//insert parameters into the statement
		    ps.setString(2, details.getDescription());
		    ps.setString(3, details.getStatus().toString());
		    ps.setBlob(4, inStrm);
		    ps.executeUpdate();
		    
		    return "Success";
	    }
	  return "Order does not exist";   
  }
  	
  
  
  /**this method handles file received from user
   * 
   * 
   * @param ipAddress - ip of the client
   * @param portNo - open port for sending/receiving the file
   * @param fileLocation - Path to where file would be saved on server side
   * @throws IOException - IOException might be thrown during the process
 * @throws InterruptedException 
 * @throws SQLException 
 * @throws ClassNotFoundException 
   */
  /*public void receiveFileFromClient(String ipAddress,int portNo) throws IOException, InterruptedException, ClassNotFoundException, SQLException
  {
	  	String fileLocation="/home/mdhttr/Documents/complaints/";
	  	fileLocation+=incomingFileName;
	  	InputStream inpt = null;
		int bytesRead=0;
		int current = 0;
		FileOutputStream fileOutputStream = null;
		BufferedOutputStream bufferedOutputStream = null;
		Socket socket = null;
		try {
			//creating connection.
			//this.setPort(5556);
			
			Thread.sleep(250);
			socket = new Socket(ipAddress,portNo);
			System.out.println("connected.");
			
			// receive file
			byte [] byteArray  = new byte [6022386];					//I have hard coded size of byteArray, you can send file size from socket before creating this.
			System.out.println("Please wait downloading file");
			
			//reading file from socket
			InputStream inputStream = socket.getInputStream();
			fileOutputStream = new FileOutputStream(fileLocation);
			bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
			bytesRead = inputStream.read(byteArray,0,byteArray.length);					//copying file from socket to byteArray

			current = bytesRead;
			do {
				bytesRead =inputStream.read(byteArray, current, (byteArray.length-current));
				if(bytesRead >= 0) current += bytesRead;
			} while(bytesRead > -1);
			bufferedOutputStream.write(byteArray, 0 , current);							//writing byteArray to file
			bufferedOutputStream.flush();												//flushing buffers
			
			System.out.println("File " + fileLocation  + " downloaded ( size: " + current + " bytes read)");
			inpt=inputStream;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
			if (fileOutputStream != null) fileOutputStream.close();
			if (bufferedOutputStream != null) bufferedOutputStream.close();
			if (socket != null) socket.close();
		}
		try {
			InputStream input=new FileInputStream(fileLocation);
			this.insertPhotoToDB(input);
			this.convertInputStreamToFile(getPhotoFromDB("1111"));
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}			*/
  
  
  /** This method handles product search messages received from the client.
  *
  * @param msg The message received from the client.
  * @param client The connection from which the message originated.
 * @throws SQLException 
 * @throws ClassNotFoundException 
 * @throws IOException 
  */
/*  public ArrayList<String> getProduct(ConnectionToClient clnt,Object asked) throws SQLException, InterruptedException, ClassNotFoundException
  {
	  ArrayList<String> msg1 = new ArrayList<String>();
	  Statement stmt;
	  String str = (String) asked;								//The asked Product
	  try
	    {
	    con = connectToDB();	//call method to connect to DB
	    if(con!=null)
	    System.out.println("Connection to Data Base succeeded");  
	    }
	    catch( SQLException e)	//catch exception
	    {
	      System.out.println("SQLException: " + e.getMessage() );
	    }
	  stmt = con.createStatement();
	  ResultSet rs = stmt.executeQuery("SELECT * FROM projectx.product WHERE ProductID = "+str);	//query for extracting a prodcut's details
	  
	  
	  while(rs.next()) {	//run for the extracted data and add it to an arrayList of strings  
		  msg1.add(rs.getString(1));
		  msg1.add(rs.getString(2));
		  msg1.add(rs.getString(3));
	  }
	  
		  
	  
	 if(msg1.isEmpty())
		 System.out.println("No data found");
	  for(String s:msg1)
		System.out.println(s.toString()+" ");
	  return msg1; 
  }						*/
  	
  	public ProductEntity getProductFromDB(int productID) throws SQLException, ClassNotFoundException, IOException {
  		Statement stmt;
  		ProductEntity pe = new ProductEntity();
  		 try
 	    {
 	    con = connectToDB();	//call method to connect to DB
 	    if(con!=null)
 	    System.out.println("Connection to Data Base succeeded");  
 	    }
 	    catch( SQLException e)	//catch exception
 	    {
 	      System.out.println("SQLException: " + e.getMessage() );
 	    }
  		stmt = con.createStatement();
  		ResultSet rs = stmt.executeQuery("SELECT * FROM projectx.product WHERE ProductID = "+productID);	//query for extracting a prodcut's details
  	  
  		Blob b = con.createBlob();							//Object to contain the data from the data base
  		
  	  if(rs.next()) {						//run for the extracted data and add it to an arrayList of strings  
  		  pe.setProductID(rs.getInt(1));
  		  pe.setProductName(rs.getString(2));
  		  pe.setProductType(rs.getString(3));
  		  pe.setProductPrice(rs.getDouble(4));
  		  pe.setProductDescription(rs.getString(5));
  		 
  		  b=rs.getBlob(6);
  		  InputStream is=b.getBinaryStream();
  		  pe.setProductImage(FilesConverter.convertInputStreamToByteArray(is));
  		  
  		  pe.setProductDominantColor(rs.getString(7));
  	  }
  	  
  	  else {
  		  return null;
  	  }
  	  
  	  return pe;
  	}

    
  /**
   * This method overrides the one in the superclass.  Called
   * when the server starts listening for connections.
   */
  protected void serverStarted()
  {
    System.out.println
      ("Server listening for connections on port " + getPort());
  }
  
  /**
   * This method overrides the one in the superclass.  Called
   * when the server stops listening for connections.
   */
  protected void serverStopped()
  {
    System.out.println
      ("Server has stopped listening for connections.");
  }
  /**
   * This method handles the message received from the client
   */
  @Override
	protected synchronized void handleMessageFromClient(Object msg, ConnectionToClient client) {
	// TODO Auto-generated method stub
	  String generalMessage;
	  String operation;
	  MessageToSend messageToSend=(MessageToSend)msg;
	  Object messageFromClient=messageToSend.getMessage();		//the sent data from the client
	  
	  
	  operation=messageToSend.getOperation();					//the operation required
	//  messageFromClient=m.getMessage();
	  
	//  messageFromClient=messageFromClient.substring(messageFromClient.indexOf("!")+1,messageFromClient.length());	/**set apart the operation from the message from the client**/
	  
/////	  	operation=operation.substring(0,operation.indexOf("!"));
	  	
		ArrayList<String>retval=new ArrayList<String>();
		
		try {
		System.out.println("<user>"+operation);
		
		if(operation.equals("getCatalog"))
		{
			ArrayList<ProductEntity> listOfProducts = new ArrayList<ProductEntity>();	//an arrayList that holds all the products in the catalog
			listOfProducts = getCatalog();
			messageToSend.setMessage(listOfProducts);		//set the message for sending back to the client
			sendToAllClients(messageToSend);
			
		}
		if(operation.equals("getSelfDefinedProduct"))
		{
			ArrayList<ProductEntity> listOfProducts = new ArrayList<ProductEntity>();	//an arrayList that holds all the products in the catalog
			listOfProducts = getSelfDefinedProducts((ArrayList<String>)messageFromClient);
			messageToSend.setMessage(listOfProducts);
			sendToAllClients(messageToSend);
		}
		if(operation.equals("cancelRequest"))
		{
			String retMsg = cancelRequest((Integer)messageFromClient);
			messageToSend.setMessage(retMsg);
			sendToAllClients(messageToSend);
		}
		if(operation.equals("getCancelRequests"))
		{
			ArrayList<OrderEntity> listOfOrders = new ArrayList<OrderEntity>();
			listOfOrders = getCancelRequests();
			messageToSend.setMessage(listOfOrders);
			sendToAllClients(messageToSend);
		}
		if(operation.equals("cancelOrder"))
		{
			String retMsg = cancelOrder((Integer)messageFromClient);
			messageToSend.setMessage(retMsg);
			sendToAllClients(messageToSend);
		}
		if(operation.equals("createNewOrder"))
		{
			try
			{
			retval = createNewOrder((OrderEntity)messageFromClient);		///////////////////////////////////
			}
			catch(Exception e)
			{
				retval.add("We are sorry,Something went wrong, Please try again later.");
				e.printStackTrace();
			}
			messageToSend.setMessage(retval);
			sendToAllClients(messageToSend);
		}
		
		if(operation.equals("getCustomerOrders"))		//for getting a specific customer's list of orders
		{
			ArrayList<OrderEntity> listOfOrders = new ArrayList<OrderEntity>();
			listOfOrders = getCustomerOrders((String)messageFromClient);
			messageToSend.setMessage(listOfOrders);
			sendToAllClients(messageToSend);
		}
		
		if(operation.equals("getAllOrders"))			//for getting ALL of the orders in the DB
		{
			
		}
	
//		if(operation.equals("addProductToCatalog"))
//		{
//			
//		}
		
		if(operation.equals("createAccount")) {
			CustomerEntity custen=(CustomerEntity)messageFromClient;
			try {
			this.insertNewCustomer(custen);
			MessageToSend toClient=new MessageToSend("added","retval");
			client.sendToClient(toClient);
			
			}
			catch(Exception e) {
				MessageToSend toClient = new MessageToSend("failed","retval");
				client.sendToClient(toClient);
			}
			
		}
		
		
		if(operation.equals("getAllStores"))
		{
			ArrayList<StoreEntity> listOfAllStores = new ArrayList<StoreEntity>();		//an arrayList that holds all the stores in the DB
			listOfAllStores = getAllstoresFromDB();
			messageToSend.setMessage(listOfAllStores);		//set the message for sending back to the client
			sendToAllClients(messageToSend);
		}
		
//		if(operation.equals("addProductToCatalog"))
//		{
//			
//		}
		
		if(operation.equals("exitApp"))
		{
			this.terminateConnection((String)messageFromClient);	//calls a method to remove the user from the connected list
		}
		
		if(operation.equals("login"))
		{
			retval = this.login(client,(String)messageFromClient);
			messageToSend.setMessage(retval);	//set the message for sending back to the client
			sendToAllClients(messageToSend);	//send arraylist back to client
		}
		
		if(operation.equals("getProduct"))	//check if asked to find an existing product
		{
			ProductEntity prodEnt = this.getProductFromDB((int)messageFromClient);	//get the product's details
			messageToSend.setMessage(prodEnt);	//set the message for sending back to the client
			sendToAllClients(messageToSend);	//send arraylist back to client
		}
		
		if(operation.equals("createProduct"))
		{
			String str =(String)messageFromClient;
			str = str.substring(1,str.length());
			//String str=messageFromClient
			if((this.insertProduct((String)messageFromClient, client)).equals("Success"))	//check if asked to create a new product and check if it was create successfully
			{
				generalMessage = new String("Product was successfully added to the DataBase");
			}
			
			else
			{
				generalMessage = new String("Product was not added to the DataBase.\n(Product ID already exists)");
			}
			
			sendToAllClients(generalMessage);	//send string back to client
		}
		
		if(operation.equals("complaint")) {
			ComplaintEntity complaint = (ComplaintEntity)messageToSend.getMessage();
			this.incomingFileName=complaint.getOrderID();
			if(this.complaint(complaint).equals("Success")) {
				System.out.println("complaint added by customer");
			//	generalMessage = (String)("Added");
				messageToSend.setMessage("Added"); 		//set the message for sending back to the client
				sendToAllClients(messageToSend);
			}
			else {
				System.out.println("failed to add complaint");
			//	generalMessage = (String)("failed");
				messageToSend.setMessage("failed"); //set the message for sending back to the client
				sendToAllClients(messageToSend);
			}
		}
		
		if(operation.equals("downloadFile")) {
			
			System.out.println("Server downloading file sent from client");
			String filePath=(String)messageToSend.getMessage();
			filePath=filePath.substring(filePath.indexOf("!")+1,filePath.length());
			
			//this.receiveFileFromClient("localhost", 5556);
			}
		}
		
		catch(Exception ex) {
			ex.printStackTrace();
			}
		
	
//		sendToAllClients(messageToSend);
		
	}
  
  
  public void insertNewCustomer(CustomerEntity ce) throws SQLException {
	  
	  try {
		  con=connectToDB();
		  System.out.println("Connection to Database succeeded");
	  }
	  catch(Exception e) {
		  e.printStackTrace();
		  System.out.println("Connection to Database failed");
	  }
	  
	  PreparedStatement ps=con.prepareStatement("INSERT INTO projectx.customers (Username,Password,UserID,SubscriptionDiscount,JoinTime,Credit) VALUES (?,?,?,?,?,?)");
	  ps.setString(1, ce.getUserName());
	  ps.setString(2, ce.getPassword());
	  ps.setLong(3, ce.getCustomerID());
	  ps.setDouble(4,ce.getSubscriptionDiscount());
	  
	  Timestamp timestamp = new Timestamp(System.currentTimeMillis());		//get current time
//	  DateFormat df = new SimpleDateFormat("dd/MM/yy");
//      Date dateobj = new Date();
      ps.setTimestamp(5, timestamp);
//	  ps.setString(5, df.format(dateobj).toString());
	  ps.setLong(6, ce.getCreditCardNumber());
	  ps.executeUpdate();										//add new customer to Database
	  
	  ps=con.prepareStatement("INSERT INTO projectx.users (Username,Password,UserType,LoginAttempts) VALUES (?,?,?,?)");
	  ps.setString(1, ce.getUserName());
	  ps.setString(2, ce.getPassword());
	  ps.setString(3, "C");
	  ps.setInt(4, 0);
	  
	  ps.executeUpdate();
	  
	  System.out.println("new customer added to Database");
  }
  
  
  //Class methods ***************************************************
  
  /**
   * This method is responsible for the creation of 
   * the server instance (there is no UI in this phase).
   *
   * @param args[0] The port number to listen on.  Defaults to 5555 
   *          if no argument is entered.
   */
	 public static void main(String[] args) 
  {
    int port = 0; //Port to listen on
    
    try
    {
      port = Integer.parseInt(args[0]); //Get port from command line
    }
    catch(Throwable t)
    {
      port = DEFAULT_PORT; //Set port to 5555
    }
	
    ProjectServer sv = new ProjectServer(port);
    
    try 
    {
      sv.listen(); //Start listening for connections
    } 
    catch (Exception ex) 
    {
      System.out.println("ERROR - Could not listen for clients!");
    }
  }			


}