import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class ForsideKlasse extends SceneKlasse {
   
   private VBox rot;
 
   private int antallMenyer = 3;
   private Node[] menyer = new Node[antallMenyer+1];
   private int menyValg = 1;
   
   ForsideKlasse(VBox rot) {
      super(rot);
      this.rot = rot;
      for (int i = 1; i < antallMenyer+1; i++) {
         menyer[i] = rot.getChildren().get(i);
      }
      
   }
   
   public void trykketOPP() {
      if (menyValg > 1) {
         menyValg--;
         oppdaterValgtMeny();
      }
   }
   
   public void trykketNED() {
      if (menyValg < antallMenyer) {
         menyValg++;
         oppdaterValgtMeny();
      }
   }
   
   public void trykketTILBAKE() {}
   
   public void trykketOK(){
      SceneKlasse nesteScene = Main.finnNesteScene(menyValg);
      if (nesteScene != null) Main.endreSceneTil(nesteScene);
   }
   
   public void trykketSLETT(){}
   
   //Endrer hvilket menyvalg som er farget naar brukeren blar opp og ned
   public void oppdaterValgtMeny() {
      for (int i = 1; i < antallMenyer+1; i++) {
         if (i == menyValg) {
            menyer[i].setStyle("-fx-background-color: #6CCEF5;");
         } else {
            menyer[i].setStyle("-fx-background-color: #FEFEFE;");
         }
      }
   }
}
