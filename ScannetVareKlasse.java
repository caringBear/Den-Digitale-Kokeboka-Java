import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;

public class ScannetVareKlasse extends SceneKlasse {
   
   private static AnchorPane rot = new AnchorPane();
   private static SceneKlasse forrigeScene;
   private static Image scannetVareBilde;
   private static ImageView scannetVareImageView = new ImageView();
   
   ScannetVareKlasse() {
      super(rot);
      this.rot = rot;
      rot.getChildren().add(scannetVareImageView);
   
   }
   
   public static void vareScannet(String vare) {
      scannetVareBilde = Main.lagBilde("tacoStore/"+vare);
      scannetVareImageView.setImage(scannetVareBilde);
   }
   
   //Ingen av metodene implementert da ingen av knappene skal gjoere noe mens bilde av scannet matvare vises
   public void trykketOPP() {}
   public void trykketNED() {}
   public void trykketTILBAKE() {}
   public void trykketOK(){}
   public void trykketSLETT(){}
}
