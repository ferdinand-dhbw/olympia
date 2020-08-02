package main.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.text.TextFlow;
import javafx.stage.Stage;
import main.application.*;

import java.io.IOException;
import java.util.HashMap;

public class AddEventController {
    /**
     * Starts the process of creating a new event as part of the front-end. <p>
     * The Steps in the user experience are Step 1: showEntryForm(...)-> Step 2: athleteSelection(...) -> Step 3: getEventAndCreateAthlete(...)
     * Explicitly in this function the user is asked to input general attributes of the athlete such as name.
     * @param owner Stage or Window that will own the created pop-up
     * @param athletes HashMap of existing Athletes
     */
    public static void showEntryForm(Stage owner, HashMap<Integer, Athlete> athletes, HashMap<Integer, Athlete> modifiedAthletes){
        try {
            Scene formScene = new Scene(FXMLLoader.load(AddEventController.class.getResource("AddEvent.fxml")));
            Stage formView = new Stage();

            ComboBox<String> seasonComboBox = (ComboBox) formScene.lookup("#seasonComboBox");
            seasonComboBox.setItems(FXCollections.observableArrayList(
                    "Summer",
                    "Winter"
            ));
            seasonComboBox.getSelectionModel().selectFirst();

            Button submitBtn = (Button) formScene.lookup("#submitBtn");
            submitBtn.setOnMouseClicked(event -> {
                if(event.getButton() == MouseButton.PRIMARY){
                    try {
                        Integer.parseInt(((TextField) formScene.lookup("#yearTextField")).getText());
                    }catch(NumberFormatException e){
                        return;
                    }
                    athleteSelection(submitEntryForm(formScene), athletes, modifiedAthletes, formView);
                }
            });

            formView.setTitle("Add new Event");
            formView.initOwner(owner);
            formView.setScene(formScene);
            formView.show();
        }catch(IOException e){
            System.err.println("Fatal: Cannot find AddEvent.fxml");
            e.printStackTrace();
        }
    }

    /**
     * Is called by submitting a form and reads the inputs to return an event
     * @param formScene Scene with form in it
     * @return Event with given parameters
     */
    protected static Event submitEntryForm(Scene formScene){
        String title = ((TextField) formScene.lookup("#titleTextField")).getText(),
                sport = ((TextField) formScene.lookup("#sportTextField")).getText(),
                year = ((TextField) formScene.lookup("#yearTextField")).getText(),
                season = ((ComboBox<String>) formScene.lookup("#seasonComboBox")).getValue(),
                city = ((TextField) formScene.lookup("#cityTextField")).getText();
        return new Event(title, sport, new Game(Integer.parseInt(year), season, city));
    }

    /**
     * Creates new window to ask the user for the age and medal <p>
     * Finally a new Participation will be created and add to the given athlete
     * @param athlete The given athlete
     * @param event The corresponding event
     * @param owner The logical owner window of the new pop-up
     */
    protected static void getAgeAndAddParticipation(Athlete athlete, HashMap<Integer, Athlete> modifiedAthletes, Event event, Stage owner){
        try{
            Stage stage = new Stage();
            Scene ageEntryScene = new Scene(FXMLLoader.load(AddEventController.class.getResource("AddAge.fxml")));

            Spinner ageSpinner = ((Spinner) ageEntryScene.lookup("#ageSpinner"));
            ageSpinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
                try{
                    if(0 > Integer.parseInt(newValue) || 99 < Integer.parseInt(newValue))
                        ageSpinner.getEditor().textProperty().set(oldValue);
                }catch(Exception e){
                    ageSpinner.getEditor().textProperty().set(oldValue);
                }
            });

            ControllerUtilities.fillTextFlow((TextFlow) ageEntryScene.lookup("#messageTextFlow"), "Input Age for: ".concat(athlete.getName()), 25);

            ComboBox<String> medalComboBox = (ComboBox) ageEntryScene.lookup("#medalComboBox");
            ObservableList<String> medalValues = FXCollections.observableArrayList();
            medalValues.add("None");
            for(Medal.Value value : Medal.Value.values())
                medalValues.add(value.toString());
            medalComboBox.setItems(medalValues);
            medalComboBox.getSelectionModel().selectFirst();

            ageEntryScene.lookup("#submitBtn").setOnMouseClicked(evt -> {
                if(evt.getButton() == MouseButton.PRIMARY){
                    int age;
                    try{
                        age = ((Spinner<Integer>) ageSpinner).getValue().intValue();
                    }catch(NumberFormatException e){
                        return;
                    }

                    athlete.addParticipation(new Participation(age, event));

                    for(Medal.Value value : Medal.Value.values())
                        if(value.toString().equals(medalComboBox.getValue())){
                            athlete.addMedal(new Medal(value, event));
                            break;
                        }

                    modifiedAthletes.put(athlete.getId(), athlete);
                    (new Serializer()).write(modifiedAthletes, "athletes.ser");
                    stage.close();
                }
            });

            stage.setScene(ageEntryScene);
            stage.initOwner(owner);
            stage.showAndWait();
        }catch(IOException e){
            System.err.println("Fatal: Cannot find AddAge.fxml");
            e.printStackTrace();
        }
    }

    /**
     * In the creation process of an event this method is called for creating a new pop-up to select the participating athletes.
     * @param event The corresponding event
     * @param athletes The HashMap of all athletes
     * @param stage The logical owner window of the new pop-up
     */
    private static void athleteSelection(Event event, HashMap<Integer, Athlete> athletes, HashMap<Integer, Athlete> modifiedAthletes, Stage stage){
        try{
            Scene athleteSelectionScene = new Scene(FXMLLoader.load(AddEventController.class.getResource("AddAthletesToEvent.fxml")));

            TableView table = (TableView) athleteSelectionScene.lookup("#table");
            ControllerUtilities.fillTable(ControllerUtilities.filterAthletes(athletes, (TextField) athleteSelectionScene.lookup("#searchBar")), table);
            table.getSelectionModel().setSelectionMode(
                    SelectionMode.MULTIPLE
            );

            ControllerUtilities.fillTextFlow((TextFlow) athleteSelectionScene.lookup("#instructionTextFlow"),
                    "Choose now the athletes participated in the event.\n" + "Use 'Ctrl' to select more than one.",
                    20);

            Button submitBtn = (Button) athleteSelectionScene.lookup("#submitBtn");
            submitBtn.setOnMouseClicked(evt -> {
                if(evt.getButton() == MouseButton.PRIMARY){
                    ObservableList<Athlete> selectedAthletes = table.getSelectionModel().getSelectedItems();
                    if(!selectedAthletes.isEmpty()){
                       selectedAthletes.forEach(athlete ->
                           getAgeAndAddParticipation(athlete, modifiedAthletes, event, stage)
                       );
                       stage.close();
                    }
                }
            });

            stage.setScene(athleteSelectionScene);
        }catch(IOException e){
            System.err.println("Fatal: Cannot find AddAthleteToEvent.fxml");
            e.printStackTrace();
        }
    }
}