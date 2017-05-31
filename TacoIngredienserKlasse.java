import javafx.scene.Node;
import javafx.scene.layout.VBox;

public class TacoIngredienserKlasse extends SceneKlasse {
   
   private VBox rot;
   
   private int antallIngredienser = 7;
   private Node[] ingredienser = new Node[antallIngredienser+1];
   
   
   TacoIngredienserKlasse(VBox rot) {
      super(rot);
      this.rot = rot;
      for (int i = 1; i < antallIngredienser+1; i++) {
         ingredienser[i] = rot.getChildren().get(i);
      }
   }
   
   public void trykketOPP() {}
   public void trykketNED() {}
   
   public void trykketTILBAKE() {
      Main.endreSceneTil(Main.finnForrigeScene(0));
   }
   
   public void trykketOK(){
      SceneKlasse nesteScene = Main.finnNesteScene(0);
      if (nesteScene != null) Main.endreSceneTil(nesteScene);
   }
   
   public void trykketSLETT(){}
}
