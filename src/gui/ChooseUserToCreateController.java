package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

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
import javafx.stage.Stage;
/**
 * this class shows user account
 * and allows to change user details
 * @author Eliran Toledano
 * @author Lana Krikheli
 * @author Katya Yakovlev
 * @author Tal Gross
 *
 * Project Name gitProjectX
 *
 */
public class ChooseUserToCreateController implements Initializable{

	@FXML private ComboBox<String> prmCmb;
	@FXML private Button bckBtn;
	@FXML private Button okBtn;
	
	AdministratorMenuController am;
	
	private ObservableList<String> list;
	 
	 /**
	  * necessary empty constructor for the App
	  */
	 public ChooseUserToCreateController() {
	}
	 
	 /**
	  * This method saves the connection to the previous menu
	  * @param m the menu controller
	  */
	 public void setConnectionData(AdministratorMenuController m)
		{
			this.am=m;
		}
	 
	 /**
	  * Permissions varieties
	  */
		private void premissionsComboBox()
		{
			ArrayList<String> al = new ArrayList<String>();	
			al.add("Service Expert");
			al.add("Customer Service Worker");
			al.add("Store Worker");
			al.add("Store Manager");
			al.add("Chain Manager");
			al.add("Chain Worker");
			
			list = FXCollections.observableArrayList(al);
			prmCmb.setItems(list);
		}
		/**
		 * when OK button pressed
		 * @param event OK button pressed
		 * @throws InterruptedException
		 * @throws IOException
		 */
		public void enterOK(ActionEvent event) throws InterruptedException, IOException 
		{
			if(!(prmCmb.getSelectionModel().isEmpty()))					//check if permission choose
			{
				String entity = "";
				if(prmCmb.getSelectionModel().getSelectedItem().equals("Store Manager") || prmCmb.getSelectionModel().getSelectedItem().equals("Store Worker"))
				{
					((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
					entity=prmCmb.getSelectionModel().getSelectedItem();
				
					FXMLLoader loader = new FXMLLoader();
					Parent root = loader.load(getClass().getResource("/gui/CreateNewStoreManagerOrWorkerBoundary.fxml").openStream());
				
					CreateNewStoreManagerOrWorkerController cns = loader.getController();
					Stage primaryStage=new Stage();
					Scene scene=new Scene(root);
					cns.setEntity(entity);
					cns.setConnectionData(this);
				
					primaryStage.setTitle("Create");
					primaryStage.setScene(scene);
					primaryStage.show();
				}
				else
				{
					((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
					entity=prmCmb.getSelectionModel().getSelectedItem();
				
					FXMLLoader loader = new FXMLLoader();
					Parent root = loader.load(getClass().getResource("/gui/CreateNewServiceExpertOrWorkerBoundary.fxml").openStream());
				
					CreateNewServiceExpertOrWorkerController cns = loader.getController();
					Stage primaryStage=new Stage();
					Scene scene=new Scene(root);
					cns.setEntity(entity);
					cns.setConnectionData(this);
				
					primaryStage.setTitle("Create");
					primaryStage.setScene(scene);
					primaryStage.show();
				}
				
			}
			else			
			{
				GeneralMessageController.showMessage("Please choose user's premission");
			}
				
		}
	
		/**
		 * when back button pressed
		 * @param event pressed back button
		 * @throws IOException
		 */	
		public void bckBtnHandler(ActionEvent event) throws IOException {
			((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
			 FXMLLoader loader = new FXMLLoader();
			 Parent root = loader.load(getClass().getResource("/gui/AdministratorMenuBoundary.fxml").openStream());
			 
			Stage primaryStage=new Stage();
			Scene scene=new Scene(root);
			primaryStage.setTitle("Administrator menu");
			primaryStage.setScene(scene);
			primaryStage.show();
		}
		
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		premissionsComboBox();
	}

}
