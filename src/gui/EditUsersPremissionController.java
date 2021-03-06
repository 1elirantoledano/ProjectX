package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.Client;
import entities.StoreEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.stage.Stage;
import logic.MessageToSend;

/**
 * this class allows to change users permission
 * @author Eliran Toledano
 * @author Lana Krikheli
 * @author Katya Yakovlev
 * @author Tal Gross
 *
 * Project Name gitProjectX
 *
 */


public class EditUsersPremissionController implements Initializable{
	
	@FXML
	private Button bckBtn;
	
	@FXML
	private Button okBtn;
	
	@FXML
	private ComboBox<String> prmsCmb;
	
	@FXML 
	private ListView<String> userLstVw;
	
    @FXML
    private Label strIDLbl;

    @FXML
    private ComboBox strCmbBox;
	
	private AdministratorMenuController amc;
	private ObservableList<String> list;
	private ObservableList<String> users;
	private ObservableList<String> stores;
	private ArrayList<String> listOfUsers;
	private String username;
	private ArrayList<String> storesList=new ArrayList<String>();
	private ArrayList<StoreEntity> allstores;
	
	/**
	 * Necessary constructor for the APP
	 */
	public EditUsersPremissionController() {
	}
	
	/**
	 * shows all the users in database
	 * @throws InterruptedException
	 */
	public void getUsers() throws InterruptedException
	{
		
		MessageToSend message = new MessageToSend("", "getAllUsers");
		Client.getClientConnection().setDataFromUI(message);						//set the data and the operation to send from the client to the server
		Client.getClientConnection().accept();										//sends to server
		while(!Client.getClientConnection().getConfirmationFromServer())			//wait until server replies
			Thread.sleep(100);
		Client.getClientConnection().setConfirmationFromServer();		//reset confirmation to false
		MessageToSend m = Client.getClientConnection().getMessageFromServer();
		listOfUsers = (ArrayList<String>)m.getMessage();
		
		message.setMessage("");
		message.setOperation("getAllStores");
		Client.getClientConnection().setDataFromUI(message);	//set operation to get all stores from DB
		Client.getClientConnection().accept();
		while(!(Client.getClientConnection().getConfirmationFromServer()))		//wait for server response
			Thread.sleep(100);
		Client.getClientConnection().setConfirmationFromServer(); 				//reset to false
		m = Client.getClientConnection().getMessageFromServer();
		allstores=(ArrayList<StoreEntity>)m.getMessage();
		
		for(StoreEntity store:allstores)
			storesList.add(store.getBranchName());
		
		stores = FXCollections.observableArrayList(storesList);
		strCmbBox.setItems(stores);
		
		this.userLstVw.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);		//unable multiple selection
		
		this.users = FXCollections.observableArrayList();		//the observable list to enter to the list  view
		
		for(String user : listOfUsers)		//build list view to contain all orders
		{
			this.users.add("User name: "+user.split("~")[0] + " User type: "+user.split("~")[1]);
		}
		
		this.userLstVw.setItems(this.users);		//set items to the list

	}
	
	public void setConnectionData(AdministratorMenuController m)
	{
		this.amc=m;
	}
	
	
	/**
	 * method for handling the edit of user permissions
	 * 
	 * @param event - event for hiding the current screen
	 * @throws InterruptedException
	 * @throws IOException
	 */
	public void editPermissions(ActionEvent event) throws InterruptedException, IOException {
	
		if( !prmsCmb.getSelectionModel().isEmpty() &&  userLstVw.getSelectionModel().getSelectedItem()!=null) {
			String s=userLstVw.getSelectionModel().getSelectedItem();
			s = s.substring(s.indexOf(":")+1, s.length());
			username=s.substring(1,s.indexOf("U")-1);
			String []userAndPermission=new String[3];
			userAndPermission[0]=username;
			userAndPermission[1]=permissionsParser(prmsCmb.getValue());
			
			if(userAndPermission[1]=="SM" || userAndPermission[1]=="SW")
				if(!strCmbBox.getSelectionModel().isEmpty())
					userAndPermission[2]=Integer.toString(branchToIDParser((String)strCmbBox.getSelectionModel().getSelectedItem()));
				else {
					GeneralMessageController.showMessage("Please choose branch");
					return;
				}
			
			MessageToSend toServer=new MessageToSend(userAndPermission,"updatePermission");
			Client.getClientConnection().setDataFromUI(toServer);
			Client.getClientConnection().accept();
			
			while(!Client.getClientConnection().getConfirmationFromServer())
				Thread.sleep(100);
			Client.getClientConnection().setConfirmationFromServer();
			
			if(Client.getClientConnection().getMessageFromServer().getMessage().equals("Updated")) {
				((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
				this.amc.showMenu();
				GeneralMessageController.showMessage("User permission updated successfully");
			}
			else if(Client.getClientConnection().getMessageFromServer().getMessage().equals("noUser")) {
				GeneralMessageController.showMessage("User does not exist");
			}
			else if(Client.getClientConnection().getMessageFromServer().getMessage().equals("customer")) {
			//	((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
				this.createNewCustomer(event);
			}
			else {
				((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
				this.amc.showMenu();
				GeneralMessageController.showMessage("Update failed\nPlease contact technical support and try again later");
			}
		}
		else {
			GeneralMessageController.showMessage("Please complete the proccess");
		}
		

	}
	
	
	/**
	 * method for changing the permission to Customer
	 * 
	 * @param event - for hiding the current screen
	 * @throws IOException
	 */
	private void createNewCustomer(ActionEvent event) throws IOException {
		// TODO Auto-generated method stub
		((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
		 FXMLLoader loader = new FXMLLoader();
		 Parent root = loader.load(getClass().getResource("CreateNewAccountBoundary.fxml").openStream());				//new window to open
		 
		 CreateNewAccountController cna=loader.getController();
		 cna.setConnectionData(this);
		 cna.setField(username);
		 Stage primaryStage=new Stage();
		 Scene scene=new Scene(root);
			
		primaryStage.setTitle("New Accout");
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	
	/**
	 * This method translates the user type to a short user type for saving in DB
	 * @param str the full type
	 * @return the short type
	 */
	private String permissionsParser(String str) {
		if(str.equals("Customer"))
			return "C";
		else if(str.equals("Store Worker"))
			return "SW";
		else if(str.equals("Store Manager"))
			return "SM";
		else if (str.equals("Customer Service Expert"))
			return "CSE";
		else if(str.equals("Customer Service Worker"))
			return "CSW";
		else
			return null;
	}
	
	
	/**
	 * method for getting the branchID from the branch name
	 * 
	 * @param branchName - the branch name we want the ID for
	 * @return
	 */
	private int branchToIDParser(String branchName) {
		for(StoreEntity se:allstores)
			if(se.getBranchName().equals(branchName))
				return se.getBranchID();
		return -1;
	}


	/**
	 * Permissions varieties in combobox
	 */
	private void premissionsComboBox()
	{
		ArrayList<String> al = new ArrayList<String>();	
		al.add("Store Worker");
		al.add("Store Manager");
		al.add("Customer Service Expert");
		al.add("Customer Service Worker");
		
		list = FXCollections.observableArrayList(al);
		prmsCmb.setItems(list);
	}
	
	
	/**
	 * when back button pressed
	 * @param event pressed back button
	 * @throws IOException
	 */	
	public void bckBtnHandler(ActionEvent event) throws IOException {
		((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
		this.amc.showMenu();										//open previous menu
		return;
	}
	
	
	/**
	 * This method loads the edit permission boundary
	 * @param event pressed edit permission
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public void showEdittingOptions(ActionEvent event) throws IOException, InterruptedException {

		 FXMLLoader loader = new FXMLLoader();
		 Parent root = loader.load(getClass().getResource("/gui/EditUsersPremissionBoundary.fxml").openStream());				//new window to open
		 EditUsersPremissionController eup = loader.getController();
		 eup.setConnectionData(this.amc);
		 eup.getUsers();
		 Stage primaryStage=new Stage();
		 Scene scene=new Scene(root);
			
			primaryStage.setTitle("User's premission");
			primaryStage.setScene(scene);
			primaryStage.show();
	}
	
	/**
	 * This method sets the labels visible
	 */
	public void setLabels() {
		strCmbBox.setVisible(false);
		strIDLbl.setVisible(false);
	}
	
	/**
	 * This method shows the comboxbox and label for a store worker or manager to choose a store
	 * @param event
	 */
	public void selectedWorkerOrManager(ActionEvent event) {
		String s = prmsCmb.getSelectionModel().getSelectedItem();
		if(s.equals("Store Worker") || s.equals("Store Manager"))
		{
			strCmbBox.setVisible(true);
			strIDLbl.setVisible(true);
		}
		else {
			setLabels();
		}
	}
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		premissionsComboBox();
	}

	
	
}
