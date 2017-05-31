import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;

public class TacoOppskriftKlasse extends SceneKlasse {
   
   private VBox rot;
   
   private int antallOppgaver = 9;
   private int antallOppgaverFerdig = 0;
   private AnchorPane[] oppgaver = new AnchorPane[antallOppgaver + 1];
   private int menyValg = 1;
   
   private int oeversteSomVises = 1;
   private int nedersteSomVises = 5;
   
   private AnchorPane[] oeverstSkjultListe = new AnchorPane[10];
   
   TacoOppskriftKlasse(VBox rot) {
      super(rot);
      this.rot = rot;
      for (int i = 1; i < antallOppgaver + 1; i++) {
         oppgaver[i] = (AnchorPane) rot.getChildren().get(i);
      }
      oppdaterValgtOppgave();
   }
   
   //Metodene trykket opp og trykket ned implementerer ogsaa scrolling i denne og et par andre scener,
   // er en liten bug om man scroller helt ned og saa opp igjen
   public void trykketOPP() {
      if (menyValg > 1) {
         menyValg--;
         oppdaterValgtOppgave();
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
   }
   
   public void trykketNED() {
      if (menyValg < antallOppgaver) {
         menyValg++;
         oppdaterValgtOppgave();
      }
   
      if (menyValg > nedersteSomVises && nedersteSomVises < antallOppgaver) {
         oeverstSkjultListe[oeversteSomVises-1] = (AnchorPane) rot.getChildren().remove(oeversteSomVises);
         oeversteSomVises++;
         nedersteSomVises++;
      }
      System.out.println(oeversteSomVises);
      System.out.println(nedersteSomVises);
      System.out.println();
      System.out.println(menyValg);
   
      for (int i = 1; i < antallOppgaver + 1; i++) {
         System.out.println(oppgaver[i].getChildren().get(0));
      }
   }
   
   public void trykketTILBAKE() {
      Main.endreSceneTil(Main.finnForrigeScene(0));
   }
   
   //Dersom bruker trykker groenn knapp paa en oppgave hukes den av med en groenn "check",
   // og antall oppgaver som er ferdig oekes med en.
   public void trykketOK() {
      ImageView check = (ImageView) oppgaver[menyValg].getChildren().get(1);
   
      if (!check.isVisible() && antallOppgaverFerdig < antallOppgaver) {
         antallOppgaverFerdig++;
         check.setVisible(true);
         System.out.println("legger til");
         System.out.println(antallOppgaverFerdig);
      }
   
      //Sjekker om alle oppgaver er ferdig
      if (antallOppgaverFerdig == antallOppgaver) {
         //Bytter til siste scene: : "Ferdig! Godt jobbet og god middag!
         SceneKlasse nesteScene = Main.finnNesteScene(0);
         if (nesteScene != null) Main.endreSceneTil(nesteScene);
      }
   }
   
   //Fjerner "check" fra valgt oppgave og minker antall utfoerte oppgaver
   public void trykketSLETT() {
      ImageView check = (ImageView) oppgaver[menyValg].getChildren().get(1);
      
      if (check.isVisible() && antallOppgaverFerdig > 0) {
         antallOppgaverFerdig--;
         System.out.println("fjerner");
         check.setVisible(false);
         System.out.println(antallOppgaverFerdig);
      }
      
   }
   
   //Endrer hvilket menyvalg som er farget naar brukeren blar opp og ned
   public void oppdaterValgtOppgave() {
      for (int i = 1; i < antallOppgaver + 1; i++) {
         if (i == menyValg) {
            oppgaver[i].setStyle("-fx-background-color: #C1FCAE;");
         } else {
            oppgaver[i].setStyle("-fx-background-color: #FEFEFE;");
         }
      }
   }
}
