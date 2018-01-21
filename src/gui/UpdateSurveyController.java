package gui;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import client.Client;
import entities.SurveyEntity;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import logic.MessageToSend;

public class UpdateSurveyController implements Initializable{

	  @FXML
	    private TextField q1TxtFld;

	    @FXML
	    private TextField q2TxtFld;

	    @FXML
	    private TextField q3TxtFld;

	    @FXML
	    private TextField q4TxtFld;

	    @FXML
	    private TextField q5TxtFld;

	    @FXML
	    private TextField q6TxtFld;

	    @FXML
	    private Button updtBtn;

	    @FXML
	    private Button cnclBtn;
	    
	    @FXML
	    private CheckBox cnfrmChkBox;

	    @FXML
	    private Label hidenLbl;
	    
	    private CustomerServiceExpertMenuController csemc;
	    
	    public UpdateSurveyController() {
	    	
	    }
	    
	    
	    public void setConnectionData(CustomerServiceExpertMenuController cs) {
	    	csemc=cs;
	    }
	    
	    
	    /**
	     * the method will verify that all the updates were made correctly and if so,
	     * updates the database with the new questions
	     * 
	     * @param event for hiding the current screen
	     * @throws InterruptedException
	     * @throws IOException
	     */
	    public void sendUpdates(ActionEvent event) throws InterruptedException, IOException {
	    	
	    	if (verifyFields()) { 
	    		if(cnfrmChkBox.isSelected()){
	    		SurveyEntity surveyEnt=loadSurveyQuestions();
	    		
	    		MessageToSend toServer = new MessageToSend(surveyEnt,"updateSurveyQs");
	    		Client.getClientConnection().setDataFromUI(toServer);
	    		Client.getClientConnection().accept();
	    		
	    		while(!Client.getClientConnection().getConfirmationFromServer())
	    			Thread.sleep(100);
	    		
	    		Client.getClientConnection().setConfirmationFromServer();
	    		
	    		if(Client.getClientConnection().getMessageFromServer().getMessage().equals("Updated")) {
	    			((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
	    			this.csemc.showCustomerServiceExpertMenu();
	    			GeneralMessageController.showMessage("Questions updated successfully");
	    		}
	    		else {
	    			((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
	    			this.csemc.showCustomerServiceExpertMenu();
	    			GeneralMessageController.showMessage("Update failed\nplease contact technical support and try again later");
	    		}
	    		
	    		
	    	}
	    		else
	    			hidenLbl.setVisible(true);
	      }
	    	else
	    		GeneralMessageController.showMessage("Please fill in all fields");
	    	
	    }
	    
	    
	    /**
	     * method in order to verify that all fields were filled
	     * 
	     * @return true if everything is OK and false if there is a missing field
	     */
	    public boolean verifyFields() {
	    	if(q1TxtFld.getText().isEmpty() || q2TxtFld.getText().isEmpty() || q3TxtFld.getText().isEmpty() || q4TxtFld.getText().isEmpty() || q5TxtFld.getText().isEmpty() || q6TxtFld.getText().isEmpty())
	    		return false;
	    	return true;
	    }
	    
	    
	    /**
	     * method to set the new questions text in the survey entity
	     * 
	     * @return SurveyEntity that holds the new questions
	     */
	    public SurveyEntity loadSurveyQuestions() {
	    	
	    	SurveyEntity survey=new SurveyEntity();
	    	
    		survey.setQuestionText(1, q1TxtFld.getText());
    		survey.setQuestionText(2, q2TxtFld.getText());
    		survey.setQuestionText(3, q3TxtFld.getText());
    		survey.setQuestionText(4, q4TxtFld.getText());
    		survey.setQuestionText(5, q5TxtFld.getText());
    		survey.setQuestionText(6, q6TxtFld.getText());
    		
    		return survey;
	    }
	    
	    
	    /**
	     * method for canceling the updated and returning to the previous menu
	     * 
	     * @param event the current screen for hiding it
	     */
	    public void cancelUpdate(ActionEvent event) {
	    	((Node)event.getSource()).getScene().getWindow().hide();		//hide current window
	    	this.csemc.showCustomerServiceExpertMenu();	
	    }
	    
	    
	    /**
	     * the method fills the text fields with the current questions
	     * 
	     * @param survey - the survey's current question that should be updated
	     */
	    public void setTextFields(SurveyEntity survey) {
	    	q1TxtFld.setText(survey.getQuestion(1));
	    	q2TxtFld.setText(survey.getQuestion(2));
	    	q3TxtFld.setText(survey.getQuestion(3));
	    	q4TxtFld.setText(survey.getQuestion(4));
	    	q5TxtFld.setText(survey.getQuestion(5));
	    	q6TxtFld.setText(survey.getQuestion(6));
	    }


		@Override
		public void initialize(URL location, ResourceBundle resources) {
			// TODO Auto-generated method stub
			hidenLbl.setVisible(false);
		}
}
