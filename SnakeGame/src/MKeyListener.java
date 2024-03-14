import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

class MKeyListener extends KeyAdapter {

    @Override
    public void keyPressed(KeyEvent event) {

        int ch = event.getKeyCode();
        switch(ch){
            case 38:
                System.out.println("Up");
                break;
            case 40:
                System.out.println("Down");
                break;
            case 37:
                System.out.println("Left");
                break;
            case 39:
                System.out.println("Right");
                break;
        }


    }
}
