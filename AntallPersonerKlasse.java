import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class AntallPersonerKlasse extends SceneKlasse {
   
   private AnchorPane rot;
   public static int antallPersonerSomVises = 3;
   private Label antPersonerLabel;
   private ImageView[] personBildeImageView = new ImageView[6];
   
   AntallPersonerKlasse(AnchorPane rot) {
      super(rot);
      this.rot = rot;
      antPersonerLabel = (Label) rot.getChildren().get(1);
      
      for (int i = 1; i < 6; i++) {
         personBildeImageView[i] = (ImageView) rot.getChildren().get(i+1); //Caster Node til ImageView
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
   
   public void trykketOPP() {
      if (antallPersonerSomVises < 5) {
         antallPersonerSomVises++;
         oppdaterPersoner();
      }
   }
   
   public void trykketNED() {
      if (antallPersonerSomVises > 1) {
         antallPersonerSomVises--;
         oppdaterPersoner();
      }
   }
   
   public void trykketTILBAKE() {
      Main.endreSceneTil(Main.finnForrigeScene(0));
   }
   
   public void trykketOK(){
      Main.endreSceneTil(Main.finnNesteScene(0));
   }
   
   public void trykketSLETT(){}
}
