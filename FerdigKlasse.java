import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class FerdigKlasse extends SceneKlasse {
   
   private static AnchorPane rot = new AnchorPane();
   private static SceneKlasse forrigeScene;
   private static Image ferdigBilde = Main.lagBilde("ferdig");
   private static ImageView ferdigBildeImageView = new ImageView(ferdigBilde);
   
   FerdigKlasse() {
      super(rot);
      this.rot = rot;
      rot.getChildren().add(ferdigBildeImageView);
      
   }
   
   public void trykketOPP() {}
   public void trykketNED() {}
   
   public void trykketTILBAKE() {
      Main.endreSceneTil(Main.finnForrigeScene(0));
   }
   
   public void trykketOK(){}
   
   public void trykketSLETT(){}
}
