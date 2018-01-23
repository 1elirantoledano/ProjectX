package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.Client;
import entities.SurveyEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.stage.Stage;
import logic.MessageToSend;

public class CustomerServiceExpertMenuController implements Initializable {

	  @FXML private Button avrBtn;
	  @FXML private Button vsrBtn;
	  @FXML private Button logBtn;
	  
	  private Client clnt;
	  private SurveyEntity survey;

	/**
	 * A necessary constructor for the App
	 * @throws IOException 
	 */
	  public CustomerServiceExpertMenuController() {
	  }
	
	  
	  /**
	   * open customer service expert menu
	   * @throws IOException
	   */
	public void showCustomerServiceExpertMenu() throws IOException
	{
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load(getClass().getResource("/gui/CustomerServiceExpertMenuBoundary.fxml").openStream());
		
		Stage primaryStage=new Stage();
		Scene scene=new Scene(root);
		CustomerServiceExpertMenuController csem = loader.getController();	//set the controller to the FindProductBoundary to control the SearchProductGUI window
		csem.setConnectionData(this.clnt);
		primaryStage.setTitle("Customer service expert menu");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	private void setConnectionData(Client clnt2) {
		this.clnt=clnt2;
	}
	
	
	/**
	 * method to handle the upload verbal report button
	 * 
	 * @param event
	 * @throws IOException 
	 */
	public void uploadReportPressed(ActionEvent event) throws IOException {
		
		((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
		
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load(getClass().getResource("/gui/UploadVerbalReportBoundary.fxml").openStream());
		UploadVerbalReportController uvrc=loader.getController();
		uvrc.setConnectionData(this);
		Stage primaryStage=new Stage();
		Scene scene=new Scene(root);
		
		primaryStage.setTitle("Upload report");
		primaryStage.setScene(scene);
		primaryStage.show();
		
	}
	
	
	/**
	 * This method loads the satisfaction report
	 * @param event	pressed Analyze survey
	 * @throws InterruptedException for sleep
	 * @throws IOException loading a window
	 */
	public void showSatisfationReport(ActionEvent event) throws InterruptedException, IOException {
		// **** get  survey from DB for the selected quarter ***//
		MessageToSend messageToSend = new MessageToSend("", "getSurvey");
		Client.getClientConnection().setDataFromUI(messageToSend); //set operation to get all stores from DB
		Client.getClientConnection().accept();
		
		while (!(Client.getClientConnection().getConfirmationFromServer())) //wait for server response
			Thread.sleep(100);
		Client.getClientConnection().setConfirmationFromServer(); //reset to false
		
		messageToSend = Client.getClientConnection().getMessageFromServer();
		this.survey = (SurveyEntity) messageToSend.getMessage(); //get the list of stores from the message class

		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load(getClass().getResource("/gui/ChainStoreManagerSatisfactionReportBoundary.fxml").openStream());
		ChainStoreManagerSatisfactionReportController c = new ChainStoreManagerSatisfactionReportController();
		c = loader.getController();
		c.showOrders(this.survey);
		Stage primaryStage = new Stage();
		Scene scene = new Scene(root);
		primaryStage.setTitle("Satisfaction report");
		primaryStage.setScene(scene);
		primaryStage.show();
	}
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub

	}
	/**
	 * when log out button pressed
	 * @param event log out button pressed
	 * @throws IOException
	 */
	public void logOut(ActionEvent event) throws IOException	
	{
		((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
		
		LoginController.signalLogOut();
		
		FXMLLoader loader = new FXMLLoader();
		Parent root = loader.load(getClass().getResource("/gui/LoginBoundary.fxml").openStream());
		Stage primaryStage=new Stage();
		Scene scene=new Scene(root);
		
		primaryStage.setTitle("Login");
		primaryStage.setScene(scene);
		primaryStage.show();
		
		GeneralMessageController.showMessage("Customer service expert "+Client.getClientConnection().getUsername()+" logged out");
		Client.getClientConnection().setClientUserName(null);
	}

}
