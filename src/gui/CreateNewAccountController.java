package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import client.Client;
import entities.CustomerEntity;
import entities.StoreEntity;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import logic.MessageToSend;

/**
 * 
 *
 * @author Eliran Toledano
 * @author Lana Krikheli
 * @author Katya Yaakovlev
 * @author Tal Gross
 *
 * Project Name gitProjectX
 *
 */

public class CreateNewAccountController implements Initializable {

	@FXML private Label usrLbl;
    @FXML private Label idLbl;
    @FXML private Label pswrdLbl;
    @FXML private Label pswrd2Lbl;
    @FXML private Label subscrptLbl;
    @FXML private Label crdLbl;
    @FXML private TextField usrFld;
    @FXML private TextField idFld;
    @FXML private PasswordField pswrdFld;
    @FXML private PasswordField pswrd2Fld;
    @FXML private TextField crdFld;
    @FXML private Button bckBtn;
    @FXML private Button crtBtn;
    @FXML private ComboBox<String> subscrptCmb;
    @FXML private TextField emlFld;
    @FXML private TextField phnFld;
    @FXML private TextField adrsFld;


    private ObservableList<String> list;
    private StoreManagerMenuController mmc=null;
    private EditUsersPremissionController eupc=null;
    private StoreEntity store;
    
    
	/**
	 * A necessary constructor for the App
	 */
	public CreateNewAccountController() {
		
	}
	
	/**
	 * 
	 * method for connecting the previous screen
	 * in case of store manager access
	 * 
	 * @param m
	 */
	public void setConnectionData(StoreManagerMenuController m,StoreEntity store) {
		this.mmc=m;
		this.store=store;
	}
	
	/**
	 * setter for the previous controller
	 * @param edit
	 */
	public void setConnectionData(EditUsersPremissionController edit) {
		this.eupc=edit;
	}
		
	
	/**
	 *This method initializes the combo box
	 * and defines the values showed on the combobox
	 * 
	 */
	private void subscriptionComboBox()
	{
		ArrayList<String> al = new ArrayList<String>();	
		al.add("Monthly");
		al.add("Yearly");
		al.add("None");
				
		list = FXCollections.observableArrayList(al);
		subscrptCmb.setItems(list);
	
	}
	
	
	/**
	 * when create button pressed
	 * check if all required fields are filled in
	 * @return false if there is empty required field
	 */
	public boolean checkRequiredFields() {
		if(usrFld.getText().isEmpty() ||emlFld.getText().isEmpty() || phnFld.getText().isEmpty() || idFld.getText().isEmpty() || pswrdFld.getText().isEmpty() || pswrd2Fld.getText().isEmpty() || adrsFld.getText().isEmpty() || subscrptCmb.getSelectionModel().isEmpty())
			return false;

		return true;
	}
	
	
	/**
	 * when "create" button pressed
	 * checks if all data is correct
	 * checks if all required fields are filled in
	 * @param event
	 * @throws IOException
	 * @throws InterruptedException 
	 */
	public void createNewUser(ActionEvent event) throws IOException, InterruptedException {							////////*hide window if neccessary param ActionEvent event -->event(bla bla).hide()
		if(checkRequiredFields()) 												//check required fields are ok
			if(!pswrdFld.getText().equals(pswrd2Fld.getText())) {				//check matching passwords
				GeneralMessageController.showMessage("Passwords are not the same\nPlease try again");
				return;
			}
			else {
				try {
					Long.parseLong(idFld.getText());
				}
				catch(Exception e) {
					GeneralMessageController.showMessage("Illegal ID\nPlease try again later");
					return;
				}
				
				CustomerEntity cust=new CustomerEntity();
				cust.setUserName(usrFld.getText());						//set Fields of the new customer
				cust.setID(Long.parseLong(idFld.getText()));
				cust.setPassword(pswrdFld.getText());
				cust.setSubscriptionDiscount((String)subscrptCmb.getValue());
				cust.setEmailAddress(emlFld.getText());
				cust.setPhoneNumber(phnFld.getText());
				cust.setAddress(adrsFld.getText());

				if(!crdFld.getText().isEmpty()) 								//if credit card is entered
					cust.setCreditCardNumber(Long.parseLong(crdFld.getText()));
				
				
				MessageToSend msg=new MessageToSend(cust, "createAccount");			//defining the job for the server
				Client.getClientConnection().setDataFromUI(msg);					//arranging the sending of the wanted message
				Client.getClientConnection().accept();								//sending data to server
				
				
				while(!Client.getClientConnection().getConfirmationFromServer())
					Thread.sleep(100);
				
				Client.getClientConnection().setConfirmationFromServer();
				
				if(Client.getClientConnection().getMessageFromServer().getMessage().equals("added")) {
					((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
					this.mmc.setStore(this.store);
					this.mmc.showMenu();										//open previous menu
					GeneralMessageController.showMessage("New customer "+cust.getUserName()+" was added succesfully");
				}
				else {
					GeneralMessageController.showMessage("There was a problem, please try again");
				}
			}
		else {
			GeneralMessageController.showMessage("Please fill in all the required fields");
		}
		
	}
	
	
	/**
	 * when back button pressed
	 * @param event pressed back button
	 * @throws IOException
	 * @throws InterruptedException 
	 */	
	public void bckBtnHandler(ActionEvent event) throws IOException, InterruptedException {
		((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
		if(this.mmc!=null) {
			this.mmc.setStore(this.store);
			this.mmc.showMenu();										//open previous menu
		}
		else
			this.eupc.showEdittingOptions(event);
		return;
	}

	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		subscriptionComboBox();
	}

	/**
	 * Setter for the text field
	 * @param text
	 */
	public void setField(String text) {
		// TODO Auto-generated method stub
		usrFld.setText(text);
	}	
}
