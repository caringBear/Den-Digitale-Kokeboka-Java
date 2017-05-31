import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class OppskriftValgKlasse extends SceneKlasse {
   
   private VBox rot;
   
   private int antallOppskrifter = 7;
   private AnchorPane[] oppskrifter = new AnchorPane[antallOppskrifter + 1];
   private int menyValg = 1;
   
   private int oeversteSomVises = 1;
   private int nedersteSomVises = 5;
   
   private AnchorPane[] oeverstSkjultListe = new AnchorPane[10];
   
   
   OppskriftValgKlasse(VBox rot) {
      super(rot);
      this.rot = rot;
      for (int i = 1; i < antallOppskrifter + 1; i++) {
         oppskrifter[i] = (AnchorPane) rot.getChildren().get(i);
      }
      oppdaterValgtOppskrift();
   }
   
   public void trykketOPP() {
      if (menyValg > 1) {
         menyValg--;
         oppdaterValgtOppskrift();
      }
      
      if (menyValg < oeversteSomVises) {
         rot.getChildren().add(1, oeverstSkjultListe[oeversteSomVises-2]);
         oeversteSomVises--;
         nedersteSomVises--;
      }
      System.out.println(oeversteSomVises);
      System.out.println(nedersteSomVises);
      System.out.println();
      System.out.println(menyValg);
   
      for (int i = 1; i < antallOppskrifter + 1; i++) {
         System.out.println(oppskrifter[i].getChildren().get(0));
      }
   }
   
   public void trykketNED() {
      if (menyValg < antallOppskrifter) {
         menyValg++;
         oppdaterValgtOppskrift();
      }
      
      if (menyValg > nedersteSomVises && nedersteSomVises < antallOppskrifter) {
         oeverstSkjultListe[oeversteSomVises-1] = (AnchorPane) rot.getChildren().remove(oeversteSomVises);
         oeversteSomVises++;
         nedersteSomVises++;
      }
      System.out.println(oeversteSomVises);
      System.out.println(nedersteSomVises);
      System.out.println();
      System.out.println(menyValg);
   
      for (int i = 1; i < antallOppskrifter + 1; i++) {
         System.out.println(oppskrifter[i].getChildren().get(0));
      }
   }
   
   public void trykketTILBAKE() {
      Main.endreSceneTil(Main.finnForrigeScene(0));
   }
   
   public void trykketOK() {
      SceneKlasse nesteScene = Main.finnNesteScene(menyValg);
      if (nesteScene != null) Main.endreSceneTil(nesteScene);
   }
   
   public void trykketSLETT() {
   
   }
   
   //Endrer hvilket menyvalg som er farget naar brukeren blar opp og ned
   public void oppdaterValgtOppskrift() {
      for (int i = 1; i < antallOppskrifter + 1; i++) {
         if (i == menyValg) {
            oppskrifter[i].setStyle("-fx-background-color: #F49AC1;");
         } else {
            oppskrifter[i].setStyle("-fx-background-color: #FEFEFE;");
         }
      }
   }
}
