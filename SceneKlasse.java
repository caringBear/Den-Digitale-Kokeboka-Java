import javafx.scene.Parent;
import javafx.scene.Scene;

//Abstrakt klasse som alle de ulike sceneklassene arver fra, skal ikke lage noen instans av denne klassen.
public abstract class SceneKlasse extends Scene implements SceneKlasseInterface {
   SceneKlasse(Parent rot){
      super(rot);
   }
}