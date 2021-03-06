package gui;

import java.io.IOException;
import java.net.URL;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.TimeUnit;

import client.Client;
import entities.ComplaintEntity;
import entities.OrderEntity;
import entities.ProductEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import logic.MessageToSend;
import logic.TimeCalculation;
/**
 * This class is the controller for the customer order details boundary
 * 
 * CustomerOrderDetailsController.java
 *
 * @author Eliran Toledano
 * @author Lana Krikheli
 * @author Katya Yakovlev
 * @author Tal Gross
 *
 * Project Name gitProjectX
 *
 */
public class CustomerOrderDetailsController implements Initializable {

	@FXML
	private Button bckBtn;

	@FXML
	private TextArea cmplntDtlsTxtArea;

	@FXML
	private TextArea repliedTxtAre;

	@FXML
	private Label cmplntFldOnLable;

	@FXML
	private Button cnclOrdrBtn;

	@FXML
	private Button cmplntBtn;

	@FXML
	private ListView<String> ordrLstVw;

	@FXML
	private TreeView<String> dtlsTrVw;

	ObservableList<String> listOfOrders;
	ArrayList<OrderEntity> arraylistOfOrders;
	ArrayList<ComplaintEntity> listOfComplaints;
	
	/**
	 * This method builds the ListView that contains the customer's orders
	 * @throws InterruptedException
	 */
	public void showOrders() throws InterruptedException
	{
					//** get complaints **//
		ArrayList<String> msg = new ArrayList<String>();
		msg.add("all");
		msg.add("all");
		MessageToSend message = new MessageToSend(msg, "getComplaints");
		Client.getClientConnection().setDataFromUI(message);							//set the data and the operation to send from the client to the server
		Client.getClientConnection().accept();										//sends to server
		while(!Client.getClientConnection().getConfirmationFromServer())			//wait until server replies
			Thread.sleep(100);
		Client.getClientConnection().setConfirmationFromServer();		//reset confirmation to false
		MessageToSend m = Client.getClientConnection().getMessageFromServer();
		listOfComplaints = (ArrayList<ComplaintEntity>)m.getMessage();
		
		
		message = new MessageToSend(Client.getClientConnection().getUsername(), "getCustomerOrders");
		Client.getClientConnection().setDataFromUI(message);							//set the data and the operation to send from the client to the server
		Client.getClientConnection().accept();										//sends to server
		while(!Client.getClientConnection().getConfirmationFromServer())			//wait until server replies
			Thread.sleep(100);
		Client.getClientConnection().setConfirmationFromServer();		//reset confirmation to false
		m = Client.getClientConnection().getMessageFromServer();
		
		arraylistOfOrders = (ArrayList<OrderEntity>)m.getMessage();
		
		this.ordrLstVw.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);		//unable multiple selection
		
		this.listOfOrders = FXCollections.observableArrayList();		//the observable list to enter to the list  view
		
		for(OrderEntity order : arraylistOfOrders)		//build list view to contain all orders
		{
			this.listOfOrders.add("Order number "+order.getOrderID());
		}
		
		this.ordrLstVw.setItems(this.listOfOrders);		//set items to the list

		EventHandler<MouseEvent> mouseEventHandle = (MouseEvent event) -> {
			showOrderDetails(event);
		};

		this.ordrLstVw.addEventHandler(MouseEvent.MOUSE_CLICKED, mouseEventHandle); 

	}
	
	/**
	 * This method builds the treeView of a specific order chosen
	 * @param event order chosen from the list
	 */
	public void showOrderDetails(MouseEvent event)
	{
		if(this.ordrLstVw.getSelectionModel().isEmpty())	//if nothing was selected
			return;
		TreeItem<String> root;

		root = new TreeItem<>(); //set the root for the tree
		root.setExpanded(true); //set it to expanded by default  

	
					//**------Build a treeView that contains all the order's details-------**//
			for (OrderEntity order : arraylistOfOrders)
			{
				if(this.ordrLstVw.getSelectionModel().getSelectedItem().substring(13).equals(order.getOrderID().toString())) 		//check which order was selected
				{
				TreeItem<String> OrderID = new TreeItem<>("Order number : "+order.getOrderID().toString()); //set the branch as the product's name to be the parent of it's details
							/* Set all the order's details to be leaves on the branch */
				TreeItem<String> totalprice = new TreeItem<>("Total price : "+order.getTotalPrice().toString()); 		//create a new leaf
				OrderID.getChildren().add(totalprice); 									//set as a child 
				
				TreeItem<String> orderTime = new TreeItem<>("Order Time : "+order.getOrderTime().toString()); 		//create a new leaf
				OrderID.getChildren().add(orderTime); 									//set as a child 
				TreeItem<String> store = new TreeItem<>("From Store named : "+order.getStore().getBranchName());
				OrderID.getChildren().add(store);
				
				if(order.getCard() != null)		//if there is a card
				{
					TreeItem<String> card = new TreeItem<>("Added card : "+order.getCard().getText());
					OrderID.getChildren().add(card);
				}
				
				TreeItem<String> orderPickup = new TreeItem<>(order.getOrderPickup().toString());
				OrderID.getChildren().add(orderPickup);
				TreeItem<String> receivingTime = new TreeItem<>("Receiving time : "+order.getReceivingTimestamp().toString());
				OrderID.getChildren().add(receivingTime);
				TreeItem<String> status = new TreeItem<>("Order Status : "+order.getStatus().toString());
				OrderID.getChildren().add(status);
				TreeItem<String> paid = new TreeItem<>("Order paid ? : "+((order.getPaid())? "Yes":"No"));
				OrderID.getChildren().add(paid);
				TreeItem<String> paymendMethod = new TreeItem<>("Payment method : "+order.getPaymendMethod().toString());
				OrderID.getChildren().add(paymendMethod);
				
				if(order.getDeliveryDetails() != null)		//if there is a delivery
				{
				TreeItem<String> deliveryDetails = new TreeItem<>("Delivery Details");
				OrderID.getChildren().add(deliveryDetails);
				TreeItem<String> recipientName = new TreeItem<>("Recipient name : "+order.getDeliveryDetails().getRecipientName());
				deliveryDetails.getChildren().add(recipientName);
				TreeItem<String> address = new TreeItem<>("Recipient address : "+order.getDeliveryDetails().getDeliveryAddress());
				deliveryDetails.getChildren().add(address);
				TreeItem<String> phoneNumber = new TreeItem<>("Recipient phone number : "+order.getDeliveryDetails().getPhoneNumber());
				deliveryDetails.getChildren().add(phoneNumber);
				TreeItem<String> deliveryprice = new TreeItem<>("Delivery fee : "+order.getDeliveryDetails().getDeliveryPrice().toString());
				deliveryDetails.getChildren().add(deliveryprice);
				}
							//** the lists of products in the order **//
				TreeItem<String> products = new TreeItem<>("Products in the order");
				OrderID.getChildren().add(products);
				
				for(ProductEntity product : order.getProductsInOrder())
				{
					TreeItem<String> productName = new TreeItem<>("Product name : "+product.getProductName());
					products.getChildren().add(productName);
					TreeItem<String> productID = new TreeItem<>("Product ID : "+product.getProductID().toString());
					productName.getChildren().add(productID);
					TreeItem<String> ProductType = new TreeItem<>("Product Type : "+product.getProductType());
					productName.getChildren().add(ProductType);
					TreeItem<String> productDescription = new TreeItem<>("Product description : "+product.getProductDescription());
					productName.getChildren().add(productDescription);
					Double price;
					TreeItem<String> productPrice;
					if((price = order.getStore().getStoreDiscoutsSales().get(product.getProductID())) != null)	//check for disocunt
						productPrice = new TreeItem<>("Product price : "+price.toString());
					else
						productPrice = new TreeItem<>("Product price : "+product.getProductPrice().toString());
					productName.getChildren().add(productPrice);
					
				}
				
				this.repliedTxtAre.setVisible(false);
				this.cmplntDtlsTxtArea.setVisible(false);
				this.cmplntFldOnLable.setVisible(false);
				
				showComplaintDetails(order.getOrderID());
				
				root.getChildren().add(OrderID);
				OrderID.setExpanded(true); 				//set the tree expanded by default
				
				
				}
			}
			
		this.dtlsTrVw.setRoot(root);
		this.dtlsTrVw.setShowRoot(false); //make root expanded every time it starts
	}
	
	/**
	 * This method shows the complaint details
	 * @param oderID	the order id
	 */
	private void showComplaintDetails(Integer orderID)
	{
		for(ComplaintEntity complaint : this.listOfComplaints)
		{
			if(complaint.getOrderID() == orderID)
			{
				this.cmplntDtlsTxtArea.setVisible(true);
				this.repliedTxtAre.setVisible(true);
				this.cmplntFldOnLable.setVisible(true);
				this.cmplntDtlsTxtArea.setText(complaint.getDescription());		//set the complaint details to the text area
				if(complaint.getStatus().toString().equals("handled"))			//if handled, show the reply
					this.repliedTxtAre.setText(complaint.getStoreReply());
				else
					this.repliedTxtAre.setText("Processing");
				this.cmplntFldOnLable.setText(complaint.getFiledOn().toString());		//set the time it was filed
			}
		}
	}
	
	
	/**
	 * This method loads the main menu window
	 * @param event	pressed back
	 * @throws IOException	
	 */
	public void backToMainMenu(ActionEvent event) throws IOException {

		((Node) event.getSource()).getScene().getWindow().hide(); //hide current window
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load(getClass().getResource("/gui/CustomerOrderMenuBoundary.fxml").openStream());
		CustomerOrderController ord = loader.getController(); //set the controller to the FindProductBoundary to control the SearchProductGUI window
		Stage primaryStage = new Stage();
		Scene scene = new Scene(root);
		scene.getStylesheets().add("/gui/LoginStyle.css");

		primaryStage.setTitle("Order");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	/**
	 * This method handle the customer's cancellation request
	 * @param event	pressed cancel order
	 * @throws IOException 
	 * @throws InterruptedException 
	 */
	public void pressedCancelOrder(ActionEvent event) throws IOException, InterruptedException 
	{
		OrderEntity orderToCancel = null;
		
		if(this.ordrLstVw.getSelectionModel().isEmpty())	//if nothing was selected
			GeneralMessageController.showMessage("No order was selected.");
		else
		{
			for(OrderEntity order : this.arraylistOfOrders)
			{
				if(order.getOrderID() == Integer.parseInt(this.ordrLstVw.getSelectionModel().getSelectedItem().substring(13)))	//get the selected order
					orderToCancel=order;
			}
			if(TimeCalculation.calculateTimeDifference(orderToCancel.getReceivingTimestamp(), new Timestamp(System.currentTimeMillis()) ) <0)
			{
				GeneralMessageController.showMessage("Order receiving time has passed,\nCan not cancel the order.");
				return;
			}
			
			if(!orderToCancel.getStatus().equals(OrderEntity.OrderStatus.cancel_requested) && !orderToCancel.getStatus().equals(OrderEntity.OrderStatus.cancelled))		//check if order cancelation was already asked
			{
			Alert alert = new Alert(AlertType.CONFIRMATION);		//set new alert for confirmation
			alert.setTitle("Confirmation");
			alert.setHeaderText("Cancel order");
			alert.setContentText("Are you sure you want to cancel this order");

			Optional<ButtonType> result = alert.showAndWait();
			if (result.get() == ButtonType.OK){						//if pressed OK
				MessageToSend message = new MessageToSend(Integer.parseInt(this.ordrLstVw.getSelectionModel().getSelectedItem().substring(13)), "cancelRequest");	//get the order ID

				Client.getClientConnection().setDataFromUI(message);							//set the data and the operation to send from the client to the server
				Client.getClientConnection().accept();										//sends to server
				while(!Client.getClientConnection().getConfirmationFromServer())			//wait until server replies
					Thread.sleep(100);
				Client.getClientConnection().setConfirmationFromServer();		//reset confirmation to false
				MessageToSend m = Client.getClientConnection().getMessageFromServer();
				GeneralMessageController.showMessage((String)m.getMessage());
				showOrders();
			} else {
			    // ... user chose CANCEL or closed the dialog
			}
			}
			else
				GeneralMessageController.showMessage("Cancellation was already requested for the order");
			
		}
		
		

	}

	/**
	 * This method handles the complaint
	 * @param event pressed complaint button
	 * @throws IOException 
	 */
	public void PressedComplaint(ActionEvent event) throws IOException {
		
		((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
		 FXMLLoader loader = new FXMLLoader();
		 Parent root = loader.load(getClass().getResource("/gui/ComplaintBoundary.fxml").openStream());
		 ComplaintController cmpc= loader.getController();	//set the controller to the ComplaintBoundary to control the SearchProductGUI window
		 cmpc.setOrderID(Integer.parseInt(this.ordrLstVw.getSelectionModel().getSelectedItem().substring(13)));
		Stage primaryStage=new Stage();
		Scene scene=new Scene(root);
		scene.getStylesheets().add("/gui/LoginStyle.css");

		primaryStage.setTitle("Complaint");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		try
		{
			showOrders();
		} catch (InterruptedException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
