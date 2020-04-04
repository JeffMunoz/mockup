package Ambassadrs;

import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Properties;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

public class Controller {

  private Statement stmt = null;
  private Connection conn = null;

  private final ArrayList<Ambassador> arrayOfAmbassadors = new ArrayList();
  private ObservableList<Ambassador> pbList;
  @FXML private TableView tableOfAmbassadors = new TableView();
  @FXML private Label prErrorLabel = new Label();
  @FXML private Label deleteErrorLabel = new Label();
  @FXML private Label updateErrorLabel = new Label();
  @FXML private Label totalHoursLabel = new Label();
  @FXML private TextField ambassadorName = new TextField();
  @FXML private TextField ambassadorUin = new TextField();
  @FXML private TextField ambassadorMajor = new TextField();
  @FXML private TextField ambassadorHours = new TextField();

  public void initialize(){

    pbList = FXCollections.observableList(arrayOfAmbassadors);
    TableColumn<Ambassador, Integer> UIN = new TableColumn<>("UIN");
    UIN.setCellValueFactory(new PropertyValueFactory<>("uin"));
    TableColumn<Ambassador, String> currentName = new TableColumn<>("Name");
    currentName.setCellValueFactory(new PropertyValueFactory<>("name"));
    TableColumn<Ambassador, String> major = new TableColumn<>("Major");
    major.setCellValueFactory(new PropertyValueFactory<>("major"));
    TableColumn<Ambassador, Integer> hours = new TableColumn<>("Hours");
    hours.setCellValueFactory(new PropertyValueFactory<>("hours"));

    tableOfAmbassadors.getColumns().addAll(UIN);
    tableOfAmbassadors.getColumns().add(currentName);
    tableOfAmbassadors.getColumns().add(major);
    tableOfAmbassadors.getColumns().add(hours);


    populateAmbassadorTable();
  }


  @FXML
  private void addAmbassador(ActionEvent event) {
    initializeDB();
    try {
      //erase the label text
      prErrorLabel.setText("");
      // non constant string replaced with a prepared statement
      String preparedStm = "INSERT INTO Ambassador(uin, name, major) VALUES ( ?, ?, ?);";
      PreparedStatement preparedStatement = conn.prepareStatement(preparedStm);
      // adds the parameters to the preparedStatement
      if (!ambassadorName.getText().isEmpty() && !ambassadorUin.getText().isEmpty() && !ambassadorMajor.getText().isEmpty()) {
        preparedStatement.setInt(1, Integer.parseInt(ambassadorUin.getText()));
        preparedStatement.setString(2, ambassadorName.getText());
        preparedStatement.setString(3, ambassadorMajor.getText());

        preparedStatement.executeUpdate();
        populateAmbassadorTable();

        preparedStatement.close();
        ambassadorName.clear();
        ambassadorUin.clear();
        ambassadorMajor.clear();
      } else {
        prErrorLabel.setText("All fields are needed. Please try again.");
      }

    } catch (SQLException e) {
      e.printStackTrace();
    }catch (NumberFormatException e){
      prErrorLabel.setText("Check UIN");
    }
    closeDb();
  }



  /** This method will delete the selected Ambassador from the database. */
  public void removeAmbassador() {
    initializeDB();
    try {
      deleteErrorLabel.setText("");
      updateErrorLabel.setText("");
      Ambassador ambassadorToBeDeleted = (Ambassador) tableOfAmbassadors.getSelectionModel().getSelectedItem();
      int delete = ambassadorToBeDeleted.getUin();
      String preparedStm = "DELETE FROM Ambassador WHERE UIN = ?;";
      PreparedStatement preparedStatement;
      preparedStatement = conn.prepareStatement(preparedStm);
      preparedStatement.setInt(1, delete);
      preparedStatement.executeUpdate();
      ObservableList<Ambassador> allAmbassador = tableOfAmbassadors.getItems();
      ObservableList<Ambassador> selectedAmbassador =
          tableOfAmbassadors.getSelectionModel().getSelectedItems();
      selectedAmbassador.forEach(allAmbassador::remove);

      preparedStatement.close();
      totalNumberOfHours();
    } catch (SQLException e) {
      e.printStackTrace();
    }catch(RuntimeException e){
      deleteErrorLabel.setText("An Ambassador must be selected for it to be deleted");
    }

    populateAmbassadorTable();
    closeDb();
  }
  /** This method will update the hours of the selected Ambassador. */
  public void updateAmbassadorHours() {
    initializeDB();
    try {
      updateErrorLabel.setText("");
      deleteErrorLabel.setText("");
      Ambassador ambassadorToUpdate = (Ambassador) tableOfAmbassadors.getSelectionModel().getSelectedItem();
      int uinOfAmbassadorTobeUpdated = ambassadorToUpdate.getUin();
      double currentHours = ambassadorToUpdate.getHours();
      double earnedHours = Double.parseDouble(ambassadorHours.getText());
      String preparedStm = "UPDATE Ambassador SET HOURS = ? WHERE UIN = ?;";
      PreparedStatement preparedStatement;
      preparedStatement = conn.prepareStatement(preparedStm);
     // System.out.println(currentHours+earnedHours);
      preparedStatement.setDouble(1, (currentHours+earnedHours));
      preparedStatement.setInt(2, uinOfAmbassadorTobeUpdated);

      preparedStatement.executeUpdate();
      ambassadorHours.clear();

      populateAmbassadorTable();

      preparedStatement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }catch(NullPointerException e){
      updateErrorLabel.setText("An Ambassador must be selected to update the hours");
    }catch (NumberFormatException ex){
      updateErrorLabel.setText("A number is need in the field above");
    }

    populateAmbassadorTable();
    totalNumberOfHours();
    closeDb();
  }

  /** This method will update the hours of the selected Ambassador. */
  public void totalNumberOfHours() {
    initializeDB();
    try {
      double totalHours = 0;
      updateErrorLabel.setText("");
      deleteErrorLabel.setText("");
      String preparedStm = "SELECT * FROM Ambassador;";
      PreparedStatement preparedStatement;
      ResultSet rs;
      preparedStatement = conn.prepareStatement(preparedStm);
      rs = preparedStatement.executeQuery();

      while(rs.next()){
        int hours = rs.getInt("hours");
        totalHours += hours;
      }
      System.out.println(totalHours);

     totalHoursLabel.setText("The Ambassadors have performed a total of " + totalHours + " Service Learning Hours");

      preparedStatement.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }catch(RuntimeException e){
      totalHoursLabel.setText("No data available");
    }

    populateAmbassadorTable();
    closeDb();
  }

  /** This method populates the table view in the GUI. */
  public void populateAmbassadorTable() {
    initializeDB();
    String sql = "SELECT * FROM Ambassador;";
    ResultSet rs;
    arrayOfAmbassadors.clear();
    pbList.clear();
    try {
      rs = stmt.executeQuery(sql);
      // This loop is used to out put the table of data to the table view
      while (rs.next()) {
        String name = rs.getString("name");
        String major = rs.getString("major");
        int uin = rs.getInt("uin");
        int numberOfHours = rs.getInt("hours");

        Ambassador tempAmbassador = new Ambassador(name, major, uin, numberOfHours);
        arrayOfAmbassadors.add(tempAmbassador);
      }
      pbList = FXCollections.observableList(arrayOfAmbassadors);
      tableOfAmbassadors.setItems(pbList);

    } catch (SQLException e) {
      e.printStackTrace();
    }
    closeDb();
  }


  /** This method initializes the connection to the data base. */
  public void initializeDB() {

    final String Jdbc_Driver = "org.h2.Driver";
    final String Db_Url = "jdbc:h2:./res/AmbassadorsDB";
    final String user = "";
    final String pass;

    try {
      Properties prop = new Properties();
      prop.load(new FileInputStream("res/properties"));
      pass = prop.getProperty("password");
      Class.forName(Jdbc_Driver);
      conn = DriverManager.getConnection(Db_Url, user, pass);
      stmt = conn.createStatement();
    } catch (ClassNotFoundException e) {
      // e.printStackTrace();
      System.out.println("Check H2 Dependencies");
    } catch (SQLException e) {
      e.printStackTrace();
      System.out.println("Error in SQL please try again");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /** This method closes the connection to the data base. */
  public void closeDb() {
    try {
      stmt.close();
      conn.close();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
