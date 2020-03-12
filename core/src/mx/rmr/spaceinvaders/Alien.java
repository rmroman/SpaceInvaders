package mx.rmr.spaceinvaders;

import com.badlogic.gdx.graphics.Texture;

public class Alien extends Objeto
{
    public Alien(Texture textura, float x, float y) {
        super(textura, x, y);
    }

    public void mover(float dx) {
        sprite.setX(sprite.getX()+dx);
    }

    public void bajar(float dy) {
        sprite.setY(sprite.getY()-dy);
    }
}
