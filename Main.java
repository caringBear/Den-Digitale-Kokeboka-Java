//Naviger med piltastene


import com.fazecast.jSerialComm.SerialPort;
import javafx.animation.PauseTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Duration;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;


public class Main extends Application {
   
   private static Stage vindu;
   
   private static SceneKlasse currentScene;
   private static SceneKlasse forsideScene;
   private static SceneKlasse antallPersonerScene;
   private static SceneKlasse oppskriftValgScene;
   private static SceneKlasse tacoIngredienserScene;
   private static SceneKlasse tacoOppskriftScene;
   private static SceneKlasse ferdigScene;
   private static SceneKlasse mineVarerScene;
   private static SceneKlasse scannetVareScene;
   
   private static Label tacoOverskriftLabel;
   private static Label kjoettdeigMengdeLabel;
   private static Label tacolefserMengdeLabel;
   private static Label tomatMengdeLabel;
   private static Label salatMengdeLabel;
   private static Label paprikaMengdeLabel;
   private static Label maisMengdeLabel;
   private static Label ostMengdeLabel;
   private static Label tacokrMengdeLabel;
   private static Label roemmeMengdeLabel;
   private static Label kjoettdeigTotaltL;
   
   private static int mengdeKjoettdeig;
   private static int mengdeTacolefser;
   private static int mengdeSalat;
   private static int mengdeMais;
   private static int mengdePaprika;
   private static int mengdeOst;
   private static int mengdeTomat;
   
   private static AnchorPane kjoettdeigPane;
   private static AnchorPane paprikaPane;
   private static AnchorPane ostPane;
   private static VBox mineVarerVBox;
   
   private static String argsPassed = "";
   private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
   
   private ImageView[] personBildeImageView;
   private Clip beepClip;
   private int antallPersonerSomVises;
   private Label antPersonerLabel;
   
   public static void main(String[] args) {
      if (args.length > 0) {
         argsPassed = args[0];
         launch(args);
      } else {
         launch();
      }
   }
   
   public void start(Stage vindu) {
      this.vindu = vindu;
      
      setupArduino();
      setupLyd();
      
      scannetVareScene = new ScannetVareKlasse();
      ferdigScene = new FerdigKlasse();
      forsideScene = new ForsideKlasse(lagForside());
      antallPersonerScene = new AntallPersonerKlasse(lagAntallPersoner());
      oppskriftValgScene = new OppskriftValgKlasse(lagOppskriftValg());
      tacoIngredienserScene = new TacoIngredienserKlasse(lagTacoIngredienser());
      tacoOppskriftScene = new TacoOppskriftKlasse(lagTacoOppskrift());
      
      mineVarerVBox = lagMineVarer();
      mineVarerScene = new TacoOppskriftKlasse(mineVarerVBox);
   
      
      //tastatur-events, for testing, kan fjernes
      vindu.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
         String knapp = "" + event.getCode();
         switch (knapp) {
            case "UP":
               currentScene.trykketOPP();
               break;
            case "DOWN":
               currentScene.trykketNED();
               break;
            case "LEFT":
               currentScene.trykketTILBAKE();
               break;
            case "RIGHT":
               currentScene.trykketOK();
               break;
            case "ALT":
               currentScene.trykketSLETT();
               break;
            case "ESCAPE":
               System.exit(0);
               break;
         }
      });
      
      //vindu.setResizable(false);
      vindu.setY(0);
      vindu.setWidth(800);
      vindu.setHeight(480);
      //For kjoering i Windows, kommenter ut foer deployment til Raspberry
      //vindu.setWidth(800+16);
      vindu.setHeight(480 + 39 + 300);
      //Sett foerste scene til forsiden
      endreSceneTil(forsideScene);
      if (!argsPassed.equals("0")) vindu.show();
   }
   
   public void startTask(Scanner data) {
      Runnable task = new Runnable() {
         @Override
         public void run() {
            runTask(data);
         }
      };
      
      Thread backgroundThread = new Thread(task);
      backgroundThread.setDaemon(true);
      backgroundThread.start();
   }
   
   public void runTask(Scanner data) {
      while (data.hasNextLine()) {
         String linje = data.nextLine();
         Platform.runLater(new Runnable() {
            @Override
            public void run() {
               System.out.println(linje);
               if (linje.equals("OPP")) {
                  currentScene.trykketOPP();
               } else if (linje.equals("NED")) {
                  currentScene.trykketNED();
               } else if (linje.equals("TILBAKE")) {
                  currentScene.trykketTILBAKE();
               } else if (linje.equals("OK")) {
                  currentScene.trykketOK();
               } else if (linje.equals("SLETT")) {
                  currentScene.trykketSLETT();
               } else if (linje.equals("exit")) {
                  System.exit(0);
               } else {
                  beepLyd();
                  SceneKlasse tempScene = currentScene;
                  ScannetVareKlasse.vareScannet(linje);
                  endreSceneTil(scannetVareScene);
                  PauseTransition delay = new PauseTransition(Duration.seconds(2));
                  delay.setOnFinished(event -> endreSceneTil(tempScene));
                  delay.play();
                  
                  oppdaterMineVarer(linje);
               }
            }
         });
      }
   }
   
   private static void myTask() {
      System.out.println("Running");
   }
   
   public void endreSceneNonStatic(SceneKlasse nyScene) {
      currentScene = nyScene;
      vindu.setScene(nyScene);
   }
   
   public static void endreSceneTil(SceneKlasse nyScene) {
      if (nyScene == tacoIngredienserScene) {
         oppdaterTacoMengder();
      }
      currentScene = nyScene;
      vindu.setScene(nyScene);
   }
   
   //Metode som returnerer neste scene basert paa hvilken scene som er aktiv og hvilken linje i menyen man er paa
   public static SceneKlasse finnNesteScene(int menyvalg) {
      if (currentScene == forsideScene) {
         switch (menyvalg) {
            case 1:
               return antallPersonerScene;
            case 2:
               return mineVarerScene;
         }
      } else if (currentScene == antallPersonerScene) {
         return oppskriftValgScene;
      } else if (currentScene == oppskriftValgScene) {
         switch (menyvalg) {
            case 3:
               return tacoIngredienserScene;
         }
      } else if (currentScene == tacoIngredienserScene) {
         return tacoOppskriftScene;
      } else if (currentScene == tacoOppskriftScene) {
         return ferdigScene;
      }
      return null;
   }
   
   //Metode som returnerer forrige scene basert paa hvilken scene som er aktiv og hvilken linje i menyen man er paa
   public static SceneKlasse finnForrigeScene(int menyvalg) {
      if (currentScene == forsideScene) {
         switch (menyvalg) {
            case 1:
               return antallPersonerScene;
            case 2:
               return mineVarerScene;
         }
      } else if (currentScene == antallPersonerScene) {
         return forsideScene;
      } else if (currentScene == oppskriftValgScene) {
         return antallPersonerScene;
      } else if (currentScene == tacoIngredienserScene) {
         return oppskriftValgScene;
      } else if (currentScene == tacoOppskriftScene) {
         return tacoIngredienserScene;
      } else if (currentScene == ferdigScene) {
         return tacoOppskriftScene;
      }
      return null;
   }
   
   //Spiller av pipelyd naar vare scannes
   private void beepLyd() {
      beepClip.setFramePosition(0);
      beepClip.start();
   }
   
   //Lager og returnerer nytt bilde, denne maaten aa gjoere det paa luket ut en bug under kjoering paa Raspberry Pi
   public static Image lagBilde(String bildefil) {
      return new Image(Main.class.getResource("bilder/" + bildefil + ".png").toExternalForm());
   }
   
   public static void oppdaterTacoMengder() {
      int antallPersoner = AntallPersonerKlasse.antallPersonerSomVises;
      
      tacoOverskriftLabel.setText("Ingredienser til taco for " + AntallPersonerKlasse.antallPersonerSomVises + " personer:");
      
      kjoettdeigMengdeLabel.setText("" + 125 * antallPersoner + "g");
      
      ostMengdeLabel.setText("" + 50 * antallPersoner + "g");
      
      tacolefserMengdeLabel.setText("" + antallPersoner * 2);
      if (antallPersoner == 1) {
         tomatMengdeLabel.setText("0,5");
         maisMengdeLabel.setText("0,25");
         salatMengdeLabel.setText("0,25");
         paprikaMengdeLabel.setText("0,25");
         tacokrMengdeLabel.setText("0,25");
         roemmeMengdeLabel.setText("0,25");
         
      } else if (antallPersoner == 2) {
         tomatMengdeLabel.setText("1");
         maisMengdeLabel.setText("0,5");
         salatMengdeLabel.setText("0,5");
         paprikaMengdeLabel.setText("0,5");
         tacokrMengdeLabel.setText("0,5");
         roemmeMengdeLabel.setText("0,25");
         
      } else if (antallPersoner == 3) {
         tomatMengdeLabel.setText("1,5");
         maisMengdeLabel.setText("0,75");
         salatMengdeLabel.setText("0,75");
         paprikaMengdeLabel.setText("0,75");
         tacokrMengdeLabel.setText("0,75");
         roemmeMengdeLabel.setText("0,25");
         
      } else if (antallPersoner == 4) {
         tomatMengdeLabel.setText("2");
         maisMengdeLabel.setText("1");
         salatMengdeLabel.setText("1");
         paprikaMengdeLabel.setText("1");
         tacokrMengdeLabel.setText("1");
         roemmeMengdeLabel.setText("0,25");
         
      } else if (antallPersoner == 5) {
         tomatMengdeLabel.setText("2,5");
         maisMengdeLabel.setText("1,25");
         salatMengdeLabel.setText("1,25");
         paprikaMengdeLabel.setText("1,25");
         tacokrMengdeLabel.setText("1,25");
         roemmeMengdeLabel.setText("0,25");
         
      }
   }
   
   VBox lagForside() {
      AnchorPane logoTopp = new AnchorPane();
      logoTopp.setPrefSize(800, 94);
      logoTopp.setStyle("-fx-background-color: #FEFEFE;");
      
      Image logoToppBilde = lagBilde("logoTopp");
      ImageView logoToppImageView = new ImageView(logoToppBilde);
      AnchorPane.setLeftAnchor(logoToppImageView, 238.0);
      AnchorPane.setTopAnchor(logoToppImageView, 29.0);
      logoTopp.getChildren().add(logoToppImageView);
      
      
      AnchorPane kokebok = new AnchorPane();
      kokebok.setPrefSize(800, 68);
      kokebok.setStyle("-fx-background-color: #6CCEF5;");
      
      Label kokebokLabel = new Label("Kokebok");
      kokebokLabel.setFont(new Font("Arial", 30));
      kokebokLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(kokebokLabel, 188.0);
      AnchorPane.setTopAnchor(kokebokLabel, 16.0);
      kokebok.getChildren().add(kokebokLabel);
      
      
      AnchorPane mineVarer = new AnchorPane();
      mineVarer.setPrefSize(800, 68);
      mineVarer.setStyle("-fx-background-color: #FEFEFE;");
      
      Label mineVarerLabel = new Label("Mine varer");
      mineVarerLabel.setFont(new Font("Arial", 30));
      mineVarerLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(mineVarerLabel, 188.0);
      AnchorPane.setTopAnchor(mineVarerLabel, 16.0);
      mineVarer.getChildren().add(mineVarerLabel);
      
      
      AnchorPane ukesmeny = new AnchorPane();
      ukesmeny.setPrefSize(800, 68);
      ukesmeny.setStyle("-fx-background-color: #FEFEFE;");
      
      Label ukesmenyLabel = new Label("Ukesmeny");
      ukesmenyLabel.setFont(new Font("Arial", 30));
      ukesmenyLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(ukesmenyLabel, 188.0);
      AnchorPane.setTopAnchor(ukesmenyLabel, 16.0);
      ukesmeny.getChildren().add(ukesmenyLabel);
      
      
      AnchorPane bunn = new AnchorPane();
      bunn.setPrefSize(800, 244 - 68 * 1);
      bunn.setStyle("-fx-background-color: #FEFEFE;");
      
      
      VBox rot = new VBox(logoTopp, kokebok, mineVarer, ukesmeny, bunn);
      rot.setSpacing(2);
      rot.setStyle("-fx-background-color: #1E1E1E;");
      
      //Scene scene = new Scene(rot);
      return rot;
   }
   
   AnchorPane lagAntallPersoner() {
      antallPersonerSomVises = 3; //default er 3
      
      AnchorPane heleSkjermen = new AnchorPane();
      heleSkjermen.setStyle("-fx-background-color: #FEFEFE;");
      
      Image antallPersonerBilde = lagBilde("antallPersoner");
      ImageView antallPersonerImageView = new ImageView(antallPersonerBilde);
      heleSkjermen.getChildren().add(antallPersonerImageView);
      
      
      antPersonerLabel = new Label(Integer.toString(antallPersonerSomVises));
      antPersonerLabel.setFont(new Font("Arial", 40));
      antPersonerLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(antPersonerLabel, 264.0);
      AnchorPane.setTopAnchor(antPersonerLabel, 220.0);
      heleSkjermen.getChildren().add(antPersonerLabel);
      
      personBildeImageView = new ImageView[6];
      for (int i = 1; i < 6; i++) {
         Image personBilde = lagBilde("person");
         personBildeImageView[i] = new ImageView(personBilde);
         AnchorPane.setLeftAnchor(personBildeImageView[i], 394.0 + (i - 1) * 62.0);
         AnchorPane.setTopAnchor(personBildeImageView[i], 192.0);
         heleSkjermen.getChildren().add(personBildeImageView[i]);
         if (i > antallPersonerSomVises) personBildeImageView[i].setVisible(false);
      }
      
      return heleSkjermen;
   }
   
   VBox lagOppskriftValg() {
      double insetFraVenstre = 150.0;
      
      AnchorPane toppOverskrift = new AnchorPane();
      toppOverskrift.setPrefSize(800, 94);
      toppOverskrift.setStyle("-fx-background-color: #FEFEFE;");
      
      Label toppOverskriftLabel = new Label("Her er oppskrifter basert på dine varer:");
      toppOverskriftLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
      toppOverskriftLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(toppOverskriftLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(toppOverskriftLabel, 29.0);
      toppOverskrift.getChildren().add(toppOverskriftLabel);
      
      
      AnchorPane pastaBolognese = new AnchorPane();
      pastaBolognese.setPrefSize(800, 68);
      pastaBolognese.setStyle("-fx-background-color: #FEFEFE;");
      
      Label pastaBologneseLabel = new Label("Pasta bolognese");
      pastaBologneseLabel.setFont(new Font("Arial", 30));
      pastaBologneseLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(pastaBologneseLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(pastaBologneseLabel, 16.0);
      pastaBolognese.getChildren().add(pastaBologneseLabel);
      
      
      AnchorPane gulrotsuppe = new AnchorPane();
      gulrotsuppe.setPrefSize(800, 68);
      gulrotsuppe.setStyle("-fx-background-color: #FEFEFE;");
      
      Label gulrotsuppeLabel = new Label("Gulrotsuppe");
      gulrotsuppeLabel.setFont(new Font("Arial", 30));
      gulrotsuppeLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(gulrotsuppeLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(gulrotsuppeLabel, 16.0);
      gulrotsuppe.getChildren().add(gulrotsuppeLabel);
      
      
      AnchorPane taco = new AnchorPane();
      taco.setPrefSize(800, 68);
      taco.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tacoLabel = new Label("Taco");
      tacoLabel.setFont(new Font("Arial", 30));
      tacoLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tacoLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tacoLabel, 16.0);
      taco.getChildren().add(tacoLabel);
      
      
      AnchorPane meksikanskGryte = new AnchorPane();
      meksikanskGryte.setPrefSize(800, 68);
      meksikanskGryte.setStyle("-fx-background-color: #FEFEFE;");
      
      Label meksikanskGryteLabel = new Label("Meksikansk gryte");
      meksikanskGryteLabel.setFont(new Font("Arial", 30));
      meksikanskGryteLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(meksikanskGryteLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(meksikanskGryteLabel, 16.0);
      meksikanskGryte.getChildren().add(meksikanskGryteLabel);
      
      
      AnchorPane padThai = new AnchorPane();
      padThai.setPrefSize(800, 68);
      padThai.setStyle("-fx-background-color: #FEFEFE;");
      
      Label padThaiLabel = new Label("Pad Thai");
      padThaiLabel.setFont(new Font("Arial", 30));
      padThaiLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(padThaiLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(padThaiLabel, 16.0);
      padThai.getChildren().add(padThaiLabel);
      
      
      AnchorPane eplepai = new AnchorPane();
      eplepai.setPrefSize(800, 68);
      eplepai.setStyle("-fx-background-color: #FEFEFE;");
      
      Label eplepaiLabel = new Label("Eplepai");
      eplepaiLabel.setFont(new Font("Arial", 30));
      eplepaiLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(eplepaiLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(eplepaiLabel, 16.0);
      eplepai.getChildren().add(eplepaiLabel);
      
      
      AnchorPane pinneKjoett = new AnchorPane();
      pinneKjoett.setPrefSize(800, 68);
      pinneKjoett.setStyle("-fx-background-color: #FEFEFE;");
      
      Label pinneKjoettLabel = new Label("Pinnekjøtt");
      pinneKjoettLabel.setFont(new Font("Arial", 30));
      pinneKjoettLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(pinneKjoettLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(pinneKjoettLabel, 16.0);
      pinneKjoett.getChildren().add(pinneKjoettLabel);
      
      
      AnchorPane bunn = new AnchorPane();
      bunn.setPrefSize(800, 104);
      bunn.setStyle("-fx-background-color: #FEFEFE;");
      
      
      VBox rot = new VBox(toppOverskrift, pastaBolognese, gulrotsuppe, taco, meksikanskGryte, padThai, eplepai, pinneKjoett, bunn);
      rot.getChildren().remove(1);
      rot.getChildren().add(1, pastaBolognese);
      rot.setSpacing(2);
      rot.setStyle("-fx-background-color: #1E1E1E;");
      
      return rot;
   }
   
   VBox lagTacoIngredienser() {
      double insetFraVenstre = 150.0;
      double insetFraVenstreIngredienser = 200;
      double insetFraHoeyreMengde = 680;
      double insetFraHoeyreBilder = 50;
      
      int antallPersoner = AntallPersonerKlasse.antallPersonerSomVises;
      
      
      AnchorPane toppOverskrift = new AnchorPane();
      toppOverskrift.setPrefSize(800, 94);
      toppOverskrift.setStyle("-fx-background-color: #FEFEFE;");
      
      tacoOverskriftLabel = new Label();
      tacoOverskriftLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
      tacoOverskriftLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tacoOverskriftLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tacoOverskriftLabel, 29.0);
      toppOverskrift.getChildren().add(tacoOverskriftLabel);
      
      
      AnchorPane kjoettdeig = new AnchorPane();
      kjoettdeig.setPrefSize(800, 68);
      kjoettdeig.setStyle("-fx-background-color: #FEFEFE;");
      
      Label kjoettdeigLabel = new Label("Kjøttdeig");
      kjoettdeigLabel.setFont(new Font("Arial", 30));
      kjoettdeigLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(kjoettdeigLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(kjoettdeigLabel, 16.0);
      kjoettdeig.getChildren().add(kjoettdeigLabel);
      
      kjoettdeigMengdeLabel = new Label();
      kjoettdeigMengdeLabel.setFont(new Font("Arial", 30));
      kjoettdeigMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(kjoettdeigMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(kjoettdeigMengdeLabel, 16.0);
      kjoettdeig.getChildren().add(kjoettdeigMengdeLabel);
      
      Image kjoettdeigBilde = lagBilde("tacoSmaa/kjoettdeig");
      ImageView kjoettdeigImageView = new ImageView(kjoettdeigBilde);
      AnchorPane.setRightAnchor(kjoettdeigImageView, insetFraHoeyreBilder - 5);
      AnchorPane.setTopAnchor(kjoettdeigImageView, 10.0);
      kjoettdeig.getChildren().add(kjoettdeigImageView);
      
      
      AnchorPane tacolefser = new AnchorPane();
      tacolefser.setPrefSize(800, 68);
      tacolefser.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tacolefserLabel = new Label("Tacolefser");
      tacolefserLabel.setFont(new Font("Arial", 30));
      tacolefserLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tacolefserLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tacolefserLabel, 16.0);
      tacolefser.getChildren().add(tacolefserLabel);
      
      tacolefserMengdeLabel = new Label();
      tacolefserMengdeLabel.setFont(new Font("Arial", 30));
      tacolefserMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(tacolefserMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(tacolefserMengdeLabel, 16.0);
      tacolefser.getChildren().add(tacolefserMengdeLabel);
      
      Image tacolefserBilde = lagBilde("tacoSmaa/taco");
      ImageView tacolefserImageView = new ImageView(tacolefserBilde);
      AnchorPane.setRightAnchor(tacolefserImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(tacolefserImageView, 10.0);
      tacolefser.getChildren().add(tacolefserImageView);
      
      
      AnchorPane tomat = new AnchorPane();
      tomat.setPrefSize(800, 68);
      tomat.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tomatLabel = new Label("Tomater");
      tomatLabel.setFont(new Font("Arial", 30));
      tomatLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tomatLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tomatLabel, 16.0);
      tomat.getChildren().add(tomatLabel);
      
      tomatMengdeLabel = new Label();
      tomatMengdeLabel.setFont(new Font("Arial", 30));
      tomatMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(tomatMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(tomatMengdeLabel, 16.0);
      tomat.getChildren().add(tomatMengdeLabel);
      
      Image tomatBilde = lagBilde("tacoSmaa/tomat");
      ImageView tomatImageView = new ImageView(tomatBilde);
      AnchorPane.setRightAnchor(tomatImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(tomatImageView, 10.0);
      tomat.getChildren().add(tomatImageView);
      
      
      AnchorPane salat = new AnchorPane();
      salat.setPrefSize(800, 68);
      salat.setStyle("-fx-background-color: #FEFEFE;");
      
      Label salatL = new Label("Salathode");
      salatL.setFont(new Font("Arial", 30));
      salatL.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(salatL, insetFraVenstre);
      AnchorPane.setTopAnchor(salatL, 16.0);
      salat.getChildren().add(salatL);
      
      salatMengdeLabel = new Label();
      salatMengdeLabel.setFont(new Font("Arial", 30));
      salatMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(salatMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(salatMengdeLabel, 16.0);
      salat.getChildren().add(salatMengdeLabel);
      
      Image salatBilde = lagBilde("tacoSmaa/salat");
      ImageView salatImageView = new ImageView(salatBilde);
      AnchorPane.setRightAnchor(salatImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(salatImageView, 10.0);
      salat.getChildren().add(salatImageView);
      
      
      AnchorPane mais = new AnchorPane();
      mais.setPrefSize(800, 68);
      mais.setStyle("-fx-background-color: #FEFEFE;");
      
      Label maisLabel = new Label("Boks mais");
      maisLabel.setFont(new Font("Arial", 30));
      maisLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(maisLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(maisLabel, 16.0);
      mais.getChildren().add(maisLabel);
      
      maisMengdeLabel = new Label();
      maisMengdeLabel.setFont(new Font("Arial", 30));
      maisMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(maisMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(maisMengdeLabel, 16.0);
      mais.getChildren().add(maisMengdeLabel);
      
      Image maisBilde = lagBilde("tacoSmaa/mais");
      ImageView maisImageView = new ImageView(maisBilde);
      AnchorPane.setRightAnchor(maisImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(maisImageView, 10.0);
      mais.getChildren().add(maisImageView);
      
      
      AnchorPane ost = new AnchorPane();
      ost.setPrefSize(800, 68);
      ost.setStyle("-fx-background-color: #FEFEFE;");
      
      Label ostLabel = new Label("Hvitost");
      ostLabel.setFont(new Font("Arial", 30));
      ostLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(ostLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(ostLabel, 16.0);
      ost.getChildren().add(ostLabel);
      
      ostMengdeLabel = new Label();
      ostMengdeLabel.setFont(new Font("Arial", 30));
      ostMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(ostMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(ostMengdeLabel, 16.0);
      ost.getChildren().add(ostMengdeLabel);
      
      Image ostBilde = lagBilde("tacoSmaa/ost");
      ImageView ostImageView = new ImageView(ostBilde);
      AnchorPane.setRightAnchor(ostImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(ostImageView, 10.0);
      ost.getChildren().add(ostImageView);
      
      
      AnchorPane paprika = new AnchorPane();
      paprika.setPrefSize(800, 68);
      paprika.setStyle("-fx-background-color: #FEFEFE;");
      
      Label paprikaLabel = new Label("Paprika");
      paprikaLabel.setFont(new Font("Arial", 30));
      paprikaLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(paprikaLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(paprikaLabel, 16.0);
      paprika.getChildren().add(paprikaLabel);
      
      paprikaMengdeLabel = new Label();
      paprikaMengdeLabel.setFont(new Font("Arial", 30));
      paprikaMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(paprikaMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(paprikaMengdeLabel, 16.0);
      paprika.getChildren().add(paprikaMengdeLabel);
      
      Image paprikaBilde = lagBilde("tacoSmaa/paprika");
      ImageView paprikaImageView = new ImageView(paprikaBilde);
      AnchorPane.setRightAnchor(paprikaImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(paprikaImageView, 10.0);
      paprika.getChildren().add(paprikaImageView);
      
      
      AnchorPane tacokr = new AnchorPane();
      tacokr.setPrefSize(800, 68);
      tacokr.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tacokrLabel = new Label("Pose tacokrydder");
      tacokrLabel.setFont(new Font("Arial", 30));
      tacokrLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tacokrLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tacokrLabel, 16.0);
      tacokr.getChildren().add(tacokrLabel);
      
      tacokrMengdeLabel = new Label();
      tacokrMengdeLabel.setFont(new Font("Arial", 30));
      tacokrMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(tacokrMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(tacokrMengdeLabel, 16.0);
      tacokr.getChildren().add(tacokrMengdeLabel);
      
      Image tacokrBilde = lagBilde("tacoSmaa/tacokrydder");
      ImageView tacokrImageView = new ImageView(tacokrBilde);
      AnchorPane.setRightAnchor(tacokrImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(tacokrImageView, 10.0);
      tacokr.getChildren().add(tacokrImageView);
      
      
      AnchorPane roemme = new AnchorPane();
      roemme.setPrefSize(800, 68);
      roemme.setStyle("-fx-background-color: #FEFEFE;");
      
      Label roemmeLabel = new Label("Boks lettrømme");
      roemmeLabel.setFont(new Font("Arial", 30));
      roemmeLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(roemmeLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(roemmeLabel, 16.0);
      roemme.getChildren().add(roemmeLabel);
      
      roemmeMengdeLabel = new Label();
      roemmeMengdeLabel.setFont(new Font("Arial", 30));
      roemmeMengdeLabel.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(roemmeMengdeLabel, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(roemmeMengdeLabel, 16.0);
      roemme.getChildren().add(roemmeMengdeLabel);
      
      Image roemmeBilde = lagBilde("tacoSmaa/roemme");
      ImageView roemmeImageView = new ImageView(roemmeBilde);
      AnchorPane.setRightAnchor(roemmeImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(roemmeImageView, 10.0);
      roemme.getChildren().add(roemmeImageView);
      
      
      AnchorPane bunn = new AnchorPane();
      bunn.setPrefSize(800, 30);
      bunn.setStyle("-fx-background-color: #FEFEFE;");
      
      
      oppdaterTacoMengder();
      
      VBox rot = new VBox(toppOverskrift, kjoettdeig, tacokr, tacolefser, tomat, salat, roemme, mais, ost, paprika, bunn);
      rot.setSpacing(2);
      rot.setStyle("-fx-background-color: #1E1E1E;");
      
      return rot;
   }
   
   VBox lagTacoOppskrift() {
      double insetFraVenstre = 150.0;
      double insetFraVenstreIngredienser = 200;
      double insetFraHoeyreCheck = 680;
      double insetChecksFraToppen = 17;
      
      
      Image check = lagBilde("check");
      
      
      AnchorPane toppOverskrift = new AnchorPane();
      toppOverskrift.setPrefSize(800, 94);
      toppOverskrift.setStyle("-fx-background-color: #FEFEFE;");
      
      Label toppOverskriftLabel = new Label("Fremgangsmåte:");
      toppOverskriftLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
      toppOverskriftLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(toppOverskriftLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(toppOverskriftLabel, 29.0);
      toppOverskrift.getChildren().add(toppOverskriftLabel);
      
      
      AnchorPane kjoettdeig = new AnchorPane();
      kjoettdeig.setPrefSize(800, 68);
      kjoettdeig.setStyle("-fx-background-color: #FEFEFE;");
      
      Label lagKjoettdeigLabel = new Label("Stek kjøttdeigen");
      lagKjoettdeigLabel.setFont(new Font("Arial", 30));
      lagKjoettdeigLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(lagKjoettdeigLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(lagKjoettdeigLabel, 16.0);
      kjoettdeig.getChildren().add(lagKjoettdeigLabel);
      
      ImageView kjoettdeigCheck = new ImageView(check);
      AnchorPane.setRightAnchor(kjoettdeigCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(kjoettdeigCheck, insetChecksFraToppen);
      kjoettdeigCheck.setVisible(false);
      kjoettdeig.getChildren().add(kjoettdeigCheck);
      
      
      AnchorPane kjoettdeig2 = new AnchorPane();
      kjoettdeig2.setPrefSize(800, 68);
      kjoettdeig2.setStyle("-fx-background-color: #FEFEFE;");
      
      Label lagKjoettdeigLabel2 = new Label("Ha tacokrydder i kjøttdeigen");
      lagKjoettdeigLabel2.setFont(new Font("Arial", 30));
      lagKjoettdeigLabel2.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(lagKjoettdeigLabel2, insetFraVenstre);
      AnchorPane.setTopAnchor(lagKjoettdeigLabel2, 16.0);
      kjoettdeig2.getChildren().add(lagKjoettdeigLabel2);
      
      ImageView kjoettdeigCheck2 = new ImageView(check);
      AnchorPane.setRightAnchor(kjoettdeigCheck2, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(kjoettdeigCheck2, insetChecksFraToppen);
      kjoettdeigCheck2.setVisible(false);
      kjoettdeig2.getChildren().add(kjoettdeigCheck2);
      
      
      AnchorPane tomat = new AnchorPane();
      tomat.setPrefSize(800, 68);
      tomat.setStyle("-fx-background-color: #FEFEFE;");
      
      Label lagTomatLabel = new Label("Vask og kutt opp tomat");
      lagTomatLabel.setFont(new Font("Arial", 30));
      lagTomatLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(lagTomatLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(lagTomatLabel, 16.0);
      tomat.getChildren().add(lagTomatLabel);
      
      ImageView tomatCheck = new ImageView(check);
      AnchorPane.setRightAnchor(tomatCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(tomatCheck, insetChecksFraToppen);
      tomatCheck.setVisible(false);
      tomat.getChildren().add(tomatCheck);
      
      
      AnchorPane ost = new AnchorPane();
      ost.setPrefSize(800, 68);
      ost.setStyle("-fx-background-color: #FEFEFE;");
      
      Label lagOstLabel = new Label("Riv osten");
      lagOstLabel.setFont(new Font("Arial", 30));
      lagOstLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(lagOstLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(lagOstLabel, 16.0);
      ost.getChildren().add(lagOstLabel);
      
      ImageView ostCheck = new ImageView(check);
      AnchorPane.setRightAnchor(ostCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(ostCheck, insetChecksFraToppen);
      ostCheck.setVisible(false);
      ost.getChildren().add(ostCheck);
      
      
      AnchorPane salat = new AnchorPane();
      salat.setPrefSize(800, 68);
      salat.setStyle("-fx-background-color: #FEFEFE;");
      
      Label lagSalatLabel = new Label("Vask salaten");
      lagSalatLabel.setFont(new Font("Arial", 30));
      lagSalatLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(lagSalatLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(lagSalatLabel, 16.0);
      salat.getChildren().add(lagSalatLabel);
      
      ImageView salatCheck = new ImageView(check);
      AnchorPane.setRightAnchor(salatCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(salatCheck, insetChecksFraToppen);
      salatCheck.setVisible(false);
      salat.getChildren().add(salatCheck);
      
      
      AnchorPane paprika = new AnchorPane();
      paprika.setPrefSize(800, 68);
      paprika.setStyle("-fx-background-color: #FEFEFE;");
      
      Label paprikaLabel = new Label("Vask paprika og kutt i terninger");
      paprikaLabel.setFont(new Font("Arial", 30));
      paprikaLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(paprikaLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(paprikaLabel, 16.0);
      paprika.getChildren().add(paprikaLabel);
      
      ImageView paprikaCheck = new ImageView(check);
      AnchorPane.setRightAnchor(paprikaCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(paprikaCheck, insetChecksFraToppen);
      paprikaCheck.setVisible(false);
      paprika.getChildren().add(paprikaCheck);
      
      
      AnchorPane mais = new AnchorPane();
      mais.setPrefSize(800, 68);
      mais.setStyle("-fx-background-color: #FEFEFE;");
      
      Label maisLabel = new Label("Åpne maisboksen");
      maisLabel.setFont(new Font("Arial", 30));
      maisLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(maisLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(maisLabel, 16.0);
      mais.getChildren().add(maisLabel);
      
      ImageView maisCheck = new ImageView(check);
      AnchorPane.setRightAnchor(maisCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(maisCheck, insetChecksFraToppen);
      maisCheck.setVisible(false);
      mais.getChildren().add(maisCheck);
      
      
      AnchorPane tacolefser = new AnchorPane();
      tacolefser.setPrefSize(800, 68);
      tacolefser.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tacolefserLabel = new Label("Varm tacolefsene");
      tacolefserLabel.setFont(new Font("Arial", 30));
      tacolefserLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tacolefserLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tacolefserLabel, 16.0);
      tacolefser.getChildren().add(tacolefserLabel);
      
      ImageView tacolefserCheck = new ImageView(check);
      AnchorPane.setRightAnchor(tacolefserCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(tacolefserCheck, insetChecksFraToppen);
      tacolefserCheck.setVisible(false);
      tacolefser.getChildren().add(tacolefserCheck);
      
      
      AnchorPane roemme = new AnchorPane();
      roemme.setPrefSize(800, 68);
      roemme.setStyle("-fx-background-color: #FEFEFE;");
      
      Label roemmeLabel = new Label("Åpne rømmeboksen");
      roemmeLabel.setFont(new Font("Arial", 30));
      roemmeLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(roemmeLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(roemmeLabel, 16.0);
      roemme.getChildren().add(roemmeLabel);
      
      ImageView roemmeCheck = new ImageView(check);
      AnchorPane.setRightAnchor(roemmeCheck, insetFraHoeyreCheck);
      AnchorPane.setTopAnchor(roemmeCheck, insetChecksFraToppen);
      roemmeCheck.setVisible(false);
      roemme.getChildren().add(roemmeCheck);
      
      
      AnchorPane bunn = new AnchorPane();
      bunn.setPrefSize(800, 30);
      bunn.setStyle("-fx-background-color: #FEFEFE;");
      
      
      VBox rot = new VBox(toppOverskrift, kjoettdeig, kjoettdeig2, tomat, ost, salat, paprika, mais, roemme, tacolefser, bunn);
      rot.setSpacing(2);
      rot.setStyle("-fx-background-color: #1E1E1E;");
      
      return rot;
   }
   
   VBox lagMineVarer() {
      double insetFraVenstre = 150.0;
      double insetFraVenstreIngredienser = 200;
      double insetFraHoeyreMengde = 680;
      double insetFraHoeyreBilder = 50;
      
      int antallPersoner = AntallPersonerKlasse.antallPersonerSomVises;
      
      
      AnchorPane toppOverskrift = new AnchorPane();
      toppOverskrift.setPrefSize(800, 94);
      toppOverskrift.setStyle("-fx-background-color: #FEFEFE;");
      
      Label mineVarerLabel = new Label("Du har følgende varer tilgjengelig:");
      mineVarerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));
      mineVarerLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(mineVarerLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(mineVarerLabel, 29.0);
      toppOverskrift.getChildren().add(mineVarerLabel);
      
      
      kjoettdeigPane = new AnchorPane();
      kjoettdeigPane.setPrefSize(800, 68);
      kjoettdeigPane.setStyle("-fx-background-color: #FEFEFE;");
      
      Label kjoettdeigLabel = new Label("Kjøttdeig");
      kjoettdeigLabel.setFont(new Font("Arial", 30));
      kjoettdeigLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(kjoettdeigLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(kjoettdeigLabel, 16.0);
      kjoettdeigPane.getChildren().add(kjoettdeigLabel);
      
      Label kjoettdeigTotaltL2 = new Label("400g");
      kjoettdeigTotaltL2.setFont(new Font("Arial", 30));
      kjoettdeigTotaltL2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(kjoettdeigTotaltL2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(kjoettdeigTotaltL2, 16.0);
      kjoettdeigPane.getChildren().add(kjoettdeigTotaltL2);
      
      Image kjoettdeigBilde = lagBilde("tacoSmaa/kjoettdeig");
      ImageView kjoettdeigImageView = new ImageView(kjoettdeigBilde);
      AnchorPane.setRightAnchor(kjoettdeigImageView, insetFraHoeyreBilder - 5);
      AnchorPane.setTopAnchor(kjoettdeigImageView, 10.0);
      kjoettdeigPane.getChildren().add(kjoettdeigImageView);
      
      
      AnchorPane tacolefser = new AnchorPane();
      tacolefser.setPrefSize(800, 68);
      tacolefser.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tacolefserLabel = new Label("Tacolefser");
      tacolefserLabel.setFont(new Font("Arial", 30));
      tacolefserLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tacolefserLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tacolefserLabel, 16.0);
      tacolefser.getChildren().add(tacolefserLabel);
      
      Label tacolefserMengdeLabel2 = new Label("8");
      tacolefserMengdeLabel2.setFont(new Font("Arial", 30));
      tacolefserMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(tacolefserMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(tacolefserMengdeLabel2, 16.0);
      tacolefser.getChildren().add(tacolefserMengdeLabel2);
      
      Image tacolefserBilde = lagBilde("tacoSmaa/taco");
      ImageView tacolefserImageView = new ImageView(tacolefserBilde);
      AnchorPane.setRightAnchor(tacolefserImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(tacolefserImageView, 10.0);
      tacolefser.getChildren().add(tacolefserImageView);
      
      
      AnchorPane tomat = new AnchorPane();
      tomat.setPrefSize(800, 68);
      tomat.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tomatLabel = new Label("Tomater");
      tomatLabel.setFont(new Font("Arial", 30));
      tomatLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tomatLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tomatLabel, 16.0);
      tomat.getChildren().add(tomatLabel);
      
      Label tomatMengdeLabel2 = new Label("3");
      tomatMengdeLabel2.setFont(new Font("Arial", 30));
      tomatMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(tomatMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(tomatMengdeLabel2, 16.0);
      tomat.getChildren().add(tomatMengdeLabel2);
      
      Image tomatBilde = lagBilde("tacoSmaa/tomat");
      ImageView tomatImageView = new ImageView(tomatBilde);
      AnchorPane.setRightAnchor(tomatImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(tomatImageView, 10.0);
      tomat.getChildren().add(tomatImageView);
      
      
      AnchorPane salat = new AnchorPane();
      salat.setPrefSize(800, 68);
      salat.setStyle("-fx-background-color: #FEFEFE;");
      
      Label salatL = new Label("Salathode");
      salatL.setFont(new Font("Arial", 30));
      salatL.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(salatL, insetFraVenstre);
      AnchorPane.setTopAnchor(salatL, 16.0);
      salat.getChildren().add(salatL);
      
      Label salatMengdeLabel2 = new Label("1");
      salatMengdeLabel2.setFont(new Font("Arial", 30));
      salatMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(salatMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(salatMengdeLabel2, 16.0);
      salat.getChildren().add(salatMengdeLabel2);
      
      Image salatBilde = lagBilde("tacoSmaa/salat");
      ImageView salatImageView = new ImageView(salatBilde);
      AnchorPane.setRightAnchor(salatImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(salatImageView, 10.0);
      salat.getChildren().add(salatImageView);
      
      
      AnchorPane mais = new AnchorPane();
      mais.setPrefSize(800, 68);
      mais.setStyle("-fx-background-color: #FEFEFE;");
      
      Label maisLabel = new Label("Boks mais");
      maisLabel.setFont(new Font("Arial", 30));
      maisLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(maisLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(maisLabel, 16.0);
      mais.getChildren().add(maisLabel);
      
      Label maisMengdeLabel2 = new Label("5");
      maisMengdeLabel2.setFont(new Font("Arial", 30));
      maisMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(maisMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(maisMengdeLabel2, 16.0);
      mais.getChildren().add(maisMengdeLabel2);
      
      Image maisBilde = lagBilde("tacoSmaa/mais");
      ImageView maisImageView = new ImageView(maisBilde);
      AnchorPane.setRightAnchor(maisImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(maisImageView, 10.0);
      mais.getChildren().add(maisImageView);
      
      
      AnchorPane ost = new AnchorPane();
      ost.setPrefSize(800, 68);
      ost.setStyle("-fx-background-color: #FEFEFE;");
      
      Label ostLabel = new Label("Hvitost");
      ostLabel.setFont(new Font("Arial", 30));
      ostLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(ostLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(ostLabel, 16.0);
      ost.getChildren().add(ostLabel);
      
      Label ostMengdeLabel2 = new Label("1");
      ostMengdeLabel2.setFont(new Font("Arial", 30));
      ostMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(ostMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(ostMengdeLabel2, 16.0);
      ost.getChildren().add(ostMengdeLabel2);
      
      Image ostBilde = lagBilde("tacoSmaa/ost");
      ImageView ostImageView = new ImageView(ostBilde);
      AnchorPane.setRightAnchor(ostImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(ostImageView, 10.0);
      ost.getChildren().add(ostImageView);
      
      
      AnchorPane paprika = new AnchorPane();
      paprika.setPrefSize(800, 68);
      paprika.setStyle("-fx-background-color: #FEFEFE;");
      
      Label paprikaLabel = new Label("Paprika");
      paprikaLabel.setFont(new Font("Arial", 30));
      paprikaLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(paprikaLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(paprikaLabel, 16.0);
      paprika.getChildren().add(paprikaLabel);
      
      Label paprikaMengdeLabel2 = new Label("2");
      paprikaMengdeLabel2.setFont(new Font("Arial", 30));
      paprikaMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(paprikaMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(paprikaMengdeLabel2, 16.0);
      paprika.getChildren().add(paprikaMengdeLabel2);
      
      Image paprikaBilde = lagBilde("tacoSmaa/paprika");
      ImageView paprikaImageView = new ImageView(paprikaBilde);
      AnchorPane.setRightAnchor(paprikaImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(paprikaImageView, 10.0);
      paprika.getChildren().add(paprikaImageView);
      
      
      AnchorPane tacokr = new AnchorPane();
      tacokr.setPrefSize(800, 68);
      tacokr.setStyle("-fx-background-color: #FEFEFE;");
      
      Label tacokrLabel = new Label("Pose tacokrydder");
      tacokrLabel.setFont(new Font("Arial", 30));
      tacokrLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(tacokrLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(tacokrLabel, 16.0);
      tacokr.getChildren().add(tacokrLabel);
      
      Label tacokrMengdeLabel2 = new Label("4");
      tacokrMengdeLabel2.setFont(new Font("Arial", 30));
      tacokrMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(tacokrMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(tacokrMengdeLabel2, 16.0);
      tacokr.getChildren().add(tacokrMengdeLabel2);
      
      Image tacokrBilde = lagBilde("tacoSmaa/tacokrydder");
      ImageView tacokrImageView = new ImageView(tacokrBilde);
      AnchorPane.setRightAnchor(tacokrImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(tacokrImageView, 10.0);
      tacokr.getChildren().add(tacokrImageView);
      
      
      AnchorPane roemme = new AnchorPane();
      roemme.setPrefSize(800, 68);
      roemme.setStyle("-fx-background-color: #FEFEFE;");
      
      Label roemmeLabel = new Label("Boks lettrømme");
      roemmeLabel.setFont(new Font("Arial", 30));
      roemmeLabel.setTextFill(Color.web("#1E1E1E"));
      AnchorPane.setLeftAnchor(roemmeLabel, insetFraVenstre);
      AnchorPane.setTopAnchor(roemmeLabel, 16.0);
      roemme.getChildren().add(roemmeLabel);
      
      Label roemmeMengdeLabel2 = new Label("1");
      roemmeMengdeLabel2.setFont(new Font("Arial", 30));
      roemmeMengdeLabel2.setTextFill(Color.web("#585050"));
      AnchorPane.setRightAnchor(roemmeMengdeLabel2, insetFraHoeyreMengde);
      AnchorPane.setTopAnchor(roemmeMengdeLabel2, 16.0);
      roemme.getChildren().add(roemmeMengdeLabel2);
      
      Image roemmeBilde = lagBilde("tacoSmaa/roemme");
      ImageView roemmeImageView = new ImageView(roemmeBilde);
      AnchorPane.setRightAnchor(roemmeImageView, insetFraHoeyreBilder);
      AnchorPane.setTopAnchor(roemmeImageView, 10.0);
      roemme.getChildren().add(roemmeImageView);
      
      
      AnchorPane bunn = new AnchorPane();
      bunn.setPrefSize(800, 244 - 68 * 1);
      bunn.setStyle("-fx-background-color: #FEFEFE;");
      
      
      VBox rot = new VBox(toppOverskrift, /*kjoettdeigPane,*/ tacokr, tacolefser, tomat, salat, roemme, mais, ost, paprika, bunn);
      rot.setSpacing(2);
      rot.setStyle("-fx-background-color: #1E1E1E;");
      
      return rot;
   }
   
   public static void oppdaterMineVarer(String vare) {
      switch (vare) {
         case "kjoettdeig":
            mineVarerVBox.getChildren().add(1, kjoettdeigPane);
            break;
         case "salat":
            mengdeSalat += 1;
            break;
         case "tacolefser":
            mengdeTacolefser += 1;
            break;
         case "mais":
            mengdeMais += 1;
            break;
         case "paprika":
            mineVarerVBox.getChildren().add(1, paprikaPane);
            break;
         case "ost":
            mineVarerVBox.getChildren().add(1, ostPane);
            break;
         case "tomat":
            mengdeTomat += 1;
            break;
         
      }
   }
   
   public void oppdaterPersoner() {
      for (int i = 1; i < 6; i++) {
         antPersonerLabel.setText(Integer.toString(antallPersonerSomVises));
         if (i > antallPersonerSomVises) {
            personBildeImageView[i].setVisible(false);
         } else {
            personBildeImageView[i].setVisible(true);
         }
      }
   }
   
   void setupArduino() {
      try {
         SerialPort[] ports = SerialPort.getCommPorts();
         SerialPort serialPort = ports[0];
         System.out.println("ports: " + ports);
         System.out.println("ports[0] " + ports[0]);
         serialPort.openPort();
         serialPort.setComPortTimeouts(SerialPort.TIMEOUT_SCANNER, 0, 0);
         Scanner data = new Scanner(serialPort.getInputStream());
         System.out.println(data);
         startTask(data);
      } catch (ArrayIndexOutOfBoundsException e) {
         System.out.println("Arduino ikke funnet");
      }

   }
   
   void setupLyd() {
      try {
         beepClip = AudioSystem.getClip();
         InputStream audioSrc = getClass().getResourceAsStream("beep.wav");
         InputStream bufferedIn = new BufferedInputStream(audioSrc);
         AudioInputStream inputStream = AudioSystem.getAudioInputStream(bufferedIn);
         beepClip.open(inputStream);
      } catch (LineUnavailableException e) {
      } catch (UnsupportedAudioFileException e) {
      } catch (IOException e) {
      }
   }
}